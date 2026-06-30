<template>
  <section class="px-4 pb-4">
    <div class="mb-3 flex items-center justify-between">
      <div>
        <h2 class="text-base font-bold text-gray-900 dark:text-white">Gợi ý hôm nay</h2>
        <p class="text-xs text-gray-500 dark:text-gray-400">Món bán chạy được khách chọn nhiều</p>
      </div>
      <span class="inline-flex items-center rounded-full bg-orange-100 px-3 py-1 text-xs font-semibold text-orange-700 dark:bg-orange-500/15 dark:text-orange-300">
        <Icon name="lucide:flame" class="mr-1 h-3.5 w-3.5" />
        Hot
      </span>
    </div>

    <div class="rounded-2xl bg-white p-3 shadow-sm ring-1 ring-orange-100 dark:bg-gray-900 dark:ring-gray-800">
      <div class="mb-3 flex items-center gap-2 rounded-full bg-gray-50 px-3 py-2 dark:bg-gray-800">
        <Icon name="lucide:search" class="h-4 w-4 text-orange-500" />
        <input
          v-model="keyword"
          class="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400 dark:text-white"
          placeholder="Tìm trong món bán chạy..."
        >
      </div>
      <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <CustomerMenuItem
          v-for="item in filteredBestSellers"
          :key="item.id"
          :item="item"
          :is-best-seller-item="true"
          @add-to-cart="$emit('add-to-cart', $event)"
        />
      </div>
      <div v-if="isLoadingSuggestions" class="py-4 text-center text-sm text-gray-500">
        Đang tải gợi ý...
      </div>
    </div>
  </section>
  
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useMenuStore } from "~/stores/menu";
import { computed, ref } from "vue";

defineEmits<{
  'add-to-cart': [payload: any]
}>();

const menuStore = useMenuStore();
const { bestSellers, isLoadingSuggestions } = storeToRefs(menuStore);

const keyword = ref("");
const filteredBestSellers = computed(() => {
  const q = keyword.value.trim().toLowerCase();
  if (!q) return bestSellers.value;
  return bestSellers.value.filter((i) =>
    i.name.toLowerCase().includes(q) ||
    (i.description || "").toLowerCase().includes(q)
  );
});

try {
  // Luôn ưu tiên gọi API, khi lỗi thì fallback
  menuStore.fetchSuggestions(6);
} catch {
  menuStore.hydrateSuggestionsFallback(6);
}
</script>

