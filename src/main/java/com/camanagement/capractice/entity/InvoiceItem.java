package com.camanagement.capractice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * INVOICE ITEM ENTITY:
 *
 * Represents individual line items on an invoice
 * Each line = one service with quantity, rate, and amount
 *
 * Example:
 * - ITR Filing x 1 @ ₹2,500 = ₹2,500
 * - GST Return x 3 @ ₹1,000 = ₹3,000
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * BELONGS TO INVOICE:
     * Many items can belong to one invoice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * REFERENCE TO SERVICE:
     * What service is being billed?
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    /**
     * OPTIONAL: REFERENCE TO CLIENT SERVICE ASSIGNMENT:
     * Link to the actual service assignment this item bills for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_service_id")
    private ClientService clientService;

    /**
     * LINE ITEM DETAILS:
     */
    @Column(name = "description", nullable = false, length = 500)
    private String description; // Service description

    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity; // Usually 1 for services, but can be > 1 for recurring services

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice; // Price per unit

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount; // quantity * unitPrice

    @Column(name = "item_order")
    private Integer itemOrder; // Display order on invoice

    // Constructors
    public InvoiceItem() {
        this.quantity = BigDecimal.ONE;
        this.amount = BigDecimal.ZERO;
    }

    public InvoiceItem(Service service, BigDecimal quantity, BigDecimal unitPrice) {
        this();
        this.service = service;
        this.description = service.getServiceName();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    /**
     * BUSINESS LOGIC:
     */

    // Calculate amount when quantity or price changes
    public void calculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = quantity.multiply(unitPrice);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public ClientService getClientService() { return clientService; }
    public void setClientService(ClientService clientService) { this.clientService = clientService; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateAmount();
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getItemOrder() { return itemOrder; }
    public void setItemOrder(Integer itemOrder) { this.itemOrder = itemOrder; }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", amount=" + amount +
                '}';
    }
}