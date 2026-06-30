import { defineStore } from "pinia"

export const useThemeStore = defineStore("theme", {
  state: () => ({
    isDark: false,
  }),

  actions: {
    toggleTheme() {
      this.isDark = !this.isDark
      if (this.isDark) {
        document.documentElement.classList.add("dark")
        localStorage.setItem("theme", "dark")
      } else {
        document.documentElement.classList.remove("dark")
        localStorage.setItem("theme", "light")
      }
    },

    initTheme() {
      const savedTheme = localStorage.getItem("theme")
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches

      if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
        this.isDark = true
        document.documentElement.classList.add("dark")
      }
    },
  },
})
