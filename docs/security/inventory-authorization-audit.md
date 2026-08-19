# Inventory authorization audit and implementation checklist

Ngày rà soát: 2026-08-18  
Phạm vi: `services/inventory-service` và hai file seed được tham chiếu để đối chiếu. Không có mã nguồn Inventory, `permission-seed.csv`, hoặc `role-permission-seed.csv` nào bị thay đổi bởi tài liệu này.

## Kết luận

Mục 6 đã được áp dụng cho `EquipmentCategory`, `EquipmentType`, `Brand`, `EquipmentModel` và `Equipment`: các endpoint nhận `organizationId` đã gọi `InventoryDataScopeGuard`; Equipment create/update/list/search cũng kiểm tra branch khi có branch trong request. Generic `PATCH /equipment/{id}/status` chỉ nhận các trạng thái quản trị `DAMAGED`, `MAINTENANCE`, `LOST`, `RETIRED`, `INSPECTION`.

Toàn bộ controller Inventory còn lại dưới đây vẫn thiếu data-scope check. `@PreAuthorize` chỉ kiểm tra authority; nó không ngăn người có quyền BRANCH thử một ID, `organizationId`, `branchId` hoặc `warehouseId` thuộc phạm vi khác.

Lỗi permission seed cần xử lý ở Mục 19–20: `EquipmentReservationController` yêu cầu `inventory.reservation.read`, nhưng permission này không có trong `docs/security/permission-seed.csv` và cũng không thể được gán qua `role-permission-seed.csv`. Khi giữ annotation hiện tại, tất cả request query reservation sẽ bị từ chối ngay cả với role hợp lệ.

## Quy ước triển khai bắt buộc

Mỗi controller cần inject:

```java
private final InventoryDataScopeGuard dataScopeGuard;
```

Áp dụng đúng một trong các mẫu sau, trước khi gọi action làm thay đổi dữ liệu:

| Dữ liệu đầu vào | Cách kiểm tra |
| --- | --- |
| Có `organizationId`, không có branch | `dataScopeGuard.checkOrganization(organizationId)` |
| Có `organizationId` và `branchId` | `dataScopeGuard.checkBranch(organizationId, branchId)` |
| Danh sách có `branchId` tùy chọn | Có branch: `checkBranch`; không có branch: `checkOrganization` |
| Endpoint chỉ có `id` | Load response/entity trước, lấy organization + branch, gọi `checkBranch`, rồi mới thực hiện/return action |
| Transfer nguồn–đích | Kiểm tra cả source và destination branch/warehouse; không được chỉ kiểm tra nguồn |
| Endpoint con của Equipment | Resolve Equipment chủ trước, kiểm tra organization + branch của Equipment; không tin `equipmentId` hay `imageId` từ URL |

`checkBranch` đã bao hàm việc xác thực tổ chức/chi nhánh theo JWT. Với các list/search cho phép không truyền branch, bắt buộc giữ `organizationId` là request parameter bắt buộc; không được để service rơi xuống nhánh `findAll()` toàn hệ thống.

Với endpoint ID-only, việc load bản ghi trước scope check là chấp nhận được để xác định scope, nhưng response chỉ được trả về sau khi guard thành công. Các service update/confirm/cancel phải được gọi sau guard.

## Các phần cần sửa trong Inventory

### Mục 7 — Warehouse scope (5 endpoint)

Hiện trạng: có authority nhưng không có guard. `findAll` còn cho phép thiếu `organizationId`, và `WarehouseServiceImpl.findAll` sẽ trả toàn bộ warehouse.

| Endpoint | Authority hiện có | Việc phải làm |
| --- | --- | --- |
| `POST /warehouses` | `inventory.warehouse.manage` | `checkBranch(request.organizationId(), request.branchId())`, rồi `service.create`. Nên thêm `@Valid` khi DTO có validation. |
| `GET /warehouses` | `inventory.warehouse.read` | Đổi `organizationId` thành bắt buộc. Nếu `branchId != null`, gọi `checkBranch`; ngược lại `checkOrganization`. |
| `GET /warehouses/{id}` | `inventory.warehouse.read` | `WarehouseResponse current = service.findById(id)`; `checkBranch(current.organizationId(), current.branchId())`; return `current`. |
| `PUT /warehouses/{id}` | `inventory.warehouse.manage` | Load warehouse hiện tại, `checkBranch(current.organizationId(), current.branchId())`, rồi update. DTO hiện tại không đổi organization/branch nên không có destination scope. |
| `PATCH /warehouses/{id}/active` | `inventory.warehouse.manage` | Load current, check branch, rồi `changeActive`. |

### Mục 8 — StockIn (5 endpoint)

Hiện trạng: class-level `inventory.stock.in` có mặt, nhưng cả năm endpoint không scope. Service đã xác thực warehouse thuộc organization/branch khi create/confirm; đó là business integrity, không thay thế JWT scope.

| Endpoint | Việc phải làm |
| --- | --- |
| `POST /stock-in` | `checkBranch(request.organizationId(), request.branchId())`. Sau đó xác nhận `warehouseId` trong service vẫn thuộc đúng organization/branch (đã có). |
| `GET /stock-in` | `organizationId` bắt buộc; có `branchId` thì check branch, không thì check organization. Nếu nhận `warehouseId`, load warehouse và kiểm tra branch của warehouse; đồng thời từ chối filter warehouse không khớp `organizationId`/`branchId`. |
| `GET /stock-in/{id}` | Load `StockInResponse`, check branch từ response, return. |
| `POST /stock-in/{id}/confirm` | Load response, check branch, rồi confirm. |
| `POST /stock-in/{id}/cancel` | Load response, check branch, rồi cancel. |

### Mục 9 — StockOut (5 endpoint)

Hiện trạng: từng endpoint có `inventory.stock.out`, nhưng không scope. Áp dụng cùng pattern với StockIn.

| Endpoint | Việc phải làm |
| --- | --- |
| `POST /stock-out` | `checkBranch(request.organizationId(), request.branchId())`, trước create. |
| `GET /stock-out` | Bắt buộc organization; branch optional theo pattern list; nếu lọc warehouse thì resolve warehouse và check branch. |
| `GET /stock-out/{id}` | Load response, `checkBranch(response.organizationId(), response.branchId())`. |
| `POST /stock-out/{id}/confirm` | Load response, check branch, rồi confirm. |
| `POST /stock-out/{id}/cancel` | Load response, check branch, rồi cancel. |

### Mục 10 — StockTransfer (7 endpoint)

Hiện trạng: class-level `inventory.stock.transfer` có mặt, nhưng không scope. Đây là endpoint nhạy cảm nhất vì một thao tác liên quan hai branch/warehouse.

| Endpoint | Việc phải làm |
| --- | --- |
| `POST /transfers` | Load source + destination warehouse. Xác nhận cả hai thuộc `request.organizationId()`. Gọi `checkBranch(org, source.branchId)` **và** `checkBranch(org, destination.branchId)`, rồi create. Không suy diễn branch từ body vì DTO chỉ gửi warehouse ID. |
| `GET /transfers` | `organizationId` bắt buộc. Có filter `branchId`: `checkBranch`; không: `checkOrganization`. Kết quả transfer liên branch phải được service lọc sao cho caller có scope trên ít nhất source hoặc destination; không trả transfer của hai branch không được phép. |
| `GET /transfers/{id}` | Load response; cần check cả `fromBranchId` và `toBranchId` trước khi trả. |
| `POST /transfers/{id}/approve` | Load response, check cả hai branch, rồi approve. |
| `POST /transfers/{id}/dispatch` | Load response, check source branch tối thiểu; policy khuyến nghị check cả hai branch để một user BRANCH không điều phối hàng vào branch ngoài scope. |
| `POST /transfers/{id}/receive` | Load response, check destination branch tối thiểu; policy khuyến nghị check cả hai branch trước receive. |
| `POST /transfers/{id}/cancel` | Load response, check cả hai branch, rồi cancel. |

### Mục 11 — StockAudit (7 endpoint)

Hiện trạng: class-level `inventory.stock.audit` có mặt, không endpoint nào scope.

| Endpoint | Việc phải làm |
| --- | --- |
| `POST /stock-audits` | `checkBranch(request.organizationId(), request.branchId())`; service tiếp tục xác minh warehouse thuộc scope đó. |
| `GET /stock-audits` | Organization bắt buộc; branch optional theo pattern list; resolve/check warehouse khi có warehouse filter. |
| `GET /stock-audits/{id}` | Load response, check branch. |
| `POST /stock-audits/{id}/start` | Load response, check branch, rồi start. |
| `POST /stock-audits/{id}/items` | Load response, check branch, rồi record item. |
| `POST /stock-audits/{id}/complete` | Load response, check branch, rồi complete. |
| `POST /stock-audits/{id}/cancel` | Load response, check branch, rồi cancel. |

### Mục 12 — Reservation Query (2 endpoint)

Hiện trạng: cả hai endpoint dùng authority không tồn tại (`inventory.reservation.read`), không scope, và list hiện có thể gọi `repository.findAll()`.

| Endpoint | Việc phải làm |
| --- | --- |
| `GET /reservations` | Bổ sung `organizationId` bắt buộc và `branchId` optional vào API/service/repository. Check branch nếu có filter, otherwise check organization. Không cho list thiếu organization. |
| `GET /reservations/{id}` | Load response, check `response.organizationId()` + `response.branchId()`, return response. |

Authority `inventory.reservation.read` cần được thêm tại Mục 19, sau đó gán role hợp lý tại Mục 20. Không thay bằng `inventory.reservation.create` hoặc `inventory.equipment.read`: hai quyền đó không diễn đạt quyền đọc reservation.

### Mục 13 — Internal Reservation (3 endpoint)

Hiện trạng: create/confirm/release có authority đúng seed, nhưng thiếu guard.

| Endpoint | Việc phải làm |
| --- | --- |
| `POST /internal/reservations` | `checkBranch(request.organizationId(), request.branchId())`, rồi create. Giữ validation business rằng toàn bộ equipment item thuộc branch. |
| `POST /internal/reservations/{id}/confirm` | Load reservation response trước, check branch từ response, rồi confirm. |
| `POST /internal/reservations/{id}/release` | Load reservation response trước, check branch từ response, rồi release. |

### Mục 14 — Availability (1 endpoint)

Hiện trạng: authority `inventory.availability.read` đúng seed nhưng endpoint chưa scope.

```java
dataScopeGuard.checkBranch(organizationId, branchId);
return service.checkAvailability(...);
```

Check phải nằm trước service call. Điều này bảo vệ việc enumerate số lượng thiết bị sẵn có ở branch khác.

### Mục 15 — Internal Equipment Query (1 endpoint)

Hiện trạng: có `inventory.equipment.read`, không scope.

Load `InternalEquipmentResponse response = service.findById(id)`, gọi `checkBranch(response.organizationId(), response.branchId())`, rồi return `response`.

### Mục 16 — Checkout (1 endpoint)

Hiện trạng: có `inventory.stock.out`, service kiểm tra organization/branch của request khớp Equipment nhưng controller không kiểm tra scope JWT.

Trước checkout, gọi `checkBranch(request.organizationId(), request.branchId())`. Để tránh request/ID mâu thuẫn bị dùng làm side channel, load Equipment/`InternalEquipmentResponse` theo ID, check branch của bản ghi và chỉ sau đó gọi service. Service validation hiện tại vẫn phải giữ nguyên.

### Mục 17 — Checkin (1 endpoint)

Áp dụng giống Checkout: scope request với `checkBranch(request.organizationId(), request.branchId())`, resolve Equipment theo ID và check branch thực tế trước `service.checkin`. Không để Checkin generic thay đổi trạng thái; service hiện quyết định `AVAILABLE` hoặc `MAINTENANCE` từ condition là đúng hướng.

### Mục 18 — Equipment Status History (2 endpoint)

Hiện trạng có hai vấn đề nghiêm trọng:

1. `POST /equipment/{equipmentId}/status-history` không có `@PreAuthorize`.
2. Service trực tiếp `equipment.setStatus(request.newStatus())`, vì vậy endpoint này lách quy tắc Mục 6 và có thể tự set `RESERVED`, `CHECKED_OUT`, `AVAILABLE`, `IN_TRANSIT`; `changedBy` cũng lấy từ request body.

Hướng sửa bắt buộc:

- `GET`: load Equipment hoặc `InternalEquipmentResponse`, check branch, rồi list history; giữ `inventory.equipment.read`.
- `POST`: không public generic status writer. Khuyến nghị xóa route public hoặc chỉ cho workflow nội bộ ghi history. Nếu vẫn giữ route quản trị, bắt buộc gán `inventory.equipment.change-status`, check scope Equipment, chỉ cho five administrative statuses của Mục 6, và lấy actor từ JWT (`dataScopeGuard.getCurrentUserId()`), không từ `request.changedBy()`.
- Reservation, checkout, transfer, checkin phải tiếp tục là các workflow riêng tự ghi history với trạng thái nghiệp vụ tương ứng.

## Các endpoint Inventory phụ cũng còn thiếu scope

Các endpoint sau không nằm trong chuỗi Mục 7–18 nhưng vẫn là API Inventory và phải được sửa trong cùng đợt hardening.

| Controller / endpoint | Vấn đề | Scope cần áp dụng |
| --- | --- | --- |
| `EquipmentAccessoryController` — toàn bộ 5 endpoint | `equipmentId` thuộc URL, không guard | Resolve Equipment theo `equipmentId`, check org+branch trước mọi read/write. |
| `EquipmentImageController POST` | `equipmentId` nằm trong body, không guard | Resolve Equipment của `request.equipmentId()`, check org+branch trước create. |
| `EquipmentImageController GET /equipment/{equipmentId}` | Không guard | Resolve parent Equipment, check org+branch. |
| `EquipmentImageController DELETE /{id}` | Chỉ có image ID; `EquipmentImage` không chứa org/branch | Load image, resolve parent Equipment, check org+branch trước delete. Cần service query hỗ trợ lookup an toàn. |
| `EquipmentQrController` — create/get/regenerate theo equipment ID | Không guard | Resolve Equipment trước, check org+branch. |
| `EquipmentQrController GET /qr/{qrCode}` | Không thể scope từ QR string; service hiện chỉ là stub trả string | Triển khai lookup QR → Equipment, check org+branch, rồi mới trả QR/Equipment. Không mở endpoint trước khi service thật tồn tại. |
| `EquipmentTransactionController GET /equipment/{id}/transactions` | Có `inventory.equipment.read`, không scope | Resolve Equipment, check org+branch trước history. |
| `HealthController` | Không có `@PreAuthorize` | Không phải business Inventory. Chỉ giữ public nếu gateway/security config chủ đích expose health; nếu không, giới hạn ở hạ tầng. |

## Authority/seed reconciliation (Mục 19–20, không sửa trong đợt code scope)

Các authority đang dùng ở Inventory đều tồn tại trong `permission-seed.csv`, trừ `inventory.reservation.read`.

| Hành động ở Mục 19–20 | Nội dung cần thực hiện |
| --- | --- |
| Permission seed | Thêm `inventory.reservation.read,Read reservations,inventory,Xem giữ chỗ thiết bị`. |
| ADMIN role | Gán `ADMIN,inventory.reservation.read,SYSTEM`. |
| OPERATIONS_STAFF role | Gán `OPERATIONS_STAFF,inventory.reservation.read,BRANCH`. |
| MANAGER role | Chỉ gán nếu policy cho Manager đọc reservation; nếu có, dùng `BRANCH`. |
| Tests/initializer | Seed initializer đã đọc CSV và validate permission tồn tại; sau khi đổi, cập nhật test seed tương ứng. Không tự ý thêm authority mới cho StockIn/Out/Transfer/Audit vì permission hiện có đã phủ các action đó. |

## Thứ tự triển khai và kiểm thử

1. Hoàn tất Warehouse trước để có helper resolve warehouse + branch đáng tin cậy.
2. Làm StockIn, StockOut, StockTransfer, StockAudit theo thứ tự; dùng response/service lookup cho endpoint ID-only.
3. Làm Reservation Query và Internal Reservation; đồng thời đổi list query để bắt buộc organization, không query toàn hệ thống.
4. Làm Availability, Internal Equipment Query, Checkout, Checkin, rồi khóa Status History để không còn bypass status workflow.
5. Hardening các endpoint phụ Equipment (accessory, image, QR, transaction).
6. Chỉ sau code scope, cập nhật Mục 19–20 seed theo bảng reconciliation.
7. Mục 21: Rental → Inventory phải forward JWT. Nếu không forward token, Inventory không có `CurrentUser` để `InventoryDataScopeGuard` kiểm tra; không được thay bằng trust header tự do.

Tối thiểu phải có test controller/service cho từng pattern:

- token đúng authority nhưng sai organization → `403`;
- token BRANCH đúng organization nhưng sai branch → `403`;
- ID thuộc branch khác → `403` (không trả dữ liệu/không thay đổi status);
- list thiếu `organizationId` → `400`;
- transfer có destination branch ngoài scope → `403`;
- `PATCH /equipment/{id}/status` và Status History public không thể đặt `RESERVED`, `CHECKED_OUT`, `AVAILABLE`, `IN_TRANSIT`;
- happy path đúng organization/branch → `2xx` và workflow business giữ nguyên.

Sau khi thực hiện, chạy:

```bash
mvn -pl services/inventory-service -am test
```

Sau đó test tích hợp qua gateway với JWT thực tế cho `401`, authority thiếu (`403`), scope sai (`403`) và happy path. Không đánh dấu hoàn thành chỉ bằng compilation.
