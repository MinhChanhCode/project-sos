<template>
  <footer class="mt-12 border-t border-orange-100 bg-white/85 backdrop-blur dark:border-white/10 dark:bg-gray-950/75">
    <div class="container mx-auto grid gap-8 px-4 py-10 md:grid-cols-2 lg:grid-cols-4">
      <div>
        <div class="mb-4 flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-orange-500 to-rose-500 text-xl text-white shadow-lg shadow-orange-500/20">
            🍽
          </div>
          <div>
            <h3 class="text-xl font-bold">MonGo</h3>
            <p class="text-sm text-gray-500 dark:text-gray-400">Nhanh hơn mỗi buổi ăn</p>
          </div>
        </div>
        <p class="text-sm leading-6 text-gray-600 dark:text-gray-300">
          Chuyển đổi trải nghiệm phục vụ với giải pháp đặt món bằng QR Code. Nhanh hơn, chính xác hơn và thuận tiện hơn cho cả khách hàng lẫn nhà hàng.
        </p>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-wide text-gray-900 dark:text-white">Thông tin nhà hàng</h4>
        <ul class="space-y-3 text-sm text-gray-600 dark:text-gray-300">
          <li class="flex gap-2">
            <Icon name="lucide:map-pin" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>30 Trần Quang Diệu, Quy Nhơn, Bình Định</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:clock-3" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>09:00 - 22:00, tất cả các ngày</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:phone" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>0375 248 073</span>
          </li>
        </ul>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-wide text-gray-900 dark:text-white">Dịch vụ</h4>
        <ul class="space-y-3 text-sm text-gray-600 dark:text-gray-300">
          <li>Đặt món bằng mã QR tại bàn</li>
          <li>Theo dõi trạng thái món ăn</li>
          <li>Hỗ trợ nhân viên và bếp real-time</li>
          <li>Quản lý thực đơn, doanh thu, đánh giá</li>
        </ul>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-wide text-gray-900 dark:text-white">Kết nối</h4>
        <ul class="space-y-3 text-sm text-gray-600 dark:text-gray-300">
          <li class="flex gap-2">
            <Icon name="lucide:mail" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>chanhlm.21it@vku.udn.vn</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:globe-2" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>www.mongo.vn</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:message-circle" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>facebook.com/goimon</span>
          </li>
        </ul>
      </div>
    </div>

    <div class="border-t border-orange-100 px-4 py-4 text-center text-sm text-gray-500 dark:border-white/10 dark:text-gray-400">
      © 2026 MonGo. Nâng cấp trải nghiệm phục vụ cho nhà hàng Việt.
    </div>
  </footer>
</template>
