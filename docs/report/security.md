# 10. Security

## Authentication

He thong dung username/password va JWT.

Luong:

```text
POST /auth/login
  -> AuthenticateService
  -> Kiem tra username/password
  -> BCrypt password match
  -> Tao JWT HS512
  -> Frontend luu token
  -> Cac request protected gui Authorization: Bearer <token>
```

## JWT

Backend cau hinh:

- `jwt.signerKey` trong `application.properties`.
- `NimbusJwtDecoder` voi `HS512`.
- `JwtAuthenticationConverter` doc claim `scope`.
- Prefix authority: `ROLE_`.

SecurityConfig:

```java
httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))
```

## Password

Dung:

```java
BCryptPasswordEncoder(10)
```

Ly do:

- Bam mat khau co salt.
- Cham hon hash don gian, giam rui ro brute force.

## Authorization

Role chinh:

- ADMIN
- MANAGER
- STAFF
- KITCHEN

### Public endpoints

- Auth login/introspect.
- Health.
- WebSocket `/ws`.
- GET menu/categories/tables.
- Cart APIs.
- Order confirm theo session.
- Review create.
- Chatbot APIs.
- Customer sessions.
- Staff chat.
- QR token resolve.
- Public invoice request by session.

### Protected endpoints

- Dashboard: ADMIN, MANAGER.
- AI menu sync: ADMIN, MANAGER.
- Areas: ADMIN, MANAGER.
- Menu write: ADMIN, MANAGER.
- Menu patch availability: ADMIN, MANAGER, STAFF, KITCHEN.
- Tables create/update/delete: ADMIN, MANAGER.
- Table clear: ADMIN, MANAGER, STAFF, KITCHEN.
- Users: ADMIN.
- Employees/shifts/assignments: ADMIN, MANAGER.
- Review list: ADMIN, MANAGER.
- Invoices: ADMIN, MANAGER, STAFF.
- QR admin APIs: ADMIN, MANAGER.
- Order items: ADMIN, MANAGER, STAFF, KITCHEN.

## CORS

Backend co `CorsConfig` doc:

- `app.frontend-origin`.
- `app.frontend-url`.
- Pattern cho frontend deploy.

AI service hien allow:

```python
allow_origins=["*"]
allow_credentials=True
allow_methods=["*"]
allow_headers=["*"]
```

Day la tien loi cho demo nhung nen gioi han khi production.

## Validation

Backend co dependency:

```gradle
spring-boot-starter-validation
```

Co custom constraint:

- `DobConstraint`
- `DobValidator`

Tuy nhien nhieu controller dang dung `@RequestBody` chua thay `@Valid` dong loat. Diem cai thien: them validation DTO day du.

## Permission Review

Diem tot:

- Co JWT va role-based authorization.
- Public/customer API duoc tach khoi admin API.
- Password dung BCrypt.
- WebSocket endpoint public nhung topic theo table/session.

Diem can xem xet:

- `/api/v1/service-requests/**` dang permitAll toan bo, bao gom status/assign/delete. Nen gioi han cac action staff/admin.
- `/api/v1/staff-chat/**` permitAll co the chap nhan cho khach chat, nhung nen tach send public va management private.
- `jwt.signerKey` dang nam trong source properties, production nen dua vao env var.
- `OPENAI_API_KEY` phai chi nam o Railway `sos-ai`, khong dua vao frontend.
- AI CORS allow all.
- Chua co rate limiting cho login/chatbot/service request.
- Chua co CSRF la hop ly cho stateless JWT, nhung can dam bao token khong luu cookie insecure.

## Security Flow cho role

```text
Login
  -> JWT scope roles
  -> Frontend route guard
  -> Backend SecurityFilterChain
  -> Controller
  -> Service
```

Frontend khong duoc coi la lop bao mat chinh; backend SecurityConfig moi la lop quyet dinh.

