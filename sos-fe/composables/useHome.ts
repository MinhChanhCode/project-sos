export const useHome = () => {
  const interfaces = [
    {
      title: 'Khách Hàng',
      description: 'Đặt món qua QR code tại bàn',
      icon: 'lucide:smartphone',
      iconColorClass: 'text-blue-600 dark:text-blue-400',
      iconClass: 'w-16 h-16 bg-blue-100 dark:bg-blue-950/70 rounded-2xl flex items-center justify-center mx-auto mb-4 ring-1 ring-blue-200 dark:ring-blue-500/20',
      features: [
        { icon: 'lucide:qr-code', text: 'Quét QR tại bàn' },
        { icon: 'lucide:users', text: 'Tham gia nhóm bàn' },
        { emoji: '💬', text: 'Chat với nhân viên' }
      ],
      link: '/customer',
      buttonText: 'Trải nghiệm khách hàng',
      buttonClass: 'flex w-full items-center justify-center rounded-lg bg-blue-500 px-4 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:ring-offset-2 dark:focus:ring-offset-gray-950',
      cardClass: 'flex h-full flex-col overflow-hidden rounded-2xl border border-blue-100 bg-white/85 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-blue-200 hover:shadow-xl hover:shadow-blue-500/10 dark:border-white/10 dark:bg-white/5 dark:hover:border-blue-400/30'
    },
    {
      title: 'Nhân Viên',
      description: 'Phục vụ và quản lý đơn hàng',
      icon: 'lucide:chef-hat',
      iconColorClass: 'text-green-600 dark:text-green-400',
      iconClass: 'w-16 h-16 bg-green-100 dark:bg-green-950/70 rounded-2xl flex items-center justify-center mx-auto mb-4 ring-1 ring-green-200 dark:ring-green-500/20',
      features: [
        { emoji: '🧾', text: 'Quản lý đơn hàng' },
        { emoji: '🔔', text: 'Thông báo real-time' },
        { emoji: '💬', text: 'Chat với khách' }
      ],
      link: '/login?redirect=/staff',
      buttonText: 'Giao diện nhân viên',
      buttonClass: 'flex w-full items-center justify-center rounded-lg bg-green-500 px-4 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-400 focus:ring-offset-2 dark:focus:ring-offset-gray-950',
      cardClass: 'flex h-full flex-col overflow-hidden rounded-2xl border border-green-100 bg-white/85 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-green-200 hover:shadow-xl hover:shadow-green-500/10 dark:border-white/10 dark:bg-white/5 dark:hover:border-green-400/30'
    },
    {
      title: 'Quản Lý',
      description: 'Dashboard và quản trị hệ thống',
      icon: 'lucide:bar-chart-3',
      iconColorClass: 'text-purple-600 dark:text-purple-400',
      iconClass: 'w-16 h-16 bg-violet-100 dark:bg-violet-950/70 rounded-2xl flex items-center justify-center mx-auto mb-4 ring-1 ring-violet-200 dark:ring-violet-500/20',
      features: [
        { icon: 'lucide:bar-chart-3', text: 'Dashboard tổng quan' },
        { emoji: '🍽', text: 'Quản lý món ăn' },
        { icon: 'lucide:users', text: 'Quản lý nhân viên' }
      ],
      link: '/login?redirect=/admin',
      buttonText: 'Bảng điều khiển',
      buttonClass: 'flex w-full items-center justify-center rounded-lg bg-violet-500 px-4 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-violet-600 focus:outline-none focus:ring-2 focus:ring-violet-400 focus:ring-offset-2 dark:focus:ring-offset-gray-950',
      cardClass: 'flex h-full flex-col overflow-hidden rounded-2xl border border-violet-100 bg-white/85 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-violet-200 hover:shadow-xl hover:shadow-violet-500/10 dark:border-white/10 dark:bg-white/5 dark:hover:border-violet-400/30'
    },
    {
      title: 'Bếp',
      description: 'Kitchen Display System',
      icon: 'lucide:flame',
      iconColorClass: 'text-red-600 dark:text-red-400',
      iconClass: 'w-16 h-16 bg-rose-100 dark:bg-rose-950/70 rounded-2xl flex items-center justify-center mx-auto mb-4 ring-1 ring-rose-200 dark:ring-rose-500/20',
      features: [
        { emoji: '🔥', text: 'Nhận order realtime' },
        { emoji: '👨‍🍳', text: 'Cập nhật trạng thái món' },
        { emoji: '⏱', text: 'Theo dõi tiến độ' }
      ],
      link: '/login?redirect=/kitchen',
      buttonText: 'Giao diện bếp',
      buttonClass: 'flex w-full items-center justify-center rounded-lg bg-rose-500 px-4 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-rose-600 focus:outline-none focus:ring-2 focus:ring-rose-400 focus:ring-offset-2 dark:focus:ring-offset-gray-950',
      cardClass: 'flex h-full flex-col overflow-hidden rounded-2xl border border-rose-100 bg-white/85 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-rose-200 hover:shadow-xl hover:shadow-rose-500/10 dark:border-white/10 dark:bg-white/5 dark:hover:border-rose-400/30'
    }
  ]

  return {
    interfaces
  }
}
