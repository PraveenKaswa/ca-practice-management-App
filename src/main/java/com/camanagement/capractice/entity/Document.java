package com.camanagement.capractice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DOCUMENT ENTITY - COMPLETE EXPLANATION:
 *
 * PURPOSE: Represents a file (PDF, Excel, Image) uploaded to the system
 *
 * HOW IT WORKS:
 * 1. User uploads a file through web form
 * 2. File is saved to disk (e.g., uploads/documents/form16.pdf)
 * 3. This entity stores INFORMATION about that file in database:
 *    - Who uploaded it? (Client)
 *    - What type? (Tax document, Audit report)
 *    - Where is it stored? (File path)
 *    - When was it uploaded? (Upload date)
 *
 * ANALOGY: Like a library card catalog
 * - The book = Actual file on disk
 * - The card = This entity in database (tells you about the book)
 */
@Entity
@Table(name = "documents")
public class Document {

    // ==================== PRIMARY KEY ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // WHAT: Unique identifier for each document
    // WHY: Database needs a way to uniquely identify each record
    // EXAMPLE: Document ID = 1, 2, 3, etc.

    // ==================== FILE INFORMATION ====================

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    // WHAT: Original name of the uploaded file
    // WHY: So we can show "Form16_2024.pdf" to user (not the internal name)
    // EXAMPLE: "Rajesh_ITR_2024.pdf", "GST_Invoice_March.xlsx"

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    // WHAT: Where the file is actually stored on server
    // WHY: So we can find and retrieve the file when user wants to download
    // EXAMPLE: "uploads/documents/2024/10/uuid-12345.pdf"
    // NOTE: We store the PATH in database, not the actual file content!

    @Column(name = "file_size")
    private Long fileSize;
    // WHAT: Size of file in bytes
    // WHY: To show file size to users, enforce upload limits
    // EXAMPLE: 1048576 bytes = 1 MB
    // HOW TO SHOW: Format as "1.5 MB", "250 KB", etc.

    @Column(name = "file_type", length = 100)
    private String fileType;
    // WHAT: MIME type of the file
    // WHY: To know what kind of file it is, show appropriate icon
    // EXAMPLES:
    // - "application/pdf" = PDF file
    // - "application/vnd.ms-excel" = Excel file
    // - "image/jpeg" = JPEG image

    @Column(name = "file_extension", length = 10)
    private String fileExtension;
    // WHAT: File extension (.pdf, .xlsx, .jpg)
    // WHY: Quick way to identify file type without parsing MIME type
    // EXAMPLE: "pdf", "xlsx", "jpg"

    // ==================== CATEGORIZATION ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private DocumentCategory category;
    // WHAT: What type of document is this?
    // WHY: So we can organize and filter documents
    // EXAMPLES: TAX, AUDIT, COMPLIANCE, FINANCIAL, GENERAL
    // WHY ENUM: Only allow specific predefined categories (no typos!)

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    // WHAT: Which client does this document belong to?
    // WHY: Documents are always linked to a client
    // RELATIONSHIP: Many documents can belong to ONE client
    // EXAMPLE: Rajesh Kumar has 10 documents (Form16, Bank Statement, etc.)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private com.camanagement.capractice.entity.Service service;
    // WHAT: (Optional) Link document to a specific service
    // WHY: Track which service this document relates to
    // EXAMPLE: Form16 linked to "ITR Filing" service
    // NOTE: nullable = true (not all documents need a service link)

    // ==================== METADATA ====================

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    // WHAT: Optional description/notes about the document
    // WHY: User can add context ("Original copy received from client")
    // EXAMPLE: "ITR documents for FY 2023-24, submitted by client on email"

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
    // WHAT: When was this document uploaded?
    // WHY: Track history, sort documents by date
    // EXAMPLE: "2024-10-23 14:30:00"

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;
    // WHAT: Who uploaded this document? (staff member name)
    // WHY: Audit trail - know who uploaded what
    // EXAMPLE: "Praveen Kumar", "Admin"
    // NOTE: For now it's just a string. Later with User Management,
    //       this would be a relationship to User entity

    @Column(name = "financial_year", length = 20)
    private String financialYear;
    // WHAT: Which financial year does this document relate to?
    // WHY: CA documents are often organized by FY
    // EXAMPLE: "2023-24", "2024-25"

    @Column(name = "tags", length = 500)
    private String tags;
    // WHAT: Comma-separated keywords for searching
    // WHY: Make documents searchable by multiple keywords
    // EXAMPLE: "income tax, salary, form16, 2024"

    // ==================== SOFT DELETE ====================

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    // WHAT: Is this document deleted?
    // WHY: "Soft delete" - don't actually delete from database, just mark as deleted
    // BENEFIT: Can recover accidentally deleted files, maintain history
    // EXAMPLE: true = deleted (hidden from user), false = active

    // ==================== CONSTRUCTORS ====================

    public Document() {
        this.uploadDate = LocalDateTime.now();
        this.isDeleted = false;
    }
    // WHAT: Default constructor
    // WHY: JPA requires it, also sets default values
    // SETS: uploadDate = current time, isDeleted = false (not deleted)

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Get human-readable file size
     * WHAT: Convert bytes to "1.5 MB", "250 KB" format
     * WHY: Users understand "1.5 MB" better than "1572864 bytes"
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        double bytes = fileSize.doubleValue();

        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024 * 1024 * 1024));
        }
    }
    // EXAMPLE: 1048576 bytes → "1.00 MB"

    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return fileType != null && fileType.startsWith("image/");
    }
    // WHAT: Is this an image file?
    // WHY: Show image preview for images, different icon for others
    // HOW: Check if MIME type starts with "image/"
    // EXAMPLE: "image/jpeg" → true, "application/pdf" → false

    /**
     * Check if file is a PDF
     */
    public boolean isPdf() {
        return "application/pdf".equals(fileType);
    }

    /**
     * Check if file is Excel
     */
    public boolean isExcel() {
        return fileType != null &&
                (fileType.contains("excel") || fileType.contains("spreadsheet"));
    }

    // ==================== GETTERS AND SETTERS ====================
    // (Standard getters and setters for all fields)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public DocumentCategory getCategory() { return category; }
    public void setCategory(DocumentCategory category) { this.category = category; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public com.camanagement.capractice.entity.Service getService() { return service; }
    public void setService(com.camanagement.capractice.entity.Service service) { this.service = service; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    // ==================== ENUM: DOCUMENT CATEGORIES ====================

    /**
     * DOCUMENT CATEGORIES - EXPLANATION:
     *
     * WHAT: Predefined categories for organizing documents
     * WHY:
     * - Keep documents organized
     * - Easy filtering ("Show me all TAX documents")
     * - Prevent typos (user can't enter "Taxx" or "audit")
     *
     * HOW ENUM WORKS:
     * - Only these exact values are allowed
     * - Database stores the string name ("TAX", "AUDIT")
     * - Type-safe in Java code
     */
    public enum DocumentCategory {
        TAX("Tax Documents"),
        // Income Tax Returns, Form 16, Tax proofs, etc.

        AUDIT("Audit Reports"),
        // Annual audit reports, audit certificates, etc.

        COMPLIANCE("Compliance Documents"),
        // GST returns, ROC filings, regulatory compliance

        FINANCIAL("Financial Statements"),
        // Balance sheets, P&L statements, cash flow statements

        CONTRACTS("Contracts & Agreements"),
        // Engagement letters, service agreements

        IDENTITY("Identity Documents"),
        // PAN card, Aadhaar, business registration certificates

        CORRESPONDENCE("Correspondence"),
        // Emails, letters from clients/authorities

        GENERAL("General Documents");
        // Miscellaneous documents that don't fit other categories

        private final String displayName;

        DocumentCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        // WHAT: User-friendly name for display
        // WHY: Show "Tax Documents" instead of "TAX" in UI
        // EXAMPLE: TAX.getDisplayName() → "Tax Documents"
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", category=" + category +
                ", uploadDate=" + uploadDate +
                '}';
    }
}