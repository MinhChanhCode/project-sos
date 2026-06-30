# 🍽️ Gọi Món - Hệ thống đặt món thông minh

Một ứng dụng web hiện đại được xây dựng bằng **Nuxt 4** và **Vue 3** để quản lý đặt món trong nhà hàng. Hệ thống hỗ trợ 3 vai trò chính: Khách hàng, Nhân viên và Quản lý.

## ✨ Tính năng chính

### 🏠 Trang chủ
- Giao diện landing page hiện đại
- Giới thiệu các tính năng của hệ thống
- Hướng dẫn sử dụng cho từng vai trò

### 👥 Khách hàng (Customer)
- **Đặt món trực tuyến**: Xem menu và đặt món từ bàn
- **Giỏ hàng thông minh**: Quản lý đơn hàng với số lượng
- **Tìm kiếm món ăn**: Tìm kiếm theo tên và danh mục
- **Đánh giá dịch vụ**: Hệ thống rating và feedback
- **Gọi phục vụ**: Tính năng gọi nhân viên phục vụ
- **Giao diện responsive**: Tối ưu cho mobile và desktop

### 👨‍💼 Nhân viên (Staff)
- **Quản lý bàn**: Theo dõi trạng thái các bàn
- **Xử lý đơn hàng**: Cập nhật trạng thái món ăn
- **Thông báo real-time**: Nhận thông báo đơn hàng mới
- **Chat với khách**: Tương tác trực tiếp với khách hàng
- **Thống kê ca làm**: Theo dõi hiệu suất công việc

### 🎯 Quản lý (Admin)
- **Dashboard tổng quan**: Thống kê doanh thu, đơn hàng
- **Quản lý menu**: Thêm, sửa, xóa món ăn
- **Quản lý nhân viên**: Phân quyền và theo dõi nhân viên
- **Báo cáo chi tiết**: Biểu đồ và phân tích dữ liệu
- **Quản lý đánh giá**: Xem và phản hồi feedback khách hàng

## 🛠️ Công nghệ sử dụng

### Frontend
- **Nuxt 4** - Framework Vue.js hiện đại
- **Vue 3** - Progressive JavaScript framework
- **TypeScript** - Type-safe development
- **Tailwind CSS** - Utility-first CSS framework
- **Nuxt UI** - Component library cho Nuxt
- **Pinia** - State management
- **Vue Router** - Client-side routing

### UI/UX
- **Lucide Icons** - Icon library
- **Vue3 Toastify** - Toast notifications
- **Dark/Light Theme** - Hỗ trợ chế độ tối/sáng
- **Responsive Design** - Tối ưu cho mọi thiết bị

## 🚀 Cài đặt và chạy

### Yêu cầu hệ thống
- Node.js 18+ 
- npm hoặc yarn

### Cài đặt dependencies
```bash
npm install
```

### Chạy development server
```bash
npm run dev
```

Ứng dụng sẽ chạy tại `http://localhost:3000`

### Build cho production
```bash
npm run build
```

### Preview production build
```bash
npm run preview
```

## 📁 Cấu trúc project

```
sos-fe/
├── components/          # Vue components
│   ├── admin/          # Components cho admin
│   ├── customer/       # Components cho khách hàng
│   ├── staff/          # Components cho nhân viên
│   ├── home/           # Components trang chủ
│   └── ui/             # UI components chung
├── pages/              # Route pages
│   ├── index.vue       # Trang chủ
│   ├── admin.vue       # Dashboard admin
│   ├── customer.vue    # Giao diện khách hàng
│   └── staff.vue       # Giao diện nhân viên
├── composables/        # Vue composables
├── stores/             # Pinia stores
├── assets/             # Static assets
├── layouts/            # Layout components
├── plugins/            # Nuxt plugins
└── utils/              # Utility functions
```

## 🎨 Giao diện

### Trang chủ
- Landing page với hero section
- Giới thiệu các tính năng chính
- Cards hướng dẫn sử dụng

### Khách hàng
- Menu items với hình ảnh và giá
- Giỏ hàng với tính năng cập nhật số lượng
- Tìm kiếm và lọc theo danh mục
- Đánh giá dịch vụ

### Nhân viên
- Grid hiển thị trạng thái các bàn
- Thông báo real-time
- Quick actions cho các tác vụ thường dùng

### Quản lý
- Dashboard với charts và thống kê
- CRUD operations cho menu và nhân viên
- Tabs navigation cho các chức năng

## 🔧 Scripts có sẵn

```bash
npm run dev          # Chạy development server
npm run build        # Build cho production
npm run generate     # Generate static site
npm run preview      # Preview production build
npm run lint         # Chạy ESLint
npm run typecheck    # TypeScript type checking
```

## 🌙 Theme Support

Hệ thống hỗ trợ chế độ tối/sáng với:
- Toggle theme button
- Persistent theme preference
- Tailwind CSS dark mode classes

## 📱 Responsive Design

- Mobile-first approach
- Breakpoints: sm, md, lg, xl
- Touch-friendly interactions
- Optimized for tablet và desktop

## 🔒 Tính năng bảo mật

- Type-safe development với TypeScript
- Input validation
- Error handling
- Secure routing

## 🤝 Đóng góp

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📄 License

Project này được phát triển cho mục đích học tập và demo.

## 📞 Liên hệ

Nếu có câu hỏi hoặc góp ý, vui lòng tạo issue trên repository.

---

**Gọi Món** - Hệ thống đặt món thông minh cho nhà hàng hiện đại 🍽️✨ 