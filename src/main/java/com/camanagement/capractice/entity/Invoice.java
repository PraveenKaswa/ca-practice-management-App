package com.camanagement.capractice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * INVOICE ENTITY:
 *
 * Represents an invoice issued to a client for services rendered
 * Key Concepts:
 * - Invoice numbering (auto-generated)
 * - Line items (multiple services per invoice)
 * - Tax calculations (GST/CGST/SGST)
 * - Payment tracking
 * - Invoice status lifecycle
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * INVOICE NUMBER:
     * Auto-generated unique invoice number
     * Format: INV-2024-0001, INV-2024-0002, etc.
     */
    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    private String invoiceNumber;

    /**
     * CLIENT RELATIONSHIP:
     * Who is this invoice for?
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * INVOICE DATES:
     */
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * FINANCIAL DETAILS:
     * All amounts in BigDecimal for precision
     */
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal; // Total before tax

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage; // GST rate (e.g., 18.00)

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount; // Calculated tax

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage; // Discount if any

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount; // Calculated discount

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount; // Final amount

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount; // Amount paid so far

    /**
     * INVOICE STATUS:
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    /**
     * PAYMENT DETAILS:
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference; // Transaction ID, cheque number, etc.

    /**
     * INVOICE LINE ITEMS:
     * One invoice can have multiple service items
     * Cascade: When invoice is deleted, line items are also deleted
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    /**
     * ADDITIONAL DETAILS:
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Invoice() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.invoiceDate = LocalDate.now();
        this.status = InvoiceStatus.DRAFT;
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * BUSINESS LOGIC METHODS:
     */

    // Calculate and update all amounts
    public void calculateTotals() {
        // Calculate subtotal from line items
        this.subtotal = items.stream()
                .map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply discount
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercentage).divide(new BigDecimal("100"));
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // Calculate tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(taxPercentage).divide(new BigDecimal("100"));
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }

        // Calculate total
        this.totalAmount = afterDiscount.add(taxAmount);
    }

    // Add line item to invoice
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        calculateTotals();
    }

    // Remove line item
    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        calculateTotals();
    }

    // Check if invoice is overdue
    public boolean isOverdue() {
        if (status == InvoiceStatus.PAID || dueDate == null) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }

    // Get outstanding amount
    public BigDecimal getOutstandingAmount() {
        if (paidAmount == null) {
            return totalAmount;
        }
        return totalAmount.subtract(paidAmount);
    }

    // Check if partially paid
    public boolean isPartiallyPaid() {
        return paidAmount != null &&
                paidAmount.compareTo(BigDecimal.ZERO) > 0 &&
                paidAmount.compareTo(totalAmount) < 0;
    }

    // Record payment
    public void recordPayment(BigDecimal amount, PaymentMethod method, String reference) {
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        this.paidAmount = this.paidAmount.add(amount);
        this.paymentMethod = method;
        this.paymentReference = reference;
        this.paymentDate = LocalDate.now();

        // Update status
        if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", client=" + (client != null ? client.getClientName() : "null") +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }

    // Enums

    /**
     * INVOICE STATUS:
     * Lifecycle of an invoice
     */
    public enum InvoiceStatus {
        DRAFT("Draft"),
        SENT("Sent"),
        PARTIALLY_PAID("Partially Paid"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * PAYMENT METHODS:
     */
    public enum PaymentMethod {
        CASH("Cash"),
        CHEQUE("Cheque"),
        BANK_TRANSFER("Bank Transfer"),
        UPI("UPI"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        ONLINE("Online Payment");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}