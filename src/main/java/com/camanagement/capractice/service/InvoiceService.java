package com.camanagement.capractice.service;

import com.camanagement.capractice.entity.*;
import com.camanagement.capractice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * INVOICE SERVICE:
 *
 * Business logic layer for invoice operations
 * Handles:
 * - Invoice number generation
 * - Invoice creation from client services
 * - Payment recording
 * - Status updates
 * - Financial calculations
 */
@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    /**
     * GENERATE INVOICE NUMBER:
     *
     * Format: INV-2024-0001, INV-2024-0002, etc.
     * Auto-increments based on year
     */
    public String generateInvoiceNumber() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "INV-" + currentYear + "-";

        // Find the latest invoice
        Optional<Invoice> latestInvoice = invoiceRepository.findLatestInvoice();

        int nextNumber = 1;
        if (latestInvoice.isPresent()) {
            String lastNumber = latestInvoice.get().getInvoiceNumber();
            try {
                // Extract the numeric part after last hyphen
                String[] parts = lastNumber.split("-");
                if (parts.length == 3 && parts[1].equals(String.valueOf(currentYear))) {
                    nextNumber = Integer.parseInt(parts[2]) + 1;
                }
            } catch (Exception e) {
                System.err.println("Error parsing invoice number: " + lastNumber);
                // Continue with nextNumber = 1
            }
        }

        // Format: INV-2024-0001 (4 digits with leading zeros)
        return prefix + String.format("%04d", nextNumber);
    }

    /**
     * CREATE INVOICE FROM CLIENT SERVICES:
     *
     * Generate invoice for completed services of a client
     *
     * @param clientId - Client to invoice
     * @param clientServiceIds - List of completed service assignments
     * @param taxPercentage - GST percentage (default 18%)
     * @param discountPercentage - Discount percentage (default 0%)
     * @param paymentTermDays - Days until payment is due (default 15)
     * @return Created invoice
     */
    public Invoice createInvoiceFromServices(Long clientId,
                                             List<Long> clientServiceIds,
                                             BigDecimal taxPercentage,
                                             BigDecimal discountPercentage,
                                             Integer paymentTermDays) {

        System.out.println("=== CREATING INVOICE ===");
        System.out.println("Client ID: " + clientId);
        System.out.println("Service IDs: " + clientServiceIds);

        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        // Create new invoice
        Invoice invoice = new Invoice();
        invoice.setClient(client);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());

        // Set due date (default 15 days)
        int daysUntilDue = paymentTermDays != null ? paymentTermDays : 15;
        invoice.setDueDate(LocalDate.now().plusDays(daysUntilDue));

        // Set tax and discount
        invoice.setTaxPercentage(taxPercentage != null ? taxPercentage : new BigDecimal("18.00"));
        invoice.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);

        System.out.println("Invoice Number: " + invoice.getInvoiceNumber());

        // Add line items from client services
        int itemOrder = 1;
        for (Long serviceId : clientServiceIds) {
            ClientService clientService = clientServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service assignment not found with ID: " + serviceId));

            // Validate this service belongs to the client
            if (!clientService.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Service " + serviceId + " does not belong to client " + clientId);
            }

            // Create invoice item
            InvoiceItem item = new InvoiceItem();
            item.setService(clientService.getService());
            item.setClientService(clientService);
            item.setDescription(clientService.getService().getServiceName());
            item.setQuantity(BigDecimal.ONE);
            item.setUnitPrice(clientService.getQuotedPrice());
            item.setItemOrder(itemOrder++);
            item.calculateAmount();

            // Add item to invoice
            invoice.addItem(item);

            System.out.println("Added item: " + item.getDescription() + " - ₹" + item.getAmount());
        }

        // Calculate totals
        invoice.calculateTotals();

        System.out.println("Subtotal: ₹" + invoice.getSubtotal());
        System.out.println("Tax: ₹" + invoice.getTaxAmount());
        System.out.println("Total: ₹" + invoice.getTotalAmount());

        // Save invoice (cascade will save items too)
        Invoice savedInvoice = invoiceRepository.save(invoice);

        System.out.println("Invoice saved with ID: " + savedInvoice.getId());
        System.out.println("=== INVOICE CREATED SUCCESSFULLY ===");

        return savedInvoice;
    }

    /**
     * CREATE MANUAL INVOICE:
     *
     * Create invoice with custom line items (not linked to client services)
     */
    public Invoice createManualInvoice(Long clientId,
                                       List<ManualInvoiceItem> items,
                                       BigDecimal taxPercentage,
                                       BigDecimal discountPercentage,
                                       Integer paymentTermDays,
                                       String notes) {

        // Validate client
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setClient(client);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(paymentTermDays != null ? paymentTermDays : 15));
        invoice.setTaxPercentage(taxPercentage != null ? taxPercentage : new BigDecimal("18.00"));
        invoice.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setNotes(notes);

        // Add custom line items
        int itemOrder = 1;
        for (ManualInvoiceItem manualItem : items) {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(manualItem.getDescription());
            item.setQuantity(manualItem.getQuantity());
            item.setUnitPrice(manualItem.getUnitPrice());
            item.setItemOrder(itemOrder++);
            item.calculateAmount();

            invoice.addItem(item);
        }

        // Calculate and save
        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }

    /**
     * RECORD PAYMENT:
     *
     * Record a payment against an invoice
     */
    public Invoice recordPayment(Long invoiceId,
                                 BigDecimal amount,
                                 Invoice.PaymentMethod paymentMethod,
                                 String paymentReference) {

        System.out.println("=== RECORDING PAYMENT ===");
        System.out.println("Invoice ID: " + invoiceId);
        System.out.println("Amount: ₹" + amount);
        System.out.println("Method: " + paymentMethod);

        // Find invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        // Validate payment amount
        BigDecimal outstanding = invoice.getOutstandingAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        if (amount.compareTo(outstanding) > 0) {
            throw new RuntimeException("Payment amount (₹" + amount + ") exceeds outstanding amount (₹" + outstanding + ")");
        }

        // Record payment
        invoice.recordPayment(amount, paymentMethod, paymentReference);

        // Save and return
        Invoice savedInvoice = invoiceRepository.save(invoice);

        System.out.println("Payment recorded. New status: " + savedInvoice.getStatus());
        System.out.println("Outstanding: ₹" + savedInvoice.getOutstandingAmount());
        System.out.println("=== PAYMENT RECORDED SUCCESSFULLY ===");

        return savedInvoice;
    }

    /**
     * UPDATE INVOICE STATUS:
     */
    public Invoice updateInvoiceStatus(Long invoiceId, Invoice.InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        invoice.setStatus(newStatus);
        return invoiceRepository.save(invoice);
    }

    /**
     * SEND INVOICE:
     *
     * Mark invoice as SENT (ready to be sent to client)
     */
    public Invoice sendInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() != Invoice.InvoiceStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT invoices can be sent");
        }

        invoice.setStatus(Invoice.InvoiceStatus.SENT);
        return invoiceRepository.save(invoice);
    }

    /**
     * CANCEL INVOICE:
     */
    public Invoice cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        if (reason != null && !reason.isEmpty()) {
            String currentNotes = invoice.getNotes() != null ? invoice.getNotes() : "";
            invoice.setNotes(currentNotes + "\nCancellation reason: " + reason);
        }

        return invoiceRepository.save(invoice);
    }

    /**
     * GET INVOICE STATISTICS:
     */
    public InvoiceStatistics getInvoiceStatistics() {
        InvoiceStatistics stats = new InvoiceStatistics();

        // Total counts
        stats.setTotalInvoices(invoiceRepository.count());
        stats.setPaidInvoices(invoiceRepository.countByStatus(Invoice.InvoiceStatus.PAID));
        stats.setUnpaidInvoices(invoiceRepository.countByStatus(Invoice.InvoiceStatus.SENT) +
                invoiceRepository.countByStatus(Invoice.InvoiceStatus.PARTIALLY_PAID));
        stats.setOverdueInvoices(invoiceRepository.countOverdueInvoices(LocalDate.now()));

        // Financial totals
        stats.setTotalRevenue(invoiceRepository.getTotalRevenue());
        stats.setTotalOutstanding(invoiceRepository.getTotalOutstanding());

        // This month stats
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        stats.setRevenueThisMonth(invoiceRepository.getRevenueBetweenDates(startOfMonth, LocalDate.now()));
        stats.setInvoicesThisMonth(invoiceRepository.countByMonth(LocalDate.now().getYear(), LocalDate.now().getMonthValue()));

        return stats;
    }

    /**
     * CHECK FOR OVERDUE INVOICES AND UPDATE STATUS:
     *
     * Run this periodically to mark overdue invoices
     */
    public void updateOverdueInvoices() {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(today);

        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() != Invoice.InvoiceStatus.OVERDUE) {
                invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                System.out.println("Marked invoice " + invoice.getInvoiceNumber() + " as OVERDUE");
            }
        }
    }

    /**
     * HELPER CLASS: Manual Invoice Item
     * For creating invoices with custom items
     */
    public static class ManualInvoiceItem {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;

        public ManualInvoiceItem(String description, BigDecimal quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters
        public String getDescription() { return description; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }

    /**
     * HELPER CLASS: Invoice Statistics
     * For dashboard display
     */
    public static class InvoiceStatistics {
        private long totalInvoices;
        private long paidInvoices;
        private long unpaidInvoices;
        private long overdueInvoices;
        private BigDecimal totalRevenue;
        private BigDecimal totalOutstanding;
        private BigDecimal revenueThisMonth;
        private long invoicesThisMonth;

        // Getters and Setters
        public long getTotalInvoices() { return totalInvoices; }
        public void setTotalInvoices(long totalInvoices) { this.totalInvoices = totalInvoices; }

        public long getPaidInvoices() { return paidInvoices; }
        public void setPaidInvoices(long paidInvoices) { this.paidInvoices = paidInvoices; }

        public long getUnpaidInvoices() { return unpaidInvoices; }
        public void setUnpaidInvoices(long unpaidInvoices) { this.unpaidInvoices = unpaidInvoices; }

        public long getOverdueInvoices() { return overdueInvoices; }
        public void setOverdueInvoices(long overdueInvoices) { this.overdueInvoices = overdueInvoices; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getTotalOutstanding() { return totalOutstanding; }
        public void setTotalOutstanding(BigDecimal totalOutstanding) { this.totalOutstanding = totalOutstanding; }

        public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
        public void setRevenueThisMonth(BigDecimal revenueThisMonth) { this.revenueThisMonth = revenueThisMonth; }

        public long getInvoicesThisMonth() { return invoicesThisMonth; }
        public void setInvoicesThisMonth(long invoicesThisMonth) { this.invoicesThisMonth = invoicesThisMonth; }
    }
}
