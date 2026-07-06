# 2. Cong nghe su dung

## Backend

| Cong nghe | Phien ban/thu vien | Muc dich | Ly do su dung |
|---|---|---|---|
| Java | 17 | Ngon ngu backend | On dinh, pho bien trong doanh nghiep, ho tro Spring Boot tot |
| Spring Boot | 3.4.4 | Framework backend | Tao REST API, cau hinh nhanh, tich hop JPA/Security/WebSocket |
| Spring Web | `spring-boot-starter-web` | REST API | Xay dung controller va endpoint HTTP |
| Spring Data JPA | `spring-boot-starter-data-jpa` | ORM | Lam viec voi MySQL qua entity/repository |
| Hibernate | Quan ly boi Spring Data JPA | ORM implementation | Mapping Java entity sang relational schema |
| Spring Security | OAuth2 Resource Server | Bao mat API | Xac thuc JWT va phan quyen role |
| Spring WebSocket | `spring-boot-starter-websocket` | Realtime | STOMP/SockJS realtime order/table/notification |
| Spring WebFlux WebClient | `spring-boot-starter-webflux` | Goi service AI | Goi FastAPI `/chat`, `/sentiment`, `/menu/sync` |
| Flyway | `flyway-core`, `flyway-mysql` | Migration database | Quan ly thay doi schema theo version |
| Lombok | `org.projectlombok:lombok` | Giam boilerplate | Sinh getter/setter/builder/constructor |
| MapStruct | `mapstruct:1.5.5.Final` | Mapping DTO/entity | Tach DTO response/request khoi entity |
| ZXing | `com.google.zxing` | QR Code | Sinh QR code cho ban |
| OpenPDF | `com.github.librepdf:openpdf` | PDF invoice | Tao file PDF hoa don |
| MySQL Connector/J | runtime | Ket noi MySQL | Driver JDBC cho database |

## Frontend

| Cong nghe | Phien ban/thu vien | Muc dich | Ly do su dung |
|---|---|---|---|
| Nuxt | 4.x | Framework frontend | Routing, SSR/Nitro, proxy, module system |
| Vue | 3.5.x | UI framework | Composition API, component-based UI |
| TypeScript | 5.7.x | Static typing | Tang do an toan khi goi API va xu ly state |
| Pinia | 3.x | State management | Quan ly auth, cart, menu, theme |
| Tailwind CSS | 3.4.x | Styling | Tao UI nhanh, responsive |
| Nuxt UI | 2.18.x | UI component | Card, badge, input, UI primitives |
| Lucide/Nuxt Icon | lucide + iconify | Icon | Hien thi icon thao tac, trang thai |
| Chart.js | 4.4.x | Bieu do dashboard | Thong ke doanh thu, danh gia, don hang |
| qrcode.vue | 3.6.x | QR display | Hien thi QR tren admin |
| vue3-toastify | 0.2.x | Toast notification | Thong bao thanh cong/loi tren frontend |
| SockJS Client | 1.6.x | WebSocket fallback | Ket noi endpoint `/ws` cua Spring |
| STOMP.js | 7.1.x | STOMP protocol | Subscribe/publish topic realtime |

## Database

| Cong nghe | Muc dich |
|---|---|
| MySQL | Luu du lieu chinh: user, role, menu, ban, order, cart, invoice, review |
| Flyway migration | Tao va tien hoa schema theo version |
| JPA entity | Mapping logic Java voi bang SQL |

## AI

| Cong nghe | Muc dich | Ly do |
|---|---|---|
| Python | Ngon ngu service AI | Nhe, phu hop NLP/AI |
| FastAPI | REST API AI | Nhanh, de deploy, co Pydantic schema |
| Pydantic | Validate request/response | Kiem soat input `/chat`, `/sentiment`, `/menu/sync` |
| OpenAI SDK | Goi OpenAI khi co key | Tao cau tra loi chatbot tu LLM |
| Local token RAG | Tim mon bang token trong bo nho | Fallback khi khong co OpenAI key, khong can vector DB |
| Sentiment keyword | Phan tich cam xuc co ban | Don gian, khong phu thuoc LLM |

## Realtime

| Cong nghe | Muc dich |
|---|---|
| WebSocket | Ket noi realtime |
| STOMP | Topic/subscription routing |
| SockJS | Fallback cho moi truong browser/proxy |
| Spring `SimpMessagingTemplate` | Publish event tu backend |
| Nuxt plugin `realtime.client.ts` | Quan ly ket noi STOMP va fan-out handler |

## Authentication va Authorization

| Thanh phan | Mo ta |
|---|---|
| JWT HS512 | Token dang nhap |
| Spring Security Resource Server | Doc JWT tren request |
| BCryptPasswordEncoder | Bam mat khau |
| Role-based access control | ADMIN, MANAGER, STAFF, KITCHEN |
| Public endpoints | Menu, cart, chat, QR token, customer session, review create |

## Deploy

| Thanh phan | Nen tang |
|---|---|
| Frontend `sos-fe` | Vercel |
| Backend `sos-api` | Railway hoac Render, uu tien Railway cung DB |
| AI `sos-ai` | Railway |
| MySQL | Railway MySQL |
| Docker | Backend co Dockerfile |
| Environment variables | `DATABASE_URL`, `AI_SERVICE_URL`, `NUXT_PUBLIC_API_BASE`, `OPENAI_API_KEY` |

## Docker

Backend `sos-api/Dockerfile` dung multi-stage build:

1. Stage build: `eclipse-temurin:17-jdk`, chay `./gradlew --no-daemon bootJar`.
2. Stage runtime: `eclipse-temurin:17-jre`, chay `java -Xms64m -Xmx384m -jar app.jar`.

Ly do:

- Tach moi truong build va runtime.
- Giam kich thuoc image runtime.
- Phu hop deploy cloud container.

## Cac package quan trong

### Backend packages

- `com.sqc.sos.controller`: REST controllers.
- `com.sqc.sos.service`: business logic va realtime events.
- `com.sqc.sos.repository`: Spring Data repositories.
- `com.sqc.sos.model`: JPA entities.
- `com.sqc.sos.dto`: request/response DTOs.
- `com.sqc.sos.config`: security, CORS, WebSocket, audit, seed data.
- `com.sqc.sos.mapper`: MapStruct mapper.
- `com.sqc.sos.exception`: exception handling.

### Frontend folders

- `pages`: route-level views.
- `components`: UI theo role.
- `composables`: business logic UI.
- `stores`: Pinia state.
- `api-service`: typed API client wrappers.
- `plugins`: realtime, theme, toast.
- `utils`: formatter, image fallback, id, table status.

### AI files

- `sos-ai/main.py`: toan bo FastAPI app, RAG, OpenAI call, sentiment.
- `sos-ai/requirements.txt`: dependencies.
- `sos-ai/run-ai.ps1`: local run helper.

