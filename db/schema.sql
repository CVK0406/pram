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
