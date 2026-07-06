# 3. Kien truc source code

## Cau truc root

```text
D:\ProjectSOS
|-- sos-api      Backend Spring Boot
|-- sos-fe       Frontend Nuxt/Vue
|-- sos-ai       FastAPI AI service
|-- docs/report  Tai lieu ky thuat duoc sinh cho bao cao
|-- DEPLOYMENT.md
|-- AI_UPGRADES_SUMMARY.md
|-- run-restaurant-lan.ps1
|-- run-public-qr-mode.ps1
```

## Backend `sos-api`

### `controller`

Tang controller chiu trach nhiem nhan HTTP request va tra response:

- `AuthenticationController`: login, introspect, register.
- `MenuItemController`: menu item CRUD, active/available/search/category, promotion, best seller.
- `CategoryController`: CRUD danh muc.
- `TableController`: danh sach ban, chi tiet ban, tao/sua/xoa, vi tri, trang thai, clear ban.
- `CartController`: tao/mo cart, them/sua/xoa mon trong gio, confirm order qua `CartApi`.
- `OrderController`: confirm order, list order, get detail, cancel.
- `OrderItemController`: trang thai mon, batch update, add item vao order, merge duplicate.
- `InvoiceController`: danh sach hoa don, tao hoa don, thanh toan, PDF.
- `QrCodeController`: sinh va resolve QR token.
- `ReviewController`: danh gia va sentiment summary.
- `ChatController`: chatbot va history.
- `AiSyncController`: dong bo menu sang AI.
- `ServiceRequestController`: yeu cau phuc vu.
- `StaffChatController`: chat giua khach va nhan vien.
- `DashboardController`: dashboard admin/manager.
- `AreaController`: khu vuc nha hang.
- `EmployeeController`: nhan vien, ca lam, phan cong.
- `UserController`: user/profile/role.
- `ImageController`: upload/view image.
- `HealthCheckController`: health/readiness/liveness.

### `service`

Tang service chua nghiep vu:

- `AuthenticateService`: dang nhap, tao JWT, introspect token, dang ky user.
- `MenuItemService`: quan ly menu, promotion, best seller, sync AI khi menu thay doi.
- `CategoryService`: CRUD danh muc.
- `TableQueryService`: doc trang thai ban, chi tiet ban, clear table.
- `TableManagementService`: tao/sua/xoa ban, cap nhat vi tri/trang thai.
- `CartService`: cart session, open cart, add/update/remove item.
- `OrderWorkflowService`: chuyen cart thanh order, quan ly workflow order.
- `OrderItemService`: cap nhat trang thai mon theo so luong.
- `InvoicePaymentService`: tao hoa don va xac nhan thanh toan.
- `QrCodeService`: tao token QR va URL.
- `ReviewService`: luu review, goi AI sentiment, fallback keyword.
- `ChatService`: goi AI `/chat`, fallback local menu reply, luu `ChatHistory`.
- `MenuAiSyncService`: gui menu active sang AI `/menu/sync`.
- `DashboardService`: thong ke admin.
- `ServiceRequestService`: tao/xu ly yeu cau phuc vu va publish realtime.
- `StaffChatService`: luu chat va publish topic staff/table.
- `EmployeeShiftService`: quan ly nhan vien, ca, phan cong.
- `UserManagementService`: user CRUD va role assignment.
- `RealtimeService`: lang nghe domain events va publish WebSocket topic.
- Listener/Event classes: `CartRealtimeListener`, `OrderedItemsRealtimeListener`, `OrderItemStatusRealtimeListener`, `TableStatusRealtimeListener`, event payload classes.

### `repository`

Tang repository ke thua Spring Data JPA:

- User/Role: `IUserRepository`, `IRoleRepository`.
- Menu: `IMenuItemRepository`, `ICategoryRepository`.
- Ban/khu vuc: `ITableRepository`, `IAreaRepository`.
- Cart/order: `ICartRepository`, `ICartItemRepository`, `IOrderRepository`, `IOrderItemRepository`, `IOrderStaffRepository`.
- Payment/invoice: `IPaymentRepository`, `IInvoiceRepository`.
- Customer/staff: `ICustomerSessionRepository`, `IStaffChatMessageRepository`, `IEmployeeRepository`, `IStaffShiftRepository`, `IAssignmentRepository`.
- AI/review: `IReviewRepository`, `ISentimentResultRepository`, `IChatHistoryRepository`.
- QR/notification/request: `IQrCodeRepository`, `INotificationRepository`, `IServiceRequestRepository`.

### `model`

Entity JPA va enum:

- Identity/security: `User`, `Role`, `BaseEntity`.
- Restaurant layout: `Area`, `TableEntity`, `TableStatus`.
- Menu: `Category`, `MenuItem`.
- Ordering: `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderItemStatus`, `OrderStaff`.
- Operations: `Invoice`, `Payment`, `QrCode`, `Notification`, `ServiceRequest`.
- Staff: `Employee`, `StaffShift`, `Assignment`, `StaffChatMessage`.
- AI/feedback: `Review`, `SentimentResult`, `ChatHistory`.

### `dto`

DTO chia theo domain:

- `authenticate`: `LoginRequest`, `LoginResponse`, `IntrospectRequest`, `IntrospectResponse`.
- `cart`: `CartRequest`, `CartItemRequest`, `CartResponse`, `CartItemResponse`.
- `menuitem`: `MenuItemRequest`, `MenuItemResponse`.
- `table`: `TableRequest`, `TableDetailResponse`, `TableListItemResponse`, `TablePositionBatchRequest`, `TableOrderItemSummary`.
- `invoice`, `review`, `chat`, `customer`, `employee`, `servicerequest`, `qr`, `area`, `category`, `dashboard`, `page`, `user`.

### `mapper`

MapStruct interfaces:

- `IMenuItemMapper`
- `ICartMapper`
- `IServiceRequestMapper`
- `IUserMapper`

Dung de tach entity noi bo khoi response public.

### `config`

- `SecurityConfig`: JWT auth, role authorization, public route list.
- `CorsConfig`: CORS origin/pattern cho frontend.
- `WebSocketConfig`: STOMP broker `/topic`, app prefix `/app`, endpoint `/ws`.
- `AuditConfig`: auditor for created/updated fields.
- `DemoDataInitializer`: seed data demo.

### `resources/db/migration`

Flyway migrations V1.0.1 -> V1.0.15 tao schema va seed du lieu menu.

## Frontend `sos-fe`

### `pages`

- `index.vue`: home/landing dieu huong.
- `login.vue`: dang nhap nhan vien/admin.
- `customer.vue`: giao dien khach hang dat mon.
- `customer/table/[tableNumber].vue`: route khach theo table number.
- `staff.vue`: man hinh nhan vien phuc vu.
- `kitchen.vue`: man hinh bep.
- `admin.vue`: man hinh quan tri.

### `components`

Admin:

- `DashboardStats`, `ChartsSection`, `MenuManager`, `AddItemModal`, `EditItemModal`.
- `FloorPlanManager`, `QRManager`, `StaffManager`, `InvoicesSection`, `ReviewsSection`.

Customer:

- `Header`, `MenuItem`, `CartModal`, `Chatbot`, `StaffChat`, `ServiceRequestButton`, `SuggestionsSection`.

Staff:

- `TablesGrid`, `TableDetailModal`, `OrderItemStatusManager`, `ServiceRequestManager`, `ChatInbox`, `NotificationsModal`, `QuickActions`, `StatsCards`.

Shared/UI:

- `FloorPlanTable`, `Button`, `Card`, `Input`, `Tabs`, `Badge`, `ConfirmModal`, `Textarea`, `Switch`, `Label`.

Home:

- `HeroSection`, `FeaturesSection`, `InterfaceCard`, `HomeHeader`, `Footer`.

### `composables`

- `useCustomer`: toan bo luong khach hang: menu, cart, session, order, rating, realtime.
- `useStaff`: luong nhan vien: ban, order items, realtime, payment.
- `useAdmin`: dashboard, menu, review, invoice, staff.
- `useHome`: home state.
- `useConfirm`: confirm modal helper.

### `stores`

- `auth`: JWT/user/roles.
- `cart`: cart va ordered items.
- `menu`: menu, categories, suggestions.
- `staff`: staff state.
- `theme`: dark/light theme.

### `api-service`

- `BaseApi`, `BaseApiFetch`: wrapper `useFetch`/`$fetch` voi baseURL va Bearer token.
- `AuthApi`: login/me.
- `MenuApi`: menu CRUD, promotions, best sellers, image upload.
- `CartApi`: cart/session/open/order confirm.
- `TableApi`: table list/detail/update/clear.
- `OrderItemApi`: status and kitchen/staff operations.
- `ServiceRequestApi`: service request CRUD/status.
- `ExtendedApi`: dashboard, area, category, employee, invoice, review, customer session, staff chat, chat, QR.

### `plugins`

- `realtime.client.ts`: STOMP/SockJS connection manager.
- `theme.client.ts`: init theme.
- `vue3-toastify.client.ts`: toast plugin.

## AI `sos-ai`

`main.py` gom tat ca:

- FastAPI app.
- CORS middleware.
- Pydantic request/response models.
- In-memory `MENU_ITEMS`.
- Token normalize/tokenize.
- `rebuild_search_index`.
- `rag_search`, `build_combo`, `build_rag_reply`.
- `call_openai`.
- `/health`, `/chat`, `/sentiment`, `/menu/sync`, `/embeddings/rebuild`.

AI service khong tach routers/services rieng; dieu nay don gian cho demo nhung co the refactor ve sau.

