# Screens And Images

## Danh sach man hinh frontend

| Man hinh | File | Nguoi dung | Chuc nang |
|---|---|---|---|
| Trang chu | `sos-fe/app/pages/index.vue` | Khach / nhan vien / quan ly | Gioi thieu ung dung, dieu huong vao cac khu vuc chinh |
| Dang nhap | `sos-fe/app/pages/login.vue` | STAFF, KITCHEN, MANAGER, ADMIN | Dang nhap vao he thong noi bo |
| Customer | `sos-fe/app/pages/customer.vue` | Khach hang | Luong khach hang tong quan |
| Customer Table | `sos-fe/app/pages/customer/table/[tableNumber].vue` | Khach hang | Man hinh sau khi quet QR ban, xem menu, gio hang, chatbot, goi nhan vien |
| Staff | `sos-fe/app/pages/staff.vue` | Nhan vien phuc vu | Xem ban, yeu cau dich vu, chat voi khach, cap nhat phuc vu |
| Kitchen | `sos-fe/app/pages/kitchen.vue` | Bep | Xem mon can lam, cap nhat trang thai mon |
| Admin | `sos-fe/app/pages/admin.vue` | Quan ly / admin | Dashboard, menu, ban, QR, nhan vien, hoa don, danh gia |

## Component theo man hinh

### Trang chu

| Component | File | Chuc nang |
|---|---|---|
| `HomeHeader` | `sos-fe/app/components/home/HomeHeader.vue` | Header trang chu |
| `HeroSection` | `sos-fe/app/components/home/HeroSection.vue` | Khu vuc mo dau |
| `FeaturesSection` | `sos-fe/app/components/home/FeaturesSection.vue` | Gioi thieu tinh nang |
| `InterfaceCard` | `sos-fe/app/components/home/InterfaceCard.vue` | Card dieu huong/gioi thieu interface |
| `Footer` | `sos-fe/app/components/home/Footer.vue` | Chan trang |

### Khach hang

| Component | File | Chuc nang |
|---|---|---|
| `Header` | `sos-fe/app/components/customer/Header.vue` | Header man hinh khach |
| `MenuItem` | `sos-fe/app/components/customer/MenuItem.vue` | Hien thi mot mon trong menu |
| `CartModal` | `sos-fe/app/components/customer/CartModal.vue` | Gio hang va xac nhan mon |
| `Chatbot` | `sos-fe/app/components/customer/Chatbot.vue` | Chat voi AI goi y mon |
| `SuggestionsSection` | `sos-fe/app/components/customer/SuggestionsSection.vue` | Goi y mon |
| `ServiceRequestButton` | `sos-fe/app/components/customer/ServiceRequestButton.vue` | Goi nhan vien / yeu cau ho tro |
| `StaffChat` | `sos-fe/app/components/customer/StaffChat.vue` | Chat voi nhan vien |

### Staff

| Component | File | Chuc nang |
|---|---|---|
| `TablesGrid` | `sos-fe/app/components/staff/TablesGrid.vue` | Luoi ban |
| `TableDetailModal` | `sos-fe/app/components/staff/TableDetailModal.vue` | Chi tiet ban |
| `OrderItemStatusManager` | `sos-fe/app/components/staff/OrderItemStatusManager.vue` | Cap nhat trang thai mon |
| `ServiceRequestManager` | `sos-fe/app/components/staff/ServiceRequestManager.vue` | Xu ly yeu cau khach |
| `ChatInbox` | `sos-fe/app/components/staff/ChatInbox.vue` | Hop thu chat |
| `NotificationsModal` | `sos-fe/app/components/staff/NotificationsModal.vue` | Thong bao |
| `QuickActions` | `sos-fe/app/components/staff/QuickActions.vue` | Tac vu nhanh |
| `StatsCards` | `sos-fe/app/components/staff/StatsCards.vue` | The thong ke |

### Admin/Manager

| Component | File | Chuc nang |
|---|---|---|
| `DashboardStats` | `sos-fe/app/components/admin/DashboardStats.vue` | Thong ke dashboard |
| `ChartsSection` | `sos-fe/app/components/admin/ChartsSection.vue` | Bieu do |
| `MenuManager` | `sos-fe/app/components/admin/MenuManager.vue` | Quan ly menu |
| `AddItemModal` | `sos-fe/app/components/admin/AddItemModal.vue` | Them mon |
| `EditItemModal` | `sos-fe/app/components/admin/EditItemModal.vue` | Sua mon |
| `FloorPlanManager` | `sos-fe/app/components/admin/FloorPlanManager.vue` | So do ban |
| `QRManager` | `sos-fe/app/components/admin/QRManager.vue` | Quan ly QR ban |
| `StaffManager` | `sos-fe/app/components/admin/StaffManager.vue` | Quan ly nhan vien |
| `StaffDetailModal` | `sos-fe/app/components/admin/StaffDetailModal.vue` | Chi tiet nhan vien |
| `OrderItemStatusManager` | `sos-fe/app/components/admin/OrderItemStatusManager.vue` | Quan ly trang thai mon |
| `InvoicesSection` | `sos-fe/app/components/admin/InvoicesSection.vue` | Hoa don |
| `ReviewsSection` | `sos-fe/app/components/admin/ReviewsSection.vue` | Danh gia |

### Shared/UI

| Component | File | Chuc nang |
|---|---|---|
| `AppHeader` | `sos-fe/app/components/common/AppHeader.vue` | Header dung chung |
| `ThemeToggle` | `sos-fe/app/components/common/ThemeToggle.vue` | Doi theme |
| `FloorPlanTable` | `sos-fe/app/components/shared/FloorPlanTable.vue` | Hien thi ban trong so do |
| `Button`, `Card`, `Input`, `Label`, `Switch`, `Tabs`, `Textarea`, `Badge`, `ConfirmModal` | `sos-fe/app/components/ui/*` | UI primitives tai su dung |

## Anh menu

Tat ca anh nam trong `sos-fe/public/images/menu`.

| File | Kich thuoc byte | Ghi chu |
|---|---:|---|
| `banh-cuon-nong.jpg` | 65,381 | Anh mon an |
| `banh-flan.jpg` | 68,330 | Anh trang mieng |
| `banh-mi-chao.jpg` | 201,945 | Anh mon an |
| `banh-su-kem.jpg` | 60,808 | Anh trang mieng |
| `bun-bo-hue.jpg` | 158,536 | Anh mon an |
| `bun-cha-ha-noi.jpg` | 136,941 | Anh mon an |
| `bun-rieu-cua.jpg` | 74,877 | Anh mon an |
| `ca-phe-den-da.png` | 160,863 | Anh do uong |
| `ca-phe-sua-da.jpg` | 59,846 | Anh do uong |
| `cacao-nong.jpg` | 70,388 | Anh do uong |
| `cha-gio-hai-san.jpg` | 186,193 | Anh mon an |
| `che-khuc-bach.jpg` | 76,044 | Anh trang mieng |
| `che-thap-cam.jpg` | 49,167 | Anh trang mieng |
| `com-ga-xoi-mo.jpg` | 130,514 | Anh mon an |
| `com-tam-suon-bi-cha.jpg` | 175,018 | Anh mon an |
| `goi-cuon-tom-thit.jpg` | 71,508 | Anh mon an |
| `kem-sau-rieng.jpg` | 198,810 | Anh trang mieng |
| `lau-thai-hai-san.jpg` | 159,648 | Anh mon an |
| `matcha-latte.jpg` | 151,694 | Anh do uong |
| `nuoc-dua-tuoi.jpg` | 37,703 | Anh do uong |
| `nuoc-ep-cam.jpeg` | 16,642 | Anh do uong |
| `nuoc-ep-dua.jpg` | 157,154 | Anh do uong |
| `nuoc-ep-oi.jpg` | 170,995 | Anh do uong |
| `pho-bo-tai.png` | 411,169 | Anh mon an, nen toi uu |
| `sinh-to-bo.jpg` | 65,984 | Anh do uong |
| `sinh-to-xoai.jpg` | 67,888 | Anh do uong |
| `soda-bac-ha.jpg` | 175,425 | Anh do uong |
| `sua-dau-nanh.jpg` | 252,115 | Anh do uong, nen toi uu |
| `tra-chanh.jpg` | 72,112 | Anh do uong |
| `tra-dao-hat-chia.jpg` | 50,634 | Anh do uong |
| `tra-gung-mat-ong.jpg` | 186,753 | Anh do uong |
| `tra-sua-tran-chau.jpg` | 28,895 | Anh do uong |
| `tra-tac-xi-muoi.png` | 664,145 | Anh do uong, nen toi uu |
| `tra-vai.jpg` | 77,296 | Anh do uong |
| `yaourt-da.jpg` | 5,570,534 | Rat lon, nen nen/resize/chuyen WebP |

## Nhan xet ve hinh anh

Anh menu la tai nguyen truc tiep anh huong den toc do load man hinh khach. Phan lon anh o muc chap nhan duoc, nhung `yaourt-da.jpg` lon bat thuong va co the lam cham lan tai dau tien, nhat la khi dung mang di dong. Nen xu ly:

1. Resize ve kich thuoc thuc te UI can hien thi.
2. Nen ve WebP hoac AVIF.
3. Dung lazy loading cho danh sach menu.
4. Dung thumbnail nho cho card menu, chi tai anh lon khi mo chi tiet.
