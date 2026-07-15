package com.company.pram.service;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import java.util.List;

public interface AllocationService {
    AllocationResponse createAllocation(AllocationRequest request);
    AllocationResponse updateAllocation(Long id, AllocationRequest request);
    void deleteAllocation(Long id);
    List<AllocationResponse> getAllocationsByEmployee(Long employeeId);
}
