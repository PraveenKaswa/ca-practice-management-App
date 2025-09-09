package com.camanagement.capractice.repository;

// Import Spring Data JPA classes
import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.entity.Client.ClientStatus;
import com.camanagement.capractice.entity.Client.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CLIENT REPOSITORY EXPLANATION:
 *
 * @Repository - Marks this as a data access layer component
 *
 * JpaRepository<Client, Long>:
 * - Client: The entity type we're working with
 * - Long: The type of the primary key (id field)
 *
 * By extending JpaRepository, we automatically get:
 * - save(client) - Insert or update
 * - findById(id) - Find by primary key
 * - findAll() - Get all records
 * - delete(client) - Delete a record
 * - count() - Count all records
 * - And many more!
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * SPRING DATA JPA MAGIC - METHOD NAME QUERIES:
     *
     * Spring reads the method name and generates SQL automatically!
     *
     * findByClientName → SELECT * FROM clients WHERE client_name = ?
     * findByStatus → SELECT * FROM clients WHERE status = ?
     *
     * Method naming rules:
     * - findBy + FieldName
     * - findBy + FieldName + And + AnotherField
     * - findBy + FieldName + OrderBy + AnotherField + Desc
     */

    // Find clients by exact name match
    List<Client> findByClientName(String clientName);

    // Find clients by status (ACTIVE, INACTIVE, etc.)
    List<Client> findByStatus(ClientStatus status);

    // Find clients by type (INDIVIDUAL, COMPANY, etc.)
    List<Client> findByClientType(ClientType clientType);

    // Find client by PAN number (should be unique)
    Optional<Client> findByPanNumber(String panNumber);

    // Find client by email
    Optional<Client> findByEmail(String email);

    // Find clients by city
    List<Client> findByCity(String city);

    // Find clients by state
    List<Client> findByState(String state);

    /**
     * COMPLEX QUERIES WITH MULTIPLE CONDITIONS:
     *
     * Method names can get quite complex but very readable
     */

    // Find active clients in a specific city
    List<Client> findByStatusAndCity(ClientStatus status, String city);

    // Find clients by type and status
    List<Client> findByClientTypeAndStatus(ClientType clientType, ClientStatus status);

    // Find clients registered after a certain date
    List<Client> findByRegistrationDateAfter(LocalDate date);

    // Find clients registered between two dates
    List<Client> findByRegistrationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * SEARCH QUERIES - LIKE OPERATIONS:
     *
     * Containing = SQL LIKE '%text%' (search anywhere in field)
     * StartingWith = SQL LIKE 'text%' (starts with text)
     * EndingWith = SQL LIKE '%text' (ends with text)
     * IgnoreCase = Case insensitive search
     */

    // Search clients by name (partial match, case insensitive)
    List<Client> findByClientNameContainingIgnoreCase(String name);

    // Search by company name
    List<Client> findByCompanyNameContainingIgnoreCase(String companyName);

    // Search by email domain
    List<Client> findByEmailContaining(String emailDomain);

    List<Client> findAllByOrderByClientNameAsc();

    /**
     * ORDERING RESULTS:
     *
     * OrderBy + FieldName + Asc/Desc
     */

    // Find all active clients ordered by name
    List<Client> findByStatusOrderByClientNameAsc(ClientStatus status);

    // Find all clients ordered by registration date (newest first)
    List<Client> findAllByOrderByRegistrationDateDesc();

    /**
     * COUNTING QUERIES:
     *
     * Same naming rules but returns long (count)
     */

    // Count clients by status
    long countByStatus(ClientStatus status);

    // Count clients by type
    long countByClientType(ClientType clientType);

    // Count clients registered this year
    long countByRegistrationDateAfter(LocalDate date);

    /**
     * EXISTS QUERIES:
     *
     * Returns boolean - useful for validation
     */

    // Check if client with PAN already exists
    boolean existsByPanNumber(String panNumber);

    // Check if email is already taken
    boolean existsByEmail(String email);

    /**
     * CUSTOM QUERIES WITH @Query:
     *
     * When method names become too complex, write custom JPQL
     * JPQL = Java Persistence Query Language (like SQL but for entities)
     */

    // Find clients with notes containing specific text
    @Query("SELECT c FROM Client c WHERE c.notes LIKE %:keyword%")
    List<Client> findByNotesContaining(@Param("keyword") String keyword);

    // Find clients by city and state with custom query
    @Query("SELECT c FROM Client c WHERE c.city = :city AND c.state = :state")
    List<Client> findByLocation(@Param("city") String city, @Param("state") String state);

    // Count active clients by type
    @Query("SELECT COUNT(c) FROM Client c WHERE c.status = 'ACTIVE' AND c.clientType = :type")
    long countActiveClientsByType(@Param("type") ClientType type);

    // Find clients registered in a specific month/year
    @Query("SELECT c FROM Client c WHERE YEAR(c.registrationDate) = :year AND MONTH(c.registrationDate) = :month")
    List<Client> findByRegistrationMonth(@Param("year") int year, @Param("month") int month);

    /**
     * NATIVE SQL QUERIES:
     *
     * When you need database-specific features, use native SQL
     * nativeQuery = true tells Spring to use raw SQL
     */

    // Find clients with complex address search (native SQL)
    @Query(value = "SELECT * FROM clients WHERE LOWER(address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))",
            nativeQuery = true)
    List<Client> searchByAddress(@Param("searchTerm") String searchTerm);

    // Get client statistics with native SQL
    @Query(value = "SELECT client_type, COUNT(*) as count FROM clients GROUP BY client_type",
            nativeQuery = true)
    List<Object[]> getClientTypeStatistics();
}