package com.camanagement.capractice.repository;

import com.camanagement.capractice.entity.InvoiceItem;
import com.camanagement.capractice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * INVOICE ITEM REPOSITORY:
 *
 * Handles database operations for invoice line items
 * Simpler than InvoiceRepository since items are usually accessed through Invoice
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    /**
     * BASIC FIND METHODS:
     */

    // Find all items for a specific invoice
    List<InvoiceItem> findByInvoice(Invoice invoice);

    // Find items by invoice ID
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    /**
     * SERVICE-RELATED QUERIES:
     */

    // Find items linked to a specific service
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.service.id = :serviceId")
    List<InvoiceItem> findByServiceId(@Param("serviceId") Long serviceId);

    // Check if a service has been invoiced
    @Query("SELECT COUNT(ii) > 0 FROM InvoiceItem ii WHERE ii.service.id = :serviceId")
    boolean isServiceInvoiced(@Param("serviceId") Long serviceId);

    /**
     * CLEANUP METHODS:
     */

    // Delete all items for an invoice (usually handled by cascade, but available if needed)
    void deleteByInvoice(Invoice invoice);

    // Delete items by invoice ID
    void deleteByInvoiceId(Long invoiceId);

    /**
     * COUNTING QUERIES:
     */

    // Count items in an invoice
    long countByInvoice(Invoice invoice);

    // Count items by invoice ID
    long countByInvoiceId(Long invoiceId);
}