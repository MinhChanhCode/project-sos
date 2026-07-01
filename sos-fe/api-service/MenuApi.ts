import { MenuItem } from "@/stores/cart";
import { baseApi } from "./BaseApi";
import { useRuntimeConfig } from "nuxt/app";
import { getMenuImageUrl } from "~/utils/foodImages";

const baseUrl = "/api/v1/menu-items";

export type MenuItemCreateRequest = {
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  categoryId: number;
  isAvailable?: boolean;
  isActive?: boolean;
  type?: string;
  tasteTags?: string;
  spicyLevel?: number;
  ingredients?: string;
  allergens?: string;
  suitableFor?: string;
  pairing?: string;
  isVegetarian?: boolean;
  prepTimeMinutes?: number;
};

// Helper function to convert relative image URLs to absolute URLs
export const getImageUrl = (
  relativeUrl?: string,
  name?: string,
  description?: string,
  categoryId?: string | number
): string => {
  if (!relativeUrl) return getMenuImageUrl(relativeUrl, name, description, categoryId);

  if (relativeUrl.startsWith("http")) {
    return getMenuImageUrl(relativeUrl, name, description, categoryId);
  }

  // Use runtime config for consistent URL generation
  const config = useRuntimeConfig();
  const baseUrl =
    config.public.apiBase ||
    (typeof window !== "undefined" ? window.location.origin : "");
  return `${baseUrl}${relativeUrl}`;
};

export const menuApi = {
  getAvailable: () => baseApi<any>(`${baseUrl}/available`),

  getAvailablePaged: (page = 0, size = 12) =>
    baseApi<any>(`${baseUrl}/available`, {
      query: { page, size },
    }),

  getById: (id: number | string) => baseApi<MenuItem>(`${baseUrl}/${id}`),

  getByCategoryPaged: (categoryId: number | string, page = 0, size = 12) =>
    baseApi<any>(`${baseUrl}/category/${categoryId}`, {
      query: { page, size },
    }),

  searchPaged: (keyword: string, page = 0, size = 12) =>
    baseApi<any>(`${baseUrl}/search`, {
      query: { keyword, page, size },
    }),

  create: (payload: MenuItemCreateRequest) =>
    baseApi<any>(`${baseUrl}`, {
      method: "POST",
      body: payload,
    }),

  update: (id: number | string, payload: Partial<MenuItem>) =>
    baseApi<MenuItem>(`${baseUrl}/${id}`, {
      method: "PUT",
      body: payload,
    }),

  delete: (id: number | string) =>
    baseApi<void>(`${baseUrl}/${id}`, {
      method: "DELETE",
    }),

  uploadImage: (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return baseApi<string>(`/api/v1/images/upload`, {
      method: "POST",
      body: formData,
    });
  },

  // Suggestions
  getBestSellers: (limit = 8) =>
    baseApi<MenuItem[]>(`${baseUrl}/best-sellers`, {
      query: { limit },
    }),

  getPromotions: (limit = 8, now?: string) =>
    baseApi<MenuItem[]>(`${baseUrl}/promotions`, {
      query: { limit, now },
    }),

  // Admin promotions
  upsertPromotion: (payload: { menuItemId: number; promotionalPrice: number; endDate?: string }) =>
    baseApi<void>(`/api/v1/menu-items/promotions`, {
      method: "POST",
      body: payload,
    }),

  removePromotion: (menuItemId: number) =>
    baseApi<void>(`/api/v1/menu-items/promotions/${menuItemId}`, {
      method: "DELETE",
    }),
};
