<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-xl font-semibold">Quản lý món ăn</h2>
      <UButton @click="$emit('showAddItem')">
        <Icon name="lucide:plus" class="w-4 h-4 mr-2" />
        Thêm món
      </UButton>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="flex justify-center py-8">
      <UIcon name="lucide:loader-2" class="w-8 h-8 animate-spin" />
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="text-center py-8 text-red-600">
      {{ error }}
    </div>

    <!-- Menu items list -->
    <div v-else class="space-y-4">
      <UCard v-for="item in items" :key="item.id">
        <div class="p-4 flex items-center space-x-4">
          <NuxtImg
            :src="getImageUrl(item.imageUrl)"
            :alt="item.name"
            class="w-16 h-16 object-cover rounded-lg"
          />
          <div class="flex-1">
            <div class="flex items-center space-x-2 mb-1">
              <h3 class="font-semibold">{{ item.name }}</h3>
              <div class="flex space-x-1">
                <UBadge
                  v-for="badge in item.badges"
                  :key="badge"
                  :color="getBadgeColor(badge)"
                  variant="solid"
                  size="xs"
                >
                  {{ badge }}
                </UBadge>
              </div>
            </div>
            <div class="flex items-center space-x-4 text-sm text-gray-600">
              <span>{{ item.categoryId }}</span>
              <div class="text-right">
                <div v-if="item.promotionalPrice && item.promotionalPrice < item.price">
                  <div class="text-xs line-through text-gray-400">{{ formatPrice(item.originalPrice || item.price) }}</div>
                  <div class="font-semibold text-orange-600">{{ formatPrice(item.promotionalPrice) }}</div>
                </div>
                <div v-else class="font-semibold text-orange-600">{{ formatPrice(item.price) }}</div>
              </div>
              <div class="flex items-center space-x-1">
                <Icon name="lucide:star" class="w-4 h-4 text-yellow-400" />
                <span>{{ item.rating }}</span>
              </div>
              <span>{{ item.orders || 0 }} đơn</span>
            </div>
          </div>
          <div class="flex items-center space-x-2">
            <UToggle v-model="item.isAvailable" />
            <span class="text-sm">{{
              item.isAvailable ? "Có sẵn" : "Hết hàng"
            }}</span>
            <UButton
              @click="$emit('editItem', item)"
              variant="outline"
              size="sm"
              square
            >
              <Icon name="lucide:edit" class="w-4 h-4" />
            </UButton>
            <UButton
              @click="openPromotion(item)"
              variant="outline"
              size="sm"
              square
              color="green"
            >
              <Icon name="lucide:badge-percent" class="w-4 h-4" />
            </UButton>
            <UButton
              @click="$emit('deleteItem', item.id)"
              variant="outline"
              size="sm"
              square
              color="red"
            >
              <Icon name="lucide:trash-2" class="w-4 h-4" />
            </UButton>
          </div>
        </div>
      </UCard>
    </div>

    <!-- Pagination controls -->
    <div v-if="!loading && !error" class="space-y-4">
      <!-- Pagination buttons -->
      <div class="flex justify-center">
        <div class="flex items-center space-x-1">
          <!-- Previous button -->
          <UButton
            @click="$emit('changePage', currentPage - 1)"
            :disabled="currentPage === 0 || totalItems === 0"
            variant="outline"
            size="sm"
            class="px-3 py-2"
          >
            <Icon name="lucide:chevron-left" class="w-4 h-4" />
          </UButton>

          <!-- Page numbers -->
          <template v-for="pageNum in visiblePages" :key="pageNum">
            <UButton
              v-if="pageNum !== '...'"
              @click="$emit('changePage', (pageNum as number) - 1)"
              :variant="pageNum === currentPage + 1 ? 'solid' : 'outline'"
              :disabled="totalItems === 0"
              size="sm"
              class="px-3 py-2 min-w-[40px]"
            >
              {{ pageNum }}
            </UButton>
            <span v-else class="px-2 text-gray-500">...</span>
          </template>

          <!-- Next button -->
          <UButton
            @click="$emit('changePage', currentPage + 1)"
            :disabled="currentPage >= totalPages - 1 || totalItems === 0"
            variant="outline"
            size="sm"
            class="px-3 py-2"
          >
            <Icon name="lucide:chevron-right" class="w-4 h-4" />
          </UButton>
        </div>
      </div>

      <!-- Results summary and items per page -->
      <div class="flex justify-between items-center text-sm text-gray-600">
        <!-- Results summary -->
        <div>
          Kết quả: {{ totalItems > 0 ? startItem + 1 : 0 }} - {{ endItem }} /
          {{ totalItems }}
        </div>

        <!-- Items per page selector -->
        <div class="flex items-center space-x-2">
          <span>Hiển thị:</span>
          <USelect
            v-model="itemsPerPage"
            :options="[10, 20, 50, 100]"
            size="sm"
            class="w-20"
            :disabled="totalItems === 0"
            @update:model-value="$emit('changePageSize', $event)"
          />
        </div>
      </div>
    </div>
  </div>

  <!-- Promotion Modal -->
  <UModal v-model="showPromotion">
    <UCard>
      <template #header>
        <div class="flex items-center justify-between w-full">
          <h3 class="font-semibold">Thiết lập khuyến mãi</h3>
          <div class="text-sm text-gray-500" v-if="currentItem">{{ currentItem.name }}</div>
        </div>
      </template>
      <div class="space-y-4">
        <UFormGroup label="Giá gốc">
          <UInput :model-value="formatPrice(currentItem?.price || 0)" disabled />
        </UFormGroup>
        <UFormGroup label="Giá khuyến mãi">
          <UInput v-model.number="promoPrice" type="number" min="0" placeholder="Nhập giá khuyến mãi" />
        </UFormGroup>
        <UFormGroup label="Ngày kết thúc (tuỳ chọn)">
          <UInput v-model="promoEndDate" type="datetime-local" />
        </UFormGroup>
      </div>
      <template #footer>
        <div class="flex justify-between w-full">
          <UButton variant="outline" color="red" @click="onRemovePromotion" :disabled="!currentItem">Bỏ khuyến mãi</UButton>
          <UButton @click="onSavePromotion" :disabled="!canSave">Lưu</UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import type { MenuItem } from "~/stores/cart";
import { formatPrice, getBadgeColor } from "~/utils/formatters";
import { computed, ref } from "vue";
import { getImageUrl } from "~/api-service/MenuApi";
import { menuApi } from "~/api-service/MenuApi";

interface Props {
  items: MenuItem[];
  loading?: boolean;
  error?: string;
  currentPage?: number;
  totalPages?: number;
  totalItems?: number;
  pageSize?: number;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  error: "",
  currentPage: 0,
  totalPages: 0,
  totalItems: 0,
  pageSize: 12,
});

// Computed properties for pagination
const visiblePages = computed(() => {
  const pages = [];
  const current = props.currentPage + 1;
  const total = props.totalPages;

  if (total <= 7) {
    // Show all pages if total is small
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    // Always show first page
    pages.push(1);

    if (current > 3) {
      pages.push("...");
    }

    // Show pages around current
    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 2) {
      pages.push("...");
    }

    // Always show last page
    if (total > 1) {
      pages.push(total);
    }
  }

  return pages;
});

const startItem = computed(
  () => (props.currentPage || 0) * (props.pageSize || 12)
);
const endItem = computed(() => {
  const start = startItem.value;
  const currentItems = props.items?.length || 0;
  const total = props.totalItems || 0;

  // If we have items on this page, calculate end based on current page
  if (currentItems > 0) {
    return start + currentItems;
  }

  // If no items, return start (which will show as start - start)
  return start;
});
const itemsPerPage = computed({
  get: () => props.pageSize,
  set: (value: number) => emit("changePageSize", value),
});

const emit = defineEmits<{
  showAddItem: [];
  editItem: [item: MenuItem];
  deleteItem: [id: number];
  changePage: [page: number];
  changePageSize: [size: number];
}>();

// Promotion modal state
const showPromotion = ref(false);
const currentItem = ref<MenuItem | null>(null);
const promoPrice = ref<number | null>(null);
const promoEndDate = ref<string | null>(null);

const openPromotion = (item: MenuItem) => {
  currentItem.value = item;
  promoPrice.value = (item.promotionalPrice as number) || null;
  promoEndDate.value = item.promotionEndDate || null;
  showPromotion.value = true;
};

const canSave = computed(() => {
  return !!currentItem.value && typeof promoPrice.value === 'number' && promoPrice.value > 0 && promoPrice.value < (currentItem.value?.price || 0);
});

const onSavePromotion = async () => {
  if (!currentItem.value || !canSave.value) return;
  await menuApi.upsertPromotion({
    menuItemId: currentItem.value.id,
    promotionalPrice: promoPrice.value as number,
    endDate: promoEndDate.value || undefined,
  });
  // Update local item
  currentItem.value.promotionalPrice = promoPrice.value as number;
  currentItem.value.originalPrice = currentItem.value.price;
  currentItem.value.promotionEndDate = promoEndDate.value || undefined as any;
  showPromotion.value = false;
};

const onRemovePromotion = async () => {
  if (!currentItem.value) return;
  await menuApi.removePromotion(currentItem.value.id);
  currentItem.value.promotionalPrice = undefined;
  currentItem.value.originalPrice = undefined;
  currentItem.value.promotionEndDate = undefined as any;
  showPromotion.value = false;
};
</script>
