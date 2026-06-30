import { defineNuxtPlugin } from 'nuxt/app'
import { useThemeStore } from "~/stores/theme"

export default defineNuxtPlugin(() => {
  const themeStore = useThemeStore()

  // Initialize theme on client side
  if (process.client) {
    themeStore.initTheme()
  }
})
