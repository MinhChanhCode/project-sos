export const useHome = () => {
  const navItems = [
    {
      label: 'Trang chủ',
      icon: 'lucide:home',
      link: '/'
    },
    {
      label: 'Khách hàng',
      icon: 'lucide:user-round',
      link: '/customer'
    },
    {
      label: 'Nhân viên',
      icon: 'lucide:users-round',
      link: '/login?redirect=/staff'
    },
    {
      label: 'Quản lý',
      icon: 'lucide:bar-chart-3',
      link: '/login?redirect=/admin'
    },
    {
      label: 'Bếp',
      icon: 'lucide:chef-hat',
      link: '/login?redirect=/kitchen'
    }
  ]

  const interfaces = [
    {
      title: 'Khách Hàng',
      description: 'Đặt món qua QR code tại bàn',
      icon: 'lucide:qr-code',
      accent: 'orange',
      link: '/customer',
      buttonText: 'Trải nghiệm khách hàng'
    },
    {
      title: 'Nhân Viên',
      description: 'Phục vụ và quản lý đơn hàng',
      icon: 'lucide:user-round',
      accent: 'green',
      link: '/login?redirect=/staff',
      buttonText: 'Giao diện nhân viên'
    },
    {
      title: 'Quản Lý',
      description: 'Dashboard và quản trị hệ thống',
      icon: 'lucide:bar-chart-3',
      accent: 'violet',
      link: '/login?redirect=/admin',
      buttonText: 'Bảng điều khiển'
    },
    {
      title: 'Bếp',
      description: 'Kitchen Display System',
      icon: 'lucide:chef-hat',
      accent: 'red',
      link: '/login?redirect=/kitchen',
      buttonText: 'Giao diện bếp'
    }
  ] as const

  return {
    navItems,
    interfaces
  }
}
