package com.camanagement.capractice.repository;

import com.camanagement.capractice.entity.Service;
import com.camanagement.capractice.entity.Service.ServiceCategory;
import com.camanagement.capractice.entity.Service.ServiceStatus;
import com.camanagement.capractice.entity.Service.PricingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE REPOSITORY:
 * Database operations for Service entity
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // Basic finders
    List<Service> findByStatus(ServiceStatus status);
    List<Service> findByCategory(ServiceCategory category);
    List<Service> findByPricingType(PricingType pricingType);
    Optional<Service> findByServiceName(String serviceName);

    // Combined filters
    List<Service> findByCategoryAndStatus(ServiceCategory category, ServiceStatus status);
    List<Service> findByStatusOrderByServiceNameAsc(ServiceStatus status);

    // Search queries
    List<Service> findByServiceNameContainingIgnoreCase(String serviceName);
    List<Service> findByDescriptionContainingIgnoreCase(String description);

    // Price-based queries
    List<Service> findByBasePriceLessThanEqual(BigDecimal maxPrice);
    List<Service> findByBasePriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Duration-based queries
    List<Service> findByEstimatedDurationDaysLessThanEqual(Integer maxDays);

    // Counting queries
    long countByStatus(ServiceStatus status);
    long countByCategory(ServiceCategory category);

    // Validation queries
    boolean existsByServiceName(String serviceName);

    // Custom queries
    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE' AND s.category = :category ORDER BY s.basePrice ASC")
    List<Service> findActiveServicesByCategory(@Param("category") ServiceCategory category);

    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE' ORDER BY s.serviceName ASC")
    List<Service> findAllActiveServices();

    @Query("SELECT DISTINCT s.category FROM Service s WHERE s.status = 'ACTIVE'")
    List<ServiceCategory> findActiveCategories();

    // Statistics queries
    @Query("SELECT AVG(s.basePrice) FROM Service s WHERE s.status = 'ACTIVE' AND s.pricingType = 'FIXED'")
    BigDecimal getAverageServicePrice();

    @Query("SELECT COUNT(s), s.category FROM Service s WHERE s.status = 'ACTIVE' GROUP BY s.category")
    List<Object[]> getServiceCountByCategory();
}