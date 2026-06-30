<template>
  <div
    :class="cn(
      'inline-flex h-10 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground',
      className
    )"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '~/utils/cn'

interface Props {
  className?: string
}

defineProps<Props>()
</script>
