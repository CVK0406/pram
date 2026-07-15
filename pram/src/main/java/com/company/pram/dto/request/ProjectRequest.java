package com.company.pram.dto.request;

import com.company.pram.entity.ProjectStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project code must not be blank")
    @Size(max = 20, message = "Project code must not exceed 20 characters")
    private String projectCode;

    @NotBlank(message = "Project name must not be blank")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String projectName;

    @NotBlank(message = "Customer must not be blank")
    @Size(max = 100, message = "Customer must not exceed 100 characters")
    private String customer;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;

    private ProjectStatus status;

    @AssertTrue(message = "End date must be greater than or equal to start date")
    public boolean isEndDateAfterOrEqualStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
}
