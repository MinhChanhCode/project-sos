<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold">Hóa đơn</h2>
        <p class="text-sm text-gray-500">Xem lại bill, trạng thái thanh toán và QR demo</p>
      </div>
      <UButton variant="outline" size="sm" @click="$emit('refresh')">
        <Icon name="lucide:refresh-cw" class="mr-1 h-4 w-4" />
        Làm mới
      </UButton>
    </div>

    <div class="grid gap-4 lg:grid-cols-2">
      <UCard v-for="invoice in invoices" :key="invoice.id || invoice.orderId">
        <div class="flex items-start justify-between gap-3">
          <div>
            <div class="font-semibold">{{ invoice.invoiceCode || `INV-${invoice.orderId}` }}</div>
            <div class="mt-1 text-sm text-gray-500">
              {{ invoice.tableName || "Chưa rõ bàn" }} · {{ invoice.customerName || "Khách hàng" }}
            </div>
            <div class="mt-1 text-xs text-gray-400">{{ formatDate(invoice.createdAt) }}</div>
          </div>
          <UBadge :color="invoice.status === 'PAID' ? 'green' : 'yellow'" variant="soft">
            {{ invoice.status === 'PAID' ? 'Đã thanh toán' : 'Chờ thanh toán' }}
          </UBadge>
        </div>

        <div class="mt-4 rounded-lg border">
          <div
            v-for="item in invoice.items || []"
            :key="item.orderItemId"
            class="flex items-center justify-between border-b px-3 py-2 text-sm last:border-b-0"
          >
            <span>{{ item.menuItemName }} x{{ item.quantity }}</span>
            <b>{{ formatMoney(Number(item.lineTotal || 0)) }}</b>
          </div>
        </div>

        <div class="mt-4 flex items-end justify-between gap-4">
          <div class="text-sm">
            <div>Tạm tính: <b>{{ formatMoney(Number(invoice.subtotal || 0)) }}</b></div>
            <div v-if="Number(invoice.discount || 0) > 0">Giảm giá: <b>{{ formatMoney(Number(invoice.discount || 0)) }}</b></div>
            <div v-if="Number(invoice.serviceFee || 0) > 0">Phí dịch vụ: <b>{{ formatMoney(Number(invoice.serviceFee || 0)) }}</b></div>
            <div class="mt-1 text-base">Tổng: <b class="text-orange-600">{{ formatMoney(Number(invoice.total || 0)) }}</b></div>
          </div>
          <qrcode-vue :value="invoice.paymentQrPayload || ''" :size="112" level="M" render-as="svg" />
        </div>
      </UCard>
    </div>

    <p v-if="!invoices?.length" class="py-10 text-center text-gray-400">Chưa có hóa đơn</p>
  </div>
</template>

<script setup lang="ts">
import QrcodeVue from "qrcode.vue";

defineProps<{ invoices: any[] }>();
defineEmits<{ refresh: [] }>();

const formatMoney = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value || 0);

const formatDate = (value?: string) => {
  if (!value) return "";
  try {
    return new Date(value).toLocaleString("vi-VN");
  } catch {
    return "";
  }
};
</script>
