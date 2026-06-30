<template>
  <UModal v-model="isOpen">
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Thêm món ăn mới</h2>
      </template>

      <UForm :state="form" class="space-y-4">
        <UFormGroup label="Tên món" name="name">
          <UInput v-model="form.name" placeholder="Nhập tên món ăn" />
        </UFormGroup>

        <UFormGroup label="Giá" name="price">
          <UInput v-model="form.price" type="number" placeholder="0" />
        </UFormGroup>

        <UFormGroup label="Danh mục" name="category">
          <UInput
            v-model="form.category"
            type="number"
            placeholder="ID danh mục (ví dụ: 1)"
          />
        </UFormGroup>

        <UFormGroup label="Ảnh món" name="image">
          <div class="space-y-2">
            <input type="file" accept="image/*" @change="onFileChange" />
            <div v-if="form.imageFile" class="text-xs text-gray-500">
              Đã chọn: {{ (form.imageFile as File).name }}
            </div>
          </div>
        </UFormGroup>

        <UFormGroup label="Mô tả" name="description">
          <UTextarea v-model="form.description" placeholder="Mô tả món ăn" />
        </UFormGroup>
      </UForm>

      <template #footer>
        <div class="flex space-x-2">
          <UButton @click="$emit('close')" variant="outline" class="flex-1">
            Hủy
          </UButton>
          <UButton @click="$emit('submit', form)" class="flex-1">
            Thêm món
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed } from "vue";

interface FormData {
  name: string;
  price: number;
  category: string;
  description: string;
  imageFile?: File | null;
}

interface Props {
  modelValue: boolean;
  form: FormData;
}

const props = defineProps<Props>();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  close: [];
  submit: [form: FormData];
}>();

const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files && input.files.length > 0 ? input.files[0] : null;
  // mutate nested field on provided form object (same pattern as other v-models)
  (props.form as any).imageFile = file;
};
</script>
