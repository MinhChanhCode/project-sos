<template>
  <NuxtLayout name="default">
    <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 p-4">
      <UCard class="w-full max-w-md">
        <template #header>
          <h1 class="text-xl font-bold text-center">Đăng nhập hệ thống</h1>
        </template>
        <form class="space-y-4" @submit.prevent="handleLogin">
          <div>
            <label class="text-sm font-medium">Tên đăng nhập</label>
            <UInput v-model="username" placeholder="admin / staff / kitchen" class="mt-1" />
          </div>
          <div>
            <label class="text-sm font-medium">Mật khẩu</label>
            <UInput v-model="password" type="password" placeholder="••••••" class="mt-1" />
          </div>
          <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
          <UButton type="submit" block :loading="loading">Đăng nhập</UButton>
        </form>
        <p class="text-xs text-gray-500 mt-4 text-center">
          Demo: admin/admin123 · staff/staff123 · kitchen/kitchen123
        </p>
      </UCard>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { authApi } from "~/api-service/AuthApi";
import { useAuthStore } from "~/stores/auth";

definePageMeta({ layout: false });

const username = ref("admin");
const password = ref("admin123");
const error = ref("");
const loading = ref(false);
const route = useRoute();
const auth = useAuthStore();

const handleLogin = async () => {
  error.value = "";
  loading.value = true;
  try {
    const result = await authApi.login({ username: username.value, password: password.value });
    auth.setAuth(result.token, {
      userId: result.userId,
      username: result.username,
      fullName: result.fullName,
      roles: result.roles || [],
    });
    const redirect = route.query.redirect as string | undefined;
    const roles = result.roles || [];
    if (redirect && redirect !== "/login") {
      return navigateTo(redirect);
    }
    if (roles.includes("ADMIN") || roles.includes("MANAGER")) return navigateTo("/admin");
    if (roles.includes("KITCHEN")) return navigateTo("/kitchen");
    if (roles.includes("STAFF")) return navigateTo("/staff");
    navigateTo("/");
  } catch (e: any) {
    error.value = e.message || "Đăng nhập thất bại";
  } finally {
    loading.value = false;
  }
};
</script>
