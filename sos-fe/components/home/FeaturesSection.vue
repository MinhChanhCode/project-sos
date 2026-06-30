<template>
  <UCard>
    <template #header>
      <h3 class="text-2xl font-bold text-center text-gray-900 dark:text-white">Tính năng nổi bật</h3>
    </template>

    <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div 
        v-for="feature in features" 
        :key="feature.title"
        class="text-center"
      >
        <div :class="feature.iconClass">
          <Icon v-if="feature.icon" :name="feature.icon" class="w-6 h-6" :class="feature.iconColorClass" />
          <span v-else class="text-xl">{{ feature.emoji }}</span>
        </div>
        <h4 class="font-semibold mb-2">{{ feature.title }}</h4>
        <p class="text-sm text-gray-600 dark:text-gray-300">{{ feature.description }}</p>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
interface Feature {
  title: string
  description: string
  icon?: string
  emoji?: string
  iconClass: string
  iconColorClass: string
}

interface Props {
  features: Feature[]
}

defineProps<Props>()
</script>