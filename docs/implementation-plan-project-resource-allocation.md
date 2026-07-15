# Implementation Plan — Project Resource Allocation Management System

**Dựa trên:** srs-project-resource-allocation.md
**Tổng thời lượng đề xuất:** 14 ngày (có thể co giãn)
**Nguyên tắc chia giai đoạn:** mỗi giai đoạn nhỏ phải build được, chạy được, test tay được bằng Postman trước khi qua giai đoạn tiếp theo. Không giai đoạn nào phụ thuộc vào code "chưa xong" của giai đoạn sau.

---

## Tổng quan các giai đoạn

| Giai đoạn | Nội dung | Thời lượng | Có thể demo được gì |
|---|---|---|---|
| 0 | Setup project & hạ tầng | 0.5 ngày | Chạy `mvn spring-boot:run` thành công, kết nối DB OK |
| 1 | Employee module | 1 ngày | CRUD Employee qua Postman |
| 2 | Project module | 1 ngày | CRUD Project + validate status |
| 3 | Allocation module — happy path | 1.5 ngày | Tạo allocation cơ bản, chưa có rule |
| 4 | Allocation — Business Rules 1/2/3 | 1.5 ngày | Reject đúng các case vi phạm |
| 5 | Concurrency handling | 1 ngày | 2 request song song không vượt 100% |
| 6 | Reporting APIs | 1.5 ngày | 4 report chạy đúng số liệu |
| 7 | Exception Handling & Validation polish | 1 ngày | Response lỗi đồng nhất toàn hệ thống |
| 8 | Unit Test | 1.5 ngày | Coverage cho Service layer, đặc biệt AllocationService |
| 9 | Swagger + Postman Collection + README | 1 ngày | Tài liệu API đầy đủ |
| 10 | Docker Compose | 0.5 ngày | `docker compose up` chạy full stack |
| 11 | AI Bonus (optional) | 2 ngày | 2 endpoint AI hoạt động |
| 12 | Buffer / Final review theo checklist DoD | 1 ngày | Rà lại toàn bộ tiêu chí SRS mục 9 |

---

## Giai đoạn 0 — Setup Project & Hạ tầng (0.5 ngày)

### Mục tiêu
Có project chạy được, kết nối DB thành công, chưa cần logic gì.

### Task chi tiết

- [x] Tạo project Spring Boot (Spring Initializr): Web, Spring Data JPA, PostgreSQL Driver, Validation, Lombok
- [x] Cấu trúc package chuẩn:
```text
com.company.pram
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── exception
├── mapper
└── config
```
- [x] Cấu hình `application.yml` (datasource, JPA, port)
- [x] Tạo DB PostgreSQL local (hoặc Docker container tạm), chạy thử script SQL ở SRS mục 5
- [x] Kiểm tra `GET /actuator/health` (nếu thêm actuator) hoặc 1 endpoint `/ping` test thủ công trả `200 OK`

### Deliverable
- Repo git khởi tạo, commit đầu tiên "chore: init project structure"
- App chạy `http://localhost:8080` không lỗi

### Acceptance criteria
- `mvn clean install` build pass
- Kết nối DB thành công (log không lỗi Hikari/connection)

---

## Giai đoạn 1 — Employee Module (1 ngày)

### Task chi tiết

**1.1 Entity & Repository (1-2h)**
- [x] Tạo `Employee` entity đúng field ở SRS mục 3.1
- [x] Tạo `EmployeeRepository extends JpaRepository<Employee, Long>`
- [x] Thêm method `existsByEmployeeCode`, `existsByEmail`

**1.2 DTO (30p)**
- [x] `EmployeeRequest` (validation: `@NotBlank`, `@Email`)
- [x] `EmployeeResponse`

**1.3 Service (1-2h)**
- [x] `createEmployee()` — check trùng code/email trước khi save → ném `DuplicateResourceException` nếu trùng
- [x] `getEmployeeById()` — ném `EmployeeNotFoundException` nếu không có
- [x] `getAllEmployees()` — có phân trang (`Pageable`)

**1.4 Controller (1h)**
- [x] `POST /employees`, `GET /employees`, `GET /employees/{id}`

**1.5 Test tay bằng Postman (30p)**
- [x] Test case: tạo thành công → 201
- [x] Test case: trùng employeeCode → 409
- [x] Test case: email sai định dạng → 400
- [x] Test case: GET id không tồn tại → 404

### Deliverable
Employee CRUD hoạt động đầy đủ, có Postman requests lưu lại (sẽ gộp vào collection ở giai đoạn 9).

### Acceptance criteria
Tất cả 4 test case ở 1.5 đều đúng HTTP status và message.

---

## Giai đoạn 2 — Project Module (1 ngày)

### Task chi tiết

**2.1 Entity & Enum (1h)**
- [x] `Project` entity
- [x] Enum `ProjectStatus { PLANNING, ACTIVE, COMPLETED }`
- [x] `@Check` constraint `endDate >= startDate` ở DB (đã có trong script SQL)

**2.2 DTO + Validation (1h)**
- [x] `ProjectRequest`: validate `startDate`, `endDate`, custom validation `endDate >= startDate` (có thể viết `@AssertTrue` method trong DTO)

**2.3 Service (1-2h)**
- [x] `createProject()`
- [x] `updateProjectStatus()` — áp rule chuyển trạng thái tuần tự `PLANNING → ACTIVE → COMPLETED`, không cho nhảy ngược. Ném `InvalidProjectStatusException` nếu vi phạm thứ tự.
- [x] `getProjects(status)` — filter optional theo status

**2.4 Controller (1h)**
- [x] `POST /projects`, `GET /projects`, `GET /projects/{id}`, `PUT /projects/{id}/status`

**2.5 Test tay (30p)**
- [x] Tạo project → 201
- [x] endDate < startDate → 400
- [x] Chuyển COMPLETED → ACTIVE → 400

### Deliverable
Project CRUD + status transition rule hoạt động.

### Acceptance criteria
Status transition rule test pass, endDate validation pass.

---

## Giai đoạn 3 — Allocation Module: Happy Path trước (1.5 ngày)

**Lý do tách riêng:** làm allocation cơ bản (không rule) trước để chắc chắn luồng CRUD + mapping + FK hoạt động đúng, sau đó mới thêm rule ở Giai đoạn 4. Tránh vừa nghĩ rule vừa debug CRUD cùng lúc.

### Task chi tiết

**3.1 Entity & Repository (1-2h)**
- [x] `Allocation` entity (FK tới Employee, Project qua `@ManyToOne`)
- [x] `AllocationRepository`
- [x] Method query: `findByEmployeeEmployeeIdAndDeletedAtIsNull(Long employeeId)`

**3.2 DTO (1h)**
- [x] `AllocationRequest` (validate `@Min(1) @Max(100)` cho allocationPercent — đây là Rule 1, làm luôn ở bước này vì Bean Validation đơn giản)
- [x] `AllocationResponse` (bao gồm `employeeName`, `projectCode` — cần map từ entity liên kết)

**3.3 Service — chỉ phần tạo cơ bản (2-3h)**
- [x] `createAllocation()`: lookup Employee, lookup Project, save — **chưa check Rule 2/3**
- [x] `updateAllocation()`, `deleteAllocation()` (soft-delete: set `deletedAt = now()`)
- [x] `getAllocationsByEmployee(employeeId)`

**3.4 Controller (1h)**
- [x] `POST /allocations`, `PUT /allocations/{id}`, `DELETE /allocations/{id}`, `GET /allocations?employeeId=`

**3.5 Test tay (30p)**
- [x] Tạo allocation với employeeId/projectId hợp lệ → 201, response có đủ employeeName/projectCode
- [x] employeeId không tồn tại → 404 (`EmployeeNotFoundException`)
- [x] allocationPercent = 150 → 400 (Bean Validation bắt được nhờ `@Max(100)`)

### Deliverable
Allocation CRUD chạy được nhưng **chưa an toàn về nghiệp vụ** (chưa chặn vượt 100%, chưa chặn allocate vào COMPLETED). Đây là điều bình thường ở giai đoạn này — sẽ xử lý ở Giai đoạn 4.

### Acceptance criteria
CRUD hoạt động, mapping response đúng field liên kết.

---

## Giai đoạn 4 — Allocation: Business Rule 2 & 3 (1.5 ngày)

### Task chi tiết

**4.1 Rule 3 — Không allocate vào project COMPLETED (30p-1h)**
- [x] Thêm check trong `createAllocation()`: nếu `project.status == COMPLETED` → ném `InvalidProjectStatusException`
- [x] Test: allocate vào project COMPLETED → 400 đúng message

**4.2 Rule 2 — Tổng allocation ≤ 100% (2-3h, phần khó nhất)**
- [x] Viết query `sumOverlappingAllocations(employeeId, startDate, endDate, excludeAllocationId)` trong `AllocationRepository` dùng `@Query` (JPQL), logic overlap:
```sql
WHERE a.employee_id = :employeeId
  AND a.deleted_at IS NULL
  AND (a.end_date IS NULL OR a.end_date >= :startDate)
  AND a.start_date <= COALESCE(:endDate, a.start_date + INTERVAL '100 years')
```
- [x] Trong `createAllocation()`: gọi query trên → cộng với `allocationPercent` mới → nếu > 100 → ném `AllocationExceededException`
- [x] Trong `updateAllocation()`: **loại trừ chính bản ghi đang sửa** ra khỏi tổng trước khi so sánh (lỗi thường gặp: quên loại trừ, dẫn tới không bao giờ update được)

**4.3 Test tay đầy đủ theo bảng ví dụ trong SRS (1h)**
- [x] 60% + 40% = 100% → thành công
- [x] 60% + 50% = 110% → 400 "Employee allocation exceeds 100%"
- [x] Update allocation hiện tại từ 60% → 70% (khi tổng đang là 100%, tự trừ 60% cũ ra trước) → phải thành công vì 40%(còn lại) + 70% = 110% → thực ra phải reject; kiểm tra kỹ case này bằng tay
- [x] 2 allocation không overlap thời gian (VD dự án A kết thúc trước khi dự án B bắt đầu) → không cộng dồn, đều pass dù mỗi cái 100%

### Deliverable
Allocation module an toàn về nghiệp vụ, đúng theo SRS mục 3.3.

### Acceptance criteria
Toàn bộ test case ở 4.3 đúng kết quả mong đợi — đây là phần sẽ bị soi kỹ nhất khi review.

---

## Giai đoạn 5 — Concurrency Handling (1 ngày)

### Task chi tiết

**5.1 Chọn giải pháp (đã khuyến nghị Option A - Pessimistic Lock trong SRS mục 7.1)**
- [x] Thêm method repository dùng lock:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Allocation a WHERE a.employee.id = :employeeId AND a.deletedAt IS NULL")
List<Allocation> findByEmployeeForUpdate(@Param("employeeId") Long employeeId);
```
- [x] Gọi method này ở đầu `createAllocation()` trước khi tính tổng, để lock các row liên quan cho tới khi transaction commit
- [x] Đảm bảo `createAllocation()` có `@Transactional`

**5.2 Viết test giả lập concurrency (1-2h)**
- [x] Viết integration test dùng 2 thread (hoặc `CompletableFuture`) cùng gọi `createAllocation()` cho cùng 1 employee với tổng > 100% nếu cả 2 pass
- [x] Assert: chỉ 1 trong 2 request thành công, request còn lại nhận `AllocationExceededException`

**5.3 Đo thời gian phản hồi khi có lock (30p)**
- [x] Kiểm tra request thứ 2 không bị treo quá lâu (timeout hợp lý, ví dụ set `@Transactional(timeout = 5)`)

### Deliverable
Test concurrency pass, chứng minh hệ thống không bị race condition.

### Acceptance criteria
Chạy lại test 5.2 nhiều lần (ít nhất 5 lần) đều cho kết quả nhất quán — không có run nào để lọt tổng > 100%.

---

## Giai đoạn 6 — Reporting APIs (1.5 ngày)

### Task chi tiết

**6.1 Utilization Report (2-3h)**
- [x] Viết native query hoặc JPQL group by employee (theo SRS mục 4.1)
- [x] `GET /reports/utilization`

**6.2 Available Resource Report (1-2h)**
- [x] `GET /reports/available?minAvailable=` (param optional)

**6.3 Overloaded Employee Report (1h)**
- [x] `GET /reports/overloaded`

**6.4 Employee Workload Detail (1-2h)**
- [x] `GET /employees/{id}/workload` — trả kèm danh sách allocation chi tiết (SRS mục 4.4)

**6.5 Test tay với dữ liệu mẫu (1h)**
- [ ] Seed dữ liệu mẫu (script SQL hoặc `data.sql`) khớp với ví dụ trong SRS (Tuan 95%, Nam 100%...)
- [ ] Verify từng report trả đúng số liệu như bảng ví dụ

### Deliverable
4 report API hoạt động đúng với dữ liệu mẫu.

### Acceptance criteria
Kết quả report khớp 100% với bảng ví dụ trong SRS mục 4.

---

## Giai đoạn 7 — Exception Handling & Validation Polish (1 ngày)

### Task chi tiết

- [ ] Tạo `ErrorResponse` DTO chuẩn (timestamp, message, path — nên thêm để dễ debug)
- [ ] Hoàn thiện `GlobalExceptionHandler` cho đủ 6 exception ở SRS mục 6
- [ ] Rà soát toàn bộ Controller: đảm bảo dùng `@Valid` đúng chỗ
- [ ] Test lại toàn bộ case lỗi từ giai đoạn 1-6, đảm bảo response format nhất quán (không có chỗ nào trả lỗi khác format)
- [ ] Thêm logging (AOP hoặc đơn giản `@Slf4j` trong Service) cho Create/Update/Delete Allocation theo SRS mục 7 (Logging)

### Deliverable
Toàn hệ thống trả lỗi theo 1 format duy nhất, có log đầy đủ cho thao tác allocation.

### Acceptance criteria
Không còn exception nào "leak" ra ngoài dạng stack trace mặc định của Spring (500 Internal Server Error không rõ nguyên nhân).

---

## Giai đoạn 8 — Unit Test (1.5 ngày)

### Ưu tiên test theo độ rủi ro (không cần cover 100%, tập trung chỗ quan trọng)

| Class | Ưu tiên | Test case cần có |
|---|---|---|
| `AllocationService` | Cao nhất | Rule 1/2/3, overlap logic, update loại trừ bản ghi cũ, concurrency |
| `ProjectService` | Trung bình | Status transition rule |
| `EmployeeService` | Trung bình | Duplicate check |
| `ReportService` | Thấp | Có thể test bằng integration test với DB in-memory (H2) thay vì mock |

### Task chi tiết
- [ ] Setup Mockito cho Service test (mock Repository)
- [ ] Viết test cho `AllocationService` (mục tiêu ít nhất 8-10 test case bám theo bảng ví dụ SRS)
- [ ] Viết test cho `ProjectService`, `EmployeeService`
- [ ] (Optional) Integration test với H2 in-memory DB cho Report queries

### Deliverable
Bộ unit test chạy `mvn test` pass toàn bộ.

### Acceptance criteria
Coverage AllocationService (phần business logic) là ưu tiên số 1 vì đây là phần bị đánh giá kỹ nhất theo tiêu chí SRS mục 10.

---

## Giai đoạn 9 — Swagger + Postman + README (1 ngày)

### Task chi tiết
- [ ] Thêm `springdoc-openapi-starter-webmvc-ui` dependency
- [ ] Annotate `@Operation`, `@ApiResponse` cho các API chính (Allocation trước tiên)
- [ ] Export Postman Collection đầy đủ từ các request đã test tay ở giai đoạn 1-6
- [ ] Viết `README.md`: hướng dẫn setup DB, chạy app, link Swagger UI, danh sách API tóm tắt

### Deliverable
`/swagger-ui.html` truy cập được, Postman Collection file `.json`, README rõ ràng.

---

## Giai đoạn 10 — Docker Compose (0.5 ngày)

### Task chi tiết
- [ ] Viết `Dockerfile` cho app (multi-stage build: Maven build → JRE runtime)
- [ ] Viết `docker-compose.yml`: service `app` + service `postgres`
- [ ] Test `docker compose up` từ máy sạch (chưa có DB local) — app phải tự kết nối được vào Postgres container

### Deliverable
`docker compose up` chạy full stack thành công.

---

## Giai đoạn 11 — AI Bonus Features (2 ngày, optional)

### Task chi tiết

**11.1 AI Resource Recommendation (1 ngày)**
- [ ] Parse query đơn giản (regex/keyword extraction) để lấy `role` + `minAvailable`
- [ ] Gọi lại `ReportService.getAvailableResources()` đã có sẵn từ Giai đoạn 6, filter theo role
- [ ] `POST /ai/recommend-resource`

**11.2 AI Risk Detection (1 ngày)**
- [ ] Lấy dữ liệu utilization thực tế từ `ReportService`
- [ ] Build prompt gửi tới Claude API kèm dữ liệu, yêu cầu trả JSON theo schema cố định (SRS mục 8.2)
- [ ] `POST /ai/risk-detection`

### Deliverable
2 endpoint AI hoạt động, có ví dụ input/output khớp SRS.

---

## Giai đoạn 12 — Buffer & Final Review (1 ngày)

### Checklist rà soát cuối (đối chiếu SRS mục 9 — Definition of Done)

- [ ] Toàn bộ API mục 3, 4 hoạt động đúng, đúng HTTP status
- [ ] Business Rule 1/2/3 có unit test cả 2 chiều (pass/fail)
- [ ] Concurrency test pass ổn định
- [ ] GlobalExceptionHandler bắt đủ exception, message đúng format
- [ ] SQL report đúng kết quả với dữ liệu mẫu
- [ ] README + Postman collection đầy đủ
- [ ] Swagger UI + Docker Compose chạy được (bonus)
- [ ] AI endpoint đúng schema (nếu làm bonus)
- [ ] Dọn code: xóa TODO, xóa log debug thừa, review lại tên biến/method

---

## Ghi chú thực thi

1. **Không nhảy cóc giai đoạn** — đặc biệt Giai đoạn 3 (happy path) trước Giai đoạn 4 (rule). Nhiều bạn hay code rule ngay từ đầu và debug rất mất thời gian vì không tách được lỗi CRUD với lỗi logic.
2. **Giai đoạn 4 và 5 là phần bị đánh giá kỹ nhất** (Business Logic + Concurrency) — nếu thiếu thời gian, ưu tiên cắt giảm ở Giai đoạn 11 (AI bonus) trước.
3. Sau mỗi giai đoạn, nên commit git riêng (VD: `feat: allocation business rule 2 - max 100%`) để dễ review lại từng bước.
