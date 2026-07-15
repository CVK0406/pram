package com.company.pram.repository;

import com.company.pram.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByEmployeeEmployeeIdAndDeletedAtIsNull(Long employeeId);
}
