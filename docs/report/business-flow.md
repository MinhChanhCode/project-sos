# 7. Luong nghiep vu

## Luong tong the cua khach hang

```text
Khach vao nha hang
  -> Quet QR tren ban
  -> Mo trang customer cua dung ban
  -> He thong resolve table/token
  -> Tao/mo cart theo table
  -> Khach nhap ten
  -> Xem menu active
  -> Loc danh muc/tim kiem
  -> Them mon vao gio
  -> Xac nhan dat mon
  -> Bep nhan realtime
  -> Bep che bien
  -> Nhan vien phuc vu
  -> Khach yeu cau thanh toan
  -> Nhan vien tao/xac nhan hoa don
  -> Khach danh gia
  -> AI phan tich cam xuc
```

## Luong QR

1. Admin mo `QRManager`.
2. Chon ban va bam tao QR.
3. Backend `QrCodeService.generate(tableId)` tao `qrToken` va URL.
4. Token duoc luu trong `qr_codes`.
5. Khach quet QR.
6. Frontend goi `/api/v1/qr-codes/token/{token}`.
7. Backend tra ve table/session URL.
8. Frontend dieu huong den customer page.

## Luong cart va dat mon

```text
Customer page mounted
  -> fetch menu active
  -> fetch categories
  -> ensureCart
      -> POST /api/v1/carts/table/{tableId}/open
      -> neu cart active ton tai: tra ve
      -> neu chua co: tao cart moi
  -> hydrateCustomerName
  -> subscribe realtime topics
```

Khi them mon:

```text
Khach bam +
  -> UI optimistic update
  -> POST /api/v1/carts/session/{sessionId}/items
  -> Backend add/update cart_item
  -> Publish CartUpdatedEvent
  -> /topic/tables/{tableId}/cart
  -> Cac client cung ban sync cart
```

Khi confirm:

```text
Khach bam Dat mon
  -> POST /api/v1/orders/session/{sessionId}/confirm
  -> Backend tao order neu can
  -> Convert cart_items -> order_items pending_quantity
  -> Clear/local cart
  -> Publish ordered/order events
  -> Kitchen/staff nhan realtime
```

## Luong bep

```text
Bep mo kitchen page
  -> GET /api/v1/order-items/pending-for-management
  -> Subscribe /topic/kitchen/orders
  -> Subscribe /topic/management/order-items
  -> Nhan mon moi
  -> Cap nhat PREPARING
  -> Cap nhat COMPLETED
  -> Frontend staff/customer duoc sync realtime
```

## Luong nhan vien phuc vu

```text
Staff mo staff page
  -> GET /api/v1/tables
  -> Lay detail cua ban
  -> Subscribe management/table topics
  -> Khi mon completed, staff phuc vu
  -> PATCH /api/v1/order-items/{id}/status/quick?status=SERVED
  -> Neu khach yeu cau thanh toan, tao invoice
  -> POST /api/v1/invoices/order/{orderId}
  -> Xac nhan thanh toan
  -> POST /api/v1/invoices/payment/confirm
  -> Clear table
```

## Luong service request

```text
Khach bam goi nhan vien
  -> POST /api/v1/service-requests
  -> Backend tao ServiceRequest PENDING
  -> Publish /topic/service-requests
  -> Staff UI hien request
  -> Staff assign hoac update status
  -> Backend publish cap nhat
  -> Customer/staff sync trang thai
```

## Luong staff chat

```text
Khach gui tin nhan
  -> POST /api/v1/staff-chat
  -> Luu staff_chat_messages
  -> Publish /topic/tables/{tableId}/staff-chat
  -> Publish /topic/staff/chat
  -> Staff inbox nhan tin
```

## Luong chatbot AI

```text
Khach mo Chatbot.vue
  -> Nhap cau hoi
  -> POST /api/v1/chat
  -> ChatService goi sos-ai /chat neu AI_SERVICE_URL co cau hinh
  -> sos-ai neu co OPENAI_API_KEY: goi OpenAI
  -> neu khong co key/loi: local token RAG
  -> Backend luu chat_histories
  -> Frontend hien reply
```

## Luong dong bo menu sang AI

```text
Admin tao/sua/xoa mon
  -> MenuItemService luu DB
  -> menuAiSyncService.syncMenuSafely()
  -> POST sos-ai /menu/sync
  -> sos-ai cap nhat MENU_ITEMS va SEARCH_INDEX
```

Ngoai ra admin/manager co the goi:

```text
POST /api/v1/ai/menu/sync
```

## Luong danh gia va sentiment

```text
Khach gui danh gia
  -> POST /api/v1/reviews
  -> ReviewService luu review
  -> Goi sos-ai /sentiment neu AI_SERVICE_URL co cau hinh
  -> Neu loi: keyword sentiment fallback
  -> Luu sentiment_results
  -> Publish /topic/reviews
  -> Admin dashboard cap nhat
```

## Luong quan tri

```text
Admin dang nhap
  -> JWT role ADMIN/MANAGER
  -> Mo admin.vue
  -> Fetch dashboard/reviews/invoices/menu/staff
  -> Quan ly menu, QR, floor plan, nhan vien
  -> Cac thay doi publish realtime topic
```

