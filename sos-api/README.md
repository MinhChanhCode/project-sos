# Ordering System API

Hệ thống quản lý đơn hàng với Spring Boot, JPA, và JWT Authentication.

## 🚀 Tính năng chính

- **Authentication & Authorization**: JWT-based với role-based access control
- **Audit Trail**: Tự động track `createdBy`, `createdAt`, `updatedBy`, `updatedAt`
- **Soft Delete**: Bảo toàn dữ liệu với `isDeleted` flag
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
- Gradle 7.x+

### Cài đặt
```bash
# Clone repository
git clone <repository-url>
cd ordering-system-api

# Cấu hình database
# Chỉnh sửa application.properties với thông tin database

# Chạy ứng dụng
./gradlew bootRun
```

### Database Setup
```sql
-- Tạo database
CREATE DATABASE sos;

-- Chạy migration scripts
-- V1.0.1__init_database.sql
-- V1.0.2__init_data.sql
-- V1.0.3__create_user_role.sql
-- V1.0.4__add_audit_columns.sql
```

## 🔐 Authentication

### Login
```bash
POST /auth/login
{
  "username": "thilh",
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
- **thilh** (staff): Chỉ có quyền xem students
- **admin/admin123**: Quản trị viên, có tất cả quyền
- **staff/staff123**: Nhân viên phục vụ
- **kitchen/kitchen123**: Nhân viên bếp

### Roles
- **STAFF**: Xem danh sách students
- **ADMIN**: Tất cả quyền (CRUD users, students)

## 📊 API Endpoints

### Health Check
```bash
GET /health
```

### Students
```bash
GET /students                    # Lấy danh sách students (STAFF)
POST /students                   # Tạo student mới (ADMIN)
PUT /students/{id}              # Cập nhật student (ADMIN)
DELETE /students/{id}           # Xóa student (ADMIN)
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
- **student**: Thông tin học sinh
- **clazz**: Thông tin lớp học

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

## 🔍 Soft Delete

Hệ thống sử dụng soft delete để bảo toàn dữ liệu:
- Không xóa thực sự khỏi database
- Có thể khôi phục dữ liệu
- Audit trail đầy đủ

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
