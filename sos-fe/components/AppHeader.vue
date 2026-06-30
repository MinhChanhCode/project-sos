<template>
  <header class="bg-white dark:bg-gray-800 shadow-sm sticky top-0 z-40">
    <div class="flex items-center justify-between p-4">
      <div class="flex items-center space-x-3">
        <NuxtLink to="/" v-if="showBackButton">
          <UButton variant="ghost" size="sm" square>
            <Icon name="lucide:arrow-left" class="w-4 h-4" />
          </UButton>
        </NuxtLink>
        <div>
          <h1 class="font-bold text-lg">{{ title }}</h1>
          <p v-if="subtitle" class="text-sm text-gray-600 dark:text-gray-300">{{ subtitle }}</p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <ThemeToggle />
        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
interface Props {
  title: string
  subtitle?: string
  showBackButton?: boolean
}

withDefaults(defineProps<Props>(), {
  showBackButton: true
})
</script>
