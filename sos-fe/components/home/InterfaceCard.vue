<template>
  <article
    class="group relative flex min-h-[316px] flex-col overflow-hidden rounded-[18px] border bg-slate-950/70 p-5 shadow-2xl shadow-black/20 transition duration-300 ease hover:-translate-y-1 sm:p-6"
    :class="accentClasses.card"
  >
    <div class="absolute right-5 top-5 grid grid-cols-4 gap-1 opacity-35">
      <span
        v-for="dot in 16"
        :key="dot"
        class="h-1 w-1 rounded-full"
        :class="accentClasses.dot"
      />
    </div>

    <div class="flex flex-1 flex-col items-center justify-center text-center">
      <div
        class="mb-6 flex h-[88px] w-[88px] items-center justify-center rounded-full border shadow-xl transition duration-300 ease group-hover:scale-105"
        :class="accentClasses.iconWrap"
      >
        <Icon :name="icon" class="h-10 w-10 text-white" />
      </div>
      <h3 class="text-2xl font-extrabold text-white">{{ title }}</h3>
      <p class="mt-3 min-h-6 text-sm leading-6 text-slate-200">{{ description }}</p>
    </div>

    <NuxtLink :to="link" class="mt-6">
      <span
        class="flex h-14 w-full items-center justify-between rounded-lg px-5 text-base font-bold text-white shadow-lg transition duration-300 ease hover:brightness-110"
        :class="accentClasses.button"
      >
        {{ buttonText }}
        <Icon name="lucide:chevron-right" class="h-5 w-5" />
      </span>
    </NuxtLink>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const accentMap = {
  orange: {
    card: 'border-orange-500/50 from-orange-950/65 to-slate-950 bg-gradient-to-br hover:border-orange-400/80',
    dot: 'bg-orange-400',
    iconWrap: 'border-orange-400/70 bg-orange-500/80 shadow-orange-950/60',
    button: 'bg-gradient-to-r from-orange-600 to-orange-500 shadow-orange-950/40'
  },
  green: {
    card: 'border-emerald-500/45 from-emerald-950/65 to-slate-950 bg-gradient-to-br hover:border-emerald-400/80',
    dot: 'bg-emerald-400',
    iconWrap: 'border-emerald-400/70 bg-emerald-600/80 shadow-emerald-950/60',
    button: 'bg-gradient-to-r from-emerald-700 to-emerald-500 shadow-emerald-950/40'
  },
  violet: {
    card: 'border-violet-500/45 from-violet-950/65 to-slate-950 bg-gradient-to-br hover:border-violet-400/80',
    dot: 'bg-violet-400',
    iconWrap: 'border-violet-400/70 bg-violet-700/80 shadow-violet-950/60',
    button: 'bg-gradient-to-r from-violet-700 to-purple-600 shadow-violet-950/40'
  },
  red: {
    card: 'border-red-500/45 from-red-950/65 to-slate-950 bg-gradient-to-br hover:border-red-400/80',
    dot: 'bg-red-400',
    iconWrap: 'border-red-400/70 bg-red-700/80 shadow-red-950/60',
    button: 'bg-gradient-to-r from-red-700 to-red-600 shadow-red-950/40'
  }
} as const

interface Props {
  title: string
  description: string
  icon: string
  accent: keyof typeof accentMap
  link: string
  buttonText: string
}

const props = defineProps<Props>()
const accentClasses = computed(() => accentMap[props.accent])
</script>
