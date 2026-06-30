import Vue3Toastify, { toast, Slide } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'
import { defineNuxtPlugin } from 'nuxt/app'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(Vue3Toastify, {
    position: 'top-right',
    transition: Slide,
    toastStyle: {
      marginTop: '70px',
      borderRadius: '12px',
      minWidth: '200px',
      maxWidth: '50vw',
      boxShadow: '0 4px 24px rgba(0,0,0,0.12)'
    },
    closeButton: true,
    autoClose: 2000,
    hideProgressBar: false,
    closeOnClick: true,
    pauseOnHover: true,
    draggable: true,
    theme: 'colored'
  })
  nuxtApp.provide('toast', toast)
}) 