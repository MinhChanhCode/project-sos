# 5. API

## Quy uoc response

Backend thuong tra ve wrapper:

```json
{
  "success": true,
  "message": "Thong diep",
  "data": {}
}
```

Mot so endpoint auth tra truc tiep DTO tu service.

## Authentication

| Method | URL | Request | Response | Muc dich | Role |
|---|---|---|---|---|---|
| POST | `/auth/login` | `LoginRequest {username,password}` | `LoginResponse {token,...}` | Dang nhap lay JWT | Public |
| POST | `/auth/introspect` | `IntrospectRequest {token}` | `IntrospectResponse` | Kiem tra token | Public |
| POST | `/auth/register` | `UserRequest` | User/auth response | Tao tai khoan | ADMIN |

## Health

| Method | URL | Muc dich | Role |
|---|---|---|---|
| GET | `/health` | Health chung | Public |
| GET | `/health/ping` | Ping | Public |
| GET | `/health/readiness` | Readiness | Public |
| GET | `/health/liveness` | Liveness | Public |
| GET | `/health/test-logging` | Test log | Public |

## Menu items

| Method | URL | Request/Param | Response | Muc dich | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/menu-items` | `name`, `Pageable` | Page menu | Tim/list menu admin | Authenticated theo rule fallback, hoac manager/admin tuy method |
| GET | `/api/v1/menu-items/active` | `Pageable` | Page active menu | Khach xem menu active | Public |
| GET | `/api/v1/menu-items/available` | `Pageable` | Page available | Khach xem mon con ban | Public |
| GET | `/api/v1/menu-items/category/{categoryId}` | category + pageable | Page menu | Loc danh muc | Public |
| GET | `/api/v1/menu-items/search` | `keyword,page,size` | Page menu | Tim kiem mon | Public |
| GET | `/api/v1/menu-items/{id}` | id | MenuItemResponse | Chi tiet mon | Public |
| POST | `/api/v1/menu-items` | MenuItemRequest | MenuItemResponse | Tao mon | ADMIN, MANAGER |
| PUT | `/api/v1/menu-items/{id}` | MenuItemRequest | MenuItemResponse | Sua mon | ADMIN, MANAGER |
| DELETE | `/api/v1/menu-items/{id}` | id | void | Xoa mon | ADMIN, MANAGER |
| PATCH | `/api/v1/menu-items/{id}/toggle-availability` | id | void | Bat/tat con mon | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/menu-items/{id}/toggle-active` | id | void | An/hien mon | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/menu-items/{id}/out-of-stock` | id | void | Danh dau het mon | ADMIN, MANAGER, STAFF, KITCHEN |
| POST | `/api/v1/menu-items/promotions` | promotion payload | void | Tao/cap nhat khuyen mai | ADMIN, MANAGER |
| DELETE | `/api/v1/menu-items/promotions/{menuItemId}` | id | void | Xoa khuyen mai | ADMIN, MANAGER |
| GET | `/api/v1/menu-items/promotions` | `limit,now` | List | Goi y khuyen mai | Public GET theo menu-items rule |
| GET | `/api/v1/menu-items/best-sellers` | `limit,from` | List | Goi y ban chay | Public GET theo menu-items rule |

## Categories

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/categories` | - | List danh muc | Public |
| GET | `/api/v1/categories/{id}` | - | Chi tiet danh muc | Public |
| POST | `/api/v1/categories` | CategoryRequest | Tao danh muc | ADMIN, MANAGER |
| PUT | `/api/v1/categories/{id}` | CategoryRequest | Sua danh muc | ADMIN, MANAGER |
| DELETE | `/api/v1/categories/{id}` | - | Xoa danh muc | ADMIN, MANAGER |

## Tables

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/tables` | - | List ban | Public |
| GET | `/api/v1/tables/{tableId}/detail` | - | Chi tiet ban/order/session | Public |
| POST | `/api/v1/tables` | TableRequest | Tao ban | ADMIN, MANAGER |
| PUT | `/api/v1/tables/{tableId}` | TableRequest | Sua ban | ADMIN, MANAGER |
| PATCH | `/api/v1/tables/positions` | TablePositionBatchRequest | Cap nhat toa do floor plan | ADMIN, MANAGER, STAFF |
| PATCH | `/api/v1/tables/{tableId}/status` | `status` | Cap nhat trang thai | ADMIN, MANAGER, STAFF |
| PATCH | `/api/v1/tables/{tableId}/clear` | - | Don ban/giai phong ban | ADMIN, MANAGER, STAFF, KITCHEN |
| DELETE | `/api/v1/tables/{tableId}` | - | Xoa ban | ADMIN, MANAGER |

## Cart va order confirm

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| POST | `/api/v1/carts` | CartRequest | Tao cart | Public |
| GET | `/api/v1/carts/session/{sessionId}` | - | Lay cart theo session | Public |
| GET | `/api/v1/carts/session/{sessionId}/items` | Pageable | Lay cart item | Public |
| GET | `/api/v1/carts/table/{tableId}` | - | Lay cart theo ban | Public |
| POST | `/api/v1/carts/table/{tableId}/open` | - | Mo/tao cart idempotent | Public |
| POST | `/api/v1/carts/session/{sessionId}/items` | CartItemRequest | Them mon | Public |
| PUT | `/api/v1/carts/session/{sessionId}/items/{menuItemId}` | CartItemRequest | Sua so luong/ghi chu | Public |
| DELETE | `/api/v1/carts/session/{sessionId}/items/{menuItemId}` | - | Xoa mon | Public |
| DELETE | `/api/v1/carts/session/{sessionId}/clear` | - | Xoa tat ca mon | Public |
| DELETE | `/api/v1/carts/session/{sessionId}` | - | Deactivate cart | Public |
| GET | `/api/v1/carts/table/{tableId}/qr` | - | Link QR thô | Public |
| POST | `/api/v1/orders/session/{sessionId}/confirm` | - | Xac nhan order tu cart | Public |

## Orders

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/orders` | `status?` | List order | Public GET theo SecurityConfig |
| GET | `/api/v1/orders/{orderId}` | - | Chi tiet order | Public GET theo SecurityConfig |
| PATCH | `/api/v1/orders/{orderId}/cancel` | - | Huy order | Authenticated |

## Order items

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/order-items/statuses` | - | List enum trang thai | ADMIN, MANAGER, STAFF, KITCHEN |
| GET | `/api/v1/order-items/order/{orderId}` | - | Mon theo order | ADMIN, MANAGER, STAFF, KITCHEN |
| GET | `/api/v1/order-items/{orderItemId}` | - | Chi tiet mon order | ADMIN, MANAGER, STAFF, KITCHEN |
| GET | `/api/v1/order-items/pending-for-management` | - | Mon can xu ly | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/order-items/{orderItemId}/status` | UpdateOrderItemStatusRequest | Doi status | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/order-items/{orderItemId}/status/quick` | `status` | Doi nhanh status | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/order-items/{orderItemId}/status/partial` | `status,quantity` | Doi status mot phan so luong | ADMIN, MANAGER, STAFF, KITCHEN |
| PATCH | `/api/v1/order-items/batch/status/quick` | `orderItemIds,status` | Batch update | ADMIN, MANAGER, STAFF, KITCHEN |
| POST | `/api/v1/order-items/order/{orderId}/add-item` | `menuItemId,quantity,notes` | Them mon vao order | ADMIN, MANAGER, STAFF, KITCHEN |
| POST | `/api/v1/order-items/order/{orderId}/merge-duplicates` | - | Gop dong trung | ADMIN, MANAGER, STAFF, KITCHEN |

## Invoice/payment

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/invoices` | - | List hoa don | ADMIN, MANAGER, STAFF |
| GET | `/api/v1/invoices/order/{orderId}` | - | Hoa don theo order | ADMIN, MANAGER, STAFF |
| POST | `/api/v1/invoices/order/{orderId}` | - | Tao hoa don | ADMIN, MANAGER, STAFF |
| POST | `/api/v1/invoices/public/session/{sessionId}` | - | Khach yeu cau hoa don theo session | Public |
| POST | `/api/v1/invoices/payment/confirm` | PaymentConfirmRequest | Xac nhan thanh toan | ADMIN, MANAGER, STAFF |
| GET | `/api/v1/invoices/order/{orderId}/pdf` | - | PDF hoa don | ADMIN, MANAGER, STAFF |

## QR

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/qr-codes/table/{tableId}` | - | List QR cua ban | ADMIN, MANAGER |
| POST | `/api/v1/qr-codes/table/{tableId}/generate` | - | Sinh QR | ADMIN, MANAGER |
| GET | `/api/v1/qr-codes/token/{token}` | - | Resolve QR token | Public |

## Review va sentiment

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| GET | `/api/v1/reviews` | - | List review | ADMIN, MANAGER |
| POST | `/api/v1/reviews` | ReviewRequest | Khach gui danh gia | Public |
| GET | `/api/v1/reviews/sentiment-summary` | - | Thong ke sentiment | ADMIN, MANAGER |

## Chatbot AI

| Method | URL | Request | Response | Muc dich | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/chat` | `{sessionId,message}` | `{sessionId,reply,historyId}` | Chat voi AI | Public |
| GET | `/api/v1/chat/history/{sessionId}` | - | List ChatHistory | Lich su chat | Public |
| POST | `/api/v1/ai/menu/sync` | - | Sync result | Dong bo menu sang AI | ADMIN, MANAGER |

## Customer session va staff chat

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| POST | `/api/v1/customer-sessions` | CustomerSessionRequest | Luu ten khach/session | Public |
| GET | `/api/v1/customer-sessions/{sessionId}` | - | Lay session | Public |
| POST | `/api/v1/staff-chat` | StaffChatRequest | Gui tin nhan khach/nhan vien | Public |
| GET | `/api/v1/staff-chat/table/{tableId}` | - | Lich su chat theo ban | Public |

## Service requests

| Method | URL | Request | Muc dich | Role |
|---|---|---|---|---|
| POST | `/api/v1/service-requests` | ServiceRequestRequest | Tao yeu cau phuc vu | Public |
| GET | `/api/v1/service-requests/{id}` | - | Chi tiet yeu cau | Public theo config |
| GET | `/api/v1/service-requests` | Pageable | Tat ca yeu cau | Public theo config |
| GET | `/api/v1/service-requests/status/{status}` | Pageable | Loc status | Public theo config |
| GET | `/api/v1/service-requests/table/{tableId}` | - | Yeu cau theo ban | Public |
| GET | `/api/v1/service-requests/session/{sessionId}` | - | Yeu cau theo session | Public |
| GET | `/api/v1/service-requests/pending` | - | Yeu cau dang cho | Public theo config |
| PATCH | `/api/v1/service-requests/{id}/status` | `status,assignedTo?` | Cap nhat trang thai | Public theo config |
| PATCH | `/api/v1/service-requests/{id}/assign` | `assignedTo` | Gan nhan vien | Public theo config |
| DELETE | `/api/v1/service-requests/{id}` | - | Xoa yeu cau | Public theo config |
| GET | `/api/v1/service-requests/table/{tableId}/pending-count` | - | Dem pending | Public |

Ghi chu: SecurityConfig hien permit `/api/v1/service-requests/**`; neu bao cao can noi ve security nghiem ngat, day la diem can cai thien.

## Admin/management

| Method | URL | Muc dich | Role |
|---|---|---|---|
| GET | `/api/v1/dashboard` | Tong hop dashboard | ADMIN, MANAGER |
| CRUD | `/api/v1/areas/**` | Khu vuc | ADMIN, MANAGER |
| CRUD | `/api/v1/employees/**` | Nhan vien | ADMIN, MANAGER |
| GET/POST | `/api/v1/shifts` | Ca lam | ADMIN, MANAGER |
| GET/POST | `/api/v1/assignments` | Phan cong | ADMIN, MANAGER |
| CRUD | `/api/v1/users/**` | User/role | ADMIN |

## Image

| Method | URL | Muc dich | Role |
|---|---|---|---|
| POST | `/api/v1/images/upload` | Upload anh menu | Authenticated/Admin flow |
| GET | `/api/v1/images/view/{fileName}` | Xem anh | Public |

