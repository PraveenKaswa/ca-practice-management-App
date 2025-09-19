package com.camanagement.capractice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CLIENT-SERVICE RELATIONSHIP ENTITY:
 *
 * This entity represents the assignment of a service to a client
 * Think of it as a "contract" or "engagement" between client and service
 *
 * Examples:
 * - Client "John Doe" is assigned "ITR Filing" service
 * - Client "ABC Company" is assigned "Annual Audit" service
 */
@Entity
@Table(name = "client_services")
public class ClientService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RELATIONSHIPS TO OTHER ENTITIES:
     *
     * @ManyToOne - Many client-service records can belong to one client
     * @JoinColumn - Specifies the foreign key column name
     *
     * Example: Client "John" can have multiple services (ITR, GST, Audit)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    /**
     * SERVICE ASSIGNMENT DETAILS:
     */
    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * SERVICE STATUS:
     * Track the progress of this specific service for this client
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status;

    /**
     * PRICING FOR THIS ASSIGNMENT:
     * Might be different from base service price due to customization
     */
    @Column(name = "quoted_price", precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;

    /**
     * ADDITIONAL DETAILS:
     */
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public ClientService() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ServiceStatus.ASSIGNED;
        this.assignedDate = LocalDate.now();
        this.priority = Priority.MEDIUM;
    }

    public ClientService(Client client, Service service) {
        this();
        this.client = client;
        this.service = service;
        this.quotedPrice = service.getBasePrice();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public BigDecimal getQuotedPrice() {
        return quotedPrice;
    }

    public void setQuotedPrice(BigDecimal quotedPrice) {
        this.quotedPrice = quotedPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility Methods

    /**
     * Check if service is overdue
     */
    public boolean isOverdue() {
        return dueDate != null &&
                dueDate.isBefore(LocalDate.now()) &&
                status != ServiceStatus.COMPLETED;
    }

    /**
     * Get days remaining until due date
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return -1;
        return LocalDate.now().until(dueDate).getDays();
    }

    /**
     * Get effective price (final price if set, otherwise quoted price)
     */
    public BigDecimal getEffectivePrice() {
        return finalPrice != null ? finalPrice : quotedPrice;
    }

    @Override
    public String toString() {
        return "ClientService{" +
                "id=" + id +
                ", client=" + (client != null ? client.getClientName() : "null") +
                ", service=" + (service != null ? service.getServiceName() : "null") +
                ", status=" + status +
                ", assignedDate=" + assignedDate +
                '}';
    }

    // Enums

    /**
     * SERVICE STATUS:
     * Track the progress of this service assignment
     */
    public enum ServiceStatus {
        ASSIGNED("Assigned"),
        IN_PROGRESS("In Progress"),
        ON_HOLD("On Hold"),
        REVIEW("Under Review"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        ServiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * PRIORITY LEVELS:
     * Helps with task management and scheduling
     */
    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}