# 4. Database

## Tong quan

Database su dung MySQL, duoc quan ly bang Flyway migration trong:

```text
sos-api/src/main/resources/db/migration
```

Spring Boot cau hinh:

```properties
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/sos?...}
spring.datasource.username=${DATABASE_USERNAME:root}
spring.datasource.password=${DATABASE_PASSWORD:12345}
spring.jpa.hibernate.ddl-auto=update
spring.flyway.enabled=true
spring.flyway.validate-on-migrate=false
```

## Danh sach migration

| Version | File | Noi dung chinh |
|---|---|---|
| 1.0.1 | `V1.0.1__init_database.sql` | Tao role, user, user_roles, tables, categories, menu_items, orders, order_items, order_staff, staff_shifts, notifications, payments, carts, cart_items |
| 1.0.2 | `add_notes_to_order_items` | Them notes cho order_items |
| 1.0.3 | `add_is_active_to_cart_items` | Them is_active cho cart_items |
| 1.0.4 | `create_service_requests` | Tao service_requests |
| 1.0.5 | `add_completed_quantity_to_order_items` | Them completed_quantity |
| 1.0.6 | `order_item_per_status_quantities` | Tach so luong theo trang thai, bo quantity/status cu |
| 1.0.7 | `drop_status_from_order_items` | Don dep cot status |
| 1.0.8 | `add_unique_constraint_order_items` | Unique order_id + menu_item_id |
| 1.0.9 | `add_column_promotion_items` | original/promotional price va promotion_end_date |
| 1.0.10 | `extended_features` | Area, QR, review/sentiment, invoice, employee, assignment, chat_history, menu_embeddings |
| 1.0.11 | `dedupe_and_expand_menu` | Them danh muc/mon, an duplicate |
| 1.0.12 | `customer_sessions_staff_chat_and_table_cleanup` | Customer session, staff chat, seed 20 ban |
| 1.0.13 | `menu_ai_metadata` | Metadata AI cho menu: type, taste_tags, spicy_level, allergens... |
| 1.0.14 | `expand_categories_and_kitchen_menu` | Mo rong menu: do nuong, lau, bia/nuoc giai khat |
| 1.0.15 | `backfill_core_menu_categories` | Backfill danh muc cot loi |

## Bang va chuc nang

### `role`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Dinh danh role |
| name | VARCHAR | unique logic | Ten role: ADMIN, MANAGER, STAFF, KITCHEN |
| created_by/created_at/updated_by/updated_at | audit | | Audit tu BaseEntity |

Chuc nang: luu vai tro phan quyen.

### `user`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BINARY(16) UUID | PK | Dinh danh user |
| username | VARCHAR | UNIQUE | Ten dang nhap |
| password | VARCHAR | | Mat khau BCrypt |
| full_name | VARCHAR | | Ho ten |
| phone | VARCHAR | | So dien thoai |
| email | VARCHAR | | Email |
| audit fields | | | Ke thua BaseEntity |

Chuc nang: tai khoan dang nhap cua admin/manager/staff/kitchen.

### `user_roles`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| role_id | BIGINT | PK/FK | FK -> role.id |
| user_id | BINARY(16) | PK/FK | FK -> user.id |

Quan he many-to-many giua user va role.

### `areas`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Khu vuc |
| name | VARCHAR | | Ten khu |
| description | TEXT | | Mo ta |
| is_active | BOOLEAN | | Trang thai |
| created_at | DATETIME | | Ngay tao |

Dung de phan khu ban va phan cong nhan vien.

### `tables`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BINARY(16) UUID | PK | Dinh danh ban |
| name | VARCHAR | | Ten ban |
| capacity | INT | | Suc chua |
| is_available | BOOLEAN | | Co kha dung |
| area_id | BIGINT | FK | FK -> areas.id |
| pos_x/pos_y | INT | | Vi tri tren floor plan |
| table_status | VARCHAR/ENUM | | EMPTY, SERVING, WAITING_PAYMENT, NEEDS_CLEANING, RESERVED |

### `categories`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Danh muc |
| name | VARCHAR | | Ten danh muc |
| description | TEXT | | Mo ta |
| is_active | BOOLEAN | | Hien/an |

### `menu_items`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Mon an |
| name | VARCHAR | | Ten mon |
| description | TEXT | | Mo ta |
| price | DECIMAL | | Gia goc |
| image_url | TEXT | | Anh |
| category_id | BIGINT | FK | FK -> categories.id |
| is_available | BOOLEAN | | Con/hết mon |
| is_active | BOOLEAN | | Hien/an menu |
| type | VARCHAR | | MAIN, DRINK, COMBO, APPETIZER, GRILL, HOTPOT... |
| taste_tags | VARCHAR | | Vi: cay, beo, chua, thanh... |
| spicy_level | INT | | Do cay |
| ingredients | VARCHAR | | Nguyen lieu |
| allergens | VARCHAR | | Di ung |
| suitable_for | VARCHAR | | Phu hop doi tuong |
| pairing | VARCHAR | | Mon/uong ket hop |
| is_vegetarian | BOOLEAN | | Co phai mon chay |
| prep_time_minutes | INT | | Thoi gian chuan bi |
| original_price | DECIMAL | | Gia goc khi khuyen mai |
| promotional_price | DECIMAL | | Gia khuyen mai |
| promotion_end_date | TIMESTAMP | | Han khuyen mai |

### `carts`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Gio hang |
| table_id | BINARY(16) | FK | FK -> tables.id |
| session_id | VARCHAR | | Session cua khach/ban |
| is_active | BOOLEAN | | Gio dang hoat dong |
| created_at/updated_at | DATETIME | | Thoi gian |

### `cart_items`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Dong gio hang |
| cart_id | BIGINT | FK | FK -> carts.id |
| menu_item_id | BIGINT | FK | FK -> menu_items.id |
| quantity | INT | | So luong |
| unit_price | DECIMAL | | Gia tai thoi diem them |
| notes | TEXT | | Ghi chu |
| is_active | BOOLEAN | | Soft remove |

Unique: `(cart_id, menu_item_id)`.

### `orders`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Don hang |
| table_id | BINARY(16) | FK | FK -> tables.id |
| status | VARCHAR | | Trang thai don |
| created_at | DATETIME | | Tao don |
| completed_at | DATETIME | | Hoan thanh |

### `order_items`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Mon trong don |
| order_id | BIGINT | FK | FK -> orders.id |
| menu_item_id | BIGINT | FK | FK -> menu_items.id |
| pending_quantity | INT | | Dang cho |
| preparing_quantity | INT | | Dang che bien |
| completed_quantity | INT | | Da xong bep |
| served_quantity | INT | | Da phuc vu |
| cancelled_quantity | INT | | Da huy |
| out_of_stock_quantity | INT | | Het mon |
| notes | TEXT | | Ghi chu |

Unique: `(order_id, menu_item_id)`. Thiet ke theo quantity-per-status giup tach mot mon thanh nhieu trang thai ma khong can tao nhieu row.

### `invoices`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | Hoa don |
| order_id | BIGINT | UNIQUE/FK | Don hang |
| table_id | BINARY(16) | | Ban |
| subtotal/tax/discount/total | DECIMAL | | Tinh tien |
| status | VARCHAR | | PENDING/PAID |
| created_at/paid_at | DATETIME | | Thoi gian |

### `payments`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | UUID | PK | Thanh toan |
| order_id | BIGINT | UNIQUE/FK | Don hang |
| amount | DECIMAL | | So tien |
| method | VARCHAR | | CASH/CARD/... |
| status | VARCHAR | | Trang thai |
| paid_at | DATETIME | | Thoi gian thanh toan |

### `qr_codes`

| Truong | Kieu | Khoa | Mo ta |
|---|---|---|---|
| id | BIGINT | PK | QR |
| table_id | BINARY(16) | FK | Ban |
| code_url | TEXT | | URL QR |
| qr_token | VARCHAR | UNIQUE | Token public |
| is_active | BOOLEAN | | Active |
| created_at | DATETIME | | Tao QR |

### `reviews` va `sentiment_results`

`reviews` luu danh gia khach:

- id, table_id, session_id, customer_name, rating, comment, created_at.

`sentiment_results` luu ket qua AI:

- id, review_id unique, sentiment, confidence, analyzed_at.

Quan he 1-1 theo `review_id`.

### `service_requests`

Yeu cau phuc vu:

- id, table_id, table_name, session_id.
- request_type: GENERAL_SERVICE, FOOD_REQUEST, CLEANING, PAYMENT, OTHER.
- status: PENDING, IN_PROGRESS, DONE, CANCELLED.
- priority: LOW, MEDIUM, HIGH, URGENT.
- requested_at, started_at, completed_at, assigned_to, notes.

### `customer_sessions`

Lien ket session khach voi ban va ten khach:

- id, session_id unique, table_id, customer_name, is_active, created_at, updated_at.

### `staff_chat_messages`

Luu chat khach-nhan vien:

- id, table_id, session_id, customer_name, sender, message, created_at.

### `chat_histories`

Luu chatbot AI:

- id, session_id, user_message, bot_response, created_at.

### `employees`, `staff_shifts`, `assignments`

Dung cho quan ly nhan vien:

- employees: thong tin nhan vien va lien ket user.
- staff_shifts: ca lam theo staff.
- assignments: phan cong employee vao area/shift.

### `notifications`

Thong bao noi bo:

- id, recipient_id, title, message, type, priority, is_read, read_at, metadata.

### `order_staff`

Ghi nhan staff phu trach order:

- id, order_id, staff_id, role, assigned_at.

### `menu_embeddings`

Bang metadata cho vector/embedding menu:

- id, menu_item_id unique, content_hash, updated_at.
- Ghi chu: vector that su nam o AI service theo comment migration; trong code hien tai AI service dung token index in-memory, chua co vector DB thuc su.

## Quan he chinh

- User N-N Role qua `user_roles`.
- Category 1-N MenuItem.
- Area 1-N Table.
- Table 1-N Cart.
- Cart 1-N CartItem.
- MenuItem 1-N CartItem.
- Table 1-N Order.
- Order 1-N OrderItem.
- MenuItem 1-N OrderItem.
- Order 1-1 Invoice.
- Order 1-1 Payment.
- Table 1-N QrCode.
- Review 1-1 SentimentResult.
- Table/Session 1-N StaffChatMessage.
- Table/Session 1-N ServiceRequest.

