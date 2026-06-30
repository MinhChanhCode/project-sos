<template>
  <div :class="cn('w-full', className)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { provide, ref, watch } from 'vue'
import { cn } from '~/utils/cn'
import { readonly } from 'vue'

interface Props {
  defaultValue?: string
  value?: string
  className?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:value': [value: string]
}>()

const currentValue = ref(props.value || props.defaultValue || '')

watch(() => props.value, (newValue) => {
  if (newValue !== undefined) {
    currentValue.value = newValue
  }
})

const setValue = (value: string) => {
  currentValue.value = value
  emit('update:value', value)
}

provide('tabs', {
  currentValue: readonly(currentValue),
  setValue
})
</script>
