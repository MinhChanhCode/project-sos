<template>
  <label
    :class="cn(
      'text-sm font-medium leading-none peer-disabled:cursor-default peer-disabled:opacity-70',
      className
    )"
    v-bind="$attrs"
  >
    <slot />
  </label>
</template>

<script setup lang="ts">
import { cn } from '@/utils/cn'

interface Props {
  className?: string
}

defineProps<Props>()
</script>
