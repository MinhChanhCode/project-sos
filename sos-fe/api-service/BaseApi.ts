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
    throw new Error(error.value.message || "Fetch error");
  }

  return data.value as T;
};
