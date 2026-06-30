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
    const message = err?.data?.message || err?.message || "Fetch error";
    throw new Error(message);
  }
};
