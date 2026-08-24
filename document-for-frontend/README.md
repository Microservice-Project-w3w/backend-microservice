# Hướng dẫn tích hợp Backend cho Frontend

Tài liệu này được đối chiếu với source code và các Postman collection tại ngày 24/08/2026. Frontend nên dùng tài liệu này để chạy backend, cấu hình xác thực, phân quyền giao diện và chọn đúng API qua API Gateway.

## 1. Kết luận quan trọng

- Frontend chỉ gọi `http://localhost:8080` qua API Gateway.
- Không gọi trực tiếp các cổng `8081` đến `8087` trong code frontend.
- Không gọi bất kỳ endpoint `/internal/**` nào. Gateway không expose nhóm này.
- Ngoại trừ health và một số API auth công khai, mọi API đều cần JWT.
- Ẩn/hiện menu và nút theo `permissions` trong JWT, không chỉ dựa vào `role`.
- `organizationId`, `branchIds` và `customerId` trong JWT quyết định phạm vi dữ liệu. Giá trị frontend gửi trong query/body không thể mở rộng phạm vi này.
- Các Postman collection là lịch sử request/response đã test, không phải OpenAPI contract và chưa thể chạy trực tiếp hoàn toàn.

Thứ tự ưu tiên khi có khác biệt:

1. Controller, DTO và security trong source code hiện tại.
2. Tài liệu này và role-permission seed.
3. Postman saved examples.

## 2. Trạng thái phân quyền hiện tại

| Service | Endpoint nghiệp vụ | Trạng thái | Ghi chú cho frontend |
| --- | ---: | --- | --- |
| Identity | 28 | Đạt | 7 API auth công khai; 3 API tài khoản cá nhân chỉ cần đăng nhập; 18 API quản trị kiểm tra permission |
| Organization/Customer | 36 | Đạt | Có permission và organization/branch scope |
| Inventory | 84 | Đạt | Có permission; scope được kiểm tra ở controller/service hoặc class guard |
| Rental | 40 | Đạt | Có permission và scope; API ownership nội bộ chỉ dành cho CUSTOMER token |
| Logistics | 32 | Đạt | Có permission và branch/customer scope |
| Billing | 46 | Đạt, còn API legacy | Tất cả đều được bảo vệ; 3 API legacy chỉ kiểm tra `ADMIN`, xem mục 2.3 |
| Maintenance | 59 | Đạt | Có permission và organization/branch/customer scope |

Health endpoint của mỗi service được public có chủ đích và không tính là endpoint nghiệp vụ.

### 2.1 API công khai, không cần JWT

Các endpoint sau được public có chủ đích:

```text
GET  /health
GET  /gateway/health/identity
GET  /gateway/health/organization-customer
GET  /gateway/health/inventory
GET  /gateway/health/rental
GET  /gateway/health/logistics
GET  /gateway/health/billing
GET  /gateway/health/maintenance

POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/verification-codes
POST /api/v1/auth/verify-email
POST /api/v1/auth/password-reset
POST /api/v1/auth/reset-password
```

Việc response đăng ký hoặc yêu cầu mã xác minh có trả `verificationCode` chỉ phục vụ demo khi `AUTH_EXPOSE_VERIFICATION_CODE=true`. Frontend không được phụ thuộc vào trường này trong môi trường thật.

### 2.2 API tài khoản cá nhân

Ba endpoint sau bắt buộc có JWT nhưng không cần permission quản trị riêng vì chỉ thao tác trên tài khoản hiện tại:

```text
POST /api/v1/auth/logout
PUT  /api/v1/auth/password
GET  /api/v1/auth/me
```

### 2.3 API legacy chưa phân quyền chi tiết

Các API dưới đây không public, nhưng mới dừng ở kiểm tra role `ADMIN`, chưa dùng permission và data scope chi tiết:

| API | Vấn đề | Khuyến nghị frontend |
| --- | --- | --- |
| `POST /api/v1/billing/invoices/generate/{rentalOrderId}` | Chỉ `ADMIN`; chưa phù hợp luồng ACCOUNTANT | Không gắn vào màn hình kế toán; dùng `POST /api/v1/billing/invoices` |
| `POST /api/v1/debts/customers/{customerId}/add` | Chỉ `ADMIN`, không có organization/branch scope | Không tích hợp vào frontend |
| `POST /api/v1/debts/customers/{customerId}/reduce` | Chỉ `ADMIN`, không có organization/branch scope | Không tích hợp vào frontend |

Các API công nợ frontend nên dùng:

```text
GET  /api/v1/billing/debts
GET  /api/v1/billing/debts/{id}
PUT  /api/v1/billing/debts/{id}
POST /api/v1/billing/debts/{id}/settle
GET  /api/v1/billing/customers/{customerId}/debts
```

### 2.4 API nội bộ không dành cho frontend

Các collection Billing, Inventory và Maintenance có saved example `/internal/**`. Đây là API service-to-service, ví dụ:

```text
/internal/invoices/**
/internal/customers/**
/internal/rental-orders/**
/internal/rental-contracts/**
/internal/equipment/**
/internal/reservations/**
/internal/maintenance/**
```

Frontend gọi các path này qua Gateway sẽ nhận `404`. Không thêm route Gateway cho `/internal/**` chỉ để frontend gọi được.

## 3. Cách chạy toàn bộ backend

### 3.1 Yêu cầu

- JDK 21
- Maven 3.9+
- Docker Engine hoặc Docker Desktop có Docker Compose
- Các cổng mặc định `8080` đến `8087` chưa bị ứng dụng khác sử dụng

### 3.2 Tạo cấu hình local

Tại root backend:

```bash
cp .env.example .env
```

Các service phải dùng cùng `JWT_ISSUER` và `JWT_SECRET_BASE64`. Nếu frontend không chạy ở `http://localhost:5173`, thêm hoặc sửa:

```dotenv
FRONTEND_URL=http://localhost:3000
```

### 3.3 Chạy hạ tầng Docker

```bash
docker compose --env-file .env -f infra/docker-compose.yml up -d
docker compose --env-file .env -f infra/docker-compose.yml ps
```

Docker chạy MySQL, RabbitMQ và Redis. File `infra/mysql/init/01-create-databases.sql` chỉ tự chạy khi MySQL volume được tạo lần đầu. Nếu volume đã tồn tại, Docker không tự chạy lại file init; không xóa volume nếu đang có dữ liệu cần giữ.

### 3.4 Build

```bash
mvn clean install
```

### 3.5 Chạy các service

Trong mỗi terminal Bash/WSL, nạp biến môi trường trước:

```bash
set -a
source .env
set +a
```

Sau đó chạy mỗi lệnh trong một terminal riêng. Nên chạy Identity trước và Gateway cuối cùng:

```bash
mvn -pl services/identity-service spring-boot:run
mvn -pl services/organization-customer-service spring-boot:run
mvn -pl services/inventory-service spring-boot:run
mvn -pl services/rental-service spring-boot:run
mvn -pl services/logistics-service spring-boot:run
mvn -pl services/billing-service spring-boot:run
mvn -pl services/maintenance-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

### 3.6 Kiểm tra qua Gateway

```bash
curl http://localhost:8080/health
curl http://localhost:8080/gateway/health/identity
curl http://localhost:8080/gateway/health/organization-customer
curl http://localhost:8080/gateway/health/inventory
curl http://localhost:8080/gateway/health/rental
curl http://localhost:8080/gateway/health/logistics
curl http://localhost:8080/gateway/health/billing
curl http://localhost:8080/gateway/health/maintenance
```

Chỉ bắt đầu tích hợp frontend khi Gateway và service tương ứng trả HTTP `200`.

## 4. Cấu hình frontend

Ví dụ với Vite:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

Axios client tối thiểu:

```ts
import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const accessToken = authStore.getState().accessToken;

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});
```

Không hardcode `8081`, `8082`, ... trong component hoặc API module.

## 5. Đăng nhập, token và refresh

### 5.1 Đăng nhập

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@gmail.com",
  "password": "password"
}
```

Identity trả response có envelope:

```json
{
  "success": true,
  "timestamp": "...",
  "data": {
    "accessToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "refreshToken": "...",
    "userId": 1,
    "email": "user@gmail.com",
    "fullName": "User",
    "role": "SALES_STAFF"
  },
  "message": "Đăng nhập thành công",
  "traceId": "..."
}
```

`expiresIn` tính bằng giây. Khi access token hết hạn:

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "..."
}
```

Frontend chỉ retry request một lần sau refresh để tránh vòng lặp vô hạn. Không gọi refresh khi nhận `403`.

### 5.2 Claims cần dùng

JWT hiện có các claim quan trọng:

| Claim | Mục đích frontend |
| --- | --- |
| `userId` / `sub` | Người dùng đăng nhập |
| `roles` | Chọn layout/menu tổng quát |
| `permissions` | Guard route, menu và action |
| `organizationId` | Doanh nghiệp hiện tại |
| `branchIds` | Danh sách chi nhánh được phép truy cập |
| `customerId` | Bắt buộc cho CUSTOMER và dữ liệu OWN |
| `sessionId` | Phiên đăng nhập |

Sau khi ADMIN thay role, permission, organization hoặc branch assignment của một user, token cũ chưa tự thay đổi. User cần refresh token hoặc đăng nhập lại trước khi frontend cập nhật quyền.

### 5.3 Guard giao diện theo permission

```ts
export function can(permission: string): boolean {
  return authStore.getState().permissions.includes(permission);
}

const canApproveQuotation = can("rental.quotation.approve");
const canRefundDeposit = can("billing.deposit.refund");
```

Frontend guard chỉ cải thiện UX. Backend vẫn là nơi quyết định quyền cuối cùng; luôn xử lý HTTP `403` kể cả khi nút đã được ẩn.

## 6. Route Gateway theo module

| Module | Path frontend được dùng | Service |
| --- | --- | --- |
| Auth | `/api/v1/auth/**` | Identity |
| User/role/permission/session | `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**`, `/api/v1/sessions/**` | Identity |
| Doanh nghiệp/chi nhánh/nhân viên/khách hàng | `/api/v1/organizations/**` | Organization/Customer |
| Danh mục/thiết bị/kho | `/api/v1/inventory/**` | Inventory |
| Giá, yêu cầu thuê, báo giá, đơn, hợp đồng | `/api/v1/rental-prices/**`, `/api/v1/discount-codes/**`, `/api/v1/availability`, `/api/v1/equipment/search`, `/api/v1/rental-requests/**`, `/api/v1/quotations/**`, `/api/v1/rental-orders/**`, `/api/v1/rental-contracts/**` | Rental |
| Giao nhận/nhận trả | `/api/v1/logistics/**` | Logistics |
| Hóa đơn/thanh toán/cọc/công nợ/báo cáo | `/api/v1/billing/**` | Billing |
| Bảo trì/sửa chữa/sự cố | `/api/v1/maintenance/**` | Maintenance |

## 7. Nhóm API frontend nên tạo

### 7.1 Identity

| Nhóm | Path | Giao diện |
| --- | --- | --- |
| Auth | `/api/v1/auth/**` | Đăng ký, xác minh email, đăng nhập, quên mật khẩu, hồ sơ hiện tại |
| User | `/api/v1/users/**` | Tài khoản |
| Role/permission | `/api/v1/roles/**`, `/api/v1/permissions/**` | Role và permission |
| Session | `/api/v1/users/{id}/sessions`, `/api/v1/sessions/{id}/revoke` | Quản lý phiên |

### 7.2 Organization/Customer

| Nhóm | Path chính | Giao diện |
| --- | --- | --- |
| Doanh nghiệp | `/api/v1/organizations` | Cấu hình doanh nghiệp |
| Chi nhánh | `/api/v1/organizations/{organizationId}/branches` | Chi nhánh |
| Nhân viên | `/api/v1/organizations/{organizationId}/employees` | Nhân viên |
| Phân công chi nhánh | `/api/v1/organizations/{organizationId}/employee-branch-assignments` | Phân công nhân viên |
| Khách hàng | `/api/v1/organizations/{organizationId}/customers` | Khách hàng |
| Nhóm khách hàng | `/api/v1/organizations/{organizationId}/customer-groups` | Nhóm khách hàng |
| Khách bị hạn chế | `/api/v1/organizations/{organizationId}/restricted-customers` | Kiểm soát khách hàng |

### 7.3 Inventory

| Nhóm | Path chính |
| --- | --- |
| Danh mục | `/api/v1/inventory/categories`, `/api/v1/inventory/equipment-types`, `/api/v1/inventory/brands`, `/api/v1/inventory/models` |
| Thiết bị | `/api/v1/inventory/equipment` |
| Ảnh/phụ kiện/QR | `/api/v1/inventory/equipment-images`, `/api/v1/inventory/equipment/{id}/accessories`, `/api/v1/inventory/equipment/{id}/qr` |
| Kho | `/api/v1/inventory/warehouses` |
| Nhập/xuất | `/api/v1/inventory/stock-in`, `/api/v1/inventory/stock-out` |
| Điều chuyển | `/api/v1/inventory/transfers` |
| Kiểm kê | `/api/v1/inventory/stock-audits` |
| Reservation | `/api/v1/inventory/reservations` |

Để frontend kiểm tra khả dụng trong quy trình thuê, ưu tiên `GET /api/v1/availability` của Rental. Không dùng `/internal/equipment/availability`.

### 7.4 Rental

| Nhóm | Path chính | Role thường dùng |
| --- | --- | --- |
| Giá thuê | `/api/v1/rental-prices` | ADMIN |
| Mã giảm giá | `/api/v1/discount-codes` | ADMIN |
| Yêu cầu thuê | `/api/v1/rental-requests` | SALES_STAFF, CUSTOMER |
| Báo giá | `/api/v1/quotations` | SALES_STAFF, MANAGER, CUSTOMER |
| Đơn thuê | `/api/v1/rental-orders` | SALES_STAFF, MANAGER, CUSTOMER |
| Hợp đồng/phụ lục/gia hạn | `/api/v1/rental-contracts` | SALES_STAFF, MANAGER, CUSTOMER |

Backend hiện chưa có `GET /api/v1/quotations/{id}` và `GET /api/v1/rental-orders/{id}`. Màn hình chi tiết hai đối tượng này tạm thời phải lấy từ API danh sách hoặc cần backend bổ sung endpoint riêng.

### 7.5 Logistics

| Nhóm | Path chính |
| --- | --- |
| Chính sách phí giao nhận | `/api/v1/logistics/delivery-fee-rules` |
| Nhiệm vụ/lịch giao nhận | `/api/v1/logistics/delivery-tasks` |
| Phiếu xuất | `/api/v1/logistics/dispatch-notes` |
| Bàn giao/checklist/ảnh | `/api/v1/logistics/handover-records` |
| Yêu cầu và biên bản nhận trả | `/api/v1/logistics/returns` |

### 7.6 Billing

| Nhóm | Path chính |
| --- | --- |
| Hóa đơn | `/api/v1/billing/invoices` |
| Thanh toán/QR/chuyển khoản | `/api/v1/billing/payments` |
| Tiền đặt cọc | `/api/v1/billing/deposits` |
| Công nợ | `/api/v1/billing/debts` |
| Tổng hợp theo khách hàng/đơn/hợp đồng | `/api/v1/billing/customers/**`, `/api/v1/billing/rental-orders/**`, `/api/v1/billing/rental-contracts/**` |
| Báo cáo | `/api/v1/billing/reports/**` |

ACCOUNTANT có các permission `billing.*` cần thiết. Không dùng ba API legacy ở mục 2.3 cho màn hình kế toán.

### 7.7 Maintenance

| Nhóm | Path chính | Ghi chú |
| --- | --- | --- |
| Yêu cầu bảo trì | `/api/v1/maintenance/requests` | OPERATIONS_STAFF tạo/cập nhật; MANAGER đọc |
| Work order | `/api/v1/maintenance/work-orders` | Phân công, bắt đầu, chờ phụ tùng, hoàn tất, đóng |
| Kiểm tra hư hỏng | `/api/v1/maintenance/work-orders/{id}/inspections` | OPERATIONS_STAFF |
| Kế hoạch định kỳ | `/api/v1/maintenance/plans` | ADMIN/OPERATIONS_STAFF quản lý; MANAGER đọc |
| Linh kiện/chi phí/attachment | `/api/v1/maintenance/work-orders/{id}/parts`, `/api/v1/maintenance/work-orders/{id}/costs`, `/api/v1/maintenance/work-orders/{id}/attachments` | Theo permission bảo trì |
| Dashboard/báo cáo | `/api/v1/maintenance/dashboard/**`, `/api/v1/maintenance/reports/**`, `/api/v1/maintenance/equipment/**` | MANAGER/OPERATIONS_STAFF/ADMIN |
| Sự cố của CUSTOMER | `/api/v1/maintenance/customer/issues` | Chỉ dữ liệu của `customerId` hiện tại |
| Xử lý sự cố | `/api/v1/maintenance/issues` | OPERATIONS_STAFF/MANAGER |

Không nhầm `/customer/issues` với `/issues`: path đầu là self-service của CUSTOMER, path sau là hàng đợi xử lý nội bộ nghiệp vụ.

## 8. Menu theo role

Frontend nên dùng bảng này để dựng navigation ban đầu, sau đó kiểm tra permission cho từng action.

| Role | Menu chính |
| --- | --- |
| `ADMIN` | Dashboard, tài khoản, nhân viên, chi nhánh, danh mục, cấu hình, báo cáo |
| `MANAGER` | Dashboard quản lý, báo giá chờ duyệt, hợp đồng chờ duyệt, đơn thuê, giao nhận, công nợ, thiết bị |
| `SALES_STAFF` | Khách hàng, yêu cầu thuê, báo giá, đơn thuê, hợp đồng |
| `OPERATIONS_STAFF` | Thiết bị và kho, giao thiết bị, nhận trả thiết bị, bảo trì và sửa chữa |
| `ACCOUNTANT` | Hóa đơn, thanh toán, tiền đặt cọc, công nợ, báo cáo doanh thu |
| `CUSTOMER` | Thiết bị, yêu cầu thuê của tôi, báo giá của tôi, hợp đồng của tôi, hóa đơn, yêu cầu trả, báo cáo sự cố |

Ví dụ action cần permission riêng:

```text
rental.quotation.approve       nút phê duyệt báo giá
rental.contract.approve        nút phê duyệt hợp đồng
inventory.stock.out            xác nhận xuất kho
logistics.delivery.confirm     xác nhận giao nhận
billing.payment.confirm        xác nhận thanh toán
billing.deposit.refund         hoàn cọc
maintenance.ticket.assign      phân công bảo trì
maintenance.ticket.complete    hoàn tất bảo trì
```

## 9. Luồng tích hợp chính

### 9.1 SALES_STAFF và MANAGER

```text
POST  /api/v1/organizations/{orgId}/customers
  -> POST  /api/v1/rental-requests
  -> POST  /api/v1/quotations
  -> PATCH /api/v1/quotations/{id}/send
  -> PATCH /api/v1/quotations/{id}/approve       (MANAGER)
  -> PATCH /api/v1/quotations/{id}/accept        (CUSTOMER nếu áp dụng)
  -> POST  /api/v1/quotations/{id}/convert-to-order
  -> POST  /api/v1/rental-contracts
  -> PATCH /api/v1/rental-contracts/{id}/approve (MANAGER)
```

### 9.2 OPERATIONS_STAFF

```text
Đơn thuê được xác nhận
  -> reservation / chuẩn bị thiết bị
  -> POST stock-out và confirm
  -> tạo delivery-task / dispatch-note
  -> tạo và confirm handover-record
  -> nhận return-request
  -> tạo return inspection / return record
  -> nếu hư hỏng: tạo maintenance request/work order
  -> cập nhật thiết bị AVAILABLE hoặc MAINTENANCE qua workflow backend
```

Frontend không tự gọi các bước `/internal/**` nằm giữa các service.

### 9.3 ACCOUNTANT

```text
POST /api/v1/billing/invoices
  -> POST /api/v1/billing/invoices/{id}/issue
  -> POST /api/v1/billing/payments
  -> POST /api/v1/billing/payments/{id}/confirm
  -> quản lý deposit / debt
  -> GET /api/v1/billing/reports/**
```

### 9.4 CUSTOMER

```text
Xem thiết bị/availability
  -> tạo và theo dõi rental-request
  -> xem/chấp nhận quotation
  -> xem order/contract
  -> xem invoice/payment
  -> tạo return-request
  -> POST /api/v1/maintenance/customer/issues khi có sự cố
```

CUSTOMER phải có `customerId` trong JWT. Nếu Identity account chưa liên kết customer record, API OWN có thể trả `403` dù role là CUSTOMER.

## 10. Response và error handling

Một số API trả envelope chuẩn:

```json
{
  "success": true,
  "timestamp": "...",
  "data": {},
  "message": null,
  "traceId": "..."
}
```

Một số service/endpoint cũ trả trực tiếp object hoặc array. API client frontend nên normalize:

```ts
export function unwrap<T>(body: T | { success: boolean; data: T }): T {
  if (body && typeof body === "object" && "success" in body && "data" in body) {
    return body.data;
  }

  return body as T;
}
```

| HTTP | Ý nghĩa | Cách frontend xử lý |
| ---: | --- | --- |
| `200/201` | Thành công | Cập nhật state bằng response thật |
| `204` | Thành công, không có body | Không cố parse JSON |
| `400` | Validation hoặc sai workflow/status | Hiển thị `message` và `details` |
| `401` | Thiếu/hết hạn/sai token | Refresh một lần; thất bại thì về login |
| `403` | Thiếu permission hoặc sai data scope | Không retry; thông báo không có quyền |
| `404` | Không tồn tại hoặc không thuộc scope | Hiển thị not found an toàn |
| `409` | Xung đột dữ liệu/trạng thái | Yêu cầu reload dữ liệu |
| `500/503` | Lỗi backend hoặc service tích hợp | Hiển thị lỗi tạm thời và `traceId` |

Không sử dụng status code trong saved response Postman làm giá trị cố định. Ví dụ collection có cả case thành công lẫn case cố ý test `400`, `401`, `404`, `409`, `500` và `503`.

## 11. Cách đọc các Postman collection

| File | Saved examples | Lưu ý |
| --- | ---: | --- |
| `identity-service.postman_collection.json` | 24 | Có nhiều response khác nhau cho cùng request đăng ký/login |
| `organization.postman_collection.json` | 38 | Có placeholder `{{organizationId}}` nhưng collection không khai báo đầy đủ variable |
| `iventory-service.postman_collection.json` | 117 | Tên file đang viết nhầm `iventory`; có nhiều internal/error case |
| `rental-service.postman_collection.json` | 49 | Có cả request Identity; một số response là lỗi workflow cố ý |
| `logistics-service.postman_collection.json` | 25 | Saved request nằm trong example |
| `billing-service.postman_collection.json` | 43 | Có internal API không dành cho frontend |
| `maintenance-service.postman_collection.json` | 84 | Trộn Identity, Inventory, Rental và Maintenance; có một URL sai `ttp://` |

Phần lớn item bên ngoài có dạng `GET` không URL. Method, URL, body thật nằm trong:

```text
item[].response[].originalRequest
```

Vì vậy:

- Dùng collection để xem payload và response mẫu.
- Không copy request ngoài cùng nếu nó đang là `GET` rỗng.
- Đổi host direct-service thành `http://localhost:8080` cho API `/api/v1/**`.
- Không đổi và không sử dụng `/internal/**`.
- Thêm `Authorization: Bearer <accessToken>` cho mọi API không nằm trong mục 2.1.
- Thay toàn bộ ID, ngày giờ, email, code và dữ liệu hardcode bằng dữ liệu lấy từ response runtime.
- Không coi response snapshot tháng 08/2026 là dữ liệu hiện tại.

## 12. Khoảng trống API frontend cần biết

- Chưa có OpenAPI/Swagger làm contract máy đọc; DTO trong source là nguồn chính xác nhất cho field request/response.
- Rental chưa có API detail riêng cho quotation và rental order.
- Nhiều API list đang trả toàn bộ danh sách, chưa có pagination thống nhất.
- Dashboard tổng thể ADMIN/MANAGER chưa có một endpoint aggregate duy nhất; frontend phải ghép báo cáo từ từng module hoặc backend cần bổ sung BFF/report endpoint.
- Response envelope chưa đồng nhất giữa tất cả service.
- Ba API Billing legacy tại mục 2.3 không nên được dùng cho màn hình mới.

## 13. Checklist trước khi bàn giao màn hình

- API dùng base URL `http://localhost:8080`.
- Không có `/internal/**` trong frontend source.
- Request có Bearer token, trừ public endpoint.
- Route, menu và button kiểm tra permission.
- Không cho user chọn organization/branch ngoài claims hiện tại.
- CUSTOMER đã có `customerId`.
- Có xử lý `204`, `400`, `401`, `403`, `404`, `409`, `500/503`.
- Không retry vô hạn khi refresh token thất bại.
- Không log access token, refresh token, password hoặc verification code.
- Dùng ID từ response runtime, không dùng ID trong Postman snapshot.
- Test tối thiểu một case đúng quyền và một case `403` cho action nhạy cảm.
