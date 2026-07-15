package com.company.pram.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Project Resource Allocation Management System (PRAMS)",
                version = "1.0.0",
                description = "REST API for managing employee resource allocations across projects. " +
                        "Enforces business rules: total allocation per employee ≤ 100%, " +
                        "no allocation to COMPLETED projects, and date boundary validation.",
                contact = @Contact(name = "PRAMS Team")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)
public class OpenApiConfig {
}
