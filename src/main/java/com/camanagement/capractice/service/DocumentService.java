package com.camanagement.capractice.service;

import com.camanagement.capractice.entity.Document;
import com.camanagement.capractice.entity.Document.DocumentCategory;
import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.repository.DocumentRepository;
import com.camanagement.capractice.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * DOCUMENT SERVICE - COMPLETE EXPLANATION:
 *
 * PURPOSE: Handles ALL file operations and business logic
 *
 * KEY RESPONSIBILITIES:
 * 1. Upload files to disk
 * 2. Save file metadata to database
 * 3. Validate files (size, type, security)
 * 4. Generate unique filenames
 * 5. Handle file downloads
 * 6. Delete files (soft delete)
 *
 * FILE STORAGE STRATEGY:
 * - Files stored in: uploads/documents/YYYY/MM/unique-filename.ext
 * - Database stores: file path, metadata
 *
 * ANALOGY: Think of a library
 * - Service = Librarian (handles check-in, check-out, organization)
 * - Repository = Card catalog (tracks book locations)
 * - Disk = Physical shelves (stores actual books)
 */
@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ClientRepository clientRepository;

    // ==================== CONFIGURATION ====================

    /**
     * UPLOAD DIRECTORY CONFIGURATION
     *
     * WHAT: Where files are stored on server
     * WHY: Configurable so you can change storage location easily
     * HOW: @Value reads from application.properties
     *
     * In application.properties:
     * file.upload-dir=uploads/documents
     *
     * DEFAULT: If not specified, uses "uploads/documents"
     */
    @Value("${file.upload-dir:uploads/documents}")
    private String uploadDir;
    // EXAMPLE: "uploads/documents" or "/var/www/ca-app/documents"

    /**
     * MAX FILE SIZE (in bytes)
     *
     * WHAT: Maximum allowed file size
     * WHY: Prevent huge files from filling up server storage
     * DEFAULT: 10MB (10 * 1024 * 1024 bytes)
     */
    @Value("${file.max-size:10485760}")
    private long maxFileSize;
    // 10485760 bytes = 10 MB
    // Can configure in application.properties: file.max-size=20971520 (20MB)

    /**
     * ALLOWED FILE TYPES
     *
     * WHAT: Which MIME types are allowed
     * WHY: Security - prevent executable files, scripts
     * EXAMPLE: Only allow PDF, Excel, Images
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",                    // PDF files
            "application/vnd.ms-excel",           // Old Excel (.xls)
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // New Excel (.xlsx)
            "application/msword",                 // Old Word (.doc)
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // New Word (.docx)
            "image/jpeg",                         // JPEG images
            "image/jpg",                          // JPG images
            "image/png",                          // PNG images
            "image/gif",                          // GIF images
            "text/plain"                          // Text files
    );
    // WHY THESE: Common document types for CA work
    // WHY NOT .exe, .bat, .sh: Security risk - can contain viruses

    // ==================== FILE UPLOAD ====================

    /**
     * UPLOAD DOCUMENT - Main Method
     *
     * WHAT: Handles complete file upload process
     * WHY: Single method that does everything needed for upload
     *
     * PROCESS FLOW:
     * 1. Validate file (size, type, name)
     * 2. Create upload directory if needed
     * 3. Generate unique filename
     * 4. Save file to disk
     * 5. Create Document entity
     * 6. Save metadata to database
     * 7. Return Document object
     *
     * @param file - The uploaded file from user
     * @param clientId - Which client this belongs to
     * @param category - Document category (TAX, AUDIT, etc.)
     * @param description - Optional notes
     * @param financialYear - Which FY (e.g., "2023-24")
     * @param uploadedBy - Who uploaded it
     * @return Document object with all details
     */
    public Document uploadDocument(MultipartFile file,
                                   Long clientId,
                                   DocumentCategory category,
                                   String description,
                                   String financialYear,
                                   String uploadedBy) throws IOException {

        System.out.println("=== STARTING FILE UPLOAD ===");
        System.out.println("Original filename: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Content type: " + file.getContentType());

        // STEP 1: VALIDATE FILE
        // WHY: Security and storage management
        validateFile(file);
        System.out.println("✓ File validation passed");

        // STEP 2: GET CLIENT
        // WHY: Documents must belong to a client
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        System.out.println("✓ Client found: " + client.getClientName());

        // STEP 3: CREATE DIRECTORY STRUCTURE
        // WHY: Organize files by year/month for better management
        // STRUCTURE: uploads/documents/2024/10/
        Path uploadPath = createUploadDirectory();
        System.out.println("✓ Upload directory ready: " + uploadPath);

        // STEP 4: GENERATE UNIQUE FILENAME
        // WHY: Prevent overwriting if two users upload "Form16.pdf"
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = generateUniqueFilename(originalFilename);
        System.out.println("✓ Generated unique filename: " + uniqueFilename);

        // STEP 5: SAVE FILE TO DISK
        // WHERE: uploads/documents/2024/10/uuid-12345.pdf
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✓ File saved to disk: " + filePath);

        // STEP 6: CREATE DOCUMENT ENTITY (metadata)
        // WHAT: Store file information in database
        Document document = new Document();
        document.setFileName(originalFilename);  // Original name user sees
        document.setFilePath(filePath.toString());  // Where it's actually stored
        document.setFileSize(file.getSize());  // Size in bytes
        document.setFileType(file.getContentType());  // MIME type
        document.setFileExtension(getFileExtension(originalFilename));  // e.g., "pdf"
        document.setClient(client);  // Link to client
        document.setCategory(category);  // TAX, AUDIT, etc.
        document.setDescription(description);  // Optional notes
        document.setFinancialYear(financialYear);  // e.g., "2023-24"
        document.setUploadedBy(uploadedBy);  // Who uploaded
        document.setUploadDate(LocalDateTime.now());  // When uploaded
        document.setIsDeleted(false);  // Active (not deleted)

        // STEP 7: SAVE TO DATABASE
        Document savedDocument = documentRepository.save(document);
        System.out.println("✓ Document metadata saved to database");
        System.out.println("=== UPLOAD COMPLETE ===");
        System.out.println("Document ID: " + savedDocument.getId());

        return savedDocument;
    }

    // ==================== FILE VALIDATION ====================

    /**
     * VALIDATE FILE - Security Check
     *
     * WHAT: Checks if file is safe and acceptable
     * WHY: Prevent security issues and storage problems
     *
     * CHECKS:
     * 1. File is not null or empty
     * 2. File size within limit
     * 3. File type is allowed
     * 4. Filename is safe (no special characters)
     */
    private void validateFile(MultipartFile file) {
        // CHECK 1: File exists
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or not provided");
        }
        // WHY: Can't upload nothing!

        // CHECK 2: File size
        if (file.getSize() > maxFileSize) {
            String maxSizeMB = String.format("%.2f", maxFileSize / (1024.0 * 1024.0));
            throw new RuntimeException("File size exceeds maximum limit of " + maxSizeMB + " MB");
        }
        // WHY: Prevent 1GB files from filling up server
        // EXAMPLE: If maxFileSize = 10MB and user uploads 15MB file → Error!

        // CHECK 3: File type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new RuntimeException("File type not allowed. Allowed types: PDF, Excel, Word, Images, Text");
        }
        // WHY: Security - prevent .exe, .bat, .sh files
        // EXAMPLE: User tries to upload virus.exe → Error!

        // CHECK 4: Filename safety
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new RuntimeException("Invalid filename");
        }
        // WHY: Prevent directory traversal attacks
        // MALICIOUS: "../../../etc/passwd" could access system files
        // SAFE: "Form16.pdf" is fine

        System.out.println("File validation successful");
    }

    // ==================== DIRECTORY MANAGEMENT ====================

    /**
     * CREATE UPLOAD DIRECTORY
     *
     * WHAT: Creates folder structure for storing files
     * WHY: Organize files by date for better management
     *
     * STRUCTURE:
     * uploads/
     * └── documents/
     *     └── 2024/          ← Year
     *         └── 10/        ← Month
     *             └── files here
     *
     * BENEFITS:
     * - Easy to find files by date
     * - Better performance (fewer files per folder)
     * - Easy backup/archival (backup specific months)
     */
    private Path createUploadDirectory() throws IOException {
        // Get current year and month
        LocalDateTime now = LocalDateTime.now();
        String year = String.format("%04d", now.getYear());  // 2024
        String month = String.format("%02d", now.getMonthValue());  // 10 (October)

        // Build path: uploads/documents/2024/10
        Path path = Paths.get(uploadDir, year, month);

        // Create directories if they don't exist
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("Created directory: " + path);
        }
        // WHY createDirectories (not createDirectory):
        // createDirectories: Creates all missing folders in path
        // createDirectory: Only creates last folder, fails if parents missing

        return path;
    }

    // ==================== FILENAME GENERATION ====================

    /**
     * GENERATE UNIQUE FILENAME
     *
     * WHAT: Creates unique filename to prevent overwrites
     * WHY: If two users upload "Form16.pdf", they'd overwrite each other
     *
     * STRATEGY:
     * Original: "Form16.pdf"
     * Unique: "uuid-12345-67890_Form16.pdf"
     *
     * COMPONENTS:
     * - UUID = Universally Unique Identifier (practically unique)
     * - Timestamp = Additional uniqueness
     * - Original name = User can still recognize file
     */
    private String generateUniqueFilename(String originalFilename) {
        // Generate UUID (example: "a3f4b2c1-5678-...")
        String uuid = UUID.randomUUID().toString();

        // Get current timestamp (example: "20241023143045")
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Sanitize original filename (remove dangerous characters)
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        // WHAT THIS DOES:
        // [^a-zA-Z0-9._-] = Any character that's NOT alphanumeric, dot, underscore, or dash
        // Replace with underscore
        // EXAMPLE: "My Form (final).pdf" → "My_Form__final_.pdf"

        // Combine: uuid_timestamp_originalname
        return uuid + "_" + timestamp + "_" + safeName;
        // RESULT: "a3f4b2c1-5678_20241023143045_Form16.pdf"

        // WHY SO COMPLEX:
        // - UUID ensures uniqueness across all uploads
        // - Timestamp helps with sorting/debugging
        // - Original name helps humans identify file
    }

    /**
     * GET FILE EXTENSION
     *
     * WHAT: Extracts extension from filename
     * WHY: Store extension separately for easy filtering
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        // Get everything after last dot
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        // EXAMPLE: "Form16.PDF" → "pdf" (lowercase)
        // EXAMPLE: "document.tar.gz" → "gz" (last extension only)
    }

    // ==================== FILE RETRIEVAL ====================

    /**
     * GET DOCUMENT FILE PATH
     *
     * WHAT: Get the Path object for a document
     * WHY: Needed for reading file from disk
     */
    public Path getDocumentPath(Document document) {
        return Paths.get(document.getFilePath());
    }

    /**
     * READ DOCUMENT FILE
     *
     * WHAT: Read file contents as bytes
     * WHY: For downloading/serving files to user
     */
    public byte[] readDocumentFile(Document document) throws IOException {
        Path path = getDocumentPath(document);
        return Files.readAllBytes(path);
        // RETURNS: Byte array of file contents
        // EXAMPLE: PDF file → array of PDF bytes
        // USAGE: Send these bytes in HTTP response for download
    }

    // ==================== DOCUMENT OPERATIONS ====================

    /**
     * UPDATE DOCUMENT METADATA
     *
     * WHAT: Update document details (not the file itself)
     * WHY: User wants to change category, description, etc.
     */
    public Document updateDocument(Long documentId,
                                   DocumentCategory category,
                                   String description,
                                   String financialYear,
                                   String tags) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Update only metadata (file stays same)
        document.setCategory(category);
        document.setDescription(description);
        document.setFinancialYear(financialYear);
        document.setTags(tags);

        return documentRepository.save(document);
    }

    /**
     * DELETE DOCUMENT (Soft Delete)
     *
     * WHAT: Mark document as deleted (don't actually delete)
     * WHY: Safety - can recover accidentally deleted files
     *
     * PROCESS:
     * 1. Set isDeleted = true in database
     * 2. File stays on disk (for recovery)
     * 3. Hidden from normal queries
     */
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Soft delete - just mark as deleted
        document.setIsDeleted(true);
        documentRepository.save(document);

        System.out.println("Document soft-deleted: " + document.getFileName());
        // NOTE: File still exists on disk!
        // For permanent delete, add: Files.delete(getDocumentPath(document));
    }

    /**
     * PERMANENT DELETE
     *
     * WHAT: Actually delete file from disk AND database
     * WHY: For admin to permanently remove files
     * WARNING: Cannot be recovered!
     */
    public void permanentlyDeleteDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Delete physical file
        Path filePath = getDocumentPath(document);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("File deleted from disk: " + filePath);
        }

        // Delete database record
        documentRepository.delete(document);
        System.out.println("Document record deleted from database");
    }

    // ==================== STATISTICS & UTILITIES ====================

    /**
     * GET DOCUMENT STATISTICS
     *
     * WHAT: Get summary statistics for dashboard
     */
    public DocumentStatistics getStatistics() {
        DocumentStatistics stats = new DocumentStatistics();

        stats.setTotalDocuments(documentRepository.count());
        stats.setTaxDocuments(documentRepository.countByCategoryAndIsDeletedFalse(DocumentCategory.TAX));
        stats.setAuditDocuments(documentRepository.countByCategoryAndIsDeletedFalse(DocumentCategory.AUDIT));
        stats.setComplianceDocuments(documentRepository.countByCategoryAndIsDeletedFalse(DocumentCategory.COMPLIANCE));

        // Calculate total storage used
        // (This is simplified - in real app, query database for SUM)
        stats.setTotalStorageBytes(0L); // Placeholder

        return stats;
    }

    /**
     * STATISTICS HELPER CLASS
     */
    public static class DocumentStatistics {
        private long totalDocuments;
        private long taxDocuments;
        private long auditDocuments;
        private long complianceDocuments;
        private long totalStorageBytes;

        // Getters and setters
        public long getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

        public long getTaxDocuments() { return taxDocuments; }
        public void setTaxDocuments(long taxDocuments) { this.taxDocuments = taxDocuments; }

        public long getAuditDocuments() { return auditDocuments; }
        public void setAuditDocuments(long auditDocuments) { this.auditDocuments = auditDocuments; }

        public long getComplianceDocuments() { return complianceDocuments; }
        public void setComplianceDocuments(long complianceDocuments) { this.complianceDocuments = complianceDocuments; }

        public long getTotalStorageBytes() { return totalStorageBytes; }
        public void setTotalStorageBytes(long totalStorageBytes) { this.totalStorageBytes = totalStorageBytes; }
    }
}