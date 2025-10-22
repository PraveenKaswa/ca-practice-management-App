package com.camanagement.capractice.repository;

import com.camanagement.capractice.entity.Invoice;
import com.camanagement.capractice.entity.Invoice.InvoiceStatus;
import com.camanagement.capractice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * INVOICE REPOSITORY:
 *
 * Handles all database operations for invoices
 * Follows same pattern as ClientRepository
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * BASIC FIND METHODS:
     */

    // Find invoice by unique invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Find all invoices for a specific client
    List<Invoice> findByClient(Client client);

    // Find invoices by client ID
    List<Invoice> findByClientId(Long clientId);

    // Find invoices by status
    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * COMPLEX QUERIES WITH MULTIPLE CONDITIONS:
     */

    // Find invoices by client and status
    List<Invoice> findByClientAndStatus(Client client, InvoiceStatus status);

    // Find invoices by client ID and status
    List<Invoice> findByClientIdAndStatus(Long clientId, InvoiceStatus status);

    /**
     * DATE RANGE QUERIES:
     */

    // Find invoices created between dates
    List<Invoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);

    // Find invoices created after a specific date
    List<Invoice> findByInvoiceDateAfter(LocalDate date);

    // Find invoices due between dates
    List<Invoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * ORDERING RESULTS:
     */

    // Get all invoices ordered by invoice date (newest first)
    List<Invoice> findAllByOrderByInvoiceDateDesc();

    // Get invoices for a client ordered by date (newest first)
    List<Invoice> findByClientOrderByInvoiceDateDesc(Client client);

    // Get invoices by status ordered by due date
    List<Invoice> findByStatusOrderByDueDateAsc(InvoiceStatus status);

    /**
     * OVERDUE INVOICES:
     *
     * Critical for CA practice management
     */

    // Find all overdue invoices (due date passed and not paid/cancelled)
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    // Count overdue invoices
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    long countOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    /**
     * UNPAID INVOICES:
     */

    // Find unpaid invoices for a client (DRAFT, SENT, PARTIALLY_PAID)
    @Query("SELECT i FROM Invoice i WHERE i.client = :client AND i.status IN ('DRAFT', 'SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    List<Invoice> findUnpaidInvoicesByClient(@Param("client") Client client);

    // Find all unpaid invoices
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('DRAFT', 'SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    List<Invoice> findAllUnpaidInvoices();

    /**
     * PARTIALLY PAID INVOICES:
     */

    // Find partially paid invoices
    @Query("SELECT i FROM Invoice i WHERE i.paidAmount > 0 AND i.paidAmount < i.totalAmount")
    List<Invoice> findPartiallyPaidInvoices();

    /**
     * FINANCIAL CALCULATIONS:
     */

    // Get total outstanding amount for a client
    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM Invoice i WHERE i.client.id = :clientId AND i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal getTotalOutstandingByClient(@Param("clientId") Long clientId);

    // Get total revenue (all paid invoices)
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal getTotalRevenue();

    // Get revenue between dates
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total amount by status
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") InvoiceStatus status);

    // Get total outstanding across all clients
    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM Invoice i WHERE i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal getTotalOutstanding();

    /**
     * COUNTING QUERIES:
     */

    // Count invoices by status
    long countByStatus(InvoiceStatus status);

    // Count invoices for a client
    long countByClient(Client client);

    // Count invoices for a client by status
    long countByClientAndStatus(Client client, InvoiceStatus status);

    // Count invoices created this month
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.invoiceDate) = :year AND MONTH(i.invoiceDate) = :month")
    long countByMonth(@Param("year") int year, @Param("month") int month);

    /**
     * EXISTS QUERIES:
     */

    // Check if invoice number already exists
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * RECENT INVOICES:
     */

    // Get last 10 invoices
    List<Invoice> findTop10ByOrderByInvoiceDateDesc();

    // Get last N invoices for a client
    List<Invoice> findTop5ByClientOrderByInvoiceDateDesc(Client client);

    /**
     * LATEST INVOICE (for number generation):
     */

    // Get the latest invoice by ID (for generating next invoice number)
    @Query("SELECT i FROM Invoice i ORDER BY i.id DESC LIMIT 1")
    Optional<Invoice> findLatestInvoice();

    // Get latest invoice by invoice number
    Optional<Invoice> findTopByOrderByInvoiceNumberDesc();

    /**
     * SEARCH QUERIES:
     */

    // Search invoices by invoice number (partial match)
    List<Invoice> findByInvoiceNumberContainingIgnoreCase(String invoiceNumber);

    // Search by client name
    @Query("SELECT i FROM Invoice i WHERE LOWER(i.client.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))")
    List<Invoice> findByClientNameContaining(@Param("clientName") String clientName);

    /**
     * DASHBOARD STATISTICS:
     */

    // Get invoices created this month
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate >= :startOfMonth")
    List<Invoice> findInvoicesThisMonth(@Param("startOfMonth") LocalDate startOfMonth);

    // Count paid invoices this month
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'PAID' AND i.paymentDate >= :startOfMonth")
    long countPaidInvoicesThisMonth(@Param("startOfMonth") LocalDate startOfMonth);
}