# 1. Tong quan project

## Ten project

Ten ky thuat trong repository: **ProjectSOS**.

Ten ung dung hien thi tren frontend: **Goi Mon - He thong dat mon thong minh**.

Chu de co the su dung trong bao cao do an:

> Xay dung he thong dat mon nha hang truc tuyen tich hop chatbot AI, QR Code va cap nhat thoi gian thuc.

## Muc tieu

ProjectSOS la mot he thong ho tro nha hang so hoa quy trinh phuc vu tu luc khach vao ban den luc thanh toan va danh gia. Muc tieu chinh:

- Khach hang quet QR tai ban de vao giao dien dat mon.
- Khach xem menu, loc danh muc, tim kiem mon, them mon vao gio.
- Nhieu khach cung ban co the dong bo gio hang va mon da dat theo session/ban.
- Nhan vien va bep nhan thong bao thoi gian thuc ve mon moi, trang thai ban, yeu cau phuc vu.
- Quan ly co giao dien quan tri menu, danh muc, ban, khu vuc, nhan vien, hoa don, danh gia va dashboard.
- He thong co chatbot AI tu van mon an dua tren menu, ngan sach, khau vi, do cay, an chay, di ung va combo.
- He thong co AI phan tich cam xuc danh gia cua khach hang.
- Ho tro deploy frontend len Vercel, backend/AI/database len Railway hoac cloud tuong duong.

## Kien truc he thong

Project gom 3 phan chinh:

| Thanh phan | Thu muc | Vai tro |
|---|---|---|
| Frontend | `sos-fe` | Nuxt/Vue app cho khach hang, nhan vien, bep, quan tri |
| Backend API | `sos-api` | Spring Boot REST API, JWT, JPA, WebSocket, business logic |
| AI service | `sos-ai` | FastAPI microservice cho chatbot, local token RAG va sentiment |

Kien truc logic:

```text
Nguoi dung
  -> sos-fe Nuxt/Vue
      -> REST API /api/v1/... va /auth/...
          -> sos-api Spring Boot
              -> MySQL database
              -> WebSocket/STOMP topics
              -> sos-ai FastAPI
                  -> OpenAI API neu co OPENAI_API_KEY
                  -> Local token RAG fallback neu khong co key
```

## Luong hoat dong tong quat

1. Quan ly tao ban va tao QR cho tung ban.
2. Khach quet QR, frontend resolve token hoac nhan `tableId`/`tableNumber`.
3. Frontend mo hoac tao gio hang cho ban thong qua `/api/v1/carts/table/{tableId}/open`.
4. Khach nhap ten, xem menu active, loc danh muc, tim kiem va them mon vao gio.
5. Khi khach xac nhan, backend tao order tu cart va chuyen cac cart item thanh order item.
6. Bep/nhan vien nhan realtime event qua STOMP topic.
7. Bep cap nhat trang thai mon: PENDING -> PREPARING -> COMPLETED.
8. Nhan vien phuc vu cap nhat SERVED, tao hoa don, xac nhan thanh toan.
9. Ban duoc chuyen trang thai phu hop: EMPTY, SERVING, WAITING_PAYMENT, NEEDS_CLEANING.
10. Khach gui danh gia, backend goi AI sentiment va realtime thong bao cho dashboard.
11. Chatbot AI co the tra loi tu menu local/RAG hoac OpenAI neu cau hinh API key.

## Cac module chinh

### Frontend modules

- Home: man hinh gioi thieu va dieu huong.
- Customer: quet QR, menu, gio hang, dat mon, thanh toan, danh gia, chatbot, chat voi nhan vien.
- Staff: quan ly ban, chi tiet ban, trang thai mon, yeu cau phuc vu, chat.
- Kitchen: bep xem mon dang cho va cap nhat trang thai che bien.
- Admin: dashboard, menu, ban, QR, nhan vien, danh gia, hoa don.
- Auth: dang nhap va luu JWT/role.

### Backend modules

- Authentication va JWT.
- User/Role management.
- Menu/category management.
- Table/area/floor plan management.
- Cart va order workflow.
- Order item status management.
- Invoice/payment confirmation.
- QR Code generation/resolution.
- Review va sentiment analysis.
- Chatbot va AI menu sync.
- Service request va staff chat.
- Realtime event publishing.

### AI modules

- `/chat`: nhan cau hoi tu chatbot.
- `/sentiment`: phan tich cam xuc review.
- `/menu/sync`: nhan menu tu backend de cap nhat context.
- `/embeddings/rebuild`: rebuild local token index.
- OpenAI integration qua `OPENAI_API_KEY`.
- Local token RAG fallback khi khong co key hoac goi OpenAI loi.

## Vai tro nguoi dung

| Role | Mo ta | Quyen chinh |
|---|---|---|
| Khach hang | Nguoi dung quet QR tai ban | Xem menu, dat mon, chat AI, goi nhan vien, danh gia |
| STAFF | Nhan vien phuc vu | Quan ly ban, phuc vu mon, thanh toan, xu ly yeu cau |
| KITCHEN | Nhan vien bep | Xem mon cho che bien, cap nhat trang thai mon |
| MANAGER | Quan ly | Dashboard, menu, ban, hoa don, danh gia, nhan vien |
| ADMIN | Quan tri vien | Tat ca chuc nang, quan ly user/role |

## Luong du lieu tong the

```text
Frontend customer
  -> menu/category API
  -> cart API
  -> order confirm API
  -> realtime subscriptions

Backend
  -> MySQL tables: carts, cart_items, orders, order_items, tables, menu_items
  -> ApplicationEventPublisher
  -> SimpMessagingTemplate
  -> WebSocket topics

AI
  -> Backend ChatService -> sos-ai /chat
  -> Backend ReviewService -> sos-ai /sentiment
  -> Backend MenuAiSyncService -> sos-ai /menu/sync
```

## Ket luan tong quan

ProjectSOS la mot ung dung day du theo mo hinh full-stack: frontend SPA/SSR bang Nuxt, backend REST/WebSocket bang Spring Boot, database MySQL voi Flyway migration, va AI service doc lap bang FastAPI. He thong phu hop lam do an tot nghiep vi co day du nghiep vu nha hang, phan quyen, realtime, QR, thanh toan, dashboard va AI.

