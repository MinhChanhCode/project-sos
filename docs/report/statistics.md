# Source Statistics

## Tong quan thong ke

| Hang muc | So luong | Ghi chu |
|---|---:|---|
| Backend controller | 21 | Cac lop trong `sos-api/src/main/java/.../controller` |
| Backend service | 37 | Bao gom service nghiep vu, AI, WebSocket, invoice, order, menu |
| Backend repository | 24 | Spring Data JPA repository |
| Backend model/entity/enum | 27 | Bao gom entity va enum trong package `model` |
| Backend DTO | 49 | Request/response DTO |
| API mapping | 115 | Dem theo annotation mapping trong controller |
| Migration | 15 | `V1.0.1` den `V1.0.15` |
| Frontend page | 7 | Nuxt pages |
| Frontend component | 50 | Vue components |
| Frontend store | 5 | Pinia stores |
| Frontend API service | 10 | Lop service goi backend |
| Menu image asset | 35 | Anh trong `sos-fe/public/images/menu` |

## Backend statistics

### Controller

Project co 21 controller chinh:

| Controller | Vai tro |
|---|---|
| `AuthController` | Dang nhap, introspect token, dang ky user noi bo |
| `HealthController` | Health check, readiness, liveness |
| `AiController` | Dong bo menu sang AI |
| `AreaController` | Quan ly khu vuc nha hang |
| `CategoryController` | Quan ly danh muc mon |
| `MenuItemController` | Quan ly mon, mon dang ban, promotion, best seller |
| `CartController` | Gio hang theo session/table |
| `OrderController` | Tao va xem don hang |
| `OrderItemController` | Quan ly trang thai tung mon |
| `InvoiceController` | Tao/xem hoa don, xac nhan thanh toan, PDF |
| `QrCodeController` | Sinh va tra cuu QR table token |
| `ReviewController` | Danh gia va tong hop sentiment |
| `ChatController` | Chatbot AI va lich su chat |
| `CustomerSessionController` | Session khach theo ban |
| `StaffChatController` | Chat giua khach va nhan vien |
| `ServiceRequestController` | Goi nhan vien, yeu cau ho tro |
| `DashboardController` | Du lieu tong quan cho dashboard |
| `EmployeeController` | Nhan vien, ca lam, phan cong |
| `UserController` | Tai khoan va profile |
| `ImageController` | Upload va view anh |
| Cac controller phu tro khac | Neu co trong source, phuc vu cac endpoint lien quan module |

### Service

Project co 37 service, gom cac nhom:

| Nhom service | Noi dung |
|---|---|
| Authentication/Security | Xac thuc user, sinh JWT, introspect token |
| Menu/Category | Quan ly danh muc, mon, trang thai ban, promotion |
| Cart/Order | Gio hang, tao don, cap nhat so luong va trang thai |
| Kitchen/Staff | Dieu phoi mon, topic realtime, phuc vu |
| Invoice/Payment | Hoa don, xac nhan thanh toan, PDF |
| Table/Area/QR | So do ban, khu vuc, ma QR |
| AI/Chat/Sentiment | Chatbot, lich su chat, phan tich danh gia, dong bo menu |
| Notification/WebSocket | Gui topic realtime den frontend |
| Employee/Shift/Assignment | Nhan su, ca lam, phan cong |

### Repository

Project co 24 repository. Moi repository chiu trach nhiem truy van DB cho mot entity hoac mot nhom nghiep vu. Cac repository quan trong:

| Repository | Du lieu |
|---|---|
| `UserRepository` | Tai khoan |
| `RoleRepository` | Vai tro |
| `MenuItemRepository` | Mon an/do uong |
| `CategoryRepository` | Danh muc |
| `TableRepository` | Ban |
| `AreaRepository` | Khu vuc |
| `CartRepository` | Gio hang |
| `CartItemRepository` | Chi tiet gio hang |
| `OrderRepository` | Don hang |
| `OrderItemRepository` | Chi tiet mon trong don |
| `InvoiceRepository` | Hoa don |
| `PaymentRepository` | Thanh toan |
| `ReviewRepository` | Danh gia |
| `SentimentResultRepository` | Ket qua AI sentiment |
| `QrCodeRepository` | QR table |
| `CustomerSessionRepository` | Session khach |
| `ChatHistoryRepository` | Lich su chatbot |
| `StaffChatMessageRepository` | Tin nhan khach-nhan vien |
| `ServiceRequestRepository` | Yeu cau ho tro |
| `EmployeeRepository` | Nhan vien |
| `StaffShiftRepository` | Ca lam |
| `AssignmentRepository` | Phan cong |

## Frontend statistics

### Pages

| Page | Chuc nang |
|---|---|
| `sos-fe/app/pages/index.vue` | Trang chu / dieu huong vao he thong |
| `sos-fe/app/pages/login.vue` | Dang nhap nhan vien/quan ly/admin |
| `sos-fe/app/pages/customer.vue` | Luong khach hang tong quan |
| `sos-fe/app/pages/customer/table/[tableNumber].vue` | Man hinh khach theo ban QR |
| `sos-fe/app/pages/staff.vue` | Giao dien nhan vien phuc vu |
| `sos-fe/app/pages/kitchen.vue` | Giao dien bep |
| `sos-fe/app/pages/admin.vue` | Giao dien quan tri/quan ly |

### Components

Project co 50 Vue component, chia thanh cac nhom:

| Nhom | So luong uoc tinh | Vi du |
|---|---:|---|
| Common | 2 | `AppHeader`, `ThemeToggle` |
| Home | 5 | `HeroSection`, `FeaturesSection`, `Footer` |
| Customer | 7 | `Chatbot`, `CartModal`, `MenuItem`, `StaffChat` |
| Staff | 7 | `TablesGrid`, `TableDetailModal`, `ServiceRequestManager` |
| Admin | 13 | `MenuManager`, `QRManager`, `StaffManager`, `ReviewsSection` |
| Shared | 1 | `FloorPlanTable` |
| UI primitives | 15 | `Button`, `Card`, `Input`, `Tabs`, `Switch`, `ConfirmModal` |

### Stores

| Store | Chuc nang |
|---|---|
| `auth` | Token, user, login/logout |
| `cart` | Gio hang, them/xoa/cap nhat mon |
| `menu` | Danh sach menu, category, trang thai mon |
| `staff` | Du lieu staff, ban, order, service request |
| `theme` | Giao dien sang/toi |

### API service files

| File | Vai tro |
|---|---|
| `AuthApi` | Login, introspect, user |
| `BaseApi` | Cau hinh request chung |
| `BaseApiFetch` | Fetch wrapper |
| `CartApi` | Gio hang |
| `ExtendedApi` | API bo sung |
| `MenuApi` | Menu/category |
| `OrderItemApi` | Trang thai mon |
| `ServiceRequestApi` | Goi nhan vien |
| `TableApi` | Ban/QR/table |
| `index` | Export service |

## API statistics

Tong so endpoint mapping trong backend la 115. Nhom endpoint lon nhat:

| Module | Endpoint tieu bieu | Vai tro |
|---|---|---|
| Menu | `/api/v1/menu-items/**` | Danh sach mon, mon dang ban, promotion |
| Cart | `/api/v1/carts/**` | Gio hang theo session/table |
| Order item | `/api/v1/order-items/**` | Trang thai tung mon va batch update |
| Service request | `/api/v1/service-requests/**` | Goi nhan vien, xu ly yeu cau |
| Employee | `/api/v1/employees`, `/api/v1/shifts`, `/api/v1/assignments` | Nhan su |
| Invoice | `/api/v1/invoices/**` | Hoa don, thanh toan |
| Review | `/api/v1/reviews/**` | Danh gia va sentiment |

## Database statistics

| Hang muc | So luong |
|---|---:|
| Migration file | 15 |
| Entity/enum backend | 27 |
| Bang nghiep vu chinh | Khoang 24+ |
| Bang quan trong | `users`, `roles`, `tables`, `areas`, `categories`, `menu_items`, `orders`, `order_items`, `carts`, `cart_items`, `customer_sessions`, `invoices`, `payments`, `reviews`, `sentiment_results`, `service_requests`, `staff_chat_messages`, `chat_histories` |

## Asset statistics

| Hang muc | So luong |
|---|---:|
| Anh menu | 35 |
| Anh lon can toi uu | 1 ro rang |
| File anh lon nhat | `yaourt-da.jpg` khoang 5.57 MB |
| File anh lon tiep theo | `tra-tac-xi-muoi.png` khoang 664 KB, `pho-bo-tai.png` khoang 411 KB |

Nhan xet: Anh `yaourt-da.jpg` qua lon so voi nhu cau hien thi menu web. Nen nen anh, resize ve kich thuoc hien thi thuc te va doi sang WebP/AVIF de giam thoi gian load.
