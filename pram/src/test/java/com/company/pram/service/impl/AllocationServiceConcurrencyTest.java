package com.company.pram.service.impl;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.entity.Employee;
import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;
import com.company.pram.exception.AllocationExceededException;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.repository.ProjectRepository;
import com.company.pram.service.AllocationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AllocationServiceConcurrencyTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Employee employee;
    private Project project;

    @BeforeEach
    void setUp() {
        // Clean up test data from previous runs if any
        cleanupTestData();

        // Create test employee
        employee = employeeRepository.save(Employee.builder()
                .employeeCode("CONC_EMP")
                .fullName("Concurrency Test Employee")
                .email("conc@test.com")
                .role("Developer")
                .department("R&D")
                .build());

        // Create test project
        project = projectRepository.save(Project.builder()
                .projectCode("CONC_PROJ")
                .projectName("Concurrency Test Project")
                .customer("Test Customer")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.ACTIVE)
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        jdbcTemplate.update("DELETE FROM allocation WHERE employee_id IN (SELECT employee_id FROM employee WHERE employee_code = 'CONC_EMP')");
        jdbcTemplate.update("DELETE FROM employee WHERE employee_code = 'CONC_EMP'");
        jdbcTemplate.update("DELETE FROM project WHERE project_code = 'CONC_PROJ'");
    }

    @Test
    void testConcurrentAllocationsPreventOverAllocation() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        AllocationRequest request = AllocationRequest.builder()
                .employeeId(employee.getEmployeeId())
                .projectId(project.getProjectId())
                .allocationPercent(60) // 60% + 60% = 120% (exceeds 100%)
                .roleInProject("QA")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build();

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    startLatch.await(); // wait for start trigger
                    allocationService.createAllocation(request);
                    successCount.incrementAndGet();
                } catch (Throwable e) {
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Start execution simultaneously
        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        service.shutdown();

        assertTrue(completed, "Concurrency test timed out before threads finished");

        // Verify statistics
        assertEquals(1, successCount.get(), "Exactly 1 transaction should succeed");
        assertEquals(1, failureCount.get(), "Exactly 1 transaction should fail due to lock concurrency block");

        Throwable thrownException = exceptions.peek();
        assertNotNull(thrownException);

        // Extract deep cause
        Throwable cause = thrownException;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }

        assertTrue(cause instanceof AllocationExceededException,
                "Failure should be caused by AllocationExceededException but was: " + cause.getClass().getName());
        assertEquals("Employee allocation exceeds 100%", cause.getMessage());
    }
}
