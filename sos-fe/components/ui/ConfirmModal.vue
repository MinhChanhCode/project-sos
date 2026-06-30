<template>
  <UModal v-model="isOpen">
    <UCard>
      <template #header>
        <h3 class="text-lg font-semibold">{{ options.title }}</h3>
      </template>

      <p class="text-sm text-gray-600 dark:text-gray-300">
        {{ options.message }}
      </p>

      <template #footer>
        <div class="flex space-x-2 justify-end">
          <UButton variant="outline" @click="cancel">{{
            options.cancelText
          }}</UButton>
          <UButton
            :color="options.destructive ? 'red' : 'primary'"
            @click="confirm"
          >
            {{ options.confirmText }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { useConfirm } from "~/composables/useConfirm";
import { computed } from "vue";

const { isOpen: modalOpen, options, confirm, cancel } = useConfirm();

const isOpen = computed({
  get: () => modalOpen.value,
  set: (val: boolean) => (modalOpen.value = val),
});
</script>
