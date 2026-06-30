<template>
  <article :class="cardClass">
    <div class="border-b border-gray-100 px-5 py-6 text-center dark:border-white/10 sm:px-6">
      <div :class="iconClass">
        <Icon :name="icon" class="h-8 w-8" :class="iconColorClass" />
      </div>
      <h3 class="text-xl font-bold text-gray-950 dark:text-white">{{ title }}</h3>
      <p class="mt-1 min-h-12 text-sm leading-6 text-gray-600 dark:text-gray-300">{{ description }}</p>
    </div>

    <div class="flex flex-1 flex-col justify-between gap-5 px-5 py-5 sm:p-6">
      <div class="space-y-3">
        <div 
          v-for="feature in features" 
          :key="feature.text"
          class="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2 text-gray-700 dark:bg-white/5 dark:text-gray-200"
        >
          <Icon v-if="feature.icon" :name="feature.icon" class="h-4 w-4 text-orange-500" />
          <span v-else class="text-sm">{{ feature.emoji }}</span>
          <span class="text-sm font-medium">{{ feature.text }}</span>
        </div>
      </div>
      <NuxtLink :to="link">
        <span :class="buttonClass">
          {{ buttonText }}
        </span>
      </NuxtLink>
    </div>
  </article>
</template>

<script setup lang="ts">
interface Feature {
  icon?: string
  emoji?: string
  text: string
}

interface Props {
  title: string
  description: string
  icon: string
  iconColorClass: string
  iconClass: string
  features: Feature[]
  link: string
  buttonText: string
  buttonClass: string
  cardClass: string
}

defineProps<Props>()
</script>
