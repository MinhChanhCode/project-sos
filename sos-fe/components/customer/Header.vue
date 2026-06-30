<template>
  <header class="sticky top-0 z-30 bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-sm">
    <div class="flex items-center justify-between gap-3 px-4 py-3">
      <NuxtLink to="/" class="flex h-10 w-10 items-center justify-center rounded-full bg-white/15 transition hover:bg-white/25">
        <Icon name="lucide:chevron-left" class="h-5 w-5" />
      </NuxtLink>

      <div class="min-w-0 flex-1">
        <div class="flex items-center gap-2">
          <h1 class="truncate text-lg font-bold">Gọi Món</h1>
          <span class="rounded-full bg-white/20 px-2 py-0.5 text-xs font-medium">
            Bàn {{ tableNumber || "--" }}
          </span>
        </div>
        <p class="truncate text-xs text-orange-50">Đặt món nhanh như đi chợ online</p>
      </div>

      <button
        class="relative flex h-10 w-10 items-center justify-center rounded-full bg-white text-orange-500 shadow-md transition hover:scale-105"
        type="button"
        @click="$emit('showCart')"
        title="Giỏ hàng"
      >
        <Icon name="lucide:shopping-cart" class="h-5 w-5" />
        <span
          v-if="totalItems > 0"
          class="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[11px] font-bold text-white ring-2 ring-orange-500"
        >
          {{ totalItems }}
        </span>
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
interface Props {
  tableNumber: string
  totalItems: number
}

defineProps<Props>()
defineEmits<{
  showCart: []
}>()
</script>
