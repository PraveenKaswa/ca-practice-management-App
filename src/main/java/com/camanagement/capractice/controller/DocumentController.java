package com.camanagement.capractice.controller;

import com.camanagement.capractice.entity.Document;
import com.camanagement.capractice.entity.Document.DocumentCategory;
import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.repository.DocumentRepository;
import com.camanagement.capractice.repository.ClientRepository;
import com.camanagement.capractice.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * DOCUMENT CONTROLLER - COMPLETE EXPLANATION:
 *
 * PURPOSE: Handle all HTTP requests related to documents
 *
 * RESPONSIBILITIES:
 * 1. Show document list page
 * 2. Show upload form
 * 3. Process file uploads
 * 4. Handle file downloads
 * 5. Delete documents
 * 6. View document details
 *
 * URL MAPPING:
 * GET  /documents              → List all documents
 * GET  /documents/upload       → Show upload form
 * POST /documents/upload       → Process upload
 * GET  /documents/{id}/view    → View document details
 * GET  /documents/{id}/download → Download file
 * POST /documents/{id}/delete  → Delete document
 */
@Controller
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ClientRepository clientRepository;

    // ==================== LIST ALL DOCUMENTS ====================

    /**
     * LIST ALL DOCUMENTS
     *
     * WHAT: Show page with all documents
     * URL: GET /documents
     * RETURNS: documents.html template
     *
     * FEATURES:
     * - Filter by client
     * - Filter by category
     * - Search by filename
     *
     * FLOW:
     * User visits /documents → This method runs →
     * Fetches documents from DB → Passes to template →
     * Browser shows HTML page
     */
    @GetMapping
    public String listDocuments(
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "category", required = false) String categoryStr,
            @RequestParam(value = "search", required = false) String searchTerm,
            Model model) {

        /**
         * @RequestParam EXPLANATION:
         *
         * WHAT: Captures URL parameters
         * EXAMPLE URLs:
         * /documents                          → No filters
         * /documents?clientId=5               → Filter by client
         * /documents?category=TAX             → Filter by category
         * /documents?clientId=5&category=TAX  → Both filters
         * /documents?search=form16            → Search
         *
         * required = false → Parameter is optional
         */

        try {
            System.out.println("=== LOADING DOCUMENTS LIST ===");
            System.out.println("Client filter: " + clientId);
            System.out.println("Category filter: " + categoryStr);
            System.out.println("Search term: " + searchTerm);

            List<Document> documents;

            // APPLY FILTERS
            // WHY CHECK NULL: If parameter not provided, don't filter

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // SEARCH MODE: User entered search term
                documents = documentRepository.searchDocuments(searchTerm);
                System.out.println("Search results: " + documents.size());

            } else if (clientId != null && categoryStr != null && !categoryStr.isEmpty()) {
                // BOTH FILTERS: Client + Category
                Client client = clientRepository.findById(clientId).orElse(null);
                DocumentCategory category = DocumentCategory.valueOf(categoryStr);
                documents = documentRepository.findByClientAndCategoryAndIsDeletedFalse(client, category);
                System.out.println("Filtered by client + category: " + documents.size());

            } else if (clientId != null) {
                // CLIENT FILTER ONLY
                documents = documentRepository.findByClientIdAndIsDeletedFalse(clientId);
                System.out.println("Filtered by client: " + documents.size());

            } else if (categoryStr != null && !categoryStr.isEmpty()) {
                // CATEGORY FILTER ONLY
                DocumentCategory category = DocumentCategory.valueOf(categoryStr);
                documents = documentRepository.findByCategoryAndIsDeletedFalse(category);
                System.out.println("Filtered by category: " + documents.size());

            } else {
                // NO FILTERS: Show all documents
                documents = documentRepository.findByIsDeletedFalseOrderByUploadDateDesc();
                System.out.println("Showing all documents: " + documents.size());
            }

            // GET STATISTICS FOR DASHBOARD CARDS
            DocumentService.DocumentStatistics stats = documentService.getStatistics();

            // GET ALL CLIENTS FOR FILTER DROPDOWN
            List<Client> allClients = clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE);

            // ADD DATA TO MODEL (pass to HTML template)
            model.addAttribute("pageTitle", "Document Management");
            model.addAttribute("documents", documents);
            model.addAttribute("stats", stats);
            model.addAttribute("allClients", allClients);
            model.addAttribute("categories", DocumentCategory.values()); // All enum values

            // KEEP FILTER VALUES (so form stays filled)
            model.addAttribute("selectedClientId", clientId);
            model.addAttribute("selectedCategory", categoryStr);
            model.addAttribute("searchTerm", searchTerm);

            System.out.println("=== DOCUMENTS PAGE LOADED ===");

            return "documents"; // Returns documents.html template

        } catch (Exception e) {
            System.err.println("ERROR loading documents: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load documents");
            return "documents";
        }
    }

    // ==================== SHOW UPLOAD FORM ====================

    /**
     * SHOW UPLOAD FORM
     *
     * WHAT: Display page with file upload form
     * URL: GET /documents/upload
     * RETURNS: upload-document.html template
     *
     * WHY SEPARATE METHOD:
     * - GET shows empty form
     * - POST processes the form submission
     */
    @GetMapping("/upload")
    public String showUploadForm(
            @RequestParam(value = "clientId", required = false) Long clientId,
            Model model) {

        try {
            System.out.println("=== SHOWING UPLOAD FORM ===");

            // If clientId provided, pre-select client
            if (clientId != null) {
                Client client = clientRepository.findById(clientId).orElse(null);
                model.addAttribute("selectedClient", client);
                System.out.println("Pre-selected client: " +
                        (client != null ? client.getClientName() : "not found"));
            }

            // Get all active clients for dropdown
            List<Client> allClients = clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE);
            model.addAttribute("allClients", allClients);

            // Get all document categories for dropdown
            model.addAttribute("categories", DocumentCategory.values());

            return "upload-document";

        } catch (Exception e) {
            System.err.println("ERROR showing upload form: " + e.getMessage());
            return "redirect:/documents?error=uploadform";
        }
    }

    // ==================== PROCESS FILE UPLOAD ====================

    /**
     * PROCESS FILE UPLOAD
     *
     * WHAT: Handle form submission with file
     * URL: POST /documents/upload
     * RETURNS: Redirect to documents list
     *
     * PARAMETERS EXPLANATION:
     * @RequestParam("file") MultipartFile file
     * - Captures the uploaded file from form
     * - Form must have: <input type="file" name="file">
     * - MultipartFile contains file data, name, size, type
     *
     * @RequestParam("clientId") Long clientId
     * - Captures selected client from dropdown
     * - Form must have: <select name="clientId">
     */
    @PostMapping("/upload")
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam("category") String categoryStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "financialYear", required = false) String financialYear,
            @RequestParam(value = "tags", required = false) String tags,
            Model model) {

        /**
         * FORM SUBMISSION FLOW:
         * 1. User fills form and clicks "Upload"
         * 2. Browser sends POST request with file data
         * 3. This method receives the data
         * 4. Calls service to save file
         * 5. Redirects to success page
         */

        try {
            System.out.println("=== PROCESSING FILE UPLOAD ===");
            System.out.println("File: " + file.getOriginalFilename());
            System.out.println("Size: " + file.getSize() + " bytes");
            System.out.println("Client ID: " + clientId);
            System.out.println("Category: " + categoryStr);

            // VALIDATE FILE EXISTS
            if (file.isEmpty()) {
                model.addAttribute("error", "Please select a file to upload");
                return showUploadForm(clientId, model);
            }

            // CONVERT CATEGORY STRING TO ENUM
            DocumentCategory category = DocumentCategory.valueOf(categoryStr);

            // CALL SERVICE TO HANDLE UPLOAD
            // (Service does all the heavy lifting)
            Document document = documentService.uploadDocument(
                    file,
                    clientId,
                    category,
                    description,
                    financialYear,
                    "Admin" // TODO: Get from logged-in user later
            );

            // UPDATE TAGS IF PROVIDED
            if (tags != null && !tags.trim().isEmpty()) {
                document.setTags(tags);
                documentRepository.save(document);
            }

            System.out.println("✓ Document uploaded successfully!");
            System.out.println("Document ID: " + document.getId());
            System.out.println("=== UPLOAD COMPLETE ===");

            // REDIRECT TO DOCUMENTS LIST WITH SUCCESS MESSAGE
            // PRG Pattern: Post-Redirect-Get (prevents duplicate submission)
            return "redirect:/documents?uploaded=true";

        } catch (Exception e) {
            System.err.println("ERROR uploading document: " + e.getMessage());
            e.printStackTrace();

            // RETURN TO FORM WITH ERROR MESSAGE
            model.addAttribute("error", "Failed to upload file: " + e.getMessage());
            return showUploadForm(clientId, model);
        }
    }

    // ==================== VIEW DOCUMENT DETAILS ====================

    /**
     * VIEW DOCUMENT DETAILS
     *
     * WHAT: Show details page for one document
     * URL: GET /documents/{id}/view
     * EXAMPLE: GET /documents/5/view → Show document ID 5
     *
     * @PathVariable EXPLANATION:
     * /documents/{id}/view → {id} is a variable in the URL
     * If URL is /documents/5/view, then id = 5
     * If URL is /documents/123/view, then id = 123
     */
    @GetMapping("/{id}/view")
    public String viewDocument(@PathVariable("id") Long id, Model model) {

        try {
            System.out.println("=== VIEWING DOCUMENT ===");
            System.out.println("Document ID: " + id);

            // FIND DOCUMENT BY ID
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // ADD TO MODEL
            model.addAttribute("document", document);
            model.addAttribute("client", document.getClient());

            // CALCULATE ADDITIONAL INFO
            model.addAttribute("formattedSize", document.getFormattedFileSize());
            model.addAttribute("isPdf", document.isPdf());
            model.addAttribute("isImage", document.isImage());
            model.addAttribute("isExcel", document.isExcel());

            System.out.println("Document: " + document.getFileName());
            System.out.println("Client: " + document.getClient().getClientName());

            return "view-document";

        } catch (Exception e) {
            System.err.println("ERROR viewing document: " + e.getMessage());
            return "redirect:/documents?error=notfound";
        }
    }

    // ==================== DOWNLOAD FILE ====================

    /**
     * DOWNLOAD FILE
     *
     * WHAT: Send file to browser for download
     * URL: GET /documents/{id}/download
     * RETURNS: File bytes (not HTML)
     *
     * ResponseEntity EXPLANATION:
     * - Regular return: String → Returns HTML page
     * - ResponseEntity: byte[] → Returns raw file data
     *
     * HOW IT WORKS:
     * 1. Read file from disk
     * 2. Create HTTP response with file bytes
     * 3. Set headers to trigger download
     * 4. Browser receives and saves file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id) {

        try {
            System.out.println("=== DOWNLOADING DOCUMENT ===");
            System.out.println("Document ID: " + id);

            // FIND DOCUMENT
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // READ FILE FROM DISK
            byte[] data = documentService.readDocumentFile(document);
            System.out.println("File read: " + data.length + " bytes");

            // CREATE RESOURCE FROM BYTES
            ByteArrayResource resource = new ByteArrayResource(data);

            // BUILD HTTP RESPONSE
            return ResponseEntity.ok()
                    // HEADER: Tell browser to download (not display)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    // HEADER: Set file type
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    // HEADER: Set file size
                    .contentLength(data.length)
                    // BODY: Actual file bytes
                    .body(resource);

            /**
             * WHAT THESE HEADERS DO:
             *
             * Content-Disposition: attachment
             * - Tells browser: "Download this, don't display it"
             * - filename="Form16.pdf" → Suggested name for save dialog
             *
             * Content-Type: application/pdf
             * - Tells browser: "This is a PDF file"
             * - Browser knows how to handle it
             *
             * Content-Length: 1048576
             * - Tells browser: "File is 1MB"
             * - Browser shows download progress
             */

        } catch (IOException e) {
            System.err.println("ERROR reading file: " + e.getMessage());
            // Return error response
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("ERROR downloading document: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DELETE DOCUMENT ====================

    /**
     * DELETE DOCUMENT
     *
     * WHAT: Soft delete a document
     * URL: POST /documents/{id}/delete
     * METHOD: POST (not GET - prevents accidental deletion from URL)
     *
     * WHY POST NOT GET:
     * - GET /documents/5/delete → Can be cached, bookmarked, dangerous!
     * - POST /documents/5/delete → Requires form submission, safer
     */
    @PostMapping("/{id}/delete")
    public String deleteDocument(@PathVariable("id") Long id) {

        try {
            System.out.println("=== DELETING DOCUMENT ===");
            System.out.println("Document ID: " + id);

            // CALL SERVICE TO SOFT DELETE
            documentService.deleteDocument(id);

            System.out.println("✓ Document deleted");

            return "redirect:/documents?deleted=true";

        } catch (Exception e) {
            System.err.println("ERROR deleting document: " + e.getMessage());
            return "redirect:/documents?error=delete";
        }
    }

    // ==================== GET DOCUMENTS FOR CLIENT (AJAX) ====================

    /**
     * GET CLIENT DOCUMENTS (JSON API)
     *
     * WHAT: Return client's documents as JSON
     * URL: GET /documents/client/{clientId}
     * RETURNS: JSON (not HTML)
     *
     * WHY @ResponseBody:
     * - Without: Returns HTML template name
     * - With: Returns data as JSON
     *
     * USAGE: JavaScript/AJAX calls this to get documents
     *
     * EXAMPLE RESPONSE:
     * [
     *   {"id": 1, "fileName": "Form16.pdf", "size": 1048576},
     *   {"id": 2, "fileName": "ITR.pdf", "size": 524288}
     * ]
     */
    @GetMapping("/client/{clientId}")
    @ResponseBody
    public List<Document> getClientDocuments(@PathVariable("clientId") Long clientId) {

        try {
            return documentRepository.findByClientIdAndIsDeletedFalse(clientId);
        } catch (Exception e) {
            System.err.println("ERROR fetching client documents: " + e.getMessage());
            return List.of(); // Return empty list on error
        }
    }

    // ==================== STATISTICS (FOR DASHBOARD) ====================

    /**
     * GET DOCUMENT STATISTICS (JSON API)
     *
     * WHAT: Return document statistics as JSON
     * URL: GET /documents/stats
     * RETURNS: JSON with counts
     *
     * USAGE: Dashboard can call this to show document stats
     */
    @GetMapping("/stats")
    @ResponseBody
    public DocumentService.DocumentStatistics getStatistics() {
        return documentService.getStatistics();
    }
}