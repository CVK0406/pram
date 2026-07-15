package com.company.pram.repository;

import com.company.pram.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByEmployeeEmployeeIdAndDeletedAtIsNull(Long employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Allocation a WHERE a.employee.employeeId = :employeeId AND a.deletedAt IS NULL")
    List<Allocation> findByEmployeeForUpdate(@Param("employeeId") Long employeeId);

    @Query("SELECT COALESCE(SUM(a.allocationPercent), 0) FROM Allocation a " +
           "WHERE a.employee.employeeId = :employeeId " +
           "AND a.deletedAt IS NULL " +
           "AND a.allocationId != :excludeAllocationId " +
           "AND (a.endDate IS NULL OR a.endDate >= :startDate) " +
           "AND (a.startDate <= :endDate)")
    Integer sumOverlappingAllocations(@Param("employeeId") Long employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("excludeAllocationId") Long excludeAllocationId);

    @Query("SELECT COUNT(a) FROM Allocation a " +
           "WHERE a.deletedAt IS NULL " +
           "AND a.startDate <= :currentDate " +
           "AND (a.endDate IS NULL OR a.endDate >= :currentDate)")
    Long countActiveAllocations(@Param("currentDate") LocalDate currentDate);
}
