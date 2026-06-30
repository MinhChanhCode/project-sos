import { defineStore } from "pinia";

export interface MenuItem {
  id: number;
  name: string;
  price: number;
  imageUrl: string;
  categoryId: string;
  description: string;
  badges: string[];
  rating: number;
  prepTime: string;
  isAvailable?: boolean;
  isActive?: boolean;
  orders?: number;
  status?: string; // Thêm field status để track trạng thái món
  // Các trường phục vụ khuyến mãi/gợi ý (tùy chọn)
  originalPrice?: number;
  promotionalPrice?: number;
  promotionEndDate?: string; // ISO string
  isPromotional?: boolean;
  popularityScore?: number;
}

export interface CartItem extends MenuItem {
  quantity: number;
  note?: string;
}

export const useCartStore = defineStore("cart", {
  state: () => ({
    items: [] as CartItem[],
    orderedItems: [] as CartItem[],
    tableNumber: "B05",
    groupMembers: ["Bạn"],
  }),

  getters: {
    totalItems: (state) =>
      state.items.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) =>
      state.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
    // Tổng tiền của cả giỏ hàng hiện tại và món đã đặt
    totalPriceAll: (state) => {
      const cartTotal = state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
      const orderedTotal = state.orderedItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
      return cartTotal + orderedTotal
    },
    // Tổng số món của cả giỏ hàng hiện tại và món đã đặt
    totalItemsAll: (state) => {
      const cartItems = state.items.reduce((sum, item) => sum + item.quantity, 0)
      const orderedItems = state.orderedItems.reduce((sum, item) => sum + item.quantity, 0)
      return cartItems + orderedItems
    },
    isEmpty: (state) => state.items.length === 0,
  },

  actions: {
    setFromApiItems(apiItems: Array<{ menuItemId: number; menuItemName?: string; unitPrice: number; quantity?: number; pendingQuantity?: number; preparingQuantity?: number; completedQuantity?: number; servedQuantity?: number; notes?: string; menuItemImageUrl?: string, isActive?: boolean }> = []) {
      this.items = (apiItems || [])
        .filter(i => i.isActive !== false)
        .map((i) => ({
          id: i.menuItemId,
          name: i.menuItemName || `Món #${i.menuItemId}`,
          price: Number(i.unitPrice),
          imageUrl: i.menuItemImageUrl || "",
          categoryId: "",
          description: "",
          badges: [],
          rating: 0,
          prepTime: "",
          isAvailable: true,
          orders: 0,
          quantity: (typeof i.quantity === 'number' ? i.quantity : ((i.pendingQuantity || 0) + (i.preparingQuantity || 0) + (i.completedQuantity || 0) + (i.servedQuantity || 0))),
          note: i.notes,
        }))
    },

    setTableNumber(table: string) {
      this.tableNumber = table
    },

    addItem(item: MenuItem) {
      const existingItem = this.items.find(
        (cartItem) => cartItem.id === item.id
      );
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        this.items.push({ ...item, quantity: 1 });
      }
    },

    updateQuantity(id: number, quantity: number) {
      if (quantity <= 0) {
        this.removeItem(id);
      } else {
        const item = this.items.find((item) => item.id === id);
        if (item) {
          item.quantity = quantity;
        }
      }
    },

    removeItem(id: number) {
      const index = this.items.findIndex((item) => item.id === id);
      if (index > -1) {
        this.items.splice(index, 1);
      }
    },

    clearCart() {
      this.items = [];
    },

    commitCurrentAsOrdered() {
      if (this.items.length === 0) return
      // Thêm items hiện tại vào orderedItems (không merge quantity)
      const newOrderedItems = [...this.items]
      
      // Thêm vào đầu danh sách để hiển thị món mới nhất trước
      this.orderedItems = [...newOrderedItems, ...this.orderedItems]
      
      // Xóa items khỏi giỏ hàng hiện tại
      this.items = []
    },

    updateNote(id: number, note: string) {
      const item = this.items.find((item) => item.id === id);
      if (item) {
        item.note = note;
      }
    },
  },
});
