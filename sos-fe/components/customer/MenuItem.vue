<template>
  <article class="overflow-hidden rounded-xl bg-white shadow-sm ring-1 ring-gray-100 transition hover:-translate-y-0.5 hover:shadow-md dark:bg-gray-900 dark:ring-gray-800">
    <div class="relative aspect-[4/3] overflow-hidden bg-gray-100 dark:bg-gray-800">
      <img
        :src="imageSrc"
        :alt="item.name"
        class="h-full w-full object-cover transition duration-300 hover:scale-105"
        loading="lazy"
        @error="useFallbackImage"
      />
      <div class="absolute left-2 top-2 flex flex-wrap gap-1">
        <UBadge
          v-for="badge in item.badges"
          :key="badge"
          :color="getBadgeColor(badge)"
          variant="solid"
          size="xs"
        >
          {{ badge }}
        </UBadge>
        <span v-if="isBestSeller" class="inline-flex items-center rounded-full bg-orange-500 px-2 py-0.5 text-[11px] font-bold text-white shadow">
          <Icon name="lucide:flame" class="mr-1 h-3 w-3" />
          Bán chạy
        </span>
        <span v-if="hasPromotion" class="rounded-full bg-emerald-500 px-2 py-0.5 text-[11px] font-bold text-white shadow">
          Sale
        </span>
      </div>
    </div>

    <div class="p-3">
      <h3 class="line-clamp-2 min-h-[2.5rem] text-sm font-semibold leading-5 text-gray-900 dark:text-white">
        {{ item.name }}
      </h3>

      <p class="mt-1 line-clamp-2 min-h-[2rem] text-xs leading-4 text-gray-500 dark:text-gray-400">
        {{ item.description || "Món ngon được chuẩn bị nóng hổi tại bếp." }}
      </p>

      <div class="mt-2 flex items-center justify-between text-xs text-gray-500">
        <div class="flex items-center gap-1">
          <Icon name="lucide:star" class="h-3.5 w-3.5 fill-yellow-400 text-yellow-400" />
          <span>{{ item.rating || "4.8" }}</span>
        </div>
        <div class="flex items-center gap-1">
          <Icon name="lucide:clock" class="h-3.5 w-3.5" />
          <span>{{ item.prepTime || "10-15 phút" }}</span>
        </div>
      </div>

      <div class="mt-3 flex items-end justify-between gap-2">
        <div class="min-w-0">
          <div v-if="hasPromotion">
            <div class="text-xs text-gray-400 line-through">{{ formatPrice(originalPrice) }}</div>
            <div class="text-base font-extrabold text-orange-600">{{ formatPrice(promotionalPrice) }}</div>
            <div v-if="showCountdown" class="mt-0.5 flex items-center text-[10px] text-red-600">
              <Icon name="lucide:timer" class="mr-1 h-3 w-3" />
              <span class="truncate">Còn {{ countdownText }}</span>
            </div>
          </div>
          <div v-else class="text-base font-extrabold text-orange-600">{{ formatPrice(item.price) }}</div>
        </div>
        <button
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-orange-500 text-white shadow-md shadow-orange-500/25 transition hover:bg-orange-600 disabled:cursor-not-allowed disabled:opacity-60"
          type="button"
          :disabled="isAddingToCart"
          @click="$emit('add-to-cart', { ...item, quantity: 1 })"
          title="Thêm vào giỏ"
        >
          <Icon name="lucide:plus" class="h-5 w-5" />
        </button>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { MenuItem } from "~/stores/cart";
import { formatPrice, getBadgeColor } from "~/utils/formatters";
import { computed, onMounted, onBeforeUnmount, ref } from "vue";
import { getFoodFallbackImageUrl, getMenuImageUrl } from "~/utils/foodImages";

interface Props {
  item: MenuItem;
  isAddingToCart?: boolean;
  isBestSellerItem?: boolean; // từ danh sách bán chạy
}

const props = defineProps<Props>();

defineEmits<{
  "add-to-cart": [item: MenuItem & { quantity?: number }];
}>();

const hasPromotion = computed(() => typeof props.item.promotionalPrice === 'number' && props.item.promotionalPrice! > 0 && props.item.promotionalPrice! < props.item.price);
const promotionalPrice = computed(() => hasPromotion.value ? Number(props.item.promotionalPrice) : props.item.price);
const originalPrice = computed(() => hasPromotion.value ? (props.item.originalPrice || props.item.price) : props.item.price);
const imageSrc = computed(() =>
  getMenuImageUrl(
    props.item.imageUrl,
    props.item.name,
    props.item.description,
    props.item.categoryId
  )
);

const useFallbackImage = (event: Event) => {
  const image = event.target as HTMLImageElement;
  const fallback = getFoodFallbackImageUrl(
    props.item.name,
    props.item.description,
    props.item.categoryId
  );
  if (image.src !== fallback) {
    image.src = fallback;
  }
};

// Bán chạy: ưu tiên flag từ props, sau đó dựa trên badge/điểm phổ biến (fallback)
const isBestSeller = computed(() => {
  if (props.isBestSellerItem === true) return true;
  return props.item.badges?.includes('Hot') || (typeof props.item.popularityScore === 'number' && props.item.popularityScore > 0);
});

// ===== Countdown Timer for promotionEndDate =====
const showCountdown = computed(() => hasPromotion.value && !!props.item.promotionEndDate);
const countdownText = ref("");
let timer: any;

function updateCountdown() {
  if (!props.item.promotionEndDate) {
    countdownText.value = "";
    return;
  }
  const end = new Date(props.item.promotionEndDate).getTime();
  const now = Date.now();
  const diff = Math.max(0, end - now);
  if (diff <= 0) {
    countdownText.value = "đã kết thúc";
    return;
  }
  const totalSeconds = Math.floor(diff / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  if (days > 0) {
    countdownText.value = `${days} ngày ${hours} giờ ${minutes} phút`;
  } else if (hours > 0) {
    countdownText.value = `${hours} giờ ${minutes} phút ${seconds} giây`;
  } else {
    countdownText.value = `${minutes} phút ${seconds} giây`;
  }
}

onMounted(() => {
  if (showCountdown.value) {
    updateCountdown();
    timer = setInterval(updateCountdown, 1000);
  }
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>
