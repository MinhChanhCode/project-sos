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

        <div class="rounded-lg border border-orange-100 bg-orange-50/60 p-3 dark:border-gray-800 dark:bg-gray-900">
          <div class="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-800 dark:text-gray-100">
            <Icon name="lucide:sparkles" class="h-4 w-4 text-orange-500" />
            Dữ liệu AI tư vấn món
          </div>
          <div class="grid gap-3 sm:grid-cols-2">
            <UFormGroup label="Loại món">
              <USelect
                v-model="form.type"
                :options="menuTypeOptions"
              />
            </UFormGroup>
            <UFormGroup label="Độ cay (0-3)">
              <UInput v-model.number="form.spicyLevel" type="number" min="0" max="3" />
            </UFormGroup>
            <UFormGroup label="Thời gian chuẩn bị">
              <UInput v-model.number="form.prepTimeMinutes" type="number" min="1" placeholder="15" />
            </UFormGroup>
            <UFormGroup label="Ăn chay">
              <div class="flex h-10 items-center gap-2">
                <UToggle v-model="form.isVegetarian" />
                <span class="text-sm text-gray-600 dark:text-gray-300">Phù hợp ăn chay</span>
              </div>
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Tag khẩu vị">
              <UInput v-model="form.tasteTags" placeholder="ngọt, béo, thanh nhẹ, cay..." />
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Nguyên liệu">
              <UTextarea v-model="form.ingredients" rows="2" placeholder="bò, trứng, sữa, hải sản..." />
            </UFormGroup>
            <UFormGroup label="Dị ứng/cần tránh">
              <UInput v-model="form.allergens" placeholder="hải sản, sữa, đậu phộng..." />
            </UFormGroup>
            <UFormGroup label="Phù hợp với">
              <UInput v-model="form.suitableFor" placeholder="đi nhóm, bữa sáng, không cay..." />
            </UFormGroup>
            <UFormGroup class="sm:col-span-2" label="Gợi ý đi kèm">
              <UInput v-model="form.pairing" placeholder="trà tắc, nước cam, gỏi cuốn..." />
            </UFormGroup>
          </div>
        </div>
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
  type?: string;
  tasteTags?: string;
  spicyLevel?: number;
  ingredients?: string;
  allergens?: string;
  suitableFor?: string;
  pairing?: string;
  isVegetarian?: boolean;
  prepTimeMinutes?: number;
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

const menuTypeOptions = [
  { label: "Món chính", value: "MAIN" },
  { label: "Đồ uống", value: "DRINK" },
  { label: "Khai vị", value: "APPETIZER" },
  { label: "Tráng miệng", value: "DESSERT" },
  { label: "Combo", value: "COMBO" },
];

const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files && input.files.length > 0 ? input.files[0] : null;
  // mutate nested field on provided form object (same pattern as other v-models)
  (props.form as any).imageFile = file;
};
</script>
