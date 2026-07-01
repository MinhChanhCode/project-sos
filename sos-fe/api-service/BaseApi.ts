import { useFetch, useRuntimeConfig } from "nuxt/app";
import { getStoredToken } from "~/stores/auth";

export const baseApi = async <T>(url: string, options: any = {}) => {
  const config = useRuntimeConfig();
  const baseURL =
    config.public.apiBase ||
    (import.meta.server ? String(config.apiTarget || "") : "");
  const token = options.skipAuth ? null : getStoredToken();
  const headers: Record<string, string> = {
    ...(options.headers || {}),
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const { skipAuth, ...fetchOptions } = options;

  const { data, error } = await useFetch<T>(url, {
    baseURL,
    ...fetchOptions,
    headers,
  });

  if (error.value) {
    const statusCode = (error.value as any)?.statusCode || (error.value as any)?.status;
    let message = error.value.message || "Fetch error";
    if (statusCode === 401) {
      message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    } else if (statusCode === 403) {
      message = "Bạn không có quyền thực hiện thao tác này. Hãy dùng tài khoản nhân viên, quản lý hoặc quản trị.";
    }
    throw new Error(message);
  }

  return data.value as T;
};
