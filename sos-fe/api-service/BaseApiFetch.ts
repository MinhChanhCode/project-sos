import { useRuntimeConfig } from "nuxt/app";
import { getStoredToken } from "~/stores/auth";

export const baseApiFetch = async <T>(url: string, options: any = {}) => {
  const config = useRuntimeConfig();
  const baseURL =
    config.public.apiBase ||
    (import.meta.server ? String(config.apiTarget || "") : "");

  const { skipAuth, ...fetchOptions } = options;
  const token = skipAuth ? null : getStoredToken();
  const headers: Record<string, string> = {
    ...(options.headers || {}),
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const data = await $fetch<T>(url, {
      baseURL,
      ...fetchOptions,
      headers,
    });
    return data as T;
  } catch (err: any) {
    const statusCode = err?.statusCode || err?.response?.status;
    let message = err?.data?.message || err?.message || "Fetch error";
    if (statusCode === 401) {
      message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    } else if (statusCode === 403) {
      message = "Bạn không có quyền thực hiện thao tác này. Hãy dùng tài khoản nhân viên, quản lý hoặc quản trị.";
    }
    throw new Error(message);
  }
};
