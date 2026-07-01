<template>
  <UModal v-model="isOpen">
    <UCard v-if="item">
      <template #header>
        <h2 class="text-lg font-semibold">Chỉnh sửa món ăn</h2>
      </template>

      <UForm :state="item" class="space-y-4">
        <UFormGroup label="Tên món" name="name">
          <UInput v-model="item.name" />
        </UFormGroup>
        
        <UFormGroup label="Giá" name="price">
          <UInput v-model="item.price" type="number" />
        </UFormGroup>
        
        <UFormGroup label="Danh mục" name="category">
          <UInput v-model="item.categoryId" />
        </UFormGroup>
        
        <UFormGroup label="Trạng thái">
          <UToggle v-model="item.isAvailable" />
          <span class="ml-2 text-sm">Có sẵn</span>
        </UFormGroup>

        <div class="rounded-lg border border-orange-100 bg-orange-50/60 p-3 dark:border-gray-800 dark:bg-gray-900">
          <div class="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-800 dark:text-gray-100">
            <Icon name="lucide:sparkles" class="h-4 w-4 text-orange-500" />
            Dữ liệu AI tư vấn món
          </div>
          <div class="grid gap-3 sm:grid-cols-2">
            <UFormGroup label="Loại món">
              <USelect v-model="item.type" :options="menuTypeOptions" />
            </UFormGroup>
            <UFormGroup label="Độ cay (0-3)">
              <UInput v-model.number="item.spicyLevel" type="number" min="0" max="3" />
            </UFormGroup>
            <UFormGroup label="Thời gian chuẩn bị">
              <UInput v-model.number="item.prepTimeMinutes" type="number" min="1" />
            </UFormGroup>
            <UFormGroup label="Ăn chay">
              <div class="flex h-10 items-center gap-2">
                <UToggle v-model="item.isVegetarian" />
                <span class="text-sm text-gray-600 dark:text-gray-300">Phù hợp ăn chay</span>
              </div>
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Tag khẩu vị">
              <UInput v-model="item.tasteTags" placeholder="ngọt, béo, thanh nhẹ, cay..." />
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Nguyên liệu">
              <UTextarea v-model="item.ingredients" rows="2" />
            </UFormGroup>
            <UFormGroup label="Dị ứng/cần tránh">
              <UInput v-model="item.allergens" placeholder="hải sản, sữa, đậu phộng..." />
            </UFormGroup>
            <UFormGroup label="Phù hợp với">
              <UInput v-model="item.suitableFor" />
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Gợi ý đi kèm">
              <UInput v-model="item.pairing" />
            </UFormGroup>
          </div>
        </div>
      </UForm>

      <template #footer>
        <div class="flex space-x-2">
          <UButton @click="$emit('close')" variant="outline" class="flex-1">
            Hủy
          </UButton>
          <UButton @click="$emit('save', item)" class="flex-1">
            Lưu thay đổi
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MenuItem } from '~/stores/cart'

interface Props {
  modelValue: boolean
  item: MenuItem | null
}

const props = defineProps<Props>()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
  save: [item: MenuItem]
}>()

const menuTypeOptions = [
  { label: 'Món chính', value: 'MAIN' },
  { label: 'Đồ uống', value: 'DRINK' },
  { label: 'Khai vị', value: 'APPETIZER' },
  { label: 'Tráng miệng', value: 'DESSERT' },
  { label: 'Combo', value: 'COMBO' },
]
</script>
