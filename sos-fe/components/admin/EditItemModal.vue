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
          <UInput v-model="item.category" />
        </UFormGroup>
        
        <UFormGroup label="Trạng thái">
          <UToggle v-model="item.available" />
          <span class="ml-2 text-sm">Có sẵn</span>
        </UFormGroup>
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
</script>