package com.company.pram.service.impl;

import com.company.pram.dto.request.EmployeeRequest;
import com.company.pram.dto.response.EmployeeResponse;
import com.company.pram.entity.Employee;
import com.company.pram.exception.DuplicateResourceException;
import com.company.pram.exception.EmployeeNotFoundException;
import com.company.pram.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest createRequest;
    private Employee savedEmployee;

    @BeforeEach
    void setUp() {
        createRequest = EmployeeRequest.builder()
                .employeeCode("EMP001")
                .fullName("Tuan Ho Anh")
                .email("tuan@company.com")
                .role("Senior Developer")
                .department("FSOFT-Q1")
                .build();

        savedEmployee = Employee.builder()
                .employeeId(1L)
                .employeeCode("EMP001")
                .fullName("Tuan Ho Anh")
                .email("tuan@company.com")
                .role("Senior Developer")
                .department("FSOFT-Q1")
                .build();
    }

    @Test
    @DisplayName("TC-01: createEmployee - success returns EmployeeResponse")
    void createEmployee_success() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("tuan@company.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        EmployeeResponse response = employeeService.createEmployee(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeCode()).isEqualTo("EMP001");
        assertThat(response.getFullName()).isEqualTo("Tuan Ho Anh");
        assertThat(response.getEmail()).isEqualTo("tuan@company.com");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("TC-02: createEmployee - duplicate employeeCode throws DuplicateResourceException (409)")
    void createEmployee_duplicateCode_throwsException() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("EMP001");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-03: createEmployee - duplicate email throws DuplicateResourceException (409)")
    void createEmployee_duplicateEmail_throwsException() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("tuan@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("tuan@company.com");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-04: getEmployeeById - not found throws EmployeeNotFoundException (404)")
    void getEmployeeById_notFound_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("999");
    }
}
