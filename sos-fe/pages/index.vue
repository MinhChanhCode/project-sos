<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-slate-950 text-white">
      <button
        type="button"
        class="fixed left-4 top-4 z-50 flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-slate-950/85 text-white shadow-xl shadow-black/30 backdrop-blur transition duration-300 ease hover:border-orange-400 hover:text-orange-400 lg:hidden"
        aria-label="Mở hoặc đóng menu"
        :aria-expanded="isSidebarOpen"
        @click="toggleSidebar"
      >
        <Icon :name="isSidebarOpen ? 'lucide:x' : 'lucide:menu'" class="h-6 w-6" />
      </button>

      <button
        v-if="isSidebarOpen"
        type="button"
        class="fixed inset-0 z-30 bg-black/60 backdrop-blur-sm transition duration-300 ease lg:hidden"
        aria-label="Đóng menu"
        @click="closeSidebar"
      />

      <aside
        class="fixed inset-y-0 left-0 z-40 flex w-[280px] flex-col border-r border-white/10 bg-slate-950/95 px-5 pb-7 pt-20 shadow-2xl shadow-black/50 backdrop-blur-xl transition duration-300 ease lg:translate-x-0 lg:pt-7"
        :class="isSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="mb-10 flex items-center gap-4">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-orange-500 to-red-600 text-white shadow-lg shadow-orange-500/25">
            <Icon name="lucide:chef-hat" class="h-8 w-8" />
          </div>
          <div class="min-w-0">
            <p class="truncate text-2xl font-black leading-7">Bếp <span class="text-orange-500">Mẹ Hương</span></p>
            <p class="mt-1 text-xs text-slate-300">Vẹn nguyên tinh túy ẩm thực Việt</p>
          </div>
        </div>

        <nav class="space-y-3">
          <NuxtLink
            v-for="item in navItems"
            :key="item.label"
            :to="item.link"
            class="flex h-14 items-center gap-4 rounded-lg px-4 text-base font-semibold transition duration-300 ease"
            :class="isActive(item.link)
              ? 'bg-gradient-to-r from-orange-600 to-orange-500 text-white shadow-lg shadow-orange-950/40'
              : 'text-slate-100 hover:bg-white/5 hover:text-orange-400'"
            @click="closeSidebar"
          >
            <Icon :name="item.icon" class="h-6 w-6 shrink-0" />
            <span>{{ item.label }}</span>
          </NuxtLink>
        </nav>

        <div class="mt-8 border-t border-white/10 pt-8" />
      </aside>

      <main class="min-h-screen transition duration-300 ease lg:pl-[280px]">
        <HomeHeroSection />

        <section class="mx-auto grid max-w-7xl gap-5 px-5 py-8 sm:grid-cols-2 md:px-10 xl:grid-cols-4">
          <HomeInterfaceCard
            v-for="interfaceItem in interfaces"
            :key="interfaceItem.title"
            v-bind="interfaceItem"
          />
        </section>

        <HomeFooter />
      </main>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useHead } from 'nuxt/app'
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useHome } from '~/composables/useHome'

useHead({
  title: 'Bếp Mẹ Hương - Vẹn nguyên tinh túy ẩm thực Việt'
})

const route = useRoute()
const { interfaces, navItems } = useHome()
const isSidebarOpen = ref(false)

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

const closeSidebar = () => {
  isSidebarOpen.value = false
}

const isActive = (link: string) => {
  if (link === '/') return route.path === '/'

  const redirectMatch = link.match(/redirect=([^&]+)/)
  const target = redirectMatch?.[1] ? decodeURIComponent(redirectMatch[1]) : link

  return route.path === target
}
</script>
