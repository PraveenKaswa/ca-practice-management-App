package com.camanagement.capractice.entity;

// These imports bring in JPA (Java Persistence API) annotations
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CLIENT ENTITY EXPLANATION:
 *
 * @Entity - Tells Spring "This class represents a database table"
 * @Table - Specifies the actual table name in database (optional)
 *
 * This class will automatically create a "clients" table with columns
 * matching the field names below.
 */
@Entity
@Table(name = "clients")
public class Client {

    /**
     * PRIMARY KEY SETUP:
     *
     * @Id - Marks this field as the primary key
     * @GeneratedValue - Database automatically generates values (auto-increment)
     * GenerationType.IDENTITY - Use database's auto-increment feature
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * COLUMN CONFIGURATIONS:
     *
     * @Column - Customizes how the field maps to database column
     * - name: column name in database
     * - nullable: can this be empty? (false = required field)
     * - length: maximum character length
     * - unique: must be unique across all records
     */
    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "company_name", length = 150)
    private String companyName;

    // PAN must be unique - no two clients can have same PAN
    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    // TEXT type for longer content (addresses can be long)
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    /**
     * ENUMS IN DATABASE:
     *
     * @Enumerated(EnumType.STRING) - Store enum as text in database
     * Alternative: EnumType.ORDINAL stores as numbers (not recommended)
     *
     * Why STRING? If you add new enum values, ordinal positions change
     * but string names stay the same - much safer!
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type")
    private ClientType clientType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;

    /**
     * DATE HANDLING:
     *
     * LocalDate - Just the date (2024-03-15)
     * LocalDateTime - Date + time (2024-03-15 14:30:25)
     */
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * CONSTRUCTORS:
     *
     * Default constructor - required by JPA
     * Custom constructor - for easy object creation
     */
    public Client() {
        // Set default values when creating new client
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ClientStatus.ACTIVE;
        this.registrationDate = LocalDate.now();
    }

    public Client(String clientName, String email, String phone) {
        this(); // Call default constructor first
        this.clientName = clientName;
        this.email = email;
        this.phone = phone;
    }

    /**
     * JPA LIFECYCLE CALLBACKS:
     *
     * @PreUpdate - Runs automatically before updating record
     * This ensures updatedAt is always current when data changes
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    // JPA needs these to read/write field values

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
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

    /**
     * UTILITY METHODS:
     *
     * Business logic methods that make the entity more useful
     */
    public String getDisplayName() {
        if (companyName != null && !companyName.trim().isEmpty()) {
            return companyName + " (" + clientName + ")";
        }
        return clientName;
    }

    /**
     * toString() - Used for debugging and logging
     * Shows key fields when you print the object
     */
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * ENUMS DEFINED INSIDE THE CLASS:
     *
     * Why inside? Keeps related constants close to where they're used
     * Each enum has a displayName for user-friendly text
     */
    public enum ClientType {
        INDIVIDUAL("Individual"),
        COMPANY("Company"),
        PARTNERSHIP("Partnership"),
        LLP("Limited Liability Partnership"),
        TRUST("Trust"),
        SOCIETY("Society"),
        HUF("Hindu Undivided Family");

        private final String displayName;

        ClientType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ClientStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        SUSPENDED("Suspended"),
        ARCHIVED("Archived");

        private final String displayName;

        ClientStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}