import { useCartStore } from "~/stores/cart";
import { useMenuStore } from "~/stores/menu";
import type { MenuItem } from "~/stores/cart";
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useNuxtApp } from "nuxt/app";
import type { toast as toastType } from "vue3-toastify";
import { menuApi } from "@/api-service/MenuApi";
import { CartApi, TableApi } from "@/api-service";
import { categoryApi, customerSessionApi, reviewApi } from "@/api-service/ExtendedApi";
export const useCustomer = () => {
  const cartStore = useCartStore();
  const menuStore = useMenuStore();

  // Reactive data
  const showCart = ref(false);
  const showRatingDialog = ref(false);
  const ratingValue = ref(5);
  const ratingComment = ref("");
  const selectedCategory = ref("all");
  const searchQuery = ref("");
  const tableNumber = ref(
    typeof window !== "undefined"
      ? new URLSearchParams(window.location.search).get("tableNumber") || ""
      : ""
  );
  const customerName = ref("");
  const showCustomerNameDialog = ref(false);

  const normalizeTableLookup = (value: unknown) => {
    const raw = String(value ?? "").trim().toLowerCase();
    if (!raw) return "";
    const withoutVietnameseMarks = raw
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/đ/g, "d");
    return withoutVietnameseMarks
      .replace(/^ban\s*/, "")
      .replace(/^table\s*/, "")
      .replace(/^b0*/, "")
      .replace(/^0+/, "")
      .replace(/\s+/g, "");
  };

  const normalizeText = (value: unknown) =>
    String(value ?? "")
      .trim()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/đ/g, "d");

  const categoryUi = [
    { name: "Món chính", icon: "🍜" },
    { name: "Khai vị", icon: "🥗" },
    { name: "Đồ uống", icon: "🥤" },
    { name: "Combo", icon: "🍱" },
  ];
  function generateUUID() {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === "x" ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }

  const sessionId = ref<string>("");

  if (process.client) {
    sessionId.value =
      new URLSearchParams(window.location.search).get("sessionId") ||
      (crypto.randomUUID ? crypto.randomUUID() : generateUUID());
  } else {
    // khi SSR thì fallback
    sessionId.value = generateUUID();
  }

  const tableId = ref<string | null>(
    typeof window !== "undefined"
      ? new URLSearchParams(window.location.search).get("tableId")
      : null
  );
  const hasValidTableId = () => !!tableId.value && tableId.value !== "null" && tableId.value !== "undefined";
  const groupMembers = ref<string[]>([]);
  const loadingMenu = ref(false);
  const isFetchingMore = ref(false);
  const hasMore = ref(true);
  const menuError = ref("");
  const addingToCart = ref<Set<number>>(new Set()); // Track items being added to cart
  const updatingQuantity = ref<Set<number>>(new Set()); // Track items being updated
  const isCartOperationInProgress = ref(false); // Track if any cart operation is in progress
  const lastAddedItem = ref<{id: number, name: string, quantity: number} | null>(null)
  
  // Chuẩn hóa dữ liệu MenuItem từ API để đảm bảo có trường khuyến mãi
  const normalizeMenuItem = (i: any): MenuItem => {
    const promotionalPrice = typeof i.promotionalPrice === 'number' ? i.promotionalPrice : undefined
    const originalPrice = typeof i.originalPrice === 'number' ? i.originalPrice : i.price
    const promotionEndDate = i.promotionEndDate || undefined
    const isPromotional = typeof promotionalPrice === 'number' && promotionalPrice > 0 && promotionalPrice < Number(i.price)
    return {
      id: Number(i.id ?? i.menuItemId ?? 0),
      name: String(i.name ?? i.menuItemName ?? ''),
      price: Number(i.price ?? i.unitPrice ?? 0),
      originalPrice,
      promotionalPrice,
      promotionEndDate,
      imageUrl: String(i.imageUrl ?? i.menuItemImageUrl ?? ''),
      categoryId: String(i.categoryId ?? ''),
      description: String(i.description ?? ''),
      badges: Array.isArray(i.badges) ? i.badges : [],
      rating: Number(i.rating ?? 0),
      prepTime: String(i.prepTime ?? ''),
      isAvailable: i.isAvailable !== false,
      isActive: i.isActive !== false,
      orders: typeof i.orders === 'number' ? i.orders : 0,
      status: undefined,
      isPromotional,
      popularityScore: undefined,
    }
  }

  // Clear local cart when table is cleared
  const clearLocalCart = () => {
    if (typeof window === "undefined") return;
    try {
      localStorage.removeItem(`cart:${sessionId.value}`);
    } catch {}
  };

  // Fetch menu items from API
  const fetchMenuItems = async (reset = true): Promise<void> => {
    loadingMenu.value = true;
    menuError.value = "";
    try {
      const isSearching = !!searchQuery.value;
      const value: any = isSearching
        ? await menuApi.searchPaged(searchQuery.value, 0, menuStore.size)
        : selectedCategory.value !== "all"
        ? await menuApi.getByCategoryPaged(selectedCategory.value, 0, menuStore.size)
        : await menuApi.getAvailablePaged(0, menuStore.size);
      const rawItems: any[] = Array.isArray(value?.data?.content)
        ? value.data.content
        : value?.content ?? [];
      const items: MenuItem[] = rawItems.map(normalizeMenuItem);
      const pageMeta = value?.data?.page ?? value?.page;
      if (reset) menuStore.setItems(items);
      else menuStore.appendItems(items);
      if (pageMeta)
        menuStore.setPageInfo(
          pageMeta.number,
          pageMeta.size,
          pageMeta.totalPages,
          pageMeta.totalElements || 0
        );
      hasMore.value = pageMeta
        ? pageMeta.number + 1 < pageMeta.totalPages
        : false;
    } catch (e: any) {
      menuError.value = e.message || "Lỗi tải menu";
    } finally {
      loadingMenu.value = false;
    }
  };

  fetchMenuItems();

  const fetchCategories = async () => {
    try {
      const rawCategories: any[] = await categoryApi.list();
      const activeCategories = rawCategories.filter((category) => category?.isActive !== false);
      const mappedCategories = categoryUi
        .map((uiCategory) => {
          const matched = activeCategories.find(
            (category) => normalizeText(category.name) === normalizeText(uiCategory.name)
          );
          if (!matched) return null;
          return {
            id: String(matched.id),
            name: uiCategory.name,
            icon: uiCategory.icon,
          };
        })
        .filter(Boolean) as Array<{ id: string; name: string; icon: string }>;

      if (mappedCategories.length) {
        menuStore.setCategories([
          { id: "all", name: "Tất cả", icon: "🍽" },
          ...mappedCategories,
        ]);
      }
    } catch {
      // Keep local fallback categories when the API is unavailable.
    }
  };

  const loadMore = async (): Promise<void> => {
    if (isFetchingMore.value || !hasMore.value) return;
    isFetchingMore.value = true;
    try {
      const nextPage = (menuStore.page ?? 0) + 1;
      const isSearching = !!searchQuery.value;
      const value: any = isSearching
        ? await menuApi.searchPaged(searchQuery.value, nextPage, menuStore.size)
        : selectedCategory.value !== "all"
        ? await menuApi.getByCategoryPaged(selectedCategory.value, nextPage, menuStore.size)
        : await menuApi.getAvailablePaged(nextPage, menuStore.size);
      const rawItems: any[] = Array.isArray(value?.data?.content)
        ? value.data.content
        : value?.content ?? [];
      const items: MenuItem[] = rawItems.map(normalizeMenuItem);
      const pageMeta = value?.data?.page ?? value?.page;
      menuStore.appendItems(items);
      if (pageMeta)
        menuStore.setPageInfo(
          pageMeta.number,
          pageMeta.size,
          pageMeta.totalPages,
          pageMeta.totalElements || 0
        );
      hasMore.value = pageMeta
        ? pageMeta.number + 1 < pageMeta.totalPages
        : false;
    } catch (e) {
      // ignore
    } finally {
      isFetchingMore.value = false;
    }
  };

  // Computed
  const categories = computed(() => menuStore.categories);
  const filteredItems = computed(() => {
    let items = menuStore.items;
    if (selectedCategory.value !== "all") {
      items = menuStore.getItemsByCategory(selectedCategory.value);
    }
    if (searchQuery.value) {
      items = menuStore.searchItems(searchQuery.value);
    }
    return items;
  });

  const onScroll = () => {
    if (typeof window === "undefined") return;
    const bottomOffset = 200; // px
    const scrollPosition = window.scrollY + window.innerHeight;
    const threshold = document.documentElement.scrollHeight - bottomOffset;
    if (scrollPosition >= threshold) {
      loadMore();
    }
  };

  onMounted(() => {
    if (typeof window !== "undefined") {
      window.addEventListener("scroll", onScroll, { passive: true });
    }
  });

  onBeforeUnmount(() => {
    if (typeof window !== "undefined") {
      window.removeEventListener("scroll", onScroll);
    }
  });

  // Reset list when search query changes (debounced)
  let searchDebounceTimer: any = null;
  watch(
    () => searchQuery.value,
    () => {
      if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
      searchDebounceTimer = setTimeout(() => {
        fetchMenuItems(true);
      }, 300);
    }
  );

  watch(
    () => selectedCategory.value,
    () => {
      fetchMenuItems(true);
    }
  );

  // Methods
  // Function to resolve tableId from tableNumber if needed
  const resolveTableId = async () => {
    // Nếu đã có tableId, không cần làm gì
    if (hasValidTableId()) return

    try {
      console.log('Attempting to resolve tableId from tableNumber:', tableNumber.value)
      const tables = await TableApi.list()
      const tableList = Array.isArray((tables as any)?.data) 
        ? (tables as any).data 
        : Array.isArray(tables) 
        ? tables 
        : []
      if (!tableList.length) {
        throw new Error("Hiện chưa có bàn nào trong hệ thống. Vui lòng tạo bàn trong trang quản lý trước.")
      }

      const lookup = normalizeTableLookup(tableNumber.value)

      // Tìm bàn có name/number/id khớp linh hoạt: "Bàn 1", "Ban 1", "B01", "B1", "1"
      const foundTable = lookup
        ? tableList.find((t: any) => {
            const candidates = [
              t.id,
              t.name,
              t.number,
              t.tableNumber,
            ]
            return candidates.some((candidate) => {
              const asText = String(candidate ?? "")
              return asText === String(tableNumber.value) || normalizeTableLookup(asText) === lookup
            })
          })
        : tableList[0]

      const resolvedTable = foundTable || tableList[0]
      
      if (resolvedTable) {
        tableId.value = String(resolvedTable.id)
        tableNumber.value = String(resolvedTable.name || resolvedTable.number || tableNumber.value || "")
        console.log('Successfully resolved tableId:', tableId.value)
      } else {
        throw new Error(`Không tìm thấy bàn với số: ${tableNumber.value}. Vui lòng kiểm tra lại URL hoặc liên hệ nhân viên.`)
      }
    } catch (error: any) {
      // Nếu không thể lấy danh sách bàn (backend không chạy hoặc lỗi)
      if (error?.message?.includes('ERR_CONNECTION_REFUSED') || error?.message?.includes('Failed to fetch')) {
        throw new Error('Không thể kết nối đến server. Vui lòng kiểm tra xem backend đã chạy chưa.')
      }
      throw error
    }
  }

  const ensureCart = async () => {
    // Nếu không có tableId, thử resolve từ tableNumber
    if (!hasValidTableId()) {
      await resolveTableId()
    }
    
    if (!hasValidTableId()) {
      throw new Error('TableId is required. Please ensure the URL contains tableId parameter (e.g., ?tableId=xxx&tableNumber=xxx).')
    }
    
    try {
      // Gọi endpoint idempotent để mở/tạo giỏ theo tableId
      const cart = await CartApi.openForTable(String(tableId.value))
      
      // Đảm bảo truy cập đúng response format
      if (!cart || !cart.sessionId) {
        throw new Error('Failed to create or retrieve cart. SessionId is missing.')
      }
      
      sessionId.value = cart.sessionId
      if (cart.tableName) {
        tableNumber.value = cart.tableName
      }
      
      // Đảm bảo items không null/undefined
      const items = cart.items || []
      
      cartStore.setFromApiItems(items)
    } catch (error: any) {
      console.error('Error in ensureCart:', error)
      // Cải thiện error message cho connection errors
      if (error?.message?.includes('ERR_CONNECTION_REFUSED') || error?.message?.includes('Failed to fetch')) {
        throw new Error('Không thể kết nối đến server. Vui lòng đảm bảo backend API đang chạy tại http://localhost:8080')
      }
      throw new Error(error?.message || 'Failed to ensure cart. Please try again.')
    }
  }

  const customerNameStorageKey = computed(() =>
    sessionId.value ? `customerName:${sessionId.value}` : "customerName"
  );

  const hydrateCustomerName = async () => {
    if (typeof window === "undefined") return;
    const cached = localStorage.getItem(customerNameStorageKey.value);
    if (cached) {
      customerName.value = cached;
      showCustomerNameDialog.value = false;
      return;
    }
    if (sessionId.value) {
      try {
        const session = (await customerSessionApi.get(sessionId.value)) as any;
        if (session?.customerName) {
          customerName.value = session.customerName;
          localStorage.setItem(customerNameStorageKey.value, session.customerName);
          showCustomerNameDialog.value = false;
          return;
        }
      } catch {}
    }
    showCustomerNameDialog.value = true;
  };

  const saveCustomerName = async () => {
    const name = customerName.value.trim();
    if (!name) {
      const toast = useNuxtApp().$toast as typeof toastType;
      toast.error("Vui lòng nhập tên của bạn");
      return false;
    }
    await ensureCart();
    if (!hasValidTableId()) return false;
    await customerSessionApi.save({
      sessionId: sessionId.value,
      tableId: String(tableId.value),
      customerName: name,
    });
    if (typeof window !== "undefined") {
      localStorage.setItem(customerNameStorageKey.value, name);
    }
    showCustomerNameDialog.value = false;
    return true;
  };

  const fetchCartFromServer = async () => {
    if (!sessionId.value || isCartOperationInProgress.value) return
    
    try {
      const cart = await CartApi.getBySession(sessionId.value)
      
      // Lưu items cũ để so sánh
      const oldItems = [...cartStore.items]
      cartStore.setFromApiItems(cart.items || [])
      
      // Bỏ thông báo khi khách hàng khác thêm món
    } catch (error) {
      console.error('Failed to fetch cart from server:', error)
    }
  }

  const ensureTableClearedState = async () => {
    if (!hasValidTableId()) return;
    try {
      const detail = await TableApi.getDetail(String(tableId.value));
      const noActiveOrder = !detail.activeOrderId;
      const isEmpty = detail.status === "trống";
      // Chỉ coi là bàn đã dọn khi đồng thời không có order đang mở VÀ trạng thái bàn là trống
      if (noActiveOrder && isEmpty) {
        clearLocalCart();
        cartStore.clearCart();
        // Không xóa orderedItems vì sẽ được sync từ server
      }
    } catch {}
  };

  const addToCart = async (item: any) => {
    if (!customerName.value.trim()) {
      showCustomerNameDialog.value = true;
      return;
    }
    // Cho phép bấm nhanh liên tục: không chặn, chỉ debounce gửi API theo lượt
    const pendingCount = (window as any).__pendingAdds || (window as any).__pendingAdds === 0 ? (window as any).__pendingAdds : 0
    ;(window as any).__pendingAdds = pendingCount + 1
    
    try {
      isCartOperationInProgress.value = true
      
      // Đảm bảo cart được tạo và sessionId được set
      await ensureCart()
      
      // Kiểm tra sessionId đã được set chưa (double check)
      if (!sessionId.value) {
        throw new Error('Không thể tạo giỏ hàng. Vui lòng kiểm tra lại thông tin bàn.')
      }
      
      // Sử dụng quantity từ item nếu có, mặc định là 1
      const quantity = item.quantity || 1
      
      // OPTIMISTIC: cập nhật local state ngay lập tức
      const existingItem = cartStore.items.find(i => i.id === item.id)
      const prevQuantity = existingItem?.quantity ?? 0
      if (existingItem) {
        existingItem.quantity = prevQuantity + quantity
      } else {
        cartStore.addItem({ ...item, quantity })
      }

      // Luôn sử dụng addItem API để tránh race condition
      // Backend sẽ tự động xử lý: nếu item đã có thì cộng dồn, nếu chưa có thì thêm mới
      const addItemData = { menuItemId: item.id, quantity: quantity }
      try {
        await CartApi.addItem(sessionId.value, addItemData)
      } catch (e: any) {
        // Revert optimistic on error
        if (existingItem) {
          existingItem.quantity = prevQuantity
        } else {
          // remove the added item
          cartStore.items = cartStore.items.filter(i => i.id !== item.id)
        }
        // Re-throw với message rõ ràng hơn
        const errorMessage = e?.message || 'Không thể thêm món vào giỏ hàng'
        throw new Error(errorMessage)
      }
      
      // Track item vừa được thêm để hiển thị toast
      lastAddedItem.value = { id: item.id, name: item.name, quantity: quantity }
      
      // Bỏ thông báo toast khi thêm món
      
      // Sync từ server sau một delay nhỏ để đảm bảo UI đã cập nhật
      setTimeout(async () => {
        await fetchCartFromServer()
      }, 80)
    } catch (error: any) {
      console.error('Failed to add item to cart:', error)
      const toast = useNuxtApp().$toast as typeof toastType;
      const errorMessage = error?.message || `Lỗi thêm vào giỏ: ${item.name}`
      toast.error(errorMessage)
    } finally {
      isCartOperationInProgress.value = false
      // Cho phép click tiếp ngay; không giữ disabled state
      addingToCart.value.delete(item.id)
    }
  };

  const confirmOrder = async () => {
    if (!customerName.value.trim()) {
      showCustomerNameDialog.value = true;
      return;
    }
    await ensureCart();
    const orderId = await CartApi.confirmOrder(sessionId.value);
    const nuxt = useNuxtApp() as any;
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.success("Đặt món thành công! Đơn hàng của bạn đã được gửi đến bếp");

    // Không đăng ký subscriptions lặp lại ở đây để tránh trùng thông báo.
    // Các subscription theo bàn đã được khởi tạo khi load trang.

    // Xóa giỏ hàng cục bộ ngay lập tức để badge số lượng về 0
    // Server sẽ giữ lịch sử món đã đặt và được đồng bộ ở bước dưới
    cartStore.clearCart();

    // Không cần commitCurrentAsOrdered nữa vì server sẽ trả về đầy đủ
    // Sync lại từ server để có thông tin mới nhất
    await syncOrderedItemsFromServer();

    showCart.value = false;
  };

  const submitRating = async () => {
    const toast = useNuxtApp().$toast as typeof toastType;
    try {
      await reviewApi.create({
        tableId: tableId.value || undefined,
        sessionId: sessionId.value,
        customerName: customerName.value,
        rating: ratingValue.value,
        comment: ratingComment.value,
      });
      toast.success("Cảm ơn đánh giá! Phản hồi đã được ghi nhận");
    } catch (e: any) {
      toast.error(e.message || "Gửi đánh giá thất bại");
    }
    showRatingDialog.value = false;
    ratingComment.value = "";
  };

  // Sync ordered items từ server để hiển thị tất cả món đã đặt của cả bàn
  const syncOrderedItemsFromServer = async () => {
    if (!hasValidTableId()) {
      return
    }

    try {
      const tableDetail = await TableApi.getDetail(String(tableId.value))
      
      if (tableDetail.sessionItems && tableDetail.sessionItems.length > 0) {
        
        // Chuyển đổi sessionItems thành orderedItems format
        const { deriveOrderItemStatus } = await import('~/utils/formatters')
        const serverOrderedItems = tableDetail.sessionItems.map((item: any) => {
          const status = deriveOrderItemStatus(item)
          return {
            id: item.id,
            name: item.menuItemName,
            price: Number(item.unitPrice),
            imageUrl: item.menuItemImageUrl || "",
            categoryId: "",
            description: "",
            badges: [],
            rating: 0,
            prepTime: "",
            isAvailable: true,
            orders: 0,
            quantity: (item.totalQuantity ?? ((item.pendingQuantity || 0) + (item.preparingQuantity || 0) + (item.completedQuantity || 0) + (item.servedQuantity || 0))),
            note: item.notes || "", // Copy ghi chú từ server
            status,
            orderTime: item.orderTime || null
          }
        })
        
        // Thay thế hoàn toàn orderedItems từ server để tránh duplicate
        // Server đã có đầy đủ thông tin mới nhất
        // Hiển thị tất cả món (bao gồm cả đã phục vụ) để khách thấy lịch sử đầy đủ
        cartStore.orderedItems = serverOrderedItems
        
      } else {
        cartStore.orderedItems = []
      }
    } catch (error) {
      console.error("Failed to sync ordered items from server:", error);
    }
  };

  // Hydrate and ensure server cart on client mount
  onMounted(() => {
    fetchCategories();
    ensureTableClearedState();
    // Auto-create/sync cart for this session when customer opens the table
    // ensureCart sẽ tự động resolve tableId nếu cần
    ensureCart()
      .then(() => {
        hydrateCustomerName();
        // Sync ordered items từ server
        syncOrderedItemsFromServer()
          .then(() => {
            // synced
          })
          .catch((error) => {
            console.error("Failed to sync ordered items from server:", error);
          });
      })
      .catch((error) => {
        console.error("Failed to ensure cart:", error);
        hydrateCustomerName();
      });

    if (typeof window !== "undefined") {
      const nuxt = useNuxtApp() as any;
      if (nuxt?.$realtime && hasValidTableId()) {
        
        // Subscribe cart updates - khi có khách khác thêm/sửa/xóa món trong giỏ
        nuxt.$realtime.subscribe(`/topic/tables/${tableId.value}/cart`, (msg: any) => {
          if (msg?.type === 'CART_UPDATED') {
            // Tránh sync nếu đang có operation đang chạy
            if (isCartOperationInProgress.value) {
              return
            }
            // Thêm delay nhỏ để tránh race condition
            setTimeout(() => {
              // Đảm bảo cart được tạo trước khi sync
              ensureCart().then(() => {
                fetchCartFromServer()
                // Clear lastAddedItem để tránh toast bị conflict
                lastAddedItem.value = null
              }).catch((error) => {
                console.error('Failed to ensure cart for realtime sync:', error)
              })
            }, 100)
          }
        })
        
        // Subscribe ordered items updates - khi có khách khác confirm order
        nuxt.$realtime.subscribe(`/topic/tables/${tableId.value}/ordered`, (msg: any) => {
          if (msg?.type === 'ORDERED_UPDATED') {
            // Khi có order mới, cần sync cả ordered items và cart hiện tại
            syncOrderedItemsFromServer()
            // Clear cart local vì items đã được chuyển sang ordered
            cartStore.clearCart()
            // Sync cart từ server để đảm bảo cart badge và items được cập nhật đúng
            if (sessionId.value) {
              fetchCartFromServer()
            }
          }
        })
        
        // Subscribe general table updates - khi có thay đổi trạng thái món từ staff
        nuxt.$realtime.subscribe(`/topic/tables/${tableId.value}`, (msg: any) => {
          if (msg?.type === 'ORDER_STATUS_UPDATED') {
            syncOrderedItemsFromServer()
          } else if (msg?.type === 'ORDERED_UPDATED') {
            // Khi có ordered items update, cần sync cả ordered items và cart
            syncOrderedItemsFromServer()
            // Clear cart local vì items có thể đã được chuyển sang ordered
            cartStore.clearCart()
            // Sync cart từ server để đảm bảo cart badge và items được cập nhật đúng
            if (sessionId.value) {
              fetchCartFromServer()
            }
          } else if (msg?.type === 'TABLE_CLEARED') {
            cartStore.clearCart()
            cartStore.orderedItems = []
          }
        })
        
        // Subscribe cho order items status updates - quan trọng để customer thấy được trạng thái món realtime
        nuxt.$realtime.subscribe(`/topic/tables/${tableId.value}/order-items`, (msg: any) => {
          if (msg?.type === 'ORDER_ITEM_STATUS_CHANGED') {
            // Backend không gửi status nữa; luôn sync lại từ server để lấy quantities mới
            syncOrderedItemsFromServer()
          }
        } );

        // Subscribe cho management updates - khi staff thay đổi trạng thái món
        nuxt.$realtime.subscribe('/topic/management/order-items', (msg: any) => {
          if (msg?.type === 'ORDER_ITEM_STATUS_CHANGED') {
            syncOrderedItemsFromServer()
          }
        })
      }
    }
  });

  const updateQuantity = (() => {
    const timers = new Map<number, any>()
    return async (id: number, quantity: number) => {
      await ensureCart()

      // Optimistic update ngay lập tức
      const currentItem = cartStore.items.find(i => i.id === id)
      const oldQuantity = currentItem?.quantity || 0
      if (currentItem) currentItem.quantity = quantity

      // Debounce gửi API để cho phép click nhanh liên tục
      if (timers.has(id)) clearTimeout(timers.get(id))
      timers.set(id, setTimeout(async () => {
        try {
          if (quantity <= 0) {
            await CartApi.removeItem(sessionId.value, id)
            cartStore.items = cartStore.items.filter(i => i.id !== id)
          } else {
            await CartApi.updateItem(sessionId.value, id, { quantity })
          }
          // đồng bộ nền
          setTimeout(() => { fetchCartFromServer(); syncOrderedItemsFromServer(); }, 50)
        } catch (error) {
          console.error('Failed to update quantity:', error)
          const toast = useNuxtApp().$toast as typeof toastType;
          toast.error('Lỗi cập nhật số lượng');
          if (currentItem) currentItem.quantity = oldQuantity
        }
      }, 120))
    }
  })();

  const updateNote = async (id: number, note: string) => {
    await ensureCart();
    const item = cartStore.items.find((i) => i.id === id);
    await CartApi.updateItem(sessionId.value, id, {
      quantity: item?.quantity ?? 1,
      notes: note
    })
    await fetchCartFromServer()
  };

  // Function để refresh ordered items từ server (có thể gọi từ bên ngoài)
  const refreshOrderedItems = async () => {
    await syncOrderedItemsFromServer();
  };

  // Function để test realtime connection và hiển thị trạng thái hiện tại
  const testRealtimeStatus = () => {
    if (typeof window === "undefined") return;

    const nuxt = useNuxtApp() as any;
    if (nuxt?.$realtime) {
      // Removed verbose test logs; just trigger a sync
      syncOrderedItemsFromServer()
    } else {
      // no-op
    }
  };

  return {
    // Stores
    cartStore,
    menuStore,

    // Reactive data
    showCart,
    showRatingDialog,
    showCustomerNameDialog,
    ratingValue,
    ratingComment,
    selectedCategory,
    searchQuery,
    tableNumber,
    tableId,
    sessionId, // Expose sessionId
    customerName,
    groupMembers,
    loadingMenu,
    menuError,
    addingToCart,
    updatingQuantity,
    isCartOperationInProgress,

    // Computed
    categories,
    filteredItems,

    // Methods
    addToCart,
    confirmOrder,
    submitRating,
    updateQuantity,
    updateNote,
    fetchMenuItems,
    ensureTableClearedState,
    // expose để khi mở modal có thể đồng bộ cart từ server
    ensureCart,
    saveCustomerName,
    fetchCartFromServer,
    refreshOrderedItems,
    testRealtimeStatus,
  };
};
