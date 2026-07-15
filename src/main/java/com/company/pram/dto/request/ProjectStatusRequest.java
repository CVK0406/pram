package com.company.pram.dto.request;

import com.company.pram.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectStatusRequest {
    @NotNull(message = "Status must not be null")
    private ProjectStatus status;
}
