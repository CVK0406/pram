package com.company.pram.controller;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import com.company.pram.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    public ResponseEntity<AllocationResponse> createAllocation(@Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = allocationService.createAllocation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AllocationResponse> updateAllocation(
            @PathVariable Long id,
            @Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = allocationService.updateAllocation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable Long id) {
        allocationService.deleteAllocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllocationsByEmployee(@RequestParam Long employeeId) {
        List<AllocationResponse> response = allocationService.getAllocationsByEmployee(employeeId);
        return ResponseEntity.ok(response);
    }
}
