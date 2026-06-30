<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-gradient-to-br from-amber-50 via-white to-rose-50 text-gray-900 dark:from-gray-950 dark:via-gray-900 dark:to-slate-900 dark:text-white">
      <div class="container mx-auto px-4 py-8">
        <!-- Header -->
        <HomeHeader />

        <!-- Hero Section -->
        <HomeHeroSection />

        <!-- Interface Cards -->
        <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
          <HomeInterfaceCard 
            v-for="interfaceItem in interfaces" 
            :key="interfaceItem.title"
            v-bind="interfaceItem"
          />
        </div>
      </div>

      <HomeFooter />
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useHome } from '~/composables/useHome'
import { useHead } from 'nuxt/app'

// Meta
useHead({
  title: 'MonGo - Nhanh hơn mỗi buổi ăn'
})

// Use composable
const { interfaces } = useHome()
</script>
