-- SQL Script to Create Tables for Project Resource Allocation Management System (PRAMS)

-- 1. Employee Table
CREATE TABLE IF NOT EXISTS employee (
    employee_id     BIGSERIAL PRIMARY KEY,
    employee_code   VARCHAR(20)  UNIQUE NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    department      VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now()
);

-- 2. Project Table
CREATE TABLE IF NOT EXISTS project (
    project_id      BIGSERIAL PRIMARY KEY,
    project_code    VARCHAR(20) UNIQUE NOT NULL,
    project_name    VARCHAR(200) NOT NULL,
    customer        VARCHAR(100) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNING'
        CHECK (status IN ('PLANNING','ACTIVE','COMPLETED')),
    CONSTRAINT chk_project_dates CHECK (end_date >= start_date)
);

-- 3. Allocation Table
CREATE TABLE IF NOT EXISTS allocation (
    allocation_id       BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL REFERENCES employee(employee_id),
    project_id          BIGINT NOT NULL REFERENCES project(project_id),
    allocation_percent  INTEGER NOT NULL CHECK (allocation_percent > 0 AND allocation_percent <= 100),
    role_in_project     VARCHAR(100) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    deleted_at          TIMESTAMP NULL,
    CONSTRAINT chk_allocation_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- 4. Indexes for query optimization
CREATE INDEX IF NOT EXISTS idx_allocation_employee ON allocation(employee_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_allocation_project ON allocation(project_id);

-- 5. Seed Data (ensuring no constraints or business rules are violated)

-- Insert Employees
INSERT INTO employee (employee_id, employee_code, full_name, email, role, department)
VALUES 
(1, 'EMP001', 'Tuan Ho Anh', 'tuanha@company.com', 'Senior Java Developer', 'FSOFT-Q1'),
(2, 'EMP002', 'Le Thi C', 'cle@company.com', 'QA Lead', 'FSOFT-Q2'),
(3, 'EMP003', 'Nguyen Van B', 'bnguyen@company.com', 'Junior Java Developer', 'FSOFT-Q1'),
(4, 'EMP004', 'Test User', 'test@company.com', 'Tester', 'FSOFT-Q3'),
(5, 'EMP005', 'Le Hoang Nam', 'namlh@company.com', 'Project Manager', 'FSOFT-Q1')
ON CONFLICT (employee_code) DO NOTHING;

-- Adjust sequence for employee_id
SELECT setval(pg_get_serial_sequence('employee', 'employee_id'), COALESCE(MAX(employee_id), 1)) FROM employee;

-- Insert Projects
INSERT INTO project (project_id, project_code, project_name, customer, start_date, end_date, status)
VALUES 
(1, 'PROJ001', 'NCG Ticketing System', 'NCG Corp', '2026-01-01', '2026-12-31', 'ACTIVE'),
(2, 'PROJ002', 'E-Commerce Platform', 'Grid Retail', '2026-07-01', '2027-06-30', 'PLANNING'),
(3, 'PROJ003', 'Legacy Portal', 'Old Corp', '2025-01-01', '2025-12-31', 'COMPLETED')
ON CONFLICT (project_code) DO NOTHING;

-- Adjust sequence for project_id
SELECT setval(pg_get_serial_sequence('project', 'project_id'), COALESCE(MAX(project_id), 1)) FROM project;

-- Insert Allocations (All allocations strictly conform to Rule 2 and Rule 3)
INSERT INTO allocation (allocation_id, employee_id, project_id, allocation_percent, role_in_project, start_date, end_date)
VALUES
-- Employee 1: 50% on PROJ001 (ACTIVE), 30% on PROJ002 (PLANNING) - Total <= 100%
(1, 1, 1, 50, 'Backend Developer', '2026-01-01', '2026-12-31'),
(2, 1, 2, 30, 'Technical Architect', '2026-07-01', '2026-12-31'),
-- Employee 2: 100% on PROJ001 (ACTIVE)
(3, 2, 1, 100, 'QA Lead', '2026-01-01', '2026-12-31'),
-- Employee 3: 95% on PROJ001 (ACTIVE)
(4, 3, 1, 95, 'Junior Developer', '2026-01-01', '2026-12-31'),
-- Employee 5: 40% on PROJ002 (PLANNING)
(5, 5, 2, 40, 'Project Manager', '2026-07-01', '2026-12-31')
ON CONFLICT DO NOTHING;

-- Adjust sequence for allocation_id
SELECT setval(pg_get_serial_sequence('allocation', 'allocation_id'), COALESCE(MAX(allocation_id), 1)) FROM allocation;

