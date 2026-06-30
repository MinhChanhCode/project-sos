import { defineNuxtConfig } from "nuxt/config";

const apiTarget = process.env.SOS_API_TARGET || "http://127.0.0.1:8080";
const normalizeApiBase = (value?: string) =>
  (value || "").trim().replace(/\/+$/, "").replace(/\/api$/, "");

const publicSiteUrl =
  process.env.NUXT_PUBLIC_SITE_URL ||
  process.env.VITE_PUBLIC_APP_URL ||
  process.env.PUBLIC_APP_URL ||
  process.env.QR_BASE_URL ||
  "";
const publicApiBase = normalizeApiBase(
  process.env.NUXT_PUBLIC_API_BASE || process.env.VITE_API_BASE_URL,
);
const publicWsBase =
  process.env.NUXT_PUBLIC_WS_BASE || process.env.VITE_WS_URL || "/ws";

export default defineNuxtConfig({
  devtools: { enabled: true },
  runtimeConfig: {
    apiTarget,
    public: {
      siteUrl: publicSiteUrl,
      apiBase: publicApiBase,
      wsBase: publicWsBase,
    },
  },
  modules: [
    "@nuxt/ui",
    "@nuxtjs/tailwindcss",
    "@pinia/nuxt",
    "@nuxt/icon",
    "@nuxt/image",
  ],
  css: ["~/assets/css/main.css"],
  app: {
    head: {
      title: "Gọi Món - Hệ thống đặt món thông minh",
      meta: [
        { charset: "utf-8" },
        { name: "viewport", content: "width=device-width, initial-scale=1" },
        {
          name: "description",
          content: "Hệ thống đặt món thông minh cho nhà hàng",
        },
      ],
      link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    },
  },
  components: [
    {
      path: "~/components",
      pathPrefix: false,
    },
    {
      path: "~/components/customer",
      prefix: "Customer",
    },
    {
      path: "~/components/admin",
      prefix: "Admin",
    },
    {
      path: "~/components/staff",
      prefix: "Staff",
    },
    {
      path: "~/components/home",
      prefix: "Home",
    },
  ],
  imports: {
    dirs: ["composables", "utils"],
  },
  nitro: {
    compatibilityDate: "2025-07-29",
    routeRules: {
      "/api/**": { proxy: `${apiTarget}/api/**` },
      "/auth/**": { proxy: `${apiTarget}/auth/**` },
      "/health": { proxy: `${apiTarget}/health` },
      "/health/**": { proxy: `${apiTarget}/health/**` },
      "/ws/**": { proxy: `${apiTarget}/ws/**` },
    },
  },
  vite: {
    server: {
      allowedHosts: [
        ".trycloudflare.com",
        ".loca.lt",
        "localhost",
        "127.0.0.1",
        "192.168.0.168",
      ],
      proxy: {
        "/api": {
          target: apiTarget,
          changeOrigin: true,
        },
        "/auth": {
          target: apiTarget,
          changeOrigin: true,
        },
        "/health": {
          target: apiTarget,
          changeOrigin: true,
        },
        "/ws": {
          target: apiTarget,
          changeOrigin: true,
          ws: true,
        },
      },
    },
  },
  typescript: {
    strict: true,
  },
});
