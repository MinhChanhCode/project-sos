<template>
  <UButton 
    @click="toggleTheme" 
    variant="ghost" 
    size="sm"
    square
  >
    <Icon v-if="isDark" name="lucide:sun" class="h-5 w-5" />
    <Icon v-else name="lucide:moon" class="h-5 w-5" />
    <span class="sr-only">Toggle theme</span>
  </UButton>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const theme = ref('light')

onMounted(() => {
  // Lấy theme từ localStorage nếu có
  const saved = localStorage.getItem('theme')
  if (saved === 'dark' || saved === 'light') {
    theme.value = saved
    updateHtmlClass(saved)
  } else {
    // Nếu không có, lấy theo hệ thống
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    theme.value = prefersDark ? 'dark' : 'light'
    updateHtmlClass(theme.value)
  }
})

const isDark = computed(() => theme.value === 'dark')

function updateHtmlClass(val: string) {
  const html = document.documentElement
  if (val === 'dark') {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
}

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  updateHtmlClass(theme.value)
  localStorage.setItem('theme', theme.value)
}
</script>
