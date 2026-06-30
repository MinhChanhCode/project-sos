export const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

export const formatOrderTime = (time: string | Date) => {
  if (!time) return 'Session hiện tại'
  
  try {
    const date = new Date(time)
    if (isNaN(date.getTime())) return 'Session hiện tại'
    
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / (1000 * 60))
    
    if (diffMins < 1) return 'Vừa xong'
    if (diffMins < 60) return `${diffMins} phút trước`
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)} giờ trước`
    
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Session hiện tại'
  }
}

export const getBadgeColor = (badge: string) => {
  const colors: Record<string, string> = {
    'Hot': 'red',
    'Mới': 'green',
    'Ưu đãi': 'orange',
    'Combo': 'purple',
    'Tiết kiệm': 'blue',
    'Đặc biệt': 'pink'
  }
  return colors[badge] || 'gray'
}

export const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    // Trạng thái bàn (tiếng Việt)
    'trống': 'gray',
    'đang đặt': 'blue',
    'chờ phục vụ': 'yellow',
    'đang ăn': 'green',
    'thanh toán': 'purple',
    'đã phục vụ': 'blue',
    'sẵn sàng': 'green',
    'đang chế biến': 'yellow',
    // Trạng thái cũ (backward compatibility)
    'empty': 'gray',
    'ordering': 'blue',
    'waiting': 'yellow',
    'eating': 'green',
    'payment': 'purple',
    'active': 'green',
    'break': 'yellow',
    'offline': 'gray',
    'pending': 'gray',
    'preparing': 'yellow',
    'ready': 'green',
    'served': 'blue'
  }
  return colors[status] || 'gray'
}

export const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    // Trạng thái bàn (tiếng Việt)
    'trống': 'Trống',
    'đang đặt': 'Đang đặt món',
    'chờ phục vụ': 'Chờ phục vụ',
    'đang ăn': 'Đang ăn',
    'thanh toán': 'Thanh toán',
    'đã phục vụ': 'Đã phục vụ',
    'sẵn sàng': 'Sẵn sàng',
    'đang chế biến': 'Đang chế biến',
    // Trạng thái cũ (backward compatibility)
    'empty': 'Trống',
    'ordering': 'Đang gọi món',
    'waiting': 'Chờ phục vụ',
    'eating': 'Đang dùng bữa',
    'payment': 'Thanh toán',
    'active': 'Đang làm việc',
    'break': 'Nghỉ giải lao',
    'offline': 'Offline'
  }
  return texts[status] || status
}

// Derive pseudo status for order item based on quantities (backend no longer returns status)
export const deriveOrderItemStatus = (item: any): string => {
  const pending = Number(item?.pendingQuantity || 0)
  const preparing = Number(item?.preparingQuantity || 0)
  const completed = Number(item?.completedQuantity || 0)
  const served = Number(item?.servedQuantity || 0)
  const cancelled = Number(item?.cancelledQuantity || 0)
  const out = Number(item?.outOfStockQuantity || 0)
  if (served > 0 && pending + preparing + completed === 0) return 'SERVED'
  if (completed > 0 && pending + preparing === 0) return 'COMPLETED'
  if (preparing > 0) return 'PREPARING'
  if (pending > 0) return 'PENDING'
  if (cancelled > 0) return 'CANCELLED'
  if (out > 0) return 'OUT_OF_STOCK'
  return 'UNKNOWN'
}