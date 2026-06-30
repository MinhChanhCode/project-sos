<template>
  <div class="min-h-screen flex items-center justify-center bg-background">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-foreground mb-4">
        {{ error.statusCode || 'Lỗi' }}
      </h1>
      <p class="text-muted-foreground mb-6">
        {{ error.statusMessage || 'Đã xảy ra lỗi không mong muốn' }}
      </p>
      <UButton @click="handleError">
        Về trang chủ
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
// import { clearError } from '#app'

interface Props {
  error: {
    statusCode?: number
    statusMessage?: string
  }
}

defineProps<Props>()

const handleError = () => {
  // clearError({ redirect: '/' })
}
</script>
