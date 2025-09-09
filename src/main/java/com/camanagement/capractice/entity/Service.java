package com.camanagement.capractice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SERVICE ENTITY EXPLANATION:
 *
 * This entity represents different services that your CA practice offers
 * Examples: ITR Filing, GST Registration, Annual Audit, etc.
 */
@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * SERVICE CATEGORY ENUM:
     * Different types of CA services
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ServiceCategory category;

    /**
     * PRICING INFORMATION:
     * BigDecimal is better than double for money calculations
     * - Precise decimal arithmetic
     * - No rounding errors
     */
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type")
    private PricingType pricingType;

    /**
     * SERVICE DURATION:
     * How long does this service typically take?
     */
    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    /**
     * SERVICE STATUS:
     * Is this service currently offered?
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status;

    /**
     * REQUIREMENTS:
     * What documents/info needed from client?
     */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    /**
     * DELIVERABLES:
     * What does the client get after service completion?
     */
    @Column(name = "deliverables", columnDefinition = "TEXT")
    private String deliverables;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public Service() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ServiceStatus.ACTIVE;
    }

    public Service(String serviceName, ServiceCategory category, BigDecimal basePrice) {
        this();
        this.serviceName = serviceName;
        this.category = category;
        this.basePrice = basePrice;
        this.pricingType = PricingType.FIXED;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public void setPricingType(PricingType pricingType) {
        this.pricingType = pricingType;
    }

    public Integer getEstimatedDurationDays() {
        return estimatedDurationDays;
    }

    public void setEstimatedDurationDays(Integer estimatedDurationDays) {
        this.estimatedDurationDays = estimatedDurationDays;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDeliverables() {
        return deliverables;
    }

    public void setDeliverables(String deliverables) {
        this.deliverables = deliverables;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility Methods

    /**
     * Get formatted price for display
     */
    public String getFormattedPrice() {
        if (basePrice == null) return "Price on request";

        String price = "₹" + basePrice.toString();
        if (pricingType == PricingType.HOURLY) {
            price += "/hr";
        } else if (pricingType == PricingType.MONTHLY) {
            price += "/month";
        }
        return price;
    }

    /**
     * Get duration in human-readable format
     */
    public String getFormattedDuration() {
        if (estimatedDurationDays == null) return "Duration varies";

        if (estimatedDurationDays == 1) {
            return "1 day";
        } else if (estimatedDurationDays <= 7) {
            return estimatedDurationDays + " days";
        } else if (estimatedDurationDays <= 30) {
            int weeks = estimatedDurationDays / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "");
        } else {
            int months = estimatedDurationDays / 30;
            return months + " month" + (months > 1 ? "s" : "");
        }
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", category=" + category +
                ", basePrice=" + basePrice +
                ", status=" + status +
                '}';
    }

    // Enums

    /**
     * SERVICE CATEGORIES:
     * Different areas of CA practice
     */
    public enum ServiceCategory {
        TAXATION("Taxation"),
        AUDIT_ASSURANCE("Audit & Assurance"),
        COMPLIANCE("Compliance"),
        ADVISORY("Advisory & Consulting"),
        ACCOUNTING("Accounting"),
        REGISTRATION("Registration & Licensing"),
        OTHER("Other Services");

        private final String displayName;

        ServiceCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * PRICING TYPES:
     * How is this service priced?
     */
    public enum PricingType {
        FIXED("Fixed Price"),
        HOURLY("Per Hour"),
        MONTHLY("Monthly"),
        PERCENTAGE("Percentage Based"),
        CUSTOM("Custom Pricing");

        private final String displayName;

        PricingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * SERVICE STATUS:
     * Is this service currently available?
     */
    public enum ServiceStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        SEASONAL("Seasonal"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        ServiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}