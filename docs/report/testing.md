# Testing And Improvement Notes

## Pham vi danh gia

Tai lieu nay danh gia dua tren source code hien tai, cau truc project, endpoint, migration, frontend component va AI service. Trong qua trinh lap tai lieu khong chay test tu dong va khong sua source code.

## Chuc nang da hoan thanh

### Authentication

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Dang nhap | Da co | `AuthController`, JWT, frontend login page |
| Introspect token | Da co | Dung kiem tra token hop le |
| Dang ky user noi bo | Da co | Gioi han role ADMIN theo security config |
| Luu token frontend | Da co | Store `auth` |

### Menu va category

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Xem menu cong khai | Da co | Khach co the xem menu active/available |
| Quan ly mon | Da co | Them, sua, xoa, toggle active/availability |
| Quan ly category | Da co | CRUD category |
| Promotion | Da co | Endpoint promotion |
| Best sellers | Da co | Endpoint best seller |
| Anh menu | Da co | Luu trong public assets va/hoac upload image |

### Ban, khu vuc va QR

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Quan ly khu vuc | Da co | Area CRUD |
| Quan ly ban | Da co | Table entity, status, position |
| QR theo ban | Da co | Tao QR, token lookup |
| So do ban | Da co | Component admin/staff/shared |

### Gio hang va dat mon

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Tao cart theo session | Da co | Cart API |
| Them/sua/xoa mon trong gio | Da co | Cart item API |
| Xac nhan dat mon | Da co | Order confirm theo session |
| Hien thi don theo ban/order | Da co | Order API va frontend staff/kitchen |

### Bep va phuc vu

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Bep nhan mon realtime | Da co | Topic kitchen/order-items |
| Cap nhat trang thai mon | Da co | Pending, preparing, ready, served, cancelled |
| Cap nhat mot phan so luong | Da co | partial status |
| Batch update | Da co | batch endpoint |
| Nhan vien xem ban | Da co | Staff UI |

### Hoa don va thanh toan

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Tao hoa don theo order | Da co | Invoice API |
| Tao hoa don public theo session | Da co | Public invoice endpoint |
| Xac nhan thanh toan | Da co | Payment confirm |
| Xuat PDF | Da co | OpenPDF dependency va endpoint PDF |

### Chat va thong bao

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Chatbot AI | Da co | Frontend `Chatbot.vue`, backend Chat API, AI FastAPI |
| Lich su chatbot | Da co | `ChatHistory` |
| Staff chat | Da co | `StaffChatMessage`, frontend staff/customer |
| Goi nhan vien | Da co | `ServiceRequest` |
| WebSocket topic | Da co | STOMP/SockJS |

### Danh gia va AI sentiment

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Gui danh gia | Da co | Review API |
| Xem danh gia | Da co | Admin/manager |
| Tong hop sentiment | Da co | Sentiment summary |
| Goi AI phan tich danh gia | Da co | AI `/sentiment` |

### Dashboard va nhan su

| Chuc nang | Trang thai | Ghi chu |
|---|---|---|
| Dashboard | Da co | Dashboard API, admin charts/stats |
| Quan ly nhan vien | Da co | Employee API/UI |
| Quan ly ca lam | Da co | Shift API |
| Phan cong | Da co | Assignment API |

## Chuc nang chua hoan thien hoac can lam ro

| Hang muc | Ly do |
|---|---|
| Test tu dong | Chua thay bang chung ro rang ve bo test day du cho backend/frontend/AI |
| Vector RAG that su | AI hien tai co token search/RAG nhe, chua thay vector database hoac embedding index ben ngoai |
| Conversation memory nang cao | Co `ChatHistory`, nhung AI service chu yeu xu ly message/context hien tai |
| Tool calling | Chua thay luong tool calling LLM dung function/schema de goi API nghiep vu |
| Payment gateway thuc te | Co xac nhan thanh toan, nhung chua thay tich hop cong thanh toan nhu VNPAY/MoMo/Stripe |
| CI/CD day du | Deploy theo Vercel/Railway, nhung can kiem tra workflow automation neu muon dua vao bao cao |
| Monitoring/observability | Chua thay dashboard log/trace rieng trong source |
| Rate limit | Chua thay rate limiting cho login/chat/API public |

## Bug/risk con ton tai

### Deploy Railway

| Van de | Anh huong | Goi y |
|---|---|---|
| `server.port=8080` co the khong doc bien `PORT` Railway | Railway thuong yeu cau app lang nghe port duoc cap | Nen cau hinh `server.port=${PORT:8080}` khi deploy Railway |
| Bien moi truong frontend/API de sai domain | Frontend goi nham Render/localhost lam cham hoac loi | Kiem tra `NUXT_PUBLIC_API_BASE`, `VITE_API_BASE_URL`, `NUXT_PUBLIC_WS_BASE`, `SOS_API_TARGET` |
| Con lien ket Render cu | Co the frontend van goi endpoint Render | Xoa env Render trong Vercel/Railway, xoa webhook/service khong dung |

### Security

| Van de | Anh huong | Goi y |
|---|---|---|
| JWT signer key can quan ly bang env | Neu hardcode key se rui ro lo khoa | Dung `JWT_SECRET` tren cloud |
| AI service CORS allow all | Public qua rong | Gioi han origin frontend/backend that |
| Mot so API public rong | Co the bi spam hoac truy cap ngoai y muon | Rasoat lai `service-requests/**`, cart, chat, review |
| Chua thay rate limit | Login/chat co the bi spam | Them rate limit/gateway/protection |
| Password policy | Can xac nhan hash va do manh password | Dung BCrypt va validate password |

### Performance

| Van de | Anh huong | Goi y |
|---|---|---|
| Anh `yaourt-da.jpg` khoang 5.57 MB | Lam cham load menu | Resize/nen/WebP/lazy loading |
| Chatbot goi OpenAI dong bo | Co the cham khi LLM phan hoi lau | Timeout, fallback, streaming neu can |
| Backend/AI/DB khac region | Tang latency | Dat Vercel Functions, Railway API, Railway AI, Railway MySQL cung Singapore |
| Free tier sleep/cold start | Lan dau load cham | Dung plan khong sleep hoac warmup |

### Database

| Van de | Anh huong | Goi y |
|---|---|---|
| `ddl-auto=update` tren production | Schema co the thay doi ngoai migration | Production nen dung `validate` hoac migration-first |
| Flyway validate disabled | De bo qua mismatch migration | Production nen bat validate khi migration on dinh |
| Migration lon vua tao bang vua seed data | Kho maintain | Tach migration schema va seed ro rang |

### AI

| Van de | Anh huong | Goi y |
|---|---|---|
| RAG chua dung vector search that | Goi y mon co the chua sau | Them embedding va vector index |
| Menu metadata can giau hon | Chatbot kho hieu khau vi phuc tap | Bo sung vi cay/ngot/man, mon chay, di ung, calo, combo |
| Loi OpenAI fallback im lang | Kho debug | Log co cau truc va tra error_code noi bo |
| Chua co evaluation chatbot | Kho chung minh chat thong minh | Tao bo cau hoi test va cham diem |

## Diem can cai thien cho bao cao do an

### Ky thuat

1. Viet ro he thong gom 3 service: `sos-fe`, `sos-api`, `sos-ai`.
2. Nhan manh realtime bang WebSocket/STOMP cho bep, nhan vien, gio hang, yeu cau dich vu.
3. Mo ta AI theo dung hien trang: co LLM OpenAI khi co key, co fallback heuristic dua tren menu, co endpoint sentiment.
4. Phan biet RAG hien tai va RAG muc tieu: hien tai la truy hoi token/menu context, tuong lai co embedding vector search that.
5. Neu dua vao production, nen noi den Railway/Vercel region va env vars.

### San pham

1. Bo sung anh chup man hinh thuc te cho cac giao dien: customer QR, menu, cart, kitchen, staff, admin dashboard.
2. Them bang so sanh truoc/sau khi dung QR ordering.
3. Mo ta luong khach tu luc quet QR den danh gia.
4. Mo ta loi ich cho nhan vien bep va nhan vien phuc vu.
5. Mo ta loi ich AI: goi y mon, tra loi khau vi, phan tich danh gia.

## Goi y test case

### Backend API

| Test case | Buoc test | Ket qua mong doi |
|---|---|---|
| Login dung | POST `/auth/login` voi tai khoan hop le | Tra JWT |
| Login sai | POST `/auth/login` sai mat khau | Tra loi 401/loi hop le |
| Lay menu active | GET `/api/v1/menu-items/active` | Tra danh sach mon active |
| Them mon vao cart | POST cart item | Cart cap nhat dung |
| Xac nhan order | POST `/api/v1/orders/session/{sessionId}/confirm` | Tao order va order_items |
| Cap nhat mon ready | PATCH order item status | Status/quantity cap nhat |
| Tao invoice | POST invoice theo order | Co invoice |
| Gui review | POST review | Luu review va sentiment neu AI hoat dong |

### Frontend

| Test case | Buoc test | Ket qua mong doi |
|---|---|---|
| Customer QR | Vao `/customer/table/1` | Tao session, hien menu |
| Gio hang | Them/sua/xoa mon | Tong tien va so luong dung |
| Chatbot | Hoi "toi thich mon cay" | AI goi y mon phu hop |
| Staff | Login staff | Vao man staff, thay ban/order |
| Kitchen | Login kitchen | Thay danh sach mon can lam |
| Admin | Login manager/admin | Xem dashboard va quan ly menu |

### Realtime

| Test case | Buoc test | Ket qua mong doi |
|---|---|---|
| Cart realtime | Khach them mon | Staff/table topic nhan update |
| Order realtime | Khach xac nhan order | Bep nhan don moi |
| Status realtime | Bep chuyen READY | Staff/khach nhan cap nhat |
| Service request | Khach goi nhan vien | Staff nhan thong bao |
| Staff chat | Khach gui tin | Staff nhan realtime va tra loi duoc |

### AI

| Test case | Cau hoi | Ket qua mong doi |
|---|---|---|
| Goi y mon cay | "Toi muon an mon cay nong" | Goi y mon co tag cay/nong |
| Goi y do uong | "Co do uong mat khong?" | Goi y nuoc/tra/sinh to |
| Hoi combo | "Di 2 nguoi nen an gi?" | Goi y combo co mon chinh va do uong |
| Sentiment tich cuc | "Mon rat ngon, nhan vien than thien" | Label tich cuc |
| Sentiment tieu cuc | "Cho lau, mon nguoi" | Label tieu cuc |

## Ket luan kiem thu

Project da co day du khung chuc nang cua mot he thong goi mon nha hang hien dai: QR ordering, realtime kitchen/staff, dashboard, nhan su, hoa don, chatbot AI va sentiment. Phan can dau tu them de dat chat luong production la test tu dong, bao mat public API, toi uu anh, cau hinh deploy Railway/Vercel, va nang cap AI tu token search sang vector RAG that su.
