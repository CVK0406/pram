# SRS - Project Resource Allocation Management System

**Phiên bản:** 1.0.0
**Loại tài liệu:** Software Requirements Specification (SRS)
**Nguồn:** Project_Resource_Allocation_Assignment.md
**Ngày tạo:** 2026-07-15

---

## 1. Giới thiệu

### 1.1 Mục đích

Tài liệu này đặc tả chi tiết yêu cầu phần mềm cho hệ thống **Project Resource Allocation Management System (PRAMS)** — hệ thống quản lý phân bổ nhân sự vào các dự án cho công ty outsourcing. Tài liệu mở rộng từ bản assignment gốc, bổ sung: use case chi tiết, đặc tả API đầy đủ (request/response/status code), luồng xử lý nghiệp vụ (sequence), thiết kế layer, exception mapping, và tiêu chí nghiệm thu — đủ để bắt tay code ngay mà không cần suy đoán thêm.

### 1.2 Phạm vi

Hệ thống là một REST API backend (Java Spring Boot) phục vụ:

- PM / Resource Manager quản lý Employee, Project, Allocation.
- Theo dõi workload / utilization của từng nhân viên theo thời gian thực.
- Ràng buộc nghiệp vụ: một nhân viên không được phân bổ vượt quá 100% tổng thời gian.
- Báo cáo: Utilization, Available Resource, Overloaded Employee.
- (Bonus) Tích hợp AI để gợi ý resource và cảnh báo rủi ro.

Phạm vi **không bao gồm**: frontend UI, module tính lương, quản lý timesheet chi tiết theo giờ/ngày làm việc thực tế (chỉ quản lý allocation theo %).

### 1.3 Đối tượng sử dụng tài liệu

| Vai trò | Mục đích sử dụng |
|---|---|
| Backend Developer (Fresher) | Tham chiếu để code Entity, Service, Controller, Validation |
| Reviewer / Mentor | Đối chiếu tiêu chí đánh giá (mục 10 assignment gốc) |
| QA / Tester | Viết test case dựa trên business rule & API spec |

### 1.4 Định nghĩa - Từ viết tắt

| Thuật ngữ | Ý nghĩa |
|---|---|
| Allocation | Tỷ lệ % thời gian một nhân viên được phân bổ cho một dự án |
| Utilization | Tổng % allocation của một nhân viên trên tất cả dự án đang active |
| Available | Phần trăm thời gian còn trống = 100% - Utilization |
| Overloaded | Nhân viên có Utilization > 90% |
| PM | Project Manager |
| RM | Resource Manager |

---

## 2. Tổng quan hệ thống

### 2.1 Bối cảnh nghiệp vụ

Một công ty outsourcing chạy song song nhiều dự án. Một Developer có thể tham gia đồng thời nhiều dự án với % thời gian khác nhau, ví dụ:

```text
NCG          : 50%
GRID         : 30%
Internal AI  : 20%
-----------------
Tổng         : 100%  -> Hợp lệ
```

Ràng buộc lõi của toàn hệ thống: **tổng allocation của 1 nhân viên tại một thời điểm không được vượt 100%.**

### 2.2 Kiến trúc tổng quan (đề xuất)

```text
┌─────────────────────────────────────────────┐
│               Controller Layer               │
│  EmployeeController / ProjectController /     │
│  AllocationController / ReportController      │
└───────────────────┬───────────────────────────┘
                    │ DTO (Request/Response)
┌───────────────────▼───────────────────────────┐
│                Service Layer                   │
│  EmployeeService / ProjectService /             │
│  AllocationService / ReportService /            │
│  (Bonus) AIRecommendationService                │
└───────────────────┬───────────────────────────┘
                    │ Entity
┌───────────────────▼───────────────────────────┐
│              Repository Layer (JPA)             │
└───────────────────┬───────────────────────────┘
                    │
┌───────────────────▼───────────────────────────┐
│                 PostgreSQL                      │
└─────────────────────────────────────────────────┘

Cross-cutting: GlobalExceptionHandler, Validation (Bean Validation),
Logging (AOP hoặc Interceptor), (Bonus) Swagger/OpenAPI
```

### 2.3 Kiến trúc layer chi tiết

| Layer | Trách nhiệm | Không được làm |
|---|---|---|
| Controller | Nhận request, validate input cơ bản (@Valid), gọi Service, trả response | Không chứa business logic |
| Service | Xử lý business rule (allocation ≤ 100%, project status check...) | Không thao tác SQL trực tiếp |
| Repository | Truy vấn dữ liệu qua Spring Data JPA | Không chứa logic nghiệp vụ |
| DTO | Tách biệt Entity khỏi API contract | Không expose Entity trực tiếp ra ngoài |
| Exception Handler | Bắt exception, map sang HTTP status + message chuẩn | - |

---

## 3. Yêu cầu chức năng chi tiết

### 3.1 Employee Management

#### Thuộc tính

| Field | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| employeeId | Long | PK, auto-generate |
| employeeCode | String(20) | Unique, NotBlank |
| fullName | String(100) | NotBlank |
| email | String(100) | NotBlank, @Email, Unique |
| role | String(50) | NotBlank |
| department | String(50) | NotBlank |

#### Use Case: Tạo nhân viên

```text
Actor: PM/RM
Pre-condition: employeeCode và email chưa tồn tại
Flow:
  1. PM gửi POST /employees với thông tin nhân viên
  2. Hệ thống validate (NotBlank, @Email)
  3. Hệ thống kiểm tra trùng employeeCode / email
  4. Nếu trùng -> 409 Conflict
  5. Nếu hợp lệ -> lưu DB -> trả 201 Created
```

#### API chi tiết

**POST /employees**

Request:
```json
{
  "employeeCode": "EMP001",
  "fullName": "Tuan Ho Anh",
  "email": "tuanha@company.com",
  "role": "Senior Developer",
  "department": "FSOFT-Q1"
}
```

Response `201 Created`:
```json
{
  "employeeId": 1,
  "employeeCode": "EMP001",
  "fullName": "Tuan Ho Anh",
  "email": "tuanha@company.com",
  "role": "Senior Developer",
  "department": "FSOFT-Q1"
}
```

Response lỗi `409 Conflict` (trùng code/email):
```json
{
  "message": "Employee code already exists: EMP001"
}
```

**GET /employees** — trả danh sách, hỗ trợ phân trang: `?page=0&size=20`

**GET /employees/{id}** — 404 nếu không tồn tại:
```json
{ "message": "Employee not found with id: 99" }
```

---

### 3.2 Project Management

#### Thuộc tính

| Field | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| projectId | Long | PK |
| projectCode | String(20) | Unique, NotBlank |
| projectName | String(200) | NotBlank |
| customer | String(100) | NotBlank |
| startDate | Date | NotNull |
| endDate | Date | Phải >= startDate |
| status | Enum | PLANNING / ACTIVE / COMPLETED |

#### Business Rule: Chuyển trạng thái dự án

```text
PLANNING --> ACTIVE --> COMPLETED
```

Không cho phép nhảy ngược trạng thái (ví dụ COMPLETED -> ACTIVE bị từ chối) trừ khi có API riêng `reopen` (không nằm trong scope bắt buộc, có thể để bonus).

#### API

**POST /projects**

Request:
```json
{
  "projectCode": "NCG",
  "projectName": "NCG Ticketing Platform",
  "customer": "NCG Corp",
  "startDate": "2026-08-01",
  "endDate": "2026-12-31",
  "status": "PLANNING"
}
```

**GET /projects?status=ACTIVE** — filter theo status (optional query param).

**PUT /projects/{id}/status** — cập nhật trạng thái dự án:
```json
{ "status": "ACTIVE" }
```

---

### 3.3 Resource Allocation (Core Module)

#### Thuộc tính

| Field | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| allocationId | Long | PK |
| employeeId | Long | FK -> employee, NotNull |
| projectId | Long | FK -> project, NotNull |
| allocationPercent | Integer | 0 < x ≤ 100 |
| roleInProject | String(100) | NotBlank |
| startDate | Date | NotNull |
| endDate | Date | Nullable (allocation đang mở) |

#### Business Rule 1 — Giới hạn allocation từng bản ghi

```text
0 < allocationPercent <= 100
```
Vi phạm -> `400 Bad Request`:
```json
{ "message": "Allocation percent must be between 1 and 100" }
```

#### Business Rule 2 — Tổng allocation không vượt 100% (Rule quan trọng nhất)

**Định nghĩa phạm vi tính tổng:** chỉ tính các allocation đang **hiệu lực** tại thời điểm tạo mới, tức là:
- Allocation có `endDate IS NULL`, hoặc
- Khoảng `[startDate, endDate]` của allocation mới **giao nhau (overlap)** với allocation đã tồn tại của cùng nhân viên.

```text
Ví dụ hợp lệ:
Project A : 60%  (2026-08-01 -> 2026-12-31)
Project B : 40%  (2026-08-01 -> 2026-12-31)
Tổng = 100% -> Hợp lệ

Ví dụ không hợp lệ:
Project A : 60%
Project B : 50%
Tổng = 110% -> Reject
```

Response `400 Bad Request`:
```json
{ "message": "Employee allocation exceeds 100%" }
```

**Pseudocode xử lý (Service layer):**

```java
public Allocation createAllocation(AllocationRequest req) {
    Employee employee = employeeRepository.findById(req.getEmployeeId())
        .orElseThrow(() -> new EmployeeNotFoundException(req.getEmployeeId()));

    Project project = projectRepository.findById(req.getProjectId())
        .orElseThrow(() -> new ProjectNotFoundException(req.getProjectId()));

    // Rule 3: project phải chưa COMPLETED
    if (project.getStatus() == ProjectStatus.COMPLETED) {
        throw new InvalidProjectStatusException(project.getProjectCode());
    }

    // Rule 2: tính tổng allocation đang overlap thời gian
    int currentTotal = allocationRepository
        .sumOverlappingAllocation(employee.getEmployeeId(),
                                   req.getStartDate(), req.getEndDate());

    if (currentTotal + req.getAllocationPercent() > 100) {
        throw new AllocationExceededException(employee.getEmployeeCode(),
                                               currentTotal + req.getAllocationPercent());
    }

    Allocation allocation = AllocationMapper.toEntity(req, employee, project);
    return allocationRepository.save(allocation);
}
```

#### Business Rule 3 — Không allocate vào dự án COMPLETED

Response `400 Bad Request`:
```json
{ "message": "Cannot allocate to a COMPLETED project: NCG" }
```

#### Business Rule 4 (bổ sung, khuyến nghị) — Validate khoảng thời gian

`allocation.startDate` và `allocation.endDate` phải nằm trong khoảng `project.startDate` - `project.endDate`. Nếu không, trả `400 Bad Request`:
```json
{ "message": "Allocation period must be within project duration" }
```

#### API

**POST /allocations**

Request:
```json
{
  "employeeId": 1,
  "projectId": 2,
  "allocationPercent": 50,
  "roleInProject": "Backend Developer",
  "startDate": "2026-08-01",
  "endDate": "2026-12-31"
}
```

Response `201 Created`:
```json
{
  "allocationId": 10,
  "employeeId": 1,
  "employeeName": "Tuan Ho Anh",
  "projectId": 2,
  "projectCode": "NCG",
  "allocationPercent": 50,
  "roleInProject": "Backend Developer",
  "startDate": "2026-08-01",
  "endDate": "2026-12-31"
}
```

**PUT /allocations/{id}** — cập nhật % hoặc thời gian; **phải re-validate lại Rule 2** (trừ allocation hiện tại ra khỏi tổng trước khi cộng giá trị mới).

**DELETE /allocations/{id}** — soft-delete khuyến nghị (thêm cột `deleted_at`) để giữ lịch sử cho báo cáo.

**GET /allocations?employeeId=1** — xem toàn bộ allocation của 1 nhân viên.

---

## 4. Reporting Functions — Chi tiết

### 4.1 Employee Utilization Report

**GET /reports/utilization**

Response:
```json
[
  { "employeeId": 1, "employeeCode": "EMP001", "fullName": "Tuan Ho Anh", "totalAllocation": 100 },
  { "employeeId": 2, "employeeCode": "EMP002", "fullName": "Nguyen Van B", "totalAllocation": 80 }
]
```

SQL tham khảo:
```sql
SELECT e.employee_id, e.employee_code, e.full_name,
       COALESCE(SUM(a.allocation_percent), 0) AS total_allocation
FROM employee e
LEFT JOIN allocation a ON a.employee_id = e.employee_id
    AND (a.end_date IS NULL OR a.end_date >= CURRENT_DATE)
GROUP BY e.employee_id, e.employee_code, e.full_name;
```

### 4.2 Available Resource Report

**GET /reports/available?minAvailable=50**

Điều kiện: `100 - totalAllocation >= minAvailable` (param optional, mặc định > 0).

```sql
SELECT e.employee_id, e.full_name,
       100 - COALESCE(SUM(a.allocation_percent), 0) AS available
FROM employee e
LEFT JOIN allocation a ON a.employee_id = e.employee_id
    AND (a.end_date IS NULL OR a.end_date >= CURRENT_DATE)
GROUP BY e.employee_id, e.full_name
HAVING 100 - COALESCE(SUM(a.allocation_percent), 0) > 0;
```

### 4.3 Overloaded Employee Report

**GET /reports/overloaded** — điều kiện `totalAllocation > 90`.

```sql
SELECT e.employee_id, e.full_name,
       SUM(a.allocation_percent) AS total_allocation
FROM employee e
JOIN allocation a ON a.employee_id = e.employee_id
    AND (a.end_date IS NULL OR a.end_date >= CURRENT_DATE)
GROUP BY e.employee_id, e.full_name
HAVING SUM(a.allocation_percent) > 90;
```

### 4.4 Employee Workload Detail

**GET /employees/{id}/workload**

Response:
```json
{
  "employeeId": 1,
  "employeeName": "Tuan Ho Anh",
  "totalAllocation": 80,
  "available": 20,
  "allocations": [
    { "projectCode": "NCG", "allocationPercent": 60, "roleInProject": "Backend Developer" },
    { "projectCode": "GRID", "allocationPercent": 20, "roleInProject": "Support" }
  ]
}
```

---

## 5. Database Design (chi tiết + ràng buộc khóa ngoại)

```sql
CREATE TABLE employee (
    employee_id     BIGSERIAL PRIMARY KEY,
    employee_code   VARCHAR(20)  UNIQUE NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    department      VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now()
);

CREATE TABLE project (
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

CREATE TABLE allocation (
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

-- Index phục vụ tính tổng allocation theo employee (query hot path)
CREATE INDEX idx_allocation_employee ON allocation(employee_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_allocation_project ON allocation(project_id);
```

**Lưu ý thiết kế:**
- `deleted_at` dùng cho soft-delete, mọi query tổng hợp phải thêm `WHERE deleted_at IS NULL`.
- CHECK constraint ở DB là lớp bảo vệ cuối cùng, business rule chính vẫn nằm ở Service layer (không dựa hoàn toàn vào DB constraint vì cần custom message).

---

## 6. Exception Handling — Mapping chi tiết

| Exception | HTTP Status | Khi nào ném ra |
|---|---|---|
| `EmployeeNotFoundException` | 404 | GET/PUT/DELETE với employeeId không tồn tại |
| `ProjectNotFoundException` | 404 | GET/PUT/DELETE với projectId không tồn tại |
| `AllocationExceededException` | 400 | Tổng allocation > 100% |
| `InvalidProjectStatusException` | 400 | Allocate vào project COMPLETED |
| `DuplicateResourceException` | 409 | Trùng employeeCode / email / projectCode |
| `MethodArgumentNotValidException` | 400 | Vi phạm @NotBlank, @Email, @Min, @Max (Spring tự ném) |

### GlobalExceptionHandler mẫu

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AllocationExceededException.class)
    public ResponseEntity<ErrorResponse> handleAllocationExceeded(AllocationExceededException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({EmployeeNotFoundException.class, ProjectNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

## 7. Yêu cầu phi chức năng (Non-Functional Requirements)

| Hạng mục | Yêu cầu |
|---|---|
| Performance | API tổng hợp báo cáo (utilization) phản hồi < 500ms với 1000 nhân viên |
| Concurrency | Tạo allocation đồng thời cho cùng 1 nhân viên phải tránh race condition (tổng vượt 100%) — khuyến nghị dùng `SELECT ... FOR UPDATE` hoặc optimistic locking (`@Version`) trên Employee/aggregate |
| Validation | Toàn bộ input validate bằng Bean Validation (`@NotBlank`, `@Email`, `@Min`, `@Max`) ở tầng DTO |
| Logging | Ghi log mọi thao tác Create/Update/Delete Allocation kèm employeeId, projectId, timestamp |
| Security (khuyến nghị) | JWT-based auth, phân quyền PM/RM mới được tạo/sửa allocation |
| Documentation | Swagger/OpenAPI expose tại `/swagger-ui.html` |

### 7.1 Vấn đề Concurrency (quan trọng, hay bị bỏ sót)

Nếu 2 request tạo allocation cho cùng nhân viên chạy song song, cả 2 đều đọc tổng allocation hiện tại **trước khi** request kia commit, dẫn đến tổng vượt 100% dù mỗi request kiểm tra riêng lẻ đều pass. Giải pháp:

```text
Option A: Pessimistic lock khi đọc allocation của employee
  SELECT * FROM allocation WHERE employee_id = ? FOR UPDATE;

Option B: Optimistic locking bằng @Version trên bảng employee,
  retry khi OptimisticLockException xảy ra.

Option C (đơn giản nhất cho Fresher): dùng @Transactional +
  UNIQUE constraint tổng hợp qua trigger DB (nâng cao, optional).
```
Khuyến nghị cho phạm vi bài tập: **Option A** (đơn giản, dễ giải thích khi review).

---

## 8. AI Bonus Features — Đặc tả

### 8.1 AI Resource Recommendation

**POST /ai/recommend-resource**

Request:
```json
{ "query": "Tìm Java Developer còn tối thiểu 50% available" }
```

Luồng xử lý:
```text
1. Parse query (role keyword + minAvailable threshold) - có thể dùng
   rule-based parser đơn giản hoặc gọi LLM để extract structured params.
2. Query Available Resource Report với filter role="Java Developer"
   và available >= 50.
3. Trả kết quả JSON.
```

Response:
```json
{
  "recommendedResources": [
    { "employee": "Nguyen Van A", "available": 60 }
  ]
}
```

### 8.2 AI Risk Detection

**POST /ai/risk-detection**

Request:
```json
{ "query": "Sprint tới cần thêm 2 Java Developer" }
```

Response:
```json
{
  "risks": [
    "Team đang sử dụng 92% capacity",
    "Chỉ còn 1 resource available trên 50%"
  ]
}
```

*Ghi chú:* phần AI có thể triển khai bằng cách gọi Anthropic API (Claude) với prompt kèm dữ liệu utilization thực tế lấy từ Report Service, yêu cầu model trả về JSON theo schema cố định.

---

## 9. Tiêu chí nghiệm thu (Definition of Done)

- [ ] Toàn bộ API ở mục 3, 4 hoạt động đúng, trả đúng HTTP status.
- [ ] Business Rule 1, 2, 3 (mục 3.3) có unit test cho cả case hợp lệ và vi phạm.
- [ ] Concurrency test: 2 request đồng thời không được để tổng allocation vượt 100%.
- [ ] GlobalExceptionHandler bắt đủ các exception ở mục 6, trả message đúng format.
- [ ] SQL báo cáo (mục 4) chạy đúng kết quả với dữ liệu mẫu.
- [ ] README.md mô tả cách chạy project + Postman collection đính kèm.
- [ ] (Bonus) Swagger UI truy cập được, Docker Compose chạy được PostgreSQL + app.
- [ ] (Bonus) AI endpoint trả JSON đúng schema đã định nghĩa ở mục 8.

---

## 10. Danh sách Deliverables

1. Source Code Git Repository (cấu trúc theo layer: controller/service/repository/entity/dto/exception)
2. SQL Script tạo bảng (mục 5)
3. README.md hướng dẫn chạy project
4. Postman Collection (export .json)
5. API Screenshot (test thực tế)
6. AI Review Report (nếu làm bonus AI)

---

## 11. Roadmap đề xuất (2 tuần)

| Ngày | Nội dung |
|---|---|
| 1-2 | Setup project, DB schema, Entity + Repository (Employee, Project) |
| 3-4 | Employee & Project CRUD API + Validation + Exception Handling |
| 5-7 | Allocation module: Business Rule 1/2/3 + concurrency handling |
| 8-9 | Reporting APIs (Utilization, Available, Overloaded, Workload) |
| 10 | Unit Test cho Business Logic (Allocation Service) |
| 11 | Swagger + Postman Collection + README |
| 12-13 | AI Bonus features (nếu làm) |
| 14 | Buffer / fix bug / review lại theo tiêu chí mục 9 |

---

*Tài liệu này là bản mở rộng chi tiết dựa trên `Project_Resource_Allocation_Assignment.md`. Có thể điều chỉnh thêm nếu cần bổ sung entity hoặc thay đổi phạm vi bonus.*
