# Implementation Plan — Frontend Angular (Simple) cho Project Resource Allocation Management System

**Dựa trên:** srs-project-resource-allocation.md, implementation-plan-project-resource-allocation.md (backend)
**Phạm vi:** Frontend đơn giản, đủ dùng để demo và thao tác tay các chức năng chính — không làm UI phức tạp, không state management nặng (không cần NgRx cho quy mô này).
**Stack đề xuất:** Angular (standalone components, phiên bản mới nhất bạn đang dùng), Angular Material (UI nhanh, đẹp sẵn), RxJS, HttpClient.

---

## Nguyên tắc chia giai đoạn

Giống backend: mỗi giai đoạn phải **chạy được, nhìn thấy được trên trình duyệt** trước khi qua giai đoạn tiếp theo. Ưu tiên thứ tự: hạ tầng → hiển thị dữ liệu (read-only) → thao tác ghi (create/update) → business rule feedback → báo cáo → polish.

---

## Tổng quan các giai đoạn

| Giai đoạn | Nội dung | Thời lượng | Demo được gì |
|---|---|---|---|
| 0 | Setup project & cấu trúc | 0.5 ngày | `ng serve` chạy, gọi thử 1 API test |
| 1 | Core services & models | 0.5 ngày | Service gọi API Employee thành công (console.log) |
| 2 | Employee module (list + create) | 1 ngày | Xem danh sách + thêm nhân viên qua form |
| 3 | Project module (list + create + đổi status) | 1 ngày | Xem/thêm dự án, đổi trạng thái |
| 4 | Allocation module — form tạo allocation | 1.5 ngày | Tạo allocation, hiển thị lỗi rule rõ ràng |
| 5 | Employee Workload Detail view | 0.5 ngày | Xem chi tiết allocation của 1 nhân viên |
| 6 | Reports dashboard (3 báo cáo) | 1 ngày | Bảng Utilization / Available / Overloaded |
| 7 | Error handling & UX polish toàn cục | 1 ngày | Toast lỗi, loading spinner, validate form |
| 8 | (Bonus) AI query box | 1 ngày | Ô nhập câu hỏi tự nhiên → gọi AI endpoint |
| 9 | Final review & responsive check | 0.5 ngày | Check lại toàn bộ luồng, chạy trên nhiều màn hình |

**Tổng: ~7-8 ngày làm việc** (không tính giai đoạn 8 bonus).

---

## Giai đoạn 0 — Setup Project & Cấu trúc (0.5 ngày)

### Task chi tiết

- [x] `ng new pram-frontend --standalone --routing --style=scss`
- [x] Cài Angular Material: `ng add @angular/material` (chọn theme bất kỳ, enable animations)
- [x] Cấu hình `environment.ts` / `environment.prod.ts` chứa `apiBaseUrl` (VD: `http://localhost:8080`)
- [x] Cấu hình CORS phía backend nếu chưa có (`@CrossOrigin` hoặc `CorsConfigurationSource` — nhắc lại để không quên, vì đây là lỗi hay gặp đầu tiên khi test)
- [x] Cấu trúc thư mục:
```text
src/app
├── core/
│   ├── models/          # interface: Employee, Project, Allocation, ErrorResponse
│   ├── services/        # EmployeeService, ProjectService, AllocationService, ReportService
│   └── interceptors/     # error-handling interceptor (giai đoạn 7)
├── features/
│   ├── employees/
│   ├── projects/
│   ├── allocations/
│   └── reports/
├── shared/
│   └── components/       # loading-spinner, confirm-dialog, error-toast
└── app.routes.ts
```
- [x] Test nhanh: gọi `GET /employees` bằng `HttpClient` trong `AppComponent`, console.log kết quả

### Deliverable
Project chạy `ng serve`, gọi được API backend, không lỗi CORS.

### Acceptance criteria
Console log ra đúng dữ liệu từ backend (kể cả mảng rỗng).

---

## Giai đoạn 1 — Core Services & Models (0.5 ngày)

### Task chi tiết

**1.1 Models (interface TypeScript khớp DTO backend)**
```typescript
// core/models/employee.model.ts
export interface Employee {
  employeeId?: number;
  employeeCode: string;
  fullName: string;
  email: string;
  role: string;
  department: string;
}
```
- [ ] Tương tự cho `Project`, `Allocation`, `ErrorResponse`, `WorkloadResponse`, `UtilizationReportItem`, `AvailableReportItem`, `OverloadedReportItem`

**1.2 Services (1 service / resource, dùng `inject(HttpClient)`)**
```typescript
// core/services/employee.service.ts
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/employees`;

  getAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.baseUrl);
  }
  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/${id}`);
  }
  create(payload: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, payload);
  }
}
```
- [ ] Tương tự `ProjectService`, `AllocationService`, `ReportService`

### Deliverable
4 service hoàn chỉnh, có thể inject vào component bất kỳ.

### Acceptance criteria
Mỗi service test nhanh bằng cách gọi trong 1 component tạm, log ra dữ liệu đúng.

---

## Giai đoạn 2 — Employee Module (1 ngày)

### Task chi tiết

**2.1 Employee List (2-3h)**
- [ ] `EmployeeListComponent`: dùng `mat-table` hiển thị danh sách (employeeCode, fullName, email, role, department)
- [ ] Load dữ liệu qua `EmployeeService.getAll()` trong `ngOnInit`
- [ ] Thêm nút "Thêm nhân viên" mở dialog/form

**2.2 Employee Create Form (2-3h)**
- [ ] `EmployeeFormComponent` dùng Reactive Form (`FormBuilder`)
- [ ] Validation phía frontend khớp backend: `Validators.required`, `Validators.email`
- [ ] Submit gọi `EmployeeService.create()`, sau khi thành công: đóng dialog + refresh list + hiện thông báo thành công

**2.3 Xử lý lỗi cơ bản (30p-1h)**
- [ ] Bắt lỗi `409 Conflict` (trùng code/email) từ backend, hiển thị message ngay trên form (chưa cần interceptor toàn cục, để giai đoạn 7 làm chung)

### Deliverable
Xem danh sách + thêm nhân viên hoạt động đầy đủ qua UI.

### Acceptance criteria
Thêm nhân viên trùng code → hiện lỗi rõ ràng trên form, không phải alert xấu xí.

---

## Giai đoạn 3 — Project Module (1 ngày)

### Task chi tiết

**3.1 Project List (2h)**
- [ ] `ProjectListComponent`: bảng hiển thị projectCode, projectName, customer, startDate, endDate, status (dùng `mat-chip` màu theo status: PLANNING=xám, ACTIVE=xanh, COMPLETED=xanh dương)
- [ ] Filter theo status (dropdown đơn giản gọi lại API với query param)

**3.2 Project Create Form (2-3h)**
- [ ] Form tương tự Employee, thêm `mat-datepicker` cho startDate/endDate
- [ ] Validate `endDate >= startDate` ngay trên form trước khi submit (feedback nhanh hơn, backend vẫn validate lại)

**3.3 Đổi trạng thái dự án (1-2h)**
- [ ] Nút "Chuyển trạng thái" trên mỗi dòng — chỉ hiện option hợp lệ tiếp theo (VD: đang PLANNING chỉ cho chọn ACTIVE, không cho chọn COMPLETED trực tiếp — khớp rule backend)
- [ ] Gọi `PUT /projects/{id}/status`, xử lý lỗi nếu backend reject

### Deliverable
Project CRUD + đổi trạng thái hoạt động qua UI.

### Acceptance criteria
UI chỉ hiện các lựa chọn status hợp lệ, tránh gửi request chắc chắn bị reject.

---

## Giai đoạn 4 — Allocation Module: Form Tạo Allocation (1.5 ngày, quan trọng nhất)

**Lý do đây là giai đoạn trọng tâm:** đây là nơi UI phải phản ánh đúng 3 Business Rule của backend, và hiển thị lỗi rõ ràng thay vì để user đoán.

### Task chi tiết

**4.1 Allocation List (1-2h)**
- [ ] `AllocationListComponent`: bảng hiển thị theo employee đã chọn (dropdown chọn employee trước, giống filter)
- [ ] Cột: projectCode, allocationPercent, roleInProject, startDate, endDate, nút xóa (soft-delete)

**4.2 Allocation Create Form (3-4h)**
- [ ] Dropdown chọn Employee (autocomplete nếu danh sách dài — `mat-autocomplete`)
- [ ] Dropdown chọn Project — **chỉ hiện project có status != COMPLETED** (lọc phía frontend trước khi hiện dropdown, để tránh user chọn nhầm — nhưng backend vẫn là nguồn chặn thật sự)
- [ ] Input allocationPercent (number, min=1 max=100 ngay trên `<input type="number">`)
- [ ] **Hiển thị % còn available của employee đang chọn ngay trên form** (gọi `GET /employees/{id}/workload` khi chọn employee, hiện dòng "Nhân viên này còn 20% khả dụng" — giúp user tự ước lượng trước khi nhập, giảm số lần bị reject)
- [ ] Submit gọi `AllocationService.create()`

**4.3 Xử lý lỗi Business Rule rõ ràng (2h)**
- [ ] Bắt lỗi `400` với message "Employee allocation exceeds 100%" → hiển thị ngay dưới field allocationPercent: "Vượt quá 100% (hiện tại đã dùng X%)"
- [ ] Bắt lỗi "Cannot allocate to a COMPLETED project" → hiển thị dưới field project
- [ ] Approach: parse message string từ backend (tạm thời), ghi chú TODO để sau này backend trả thêm `errorCode` chuẩn hóa thay vì so message string (dễ vỡ nếu backend đổi câu chữ)

### Deliverable
Form tạo allocation phản ánh đầy đủ 3 rule, feedback rõ ràng ngay trên UI.

### Acceptance criteria
- Thử tạo allocation vượt 100% → thấy lỗi cụ thể ngay dưới ô nhập, không phải alert chung chung.
- Dropdown project tự động ẩn project COMPLETED.

---

## Giai đoạn 5 — Employee Workload Detail View (0.5 ngày)

### Task chi tiết

- [ ] `WorkloadDetailComponent`: click vào 1 employee ở list → mở trang/dialog hiển thị `GET /employees/{id}/workload`
- [ ] Hiển thị: tổng allocation, % available, danh sách project đang tham gia kèm role
- [ ] Vẽ 1 progress bar đơn giản (`mat-progress-bar`) thể hiện % đã dùng — trực quan hơn số liệu thô

### Deliverable
Trang chi tiết workload cho từng nhân viên.

### Acceptance criteria
Số liệu khớp với dữ liệu allocation thực tế đã tạo ở giai đoạn 4.

---

## Giai đoạn 6 — Reports Dashboard (1 ngày)

### Task chi tiết

**6.1 Layout dashboard (1h)**
- [ ] `ReportsDashboardComponent` với 3 tab hoặc 3 card: Utilization / Available / Overloaded

**6.2 Utilization Report (1-2h)**
- [ ] Bảng đơn giản: employee, totalAllocation. Có thể thêm màu cảnh báo nếu > 90% (đỏ), 70-90% (vàng), còn lại xanh

**6.3 Available Resource Report (1h)**
- [ ] Bảng: employee, available %. Có ô input `minAvailable` filter (bind với query param backend)

**6.4 Overloaded Employee Report (1h)**
- [ ] Bảng: employee, totalAllocation — highlight đỏ toàn dòng

### Deliverable
Dashboard 3 báo cáo hiển thị đúng dữ liệu.

### Acceptance criteria
Số liệu khớp 100% với dữ liệu SQL báo cáo phía backend.

---

## Giai đoạn 7 — Error Handling & UX Polish Toàn Cục (1 ngày)

### Task chi tiết

- [ ] Viết `HttpErrorInterceptor` bắt lỗi chung (500, network error) → hiện toast/snackbar (`MatSnackBar`), tránh lặp code try/catch ở từng component
- [ ] Thêm `LoadingInterceptor` hoặc spinner thủ công cho mọi request (biến `isLoading` đơn giản, không cần thư viện phức tạp)
- [ ] Rà lại toàn bộ form: disable nút submit khi đang gọi API (tránh double-submit — quan trọng vì liên quan trực tiếp tới vấn đề concurrency ở backend)
- [ ] Thêm route guard/redirect cơ bản nếu có phân trang layout (menu điều hướng Employee/Project/Allocation/Reports)
- [ ] Kiểm tra lại toàn bộ message lỗi hiển thị bằng tiếng Việt dễ hiểu (không hiện thẳng message kỹ thuật từ backend)

### Deliverable
UX nhất quán: loading, lỗi, thành công đều có feedback rõ ràng toàn ứng dụng.

### Acceptance criteria
Tắt mạng thử (dev tools → offline) → app hiện thông báo lỗi thân thiện, không bị treo trắng màn hình.

---

## Giai đoạn 8 — (Bonus) AI Query Box (1 ngày)

### Task chi tiết

- [ ] 1 ô input tự nhiên ngữ + nút "Hỏi AI" trên dashboard
- [ ] Gọi `POST /ai/recommend-resource` hoặc `/ai/risk-detection` tùy theo nội dung câu hỏi (có thể thêm 2 nút riêng biệt thay vì đoán intent, đơn giản hơn cho phạm vi bonus)
- [ ] Hiển thị kết quả dạng list/card thay vì JSON thô

### Deliverable
Ô hỏi AI hoạt động, demo được 2 luồng ở SRS mục 8.

---

## Giai đoạn 9 — Final Review & Responsive Check (0.5 ngày)

### Checklist

- [ ] Test lại toàn bộ luồng: tạo employee → tạo project → tạo allocation (đủ rule) → xem workload → xem report
- [ ] Check responsive cơ bản (thu nhỏ trình duyệt, mat-table có scroll ngang khi màn hình nhỏ)
- [ ] Xóa console.log thừa, dọn code
- [ ] Build production thử: `ng build` không lỗi/warning nghiêm trọng
- [ ] Viết mục "Frontend Setup" bổ sung vào README chung của dự án

---

## Ghi chú thực thi

1. **Không dùng NgRx / state management phức tạp** cho quy mô này — Service + RxJS `Observable` trực tiếp là đủ, tránh over-engineering.
2. **Giai đoạn 4 (Allocation form) là nơi thể hiện rõ nhất bạn hiểu business rule của backend** — khi review, đây sẽ là phần được hỏi kỹ nhất ("frontend xử lý thế nào khi vượt 100%?").
3. Nếu thiếu thời gian, có thể cắt Giai đoạn 8 (AI bonus) và rút gọn Giai đoạn 6 (chỉ làm 1-2 report thay vì cả 3) mà không ảnh hưởng luồng chính.
4. Nhớ bật CORS ở backend sớm (Giai đoạn 0) — đây là lỗi tốn thời gian debug nhất khi lần đầu nối frontend-backend.
