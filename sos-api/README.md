# SOS Restaurant Ordering API

Backend cho hệ thống gọi món nhà hàng ProjectSOS với Spring Boot, JPA, WebSocket, Flyway, JWT Authentication và tích hợp AI service.

## 🚀 Tính năng chính

- **Authentication & Authorization**: JWT-based với role-based access control
- **Restaurant ordering**: Quản lý menu, bàn, giỏ hàng, đơn hàng, trạng thái món, hóa đơn và đánh giá
- **Realtime**: WebSocket cập nhật đơn bếp, phục vụ, bàn và chat
- **AI integration**: Đồng bộ menu sang `sos-ai`, chatbot tư vấn món và sentiment review
- **Audit Trail**: Tự động track `createdBy`, `createdAt`, `updatedBy`, `updatedAt`
- **RESTful API**: Đầy đủ CRUD operations
- **Logging**: Multi-level logging với file rotation

## 🛠️ Công nghệ sử dụng

- **Spring Boot 3.x**
- **Spring Security** với JWT
- **Spring Data JPA** với Hibernate
- **MySQL Database**
- **Lombok** cho code generation
- **MapStruct** cho object mapping
- **Logback** cho logging

## 📁 Cấu trúc project

```
src/main/java/com/sqc/sos/
├── config/           # Cấu hình Spring
├── controller/       # REST Controllers
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions
├── mapper/          # Object mappers
├── model/           # JPA Entities
├── repository/      # Data repositories
└── service/         # Business logic
```

## 🔧 Cài đặt và chạy

### Yêu cầu hệ thống
- Java 17+
- MySQL 8.0+
- Gradle wrapper đi kèm project

### Cài đặt
```bash
# Clone repository
git clone <repository-url>
cd ProjectSOS/sos-api

# Cấu hình database
# Chỉnh sửa application.properties với thông tin database

# Chạy ứng dụng
./gradlew bootRun
```

### Database Setup
```sql
-- Tạo database
CREATE DATABASE sos;

-- Flyway tự chạy migration trong src/main/resources/db/migration khi backend khởi động
```

## 🔐 Authentication

### Login
```bash
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### Response
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### Sử dụng token
```bash
Authorization: Bearer <token>
```

## 👥 Users và Roles

### Default Users
- **admin/admin123**: Quản trị viên, có tất cả quyền
- **staff/staff123**: Nhân viên phục vụ
- **kitchen/kitchen123**: Nhân viên bếp

### Roles
- **ADMIN**: Quản trị hệ thống, menu, bàn, nhân viên, QR, báo cáo
- **MANAGER**: Quản lý vận hành nhà hàng
- **STAFF**: Phục vụ, xử lý bàn, hóa đơn, yêu cầu khách
- **KITCHEN**: Xử lý trạng thái món trong bếp

## 📊 API Endpoints

### Health Check
```bash
GET /health
```

### Menu
```bash
GET /api/v1/menu-items/available      # Khách xem menu
POST /api/v1/menu-items               # Admin/Manager tạo món
PUT /api/v1/menu-items/{id}           # Admin/Manager sửa món
DELETE /api/v1/menu-items/{id}        # Admin/Manager xóa món
POST /api/v1/ai/menu/sync             # Admin/Manager đồng bộ menu sang AI
```

### Ordering
```bash
POST /api/v1/carts                    # Mở giỏ hàng theo bàn/session
POST /api/v1/carts/{sessionId}/items  # Thêm món
POST /api/v1/orders/session/{sessionId}/confirm
GET /api/v1/tables                    # Xem bàn
```

### Users
```bash
POST /auth/register             # Đăng ký user mới (ADMIN)
```

## 🗄️ Database Schema

### Bảng chính
- **user**: Thông tin người dùng
- **role**: Vai trò trong hệ thống
- **user_roles**: Quan hệ many-to-many user-role
- **menu_items**: Món ăn, đồ uống, combo và metadata AI
- **tables**: Bàn nhà hàng và trạng thái phục vụ
- **carts / cart_items**: Giỏ hàng theo session khách
- **orders / order_items**: Đơn hàng và trạng thái món
- **reviews / sentiment_results**: Đánh giá và kết quả phân tích cảm xúc
- **invoices / payments**: Hóa đơn và thanh toán

### Audit Fields
Tất cả bảng đều có các trường audit:
- `created_by`: Người tạo
- `created_at`: Thời gian tạo
- `updated_by`: Người cập nhật cuối
- `updated_at`: Thời gian cập nhật cuối

## 📝 Logging

### Cấu hình
- **Console**: Log tất cả levels
- **File**: Daily rotation với max 30 files
- **Error**: Tách riêng file error.log
- **Security**: Tách riêng file security.log
- **SQL**: Tách riêng file sql.log

### Log files
```
logs/
├── application.log      # General logs
├── error.log          # Error logs
├── security.log       # Security logs
└── sql.log           # SQL queries
```

## 🚨 Exception Handling

### Custom Exceptions
- `AppException`: Base exception
- `ErrorCode`: Enum định nghĩa error codes
- `GlobalExceptionHandle`: Global exception handler

### Error Response Format
```json
{
  "code": "UNAUTHORIZED",
  "message": "Invalid credentials",
  "timestamp": "2024-01-15T10:30:00"
}
```

## 🔧 Cấu hình

### application.properties
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sos
spring.datasource.username=root
spring.datasource.password=password

# JWT
jwt.signerKey=your-secret-key-here

# Logging
logging.file.name=logs/application.log
```

## 🧪 Testing

```bash
# Chạy tests
./gradlew test

# Chạy với coverage
./gradlew test jacocoTestReport
```

## 📦 Build

```bash
# Build JAR file
./gradlew build

# Run JAR
java -jar build/libs/ordering-system-api-0.0.1-SNAPSHOT.jar
```

## 🤝 Contributing

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
