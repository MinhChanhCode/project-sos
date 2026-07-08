<template>
  <NuxtLayout name="default">
    <div class="min-h-screen bg-slate-950 text-white">
      <div class="grid min-h-screen lg:grid-cols-[1.1fr_0.9fr]">
        <section class="relative hidden overflow-hidden bg-slate-900 lg:block">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_25%_20%,rgba(16,185,129,0.26),transparent_34%),radial-gradient(circle_at_78%_18%,rgba(59,130,246,0.2),transparent_28%),linear-gradient(135deg,#020617_0%,#111827_55%,#0f172a_100%)]" />
          <div class="relative flex h-full flex-col justify-between p-12">
            <div>
              <div class="inline-flex items-center gap-2 rounded-full border border-emerald-400/30 bg-emerald-400/10 px-4 py-2 text-sm font-semibold text-emerald-200">
                <Icon name="lucide:utensils" class="h-4 w-4" />
                ProjectSOS
              </div>
              <h1 class="mt-8 max-w-xl text-5xl font-bold leading-tight">
                Quản lý nhà hàng nhanh, gọn và realtime
              </h1>
              <p class="mt-5 max-w-lg text-base leading-7 text-slate-300">
                Một màn đăng nhập cho quản trị viên, nhân viên phục vụ và bếp, tối ưu cho vận hành trong ca làm.
              </p>
            </div>
            <div class="grid max-w-xl gap-3 sm:grid-cols-3">
              <div class="rounded-xl border border-white/10 bg-white/5 p-4">
                <Icon name="lucide:layout-dashboard" class="h-5 w-5 text-emerald-300" />
                <div class="mt-3 font-semibold">Admin</div>
                <p class="mt-1 text-xs text-slate-400">Dashboard, bàn, QR, menu</p>
              </div>
              <div class="rounded-xl border border-white/10 bg-white/5 p-4">
                <Icon name="lucide:hand-platter" class="h-5 w-5 text-sky-300" />
                <div class="mt-3 font-semibold">Staff</div>
                <p class="mt-1 text-xs text-slate-400">Phục vụ và thanh toán</p>
              </div>
              <div class="rounded-xl border border-white/10 bg-white/5 p-4">
                <Icon name="lucide:chef-hat" class="h-5 w-5 text-amber-300" />
                <div class="mt-3 font-semibold">Kitchen</div>
                <p class="mt-1 text-xs text-slate-400">Nhận món và chế biến</p>
              </div>
            </div>
          </div>
        </section>

        <section class="flex items-center justify-center p-4 sm:p-8">
          <div class="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900/95 p-6 shadow-2xl shadow-black/30 sm:p-8">
            <div class="mb-8 text-center">
              <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-500 text-slate-950 shadow-lg shadow-emerald-500/20">
                <Icon name="lucide:lock-keyhole" class="h-7 w-7" />
              </div>
              <h1 class="mt-5 text-2xl font-bold">Đăng nhập hệ thống</h1>
              <p class="mt-2 text-sm text-slate-400">Vào đúng khu vực làm việc theo tài khoản của bạn</p>
            </div>

            <form class="space-y-5" @submit.prevent="handleLogin">
              <div>
                <label class="text-sm font-medium text-slate-200">Tên đăng nhập</label>
                <UInput v-model="username" placeholder="Nhập tên đăng nhập" size="lg" class="mt-2" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-200">Mật khẩu</label>
                <UInput v-model="password" type="password" placeholder="Nhập mật khẩu" size="lg" class="mt-2" />
              </div>
              <p v-if="error" class="rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-sm text-red-200">{{ error }}</p>
              <UButton type="submit" block size="lg" color="green" :loading="loading">
                <Icon name="lucide:log-in" class="mr-2 h-4 w-4" />
                Đăng nhập
              </UButton>
            </form>
          </div>
        </section>
      </div>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { authApi } from "~/api-service/AuthApi";
import { useAuthStore } from "~/stores/auth";
import { navigateTo, useRoute } from "nuxt/app";
import { ref } from "vue";

definePageMeta({ layout: false });

const username = ref("");
const password = ref("");
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
      userId: result.userId || "",
      username: result.username || "",
      fullName: result.fullName || "",
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
