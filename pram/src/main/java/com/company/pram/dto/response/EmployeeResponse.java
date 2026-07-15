package com.company.pram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
