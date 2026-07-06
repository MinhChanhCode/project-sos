# 6. Chuc nang

## 6.1 Dang nhap

### Muc dich

Cho phep nhan vien, bep, quan ly va admin truy cap giao dien noi bo.

### Thanh phan

- Frontend: `pages/login.vue`, `stores/auth.ts`, `api-service/AuthApi.ts`.
- Backend: `AuthenticationController`, `AuthenticateService`, `SecurityConfig`, `User`, `Role`.

### Luong xu ly

1. Nguoi dung nhap username/password.
2. Frontend goi `POST /auth/login`.
3. Backend tim user theo username.
4. Backend so sanh mat khau bang BCrypt.
5. Backend tao JWT HS512 co claim role/scope.
6. Frontend luu token va thong tin user.
7. Middleware auth dieu huong theo role.

### Dau ra

- Token JWT.
- User detail va role.
- Quyen truy cap admin/staff/kitchen.

## 6.2 Dang xuat

Frontend xoa token/local state. Backend khong co blacklist token; JWT ton tai den luc het han. Diem co the cai thien: refresh token va logout server-side.

## 6.3 Quan ly menu

### Chuc nang

- Xem danh sach mon.
- Tim kiem va phan trang.
- Tao/sua/xoa mon.
- Upload anh.
- Bat/tat mon con ban.
- Bat/tat mon active.
- Tao/xoa khuyen mai.
- Xem mon ban chay va khuyen mai.
- Dong bo metadata menu sang AI.

### Thanh phan

- Frontend: `MenuManager.vue`, `AddItemModal.vue`, `EditItemModal.vue`, `MenuApi.ts`, `useAdmin.ts`.
- Backend: `MenuItemController`, `MenuItemService`, `IMenuItemRepository`, `IMenuItemMapper`.
- DB: `menu_items`, `categories`.
- AI sync: `MenuAiSyncService`.

### Diem dang chu y

MenuItem co metadata AI:

- `type`
- `tasteTags`
- `spicyLevel`
- `ingredients`
- `allergens`
- `suitableFor`
- `pairing`
- `isVegetarian`
- `prepTimeMinutes`

Day la du lieu quan trong de chatbot tu van mon an theo khau vi.

## 6.4 Quan ly danh muc

Cho phep tao/sua/xoa/list danh muc mon an. Category duoc frontend dung de loc menu va backend dung foreign key trong `menu_items`.

## 6.5 Quan ly ban va floor plan

### Chuc nang

- List ban.
- Xem chi tiet ban.
- Tao/sua/xoa ban.
- Cap nhat vi tri `posX`, `posY` tren so do.
- Cap nhat trang thai ban.
- Don ban/clear table.

### Trang thai ban

- `EMPTY`: ban trong.
- `SERVING`: dang phuc vu.
- `WAITING_PAYMENT`: cho thanh toan.
- `NEEDS_CLEANING`: can don ban.
- `RESERVED`: da dat truoc.

### Thanh phan

- Frontend: `FloorPlanManager.vue`, `FloorPlanTable.vue`, `TablesGrid.vue`, `TableDetailModal.vue`.
- Backend: `TableController`, `TableQueryService`, `TableManagementService`.
- DB: `tables`, `areas`.

## 6.6 QR Code

### Muc dich

Moi ban co QR token de khach truy cap dung ban.

### Luong

1. Admin tao QR cho ban.
2. Backend sinh token va URL.
3. Frontend hien QR.
4. Khach quet QR.
5. Frontend/Backend resolve token de lay thong tin ban.

### Thanh phan

- Frontend: `QRManager.vue`, `qrcode.vue`.
- Backend: `QrCodeController`, `QrCodeService`.
- DB: `qr_codes`.

## 6.7 Dat mon

### Luong khach hang

1. Khach vao trang customer bang QR/table.
2. Frontend goi danh sach menu active.
3. Khach loc danh muc/tim kiem.
4. Khach them mon vao gio.
5. Backend tao hoac mo cart theo table/session.
6. Cart item duoc cap nhat realtime cho nhung client lien quan.
7. Khach confirm order.
8. Backend tao order va order items.
9. Bep/nhan vien nhan thong bao realtime.

### Thanh phan

- Frontend: `customer.vue`, `useCustomer.ts`, `CartModal.vue`, `MenuItem.vue`.
- Backend: `CartController`, `CartService`, `OrderController`, `OrderWorkflowService`.
- DB: `carts`, `cart_items`, `orders`, `order_items`.
- Realtime: `/topic/tables/{tableId}/cart`, `/topic/tables/{tableId}/ordered`, `/topic/kitchen/orders`.

## 6.8 Bep

### Chuc nang

- Xem mon dang cho xu ly.
- Chuyen trang thai PENDING -> PREPARING -> COMPLETED.
- Danh dau het mon.
- Nhan realtime order moi.

### Thanh phan

- Frontend: `pages/kitchen.vue`.
- Backend: `OrderItemController`, `OrderItemService`, `RealtimeService`.

## 6.9 Nhan vien phuc vu

### Chuc nang

- Xem so do ban.
- Xem chi tiet ban/order.
- Cap nhat mon da phuc vu.
- Xu ly yeu cau phuc vu.
- Chat voi khach.
- Tao/xac nhan hoa don.
- Clear ban.

### Thanh phan

- Frontend: `staff.vue`, `TablesGrid.vue`, `TableDetailModal.vue`, `ServiceRequestManager.vue`, `ChatInbox.vue`.
- Backend: `TableController`, `OrderItemController`, `InvoiceController`, `ServiceRequestController`, `StaffChatController`.

## 6.10 Thanh toan

### Luong

1. Khach yeu cau thanh toan hoac nhan vien tao hoa don.
2. Backend tinh subtotal, tax, discount, total.
3. Hoa don status PENDING.
4. Nhan vien xac nhan payment.
5. Hoa don status PAID, `paidAt` duoc cap nhat.
6. Realtime publish `/topic/payment`.
7. Ban co the chuyen sang can don/empty theo workflow.

### Thanh phan

- Frontend: `InvoicesSection.vue`, `TableDetailModal.vue`, `CartModal.vue`.
- Backend: `InvoiceController`, `InvoicePaymentService`.
- DB: `invoices`, `payments`.

## 6.11 Dashboard

Admin/manager xem thong ke tong quan:

- Doanh thu.
- Hoa don.
- Don hang.
- Review.
- Sentiment summary.
- Trang thai ban/menu.

Thanh phan:

- Frontend: `DashboardStats.vue`, `ChartsSection.vue`, `useAdmin.ts`.
- Backend: `DashboardController`, `DashboardService`.

## 6.12 Danh gia va AI sentiment

### Luong

1. Khach gui rating/comment.
2. Backend luu `reviews`.
3. Backend goi `sos-ai /sentiment` neu co `AI_SERVICE_URL`.
4. Neu AI loi, backend fallback keyword.
5. Luu `sentiment_results`.
6. Publish `/topic/reviews`.

### Sentiment

- POSITIVE
- NEUTRAL
- NEGATIVE

## 6.13 Chatbot AI

### Chuc nang

- Tu van mon an theo ngan sach, khau vi, do cay, an chay, di ung, combo.
- Dung OpenAI neu co `OPENAI_API_KEY`.
- Fallback local token RAG neu khong co key.
- Luu lich su chat trong `chat_histories`.

### Thanh phan

- Frontend: `Chatbot.vue`, `chatApi`.
- Backend: `ChatController`, `ChatService`.
- AI: `sos-ai/main.py`.

## 6.14 Staff chat

Cho phep khach va nhan vien chat theo ban:

- API luu `staff_chat_messages`.
- Realtime publish `/topic/tables/{tableId}/staff-chat` va `/topic/staff/chat`.

## 6.15 Service request

Khach goi nhan vien:

- Loai yeu cau: GENERAL_SERVICE, FOOD_REQUEST, CLEANING, PAYMENT, OTHER.
- Priority: LOW, MEDIUM, HIGH, URGENT.
- Status: PENDING, IN_PROGRESS, DONE, CANCELLED.
- Realtime topic `/topic/service-requests`.

## 6.16 Quan ly nhan vien

Admin/manager:

- Tao/sua/xoa nhan vien.
- Tao ca lam.
- Phan cong nhan vien vao khu vuc/ca.

DB: `employees`, `staff_shifts`, `assignments`.

## 6.17 Upload/xem anh

- Upload anh menu qua `/api/v1/images/upload`.
- Xem anh qua `/api/v1/images/view/{fileName}`.
- Frontend co fallback image trong `utils/foodImages.ts`.

