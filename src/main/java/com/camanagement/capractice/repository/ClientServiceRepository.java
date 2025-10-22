package com.camanagement.capractice.repository;

import com.camanagement.capractice.entity.ClientService;
import com.camanagement.capractice.entity.ClientService.ServiceStatus;
import com.camanagement.capractice.entity.ClientService.Priority;
import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CLIENT-SERVICE REPOSITORY:
 * Database operations for client-service relationships
 */
@Repository
public interface ClientServiceRepository extends JpaRepository<ClientService, Long> {

    // Find by client
    List<ClientService> findByClient(Client client);
    List<ClientService> findByClientId(Long clientId);
    List<ClientService> findByClientOrderByAssignedDateDesc(Client client);

    // Find by service
    List<ClientService> findByService(Service service);
    List<ClientService> findByServiceId(Long serviceId);

    // Find by status
    List<ClientService> findByStatus(ServiceStatus status);
    List<ClientService> findByStatusOrderByDueDateAsc(ServiceStatus status);

    // Find by priority
    List<ClientService> findByPriority(Priority priority);
    List<ClientService> findByStatusAndPriority(ServiceStatus status, Priority priority);

    // Find by dates
    List<ClientService> findByDueDate(LocalDate dueDate);
    List<ClientService> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    List<ClientService> findByDueDateBefore(LocalDate date);
    List<ClientService> findByAssignedDateBetween(LocalDate startDate, LocalDate endDate);

    // Combined queries
    List<ClientService> findByClientAndStatus(Client client, ServiceStatus status);
    List<ClientService> findByServiceAndStatus(Service service, ServiceStatus status);

    // Counting queries
    long countByStatus(ServiceStatus status);
    long countByClient(Client client);
    long countByService(Service service);
    long countByPriority(Priority priority);
    long countByDueDateBefore(LocalDate date); // Count overdue

    // Check if client already has this service assigned
    boolean existsByClientAndService(Client client, Service service);
    Optional<ClientService> findByClientAndService(Client client, Service service);

    // Custom queries for dashboard
    @Query("SELECT cs FROM ClientService cs WHERE cs.status IN ('ASSIGNED', 'IN_PROGRESS') ORDER BY cs.dueDate ASC")
    List<ClientService> findActiveClientServices();

    @Query("SELECT COUNT(cs) FROM ClientService cs WHERE cs.status IN ('ASSIGNED', 'IN_PROGRESS')")
    long countActiveClientServices();

    @Query("SELECT cs FROM ClientService cs WHERE cs.dueDate < :date AND cs.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY cs.dueDate ASC")
    List<ClientService> findOverdueServices(@Param("date") LocalDate date);

    @Query("SELECT cs FROM ClientService cs WHERE cs.dueDate BETWEEN :startDate AND :endDate AND cs.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY cs.dueDate ASC")
    List<ClientService> findUpcomingServices(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Statistics queries
    @Query("SELECT COUNT(cs) FROM ClientService cs WHERE cs.status = 'COMPLETED' AND cs.completionDate BETWEEN :startDate AND :endDate")
    long countCompletedServicesBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(cs.finalPrice) FROM ClientService cs WHERE cs.status = 'COMPLETED' AND cs.completionDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Recent activities
    @Query("SELECT cs FROM ClientService cs ORDER BY cs.createdAt DESC")
    List<ClientService> findRecentlyCreated();

    @Query("SELECT cs FROM ClientService cs WHERE cs.status = 'COMPLETED' ORDER BY cs.completionDate DESC")
    List<ClientService> findRecentlyCompleted();
}