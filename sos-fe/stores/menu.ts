import { defineStore } from "pinia";
import type { MenuItem } from "./cart";

export const useMenuStore = defineStore("menu", {
  state: () => ({
    items: [] as MenuItem[],
    // suggestions
    bestSellers: [] as MenuItem[],
    promotions: [] as MenuItem[],
    isLoadingSuggestions: false,
    page: 0,
    
    size: 12,
    totalPages: 0,
    totalItems: 0,
    isLoadingMore: false,
    categories: [
      { id: "all", name: "Tất cả", icon: "🍽" },
      { id: "2", name: "Món chính", icon: "🍜" },
      { id: "67", name: "Khai vị", icon: "🥗" },
      { id: "1", name: "Đồ uống", icon: "🥤" },
      { id: "68", name: "Combo", icon: "🍱" },
    ],
  }),

  getters: {
    getItemById: (state) => (id: number) =>
      state.items.find((item) => item.id === id),
    getItemsByCategory: (state) => (category: string) => {
      if (category === "all") return state.items;
      return state.items.filter((item) => item.categoryId === category);
    },
    searchItems: (state) => (query: string) => {
      return state.items.filter(
        (item) =>
          item.name.toLowerCase().includes(query.toLowerCase()) ||
          item.description.toLowerCase().includes(query.toLowerCase())
      );
    },

    topBestsellers: (state) => (limit = 8) => {
      const withScore = state.items.map((i) => {
        const orders = typeof i.orders === "number" ? i.orders : 0;
        const rating = typeof i.rating === "number" ? i.rating : 0;
        const availability = i.isAvailable === false ? 0 : 1;
        const score = orders * 0.7 + rating * 0.2 + availability * 0.1;
        return { ...i, popularityScore: score } as MenuItem;
      });
      return withScore
        .sort((a, b) => (b.popularityScore || 0) - (a.popularityScore || 0))
        .slice(0, limit);
    },

    activePromotions: (state) => (limit = 8) => {
      const now = Date.now();
      return state.items
        .filter((i) => {
          const hasPromoPrice = typeof i.promotionalPrice === "number" && i.promotionalPrice! > 0 && i.promotionalPrice! < i.price;
          const notExpired = !i.promotionEndDate || new Date(i.promotionEndDate).getTime() > now;
          return hasPromoPrice && notExpired;
        })
        .slice(0, limit);
    },
  },

  actions: {
    addItem(item: Omit<MenuItem, "id">) {
      const newId = Math.max(...this.items.map((i) => i.id), 0) + 1;
      this.items.push({ ...item, id: newId });
    },

    updateItem(id: number, updates: Partial<MenuItem>) {
      const index = this.items.findIndex((item) => item.id === id);
      if (index > -1) {
        this.items[index] = { ...this.items[index], ...updates };
      }
    },

    removeItem(id: number) {
      const index = this.items.findIndex((item) => item.id === id);
      if (index > -1) {
        this.items.splice(index, 1);
      }
    },

    setItems(items: MenuItem[]) {
      this.items = items;
    },

    appendItems(items: MenuItem[]) {
      this.items = [...this.items, ...items];
    },

    setCategories(categories: Array<{ id: string; name: string; icon: string }>) {
      this.categories = categories;
    },

    setPageInfo(
      page: number,
      size: number,
      totalPages: number,
      totalItems: number
    ) {
      this.page = page;
      this.size = size;
      this.totalPages = totalPages;
      this.totalItems = totalItems;
    },

    hydrateSuggestionsFallback(limit = 8) {
      // Pinia: có thể truy cập getters như thuộc tính của this
      // topBestsellers và activePromotions đều là getter trả về hàm
      const best = (this as any).topBestsellers(limit);
      const promo = (this as any).activePromotions(limit);
      this.bestSellers = best;
      this.promotions = promo;
    },
    //// suggestions
    async fetchSuggestions(limit = 8) {
      try {
        this.isLoadingSuggestions = true;
        const nowIso = new Date().toISOString();
        const menuApi = (await import("~/api-service/MenuApi")).menuApi;
        const [best, promo] = await Promise.all([
          menuApi.getBestSellers(limit),
          menuApi.getPromotions(limit, nowIso),
        ]);
        this.bestSellers = Array.isArray(best) ? (best as unknown as MenuItem[]) : [];
        this.promotions = Array.isArray(promo) ? (promo as unknown as MenuItem[]) : [];
        if (!this.bestSellers.length || !this.promotions.length) {
          this.hydrateSuggestionsFallback(limit);
        }
      } catch {
        this.hydrateSuggestionsFallback(limit);
      } finally {
        this.isLoadingSuggestions = false;
      }
    },
  },
});
