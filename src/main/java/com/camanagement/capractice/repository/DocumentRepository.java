package com.camanagement.capractice.repository;

import com.camanagement.capractice.entity.Document;
import com.camanagement.capractice.entity.Document.DocumentCategory;
import com.camanagement.capractice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DOCUMENT REPOSITORY - COMPLETE EXPLANATION:
 *
 * PURPOSE: Handles ALL database operations for documents
 *
 * HOW IT WORKS:
 * - Extends JpaRepository<Document, Long>
 *   - Document = Entity type we're working with
 *   - Long = Type of primary key (id field)
 * - Spring automatically generates SQL queries from method names!
 *
 * MAGIC OF SPRING DATA JPA:
 * You write: findByClient(Client client)
 * Spring generates: SELECT * FROM documents WHERE client_id = ?
 *
 * NO SQL CODE NEEDED! Just follow naming conventions!
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // ==================== BASIC FIND METHODS ====================

    /**
     * Find all documents for a specific client
     *
     * WHAT: Get all documents belonging to one client
     * WHY: When viewing client details, show all their documents
     * HOW: Spring reads method name:
     *      findBy + Client → WHERE client_id = ?
     * SQL: SELECT * FROM documents WHERE client_id = ? AND is_deleted = false
     */
    List<Document> findByClientAndIsDeletedFalse(Client client);
    // EXAMPLE: Get all documents for "Rajesh Kumar"
    // Returns: [Form16.pdf, BankStatement.xlsx, ITR_2024.pdf]

    /**
     * Find documents by client ID (alternative approach)
     *
     * WHY TWO METHODS?
     * - findByClient() - Use when you have Client object
     * - findByClientId() - Use when you only have the ID
     */
    List<Document> findByClientIdAndIsDeletedFalse(Long clientId);
    // EXAMPLE: findByClientId(5) → All documents for client with ID=5

    /**
     * Find documents by category
     *
     * WHAT: Get all TAX documents, or all AUDIT documents
     * WHY: Filter documents by type
     * HOW: findBy + Category → WHERE category = ?
     */
    List<Document> findByCategoryAndIsDeletedFalse(DocumentCategory category);
    // EXAMPLE: findByCategory(TAX)
    // Returns: All tax-related documents (Form16, ITR, etc.)

    /**
     * Find documents by client AND category
     *
     * WHAT: Get specific type of documents for a client
     * WHY: "Show me all TAX documents for Rajesh Kumar"
     * HOW: Spring combines conditions with AND
     */
    List<Document> findByClientAndCategoryAndIsDeletedFalse(Client client, DocumentCategory category);
    // EXAMPLE: findByClientAndCategory(rajesh, TAX)
    // Returns: All TAX documents for Rajesh only

    // ==================== SEARCH METHODS ====================

    /**
     * Search documents by filename
     *
     * WHAT: Search for documents containing text in filename
     * WHY: User searches "Form16" → Find all files with "Form16" in name
     * HOW:
     * - Containing → SQL LIKE '%text%' (matches anywhere)
     * - IgnoreCase → Case-insensitive search
     */
    List<Document> findByFileNameContainingIgnoreCaseAndIsDeletedFalse(String fileName);
    // EXAMPLE: findByFileNameContaining("form16")
    // Matches: "Form16.pdf", "rajesh_form16_2024.pdf", "FORM16_final.pdf"
    // DOESN'T match: "ITR.pdf", "BankStatement.xlsx"

    /**
     * Search in description field
     *
     * WHY: User added notes like "Original copy from email"
     *      Search can find it even if filename doesn't match
     */
    List<Document> findByDescriptionContainingIgnoreCaseAndIsDeletedFalse(String description);

    /**
     * Search in tags field
     *
     * WHAT: Search documents by keywords/tags
     * WHY: User tagged document "income, salary, 2024"
     *      Can search by any of these keywords
     */
    List<Document> findByTagsContainingIgnoreCaseAndIsDeletedFalse(String tag);
    // EXAMPLE: Document tagged "income tax, salary, form16"
    // Search "salary" → Will find this document

    // ==================== FINANCIAL YEAR QUERIES ====================

    /**
     * Find documents by financial year
     *
     * WHAT: Get all documents for FY 2023-24, FY 2024-25, etc.
     * WHY: CA work is organized by financial year
     */
    List<Document> findByFinancialYearAndIsDeletedFalse(String financialYear);
    // EXAMPLE: findByFinancialYear("2023-24")
    // Returns: All documents tagged with FY 2023-24

    /**
     * Find documents by client AND financial year
     *
     * WHAT: Get client's documents for a specific year
     * WHY: "Show me Rajesh's documents for FY 2023-24"
     */
    List<Document> findByClientAndFinancialYearAndIsDeletedFalse(Client client, String financialYear);

    // ==================== DATE RANGE QUERIES ====================

    /**
     * Find documents uploaded between dates
     *
     * WHAT: Get documents uploaded in a date range
     * WHY: "Show me all documents uploaded last month"
     * HOW: Between → WHERE upload_date >= start AND upload_date <= end
     */
    List<Document> findByUploadDateBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);
    // EXAMPLE: findByUploadDateBetween(Oct 1, Oct 31)
    // Returns: All documents uploaded in October

    /**
     * Find documents uploaded after a date
     *
     * WHAT: Get recent documents
     * WHY: "Show me documents uploaded in last 7 days"
     */
    List<Document> findByUploadDateAfterAndIsDeletedFalse(LocalDateTime date);
    // EXAMPLE: findByUploadDateAfter(7 days ago)
    // Returns: Documents uploaded in last week

    // ==================== ORDERING RESULTS ====================

    /**
     * Get all active documents ordered by upload date (newest first)
     *
     * WHAT: Get all documents sorted by date
     * WHY: Show recent documents first
     * HOW: OrderBy + FieldName + Desc
     */
    List<Document> findByIsDeletedFalseOrderByUploadDateDesc();
    // EXAMPLE: Returns documents: [Today's doc, Yesterday's doc, Last week's doc]

    /**
     * Get client's documents ordered by date
     */
    List<Document> findByClientAndIsDeletedFalseOrderByUploadDateDesc(Client client);
    // EXAMPLE: Get Rajesh's documents, newest first

    /**
     * Get documents by category, sorted by date
     */
    List<Document> findByCategoryAndIsDeletedFalseOrderByUploadDateDesc(DocumentCategory category);
    // EXAMPLE: Get all TAX documents, newest first

    // ==================== COUNTING QUERIES ====================

    /**
     * Count total documents for a client
     *
     * WHAT: How many documents does this client have?
     * WHY: Show statistics "Rajesh Kumar: 15 documents"
     * RETURNS: Long (number, not List)
     */
    long countByClientAndIsDeletedFalse(Client client);
    // EXAMPLE: countByClient(rajesh) → 15

    /**
     * Count documents by category
     */
    long countByCategoryAndIsDeletedFalse(DocumentCategory category);
    // EXAMPLE: countByCategory(TAX) → 45 (45 tax documents in system)

    /**
     * Count documents by file type
     *
     * WHY: Statistics: "You have 30 PDFs, 10 Excel files, 5 images"
     */
    long countByFileTypeAndIsDeletedFalse(String fileType);

    // ==================== RECENT DOCUMENTS ====================

    /**
     * Get last N uploaded documents
     *
     * WHAT: Get 10 most recent documents
     * WHY: Show "Recent Uploads" on dashboard
     * HOW: Top10 + OrderBy + Desc
     */
    List<Document> findTop10ByIsDeletedFalseOrderByUploadDateDesc();
    // EXAMPLE: Returns 10 most recently uploaded documents

    /**
     * Get last N documents for a client
     */
    List<Document> findTop5ByClientAndIsDeletedFalseOrderByUploadDateDesc(Client client);
    // EXAMPLE: Get Rajesh's 5 most recent documents

    // ==================== FILE TYPE QUERIES ====================

    /**
     * Find documents by file extension
     *
     * WHAT: Get all PDF files, or all Excel files
     * WHY: Filter by file type
     */
    List<Document> findByFileExtensionAndIsDeletedFalse(String extension);
    // EXAMPLE: findByFileExtension("pdf") → All PDF documents

    /**
     * Find images only
     *
     * WHAT: Get all image files (JPG, PNG, etc.)
     * WHY: Show image gallery
     * HOW: Custom query checking if MIME type starts with "image/"
     */
    @Query("SELECT d FROM Document d WHERE d.fileType LIKE 'image/%' AND d.isDeleted = false")
    List<Document> findAllImages();
    // EXAMPLE: Returns all JPEG, PNG, GIF, etc. files

    /**
     * Find PDFs only
     */
    @Query("SELECT d FROM Document d WHERE d.fileType = 'application/pdf' AND d.isDeleted = false")
    List<Document> findAllPdfs();

    // ==================== ADVANCED SEARCH ====================

    /**
     * Search across multiple fields
     *
     * WHAT: Search in filename, description, and tags
     * WHY: Comprehensive search - find documents matching any field
     * HOW: Custom @Query with OR conditions
     */
    @Query("SELECT d FROM Document d WHERE d.isDeleted = false AND " +
            "(LOWER(d.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Document> searchDocuments(@Param("searchTerm") String searchTerm);
    // WHAT THIS QUERY DOES:
    // 1. Looks in filename for searchTerm
    // 2. OR looks in description for searchTerm
    // 3. OR looks in tags for searchTerm
    // EXAMPLE: searchDocuments("tax")
    // Finds: Documents with "tax" in filename, description, OR tags

    /**
     * Advanced filter with multiple criteria
     *
     * WHAT: Filter by client, category, and date range
     * WHY: Complex filtering "Show Rajesh's TAX docs from last month"
     */
    @Query("SELECT d FROM Document d WHERE d.client = :client " +
            "AND (:category IS NULL OR d.category = :category) " +
            "AND d.uploadDate BETWEEN :startDate AND :endDate " +
            "AND d.isDeleted = false " +
            "ORDER BY d.uploadDate DESC")
    List<Document> findByMultipleCriteria(
            @Param("client") Client client,
            @Param("category") DocumentCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    // EXPLANATION OF QUERY:
    // - Must match client (required)
    // - Category is optional (:category IS NULL OR matches category)
    // - Must be in date range
    // - Must not be deleted
    // - Sorted by date, newest first

    // ==================== STATISTICS QUERIES ====================

    /**
     * Get total file size for a client
     *
     * WHAT: How much storage is this client using?
     * WHY: Show "Rajesh's documents: 45 MB"
     * HOW: SUM aggregation function
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d " +
            "WHERE d.client = :client AND d.isDeleted = false")
    Long getTotalFileSizeByClient(@Param("client") Client client);
    // EXAMPLE: getTotalFileSizeByClient(rajesh) → 45000000 (45 MB in bytes)
    // COALESCE: If no documents, return 0 instead of null

    /**
     * Get document count by category
     *
     * WHAT: Count documents in each category
     * WHY: Dashboard statistics
     */
    @Query("SELECT d.category, COUNT(d) FROM Document d " +
            "WHERE d.isDeleted = false " +
            "GROUP BY d.category")
    List<Object[]> getDocumentCountByCategory();
    // RETURNS: [[TAX, 30], [AUDIT, 15], [COMPLIANCE, 20]]
    // USAGE: for (Object[] row : results) {
    //            DocumentCategory cat = (DocumentCategory) row[0];
    //            Long count = (Long) row[1];
    //        }

    // ==================== DELETED DOCUMENTS (SOFT DELETE) ====================

    /**
     * Find deleted documents (for admin recovery)
     *
     * WHAT: Get documents marked as deleted
     * WHY: Admin can recover accidentally deleted files
     */
    List<Document> findByIsDeletedTrue();
    // EXAMPLE: Show "Recycle Bin" of deleted documents

    /**
     * Find deleted documents for a client
     */
    List<Document> findByClientAndIsDeletedTrue(Client client);

    // ==================== VALIDATION QUERIES ====================

    /**
     * Check if a document with same name exists for client
     *
     * WHAT: Does this filename already exist?
     * WHY: Prevent duplicate uploads
     */
    boolean existsByClientAndFileNameAndIsDeletedFalse(Client client, String fileName);
    // EXAMPLE: existsByClientAndFileName(rajesh, "Form16.pdf")
    // Returns: true if Rajesh already has a file named "Form16.pdf"

    /**
     * Find document by file path
     *
     * WHAT: Find document using its storage path
     * WHY: When downloading, we have the path and need the Document record
     */
    Optional<Document> findByFilePathAndIsDeletedFalse(String filePath);
}