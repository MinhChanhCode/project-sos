<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-[#f5f5f5] dark:bg-gray-950">
      <!-- Header -->
      <CustomerHeader
        :table-number="tableNumber"
        :total-items="cartStore.totalItems"
        @show-cart="showCart = true"
      />

      <section class="bg-gradient-to-br from-orange-500 via-orange-500 to-red-500 text-white">
        <div class="px-4 pb-5 pt-3">
          <div class="rounded-2xl bg-white/15 p-4 shadow-sm ring-1 ring-white/20 backdrop-blur">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-medium uppercase tracking-wide text-orange-100">Xin chào</p>
                <h1 class="mt-1 text-xl font-bold">Hôm nay bạn muốn ăn gì?</h1>
                <p class="mt-1 text-sm text-orange-50">
                  Bàn {{ tableNumber || "của bạn" }} · Gọi món nhanh, bếp nhận ngay
                </p>
              </div>
              <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-white text-orange-500 shadow-md">
                <Icon name="lucide:utensils" class="h-7 w-7" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Search -->
      <div class="sticky top-0 z-20 bg-[#f5f5f5]/95 px-4 py-3 backdrop-blur dark:bg-gray-950/95">
        <div class="flex items-center gap-2 rounded-full bg-white p-1.5 shadow-sm ring-1 ring-orange-100 dark:bg-gray-900 dark:ring-gray-800">
          <Icon name="lucide:search" class="ml-3 h-5 w-5 text-orange-500" />
          <input
            v-model="searchQuery"
            placeholder="Tìm món, đồ uống, combo..."
            class="min-w-0 flex-1 bg-transparent px-1 py-2 text-sm text-gray-900 outline-none placeholder:text-gray-400 dark:text-white"
          >
          <button
            class="rounded-full bg-orange-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-orange-600"
            type="button"
          >
            Tìm
          </button>
        </div>
      </div>

      <!-- Categories -->
      <div class="px-4 pb-2">
        <div class="flex gap-2 overflow-x-auto pb-2">
          <UButton
            v-for="category in categories"
            :key="category.id"
            @click="selectedCategory = category.id"
            :variant="selectedCategory === category.id ? 'solid' : 'soft'"
            :color="selectedCategory === category.id ? 'orange' : 'gray'"
            size="sm"
            class="shrink-0 rounded-full px-4 shadow-sm"
          >
            <span class="mr-1">{{ category.icon }}</span>
            {{ category.name }}
          </UButton>
        </div>
      </div>

      <!-- Suggestions -->
      <CustomerSuggestionsSection @add-to-cart="addToCart" />

      <!-- Menu Items -->
      <div class="px-4 pb-28">
        <div class="mb-3 flex items-center justify-between">
          <div>
            <h2 class="text-base font-bold text-gray-900 dark:text-white">Tất cả món</h2>
            <p class="text-xs text-gray-500 dark:text-gray-400">Chọn món yêu thích và thêm vào giỏ</p>
          </div>
          <UBadge color="orange" variant="soft">{{ filteredItems.length }} món</UBadge>
        </div>
        <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <CustomerMenuItem
            v-for="item in filteredItems"
            :key="item.id"
            :item="item"
            :is-adding-to-cart="addingToCart.has(item.id)"
            @add-to-cart="addToCart"
          />
        </div>
      </div>

      <!-- Service Request Button -->
      <CustomerServiceRequestButton
        :table-id="tableId"
        :table-name="tableName"
        :session-id="sessionId"
      />

      <!-- Bottom Actions -->
      <div
        class="fixed bottom-0 left-0 right-0 z-40 border-t border-orange-100 bg-white/95 p-3 shadow-[0_-8px_24px_rgba(0,0,0,0.08)] backdrop-blur dark:border-gray-800 dark:bg-gray-900/95"
      >
        <div class="mx-auto flex w-full max-w-4xl items-center gap-3">
          <button
            class="flex h-11 w-11 items-center justify-center rounded-full border border-orange-200 bg-orange-50 text-orange-600 dark:border-gray-700 dark:bg-gray-800"
            type="button"
            @click="showRatingDialog = true"
            title="Đánh giá"
          >
            <Icon name="lucide:star" class="h-5 w-5" />
          </button>
          <button
            class="flex flex-1 items-center justify-center rounded-full bg-gradient-to-r from-orange-500 to-red-500 px-4 py-3 text-sm font-bold text-white shadow-lg shadow-orange-500/25 transition hover:from-orange-600 hover:to-red-600"
            type="button"
            @click="showCart = true"
          >
            <Icon name="lucide:shopping-cart" class="mr-2 h-5 w-5" />
            Xem giỏ hàng · {{ cartStore.totalItems }} món
          </button>
        </div>
      </div>

                            <!-- Cart Modal -->
               <CustomerCartModal
          v-model="showCart"
          :table-number="tableNumber"
          :items="cartStore.items"
          :ordered-items="cartStore.orderedItems"
          :total-price="cartStore.totalPriceAll"
          :is-empty="cartStore.isEmpty"
          :updating-quantity="updatingQuantity"
          @update-quantity="updateQuantity"
          @update-note="updateNote"
          @remove-item="removeFromCart"
          @confirm-order="confirmOrder"
        />

      <!-- Rating Modal -->
      <UModal v-model="showCustomerNameDialog" prevent-close>
        <UCard>
          <template #header>
            <h2 class="text-lg font-semibold">Tên bạn là gì?</h2>
          </template>
          <form class="space-y-4" @submit.prevent="saveCustomerName">
            <UFormGroup label="Tên khách hàng">
              <UInput
                v-model="customerName"
                placeholder="Nhập tên của bạn"
                autofocus
              />
            </UFormGroup>
            <UButton class="w-full" type="submit" :loading="savingCustomerName">
              Bắt đầu đặt món
            </UButton>
          </form>
        </UCard>
      </UModal>

      <!-- Rating Modal -->
      <UModal v-model="showRatingDialog">
        <UCard>
          <template #header>
            <h2 class="text-lg font-semibold">Đánh giá dịch vụ</h2>
          </template>
          <div class="space-y-4">
            <UFormGroup label="Đánh giá (1-5 sao)">
              <div class="flex space-x-1">
                <Icon
                  v-for="star in 5"
                  :key="star"
                  name="lucide:star"
                  class="w-6 h-6 cursor-pointer"
                  :class="star <= ratingValue ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'"
                  @click="ratingValue = star"
                />
              </div>
            </UFormGroup>
            <UFormGroup label="Ghi chú thêm">
              <UTextarea v-model="ratingComment" placeholder="Chia sẻ trải nghiệm của bạn..." rows="3" />
            </UFormGroup>
          </div>
          <template #footer>
            <UButton class="w-full" @click="submitRating">Gửi đánh giá</UButton>
          </template>
        </UCard>
      </UModal>

      <CustomerChatbot :session-id="sessionId" />
      <CustomerStaffChat
        :table-id="tableId || ''"
        :table-name="tableName"
        :session-id="sessionId"
        :customer-name="customerName"
      />
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useCustomer } from "~/composables/useCustomer";
import { useHead, useNuxtApp } from "nuxt/app";
import { onMounted, watch, computed } from "vue";
import CustomerSuggestionsSection from "~/components/customer/SuggestionsSection.vue";

// Meta
useHead({
  title: "Đặt món - Gọi Món",
});

// Use composable
const {
  cartStore,
  showCart,
  showRatingDialog,
  showCustomerNameDialog,
  savingCustomerName,
  ratingValue,
  ratingComment,
  customerName,
  selectedCategory,
  searchQuery,
  tableNumber,
  // expose tableId for realtime subscription
  // @ts-ignore
  tableId,
  sessionId, // Add sessionId from useCustomer
  categories,
  filteredItems,
  addToCart,
  confirmOrder,
  submitRating,
  updateQuantity,
  updateNote,
  removeFromCart,
  saveCustomerName,
  ensureTableClearedState,
  ensureCart,
  addingToCart,
  updatingQuantity,
} = useCustomer();

// Table name for service request
const tableName = computed(() => `Bàn ${tableNumber.value}`);

// Listen realtime: khi bàn được dọn -> xóa cache local của session hiện tại
onMounted(() => {
  try {
    const nuxt = useNuxtApp() as any;
    if (nuxt?.$realtime && tableId) {
      // Avoid duplicate subscription per page lifetime
      const key = `__cust_onCleared_${String(tableId.value)}`;
      if (!(window as any)[key]) {
        (window as any)[key] = true;
        nuxt.$realtime.onTableCleared(String(tableId.value), () => {
          const url = new URL(window.location.href);
          const sessionId = url.searchParams.get("sessionId");
          if (sessionId) {
            localStorage.removeItem(`cart:${sessionId}`);
          }
          // reset UI
          cartStore.clearCart();
          // Không xóa orderedItems vì sẽ được sync từ server
        });
      }
    }
  } catch {}
});

// Khi mở giỏ hàng, đồng bộ lại trạng thái bàn để xoá lịch sử nếu bàn đã dọn
watch(showCart, async (open) => {
  if (open) {
    try {
      await ensureTableClearedState();
      // Đồng bộ dữ liệu giỏ hàng mới nhất từ server nếu có
      await ensureCart();
    } catch {}
  }
});
</script>
