<template>
  <button
    type="button"
    role="switch"
    :aria-checked="checked"
    :data-state="checked ? 'checked' : 'unchecked'"
    :class="cn(
      'peer inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-default disabled:opacity-50 data-[state=checked]:bg-primary data-[state=unchecked]:bg-input',
      className
    )"
    @click="toggle"
  >
    <span
      :data-state="checked ? 'checked' : 'unchecked'"
      class="pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform data-[state=checked]:translate-x-5 data-[state=unchecked]:translate-x-0"
    />
  </button>
</template>

<script setup lang="ts">
import { cn } from '~/utils/cn'

interface Props {
  checked?: boolean
  className?: string
}

const props = withDefaults(defineProps<Props>(), {
  checked: false
})

const emit = defineEmits<{
  'update:checked': [value: boolean]
}>()

const toggle = () => {
  emit('update:checked', !props.checked)
}
</script>
