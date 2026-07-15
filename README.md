# Project Resource Allocation Management System (PRAMS)

Monorepo cho hệ thống quản lý phân bổ nhân sự trong công ty outsourcing. Gồm 2 thành phần:

| Component | Directory | Tech | Port |
|---|---|---|---|
| **Backend API** | [`pram/`](pram/) | Java 25, Spring Boot 4.1, PostgreSQL 15 | `:8080` |
| **Frontend SPA** | [`pram-fe/`](pram-fe/) | Angular 21, Angular Material 21 | `:4200` |

---

## Quick Start

### Docker Compose (recommended)

```bash
docker compose up --build -d
```

Mở `http://localhost:4200` — frontend gọi backend tự động.

### Hoặc chạy riêng từng phần

**Backend** (cần PostgreSQL local):

```bash
cd pram
./mvnw spring-boot:run
```

**Frontend**:

```bash
cd pram-fe
npm install
ng serve
```

---

## Tính năng chính

### Core Logic & Business Rules
- **Employee CRUD** — Quản lý nhân sự (mã, tên, email, role, phòng ban)
- **Project CRUD** — Quản lý dự án + chuyển trạng thái (PLANNING → ACTIVE → COMPLETED)
- **Allocation** — Phân bổ nhân sự vào dự án, enforce business rules:
  - `1% ≤ allocationPercent ≤ 100%`
  - Tổng allocation của 1 employee không vượt quá 100%
  - Không allocation vào project COMPLETED
  - Soft-delete, concurrency lock (`PESSIMISTIC_WRITE`)
- **Workload Detail** — Xem % đã dùng + % còn available của từng employee
- **Reports Dashboard** — Utilization, Available Resources (filter by min %), Overloaded employees
- **AI Assistant** — Gợi ý resource + phát hiện rủi ro qua OpenRouter LLM
- **Swagger UI** — `http://localhost:8080/swagger-ui.html`

### UI/UX & Redesign (New)
- **Premium Dark Mode Theme**: Giao diện tối hiện đại sử dụng CSS Variables, màu sắc đồng bộ, sidebar dịch chuyển mượt mà, hiệu ứng hover, frosted-glass (glassmorphism) và glow shadows.
- **Instant Data Sync**: Trạng thái Project cập nhật inline nhanh chóng mà không cần tải lại toàn bộ trang (sử dụng Angular Change Detection reference checks).
- **MDC Form Field & Overlay Repair**:
  - Khắc phục lỗi chồng lấn viền nét đứt (notch border overlap) lên nhãn input khi click chuột.
  - Tích hợp `@include mat.core()` và các lớp CSS CDK Overlay cụ thể, giải quyết lỗi menu dropdown và Datepicker bị trong suốt hoặc nhảy sai vị trí.
- **Datepicker Validation Safety**: Ràng buộc thuộc tính `[min]` cho lịch ngày kết thúc dựa trên ngày bắt đầu, loại bỏ việc lựa chọn khoảng thời gian không hợp lệ.
- **Form Server Validation Mapping**: Ánh xạ phản hồi trùng lặp thông tin từ máy chủ trực tiếp xuống trường Reactive Form tương ứng.

---

## Cấu trúc thư mục

```
ojt/
├── pram/                    # Backend — Spring Boot REST API
│   ├── src/main/java/       # Controllers, Services, Repositories, Entities, DTOs
│   ├── db/schema.sql        # Database schema + seed data
│   ├── docker-compose.yml   # Orchestrates DB + backend + frontend
│   └── README.md            # Chi tiết backend
│
├── pram-fe/                 # Frontend — Angular SPA
│   ├── src/app/core/        # Models, Services, Interceptors
│   ├── src/app/features/    # Employee, Project, Allocation, Reports modules
│   └── README.md            # Chi tiết frontend
│
├── docker-compose.yml       # Full-stack orchestration
├── .editorconfig            # Editor settings (2-space indent, UTF-8)
├── .gitattributes           # Line ending normalization
├── .gitignore               # Global ignores
├── README.md                # File này
```

Chi tiết từng phần xem trong `pram/README.md` (backend) và `pram-fe/README.md` (frontend).
