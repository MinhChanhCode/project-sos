<template>
  <section class="relative min-h-[330px] overflow-hidden border-y border-white/10 bg-slate-950 md:min-h-[390px]">
    <img
      :src="heroImage"
      alt="Không gian nhà hàng Bếp Mẹ Hương"
      class="absolute inset-0 h-full w-full object-cover opacity-55"
    >
    <div class="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/70 to-slate-950/35" />
    <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-slate-950/35" />

    <div class="relative mx-auto flex min-h-[330px] max-w-7xl flex-col justify-center gap-8 px-5 py-12 md:min-h-[390px] md:px-10 lg:flex-row lg:items-center lg:justify-between">
      <div class="max-w-3xl">
        <p class="text-2xl font-semibold text-white md:text-3xl">Chào mừng đến với</p>
        <h1 class="mt-3 text-5xl font-black leading-none tracking-normal text-white md:text-7xl">
          Bếp <span class="font-serif font-normal italic text-orange-500">Mẹ Hương</span>
        </h1>
        <p class="mt-5 text-xl font-medium text-slate-100 md:text-2xl">Vẹn nguyên tinh túy ẩm thực Việt</p>
        <div class="mt-5 h-1 w-16 rounded-full bg-orange-500" />
      </div>

      <div class="w-full max-w-[320px] rounded-[18px] border border-white/10 bg-slate-950/75 p-6 shadow-2xl shadow-black/30 backdrop-blur md:self-end lg:self-auto">
        <div class="flex items-center gap-4 border-b border-white/10 pb-5">
          <span class="flex h-11 w-11 items-center justify-center rounded-full bg-orange-500/10 text-orange-400">
            <Icon name="lucide:calendar-days" class="h-5 w-5" />
          </span>
          <span class="text-base font-medium text-slate-100">{{ formattedDate }}</span>
        </div>
        <div class="flex items-center gap-4 pt-5">
          <span class="flex h-11 w-11 items-center justify-center rounded-full bg-orange-500/10 text-orange-400">
            <Icon name="lucide:clock-3" class="h-5 w-5" />
          </span>
          <span class="text-3xl font-bold tabular-nums text-white">{{ formattedTime }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import heroImage from '~/assets/images/home/restaurant-hero.png'

const now = ref(new Date())
let timer: ReturnType<typeof setInterval> | undefined

const formattedDate = computed(() =>
  new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(now.value)
)

const formattedTime = computed(() =>
  new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(now.value)
)

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>
