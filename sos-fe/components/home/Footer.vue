<template>
  <footer class="border-t border-white/10 bg-slate-950/95">
    <div class="mx-auto grid max-w-7xl gap-8 px-5 py-10 md:grid-cols-2 md:px-10 xl:grid-cols-[1.2fr_1fr_1fr_1fr]">
      <div>
        <div class="mb-4 flex items-center gap-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-orange-500 to-red-600 text-white shadow-lg shadow-orange-500/25">
            <Icon name="lucide:chef-hat" class="h-7 w-7" />
          </div>
          <div>
            <h3 class="text-2xl font-black text-white">Bếp <span class="text-orange-500">Mẹ Hương</span></h3>
            <p class="text-sm text-slate-300">Vẹn nguyên tinh túy ẩm thực Việt</p>
          </div>
        </div>
        <p class="max-w-sm text-sm leading-7 text-slate-300">
          Giải pháp đặt món thông minh dành cho nhà hàng, giúp nâng cao trải nghiệm khách hàng và tối ưu vận hành.
        </p>
        <p class="mt-5 text-sm text-slate-400">© 2026 Bếp Mẹ Hương. All rights reserved.</p>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-normal text-white">Thông tin nhà hàng</h4>
        <ul class="space-y-4 text-sm text-slate-300">
          <li class="flex gap-2">
            <Icon name="lucide:map-pin" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>30 Trần Quang Diệu, Quy Nhơn, Bình Định</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:phone" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>0905 123 456</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:globe-2" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>bepmehuong.vn</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:clock-3" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>09:00 - 22:00, tất cả các ngày</span>
          </li>
        </ul>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-normal text-white">Dịch vụ</h4>
        <ul class="space-y-4 text-sm text-slate-300">
          <li class="flex gap-2"><Icon name="lucide:check" class="mt-0.5 h-4 w-4 text-orange-500" />Đặt món bằng mã QR tại bàn</li>
          <li class="flex gap-2"><Icon name="lucide:check" class="mt-0.5 h-4 w-4 text-orange-500" />Theo dõi trạng thái món ăn</li>
          <li class="flex gap-2"><Icon name="lucide:check" class="mt-0.5 h-4 w-4 text-orange-500" />Hỗ trợ nhân viên và bếp real-time</li>
          <li class="flex gap-2"><Icon name="lucide:check" class="mt-0.5 h-4 w-4 text-orange-500" />Quản lý thực đơn, doanh thu, đánh giá</li>
        </ul>
      </div>

      <div>
        <h4 class="mb-4 text-sm font-bold uppercase tracking-normal text-white">Kết nối với chúng tôi</h4>
        <ul class="space-y-4 text-sm text-slate-300">
          <li class="flex gap-2">
            <Icon name="lucide:mail" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>chanhlm.21it@vku.udn.vn</span>
          </li>
          <li class="flex gap-2">
            <Icon name="lucide:globe-2" class="mt-0.5 h-4 w-4 text-orange-500" />
            <span>www.bepmehuong.vn</span>
          </li>
        </ul>
        <div class="mt-6 flex gap-3">
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-600 text-white"><Icon name="lucide:thumbs-up" class="h-5 w-5" /></span>
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-sky-600 text-white"><Icon name="lucide:message-circle" class="h-5 w-5" /></span>
          <span class="flex h-10 w-10 items-center justify-center rounded-full bg-green-600 text-white"><Icon name="lucide:phone" class="h-5 w-5" /></span>
        </div>
      </div>
    </div>
  </footer>
</template>
