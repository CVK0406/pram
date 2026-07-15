package com.company.pram.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequest {

    @NotNull(message = "Employee ID must not be null")
    private Long employeeId;

    @NotNull(message = "Project ID must not be null")
    private Long projectId;

    @NotNull(message = "Allocation percent must not be null")
    @Min(value = 1, message = "Allocation percent must be at least 1")
    @Max(value = 100, message = "Allocation percent must not exceed 100")
    private Integer allocationPercent;

    @NotBlank(message = "Role in project must not be blank")
    private String roleInProject;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    private LocalDate endDate;

    @AssertTrue(message = "End date must be greater than or equal to start date")
    public boolean isEndDateAfterOrEqualStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
}
