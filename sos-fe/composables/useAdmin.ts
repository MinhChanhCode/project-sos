import { useMenuStore } from "~/stores/menu";
import { useStaffStore } from "~/stores/staff";
import { ref, computed, onMounted } from "vue";
import { useNuxtApp } from "nuxt/app";
import type { toast as toastType } from "vue3-toastify";
import type { MenuItem } from "~/stores/cart";
import { menuApi, type MenuItemCreateRequest } from "@/api-service/MenuApi";
import { dashboardApi, employeeApi, reviewApi } from "@/api-service/ExtendedApi";
import { useConfirm } from "~/composables/useConfirm";

export const useAdmin = () => {
  const menuStore = useMenuStore();
  const staffStore = useStaffStore();

  // Reactive data
  const activeTab = ref(0);
  const selectedItem = ref<any>(null);
  const selectedStaff = ref<any>(null);
  const showAddItem = ref(false);
  const showAddStaff = ref(false);
  const loadingMenu = ref(false);
  const menuError = ref("");
  const showEditItem = computed({
    get: () => !!selectedItem.value,
    set: (value) => {
      if (!value) selectedItem.value = null;
    },
  });
  const showStaffDetail = computed({
    get: () => !!selectedStaff.value,
    set: (value) => {
      if (!value) selectedStaff.value = null;
    },
  });

  const newItemForm = ref({
    name: "",
    price: 0,
    category: "", // sẽ lưu id dạng string -> sẽ chuyển sang number khi gửi API
    description: "",
    imageFile: null as File | null,
  });

  const tabs = ref([
    { key: "dashboard", label: "Dashboard" },
    { key: "floor", label: "Sơ đồ bàn" },
    { key: "menu", label: "Món ăn" },
    { key: "staff", label: "Nhân viên" },
    { key: "reviews", label: "Đánh giá" },
    { key: "order-status", label: "Trạng thái món" },
    { key: "qr", label: "QR" },
  ]);

  const reviews = ref<any[]>([]);
  const dashboardData = ref<any>(null);

  const mockReviews = reviews;

  const dashboardStats = computed(() => ({
    activeTables: dashboardData.value?.activeTables ?? 0,
    todayRevenue: Number(dashboardData.value?.todayRevenue ?? 0),
    totalOrders: dashboardData.value?.todayOrders ?? 0,
    averageRating: 4.5,
    monthRevenue: Number(dashboardData.value?.monthRevenue ?? 0),
    pendingOrders: dashboardData.value?.pendingOrders ?? 0,
  }));

  const fetchDashboard = async () => {
    try {
      dashboardData.value = await dashboardApi.get();
    } catch (e) {
      console.warn("Dashboard load failed", e);
    }
  };

  const fetchReviews = async () => {
    try {
      reviews.value = await reviewApi.list();
    } catch {
      reviews.value = [];
    }
  };

  const fetchStaff = async () => {
    try {
      const list = await employeeApi.list();
      staffStore.staff = Array.isArray(list) ? list : [];
    } catch {
      staffStore.staff = [];
    }
  };

  onMounted(() => {
    fetchDashboard();
    fetchReviews();
    fetchStaff();
  });

  // Fetch menu items from API with pagination
  const fetchMenuItems = async (page = 0, size = 12): Promise<void> => {
    loadingMenu.value = true;
    menuError.value = "";
    try {
      const value = await menuApi.getAvailablePaged(page, size);

      const items: MenuItem[] = Array.isArray(value?.data?.content)
        ? value.data.content
        : Array.isArray(value?.content)
        ? value.content
        : Array.isArray(value?.data)
        ? value.data
        : Array.isArray(value)
        ? value
        : [];

      // Update store with items and pagination info
      menuStore.setItems(items);
      if (value?.data?.page?.totalPages !== undefined) {
        menuStore.setPageInfo(
          page,
          size,
          value.data.page.totalPages,
          value.data.page.totalElements || 0
        );
      } else {
        // Fallback: set default pagination info
        menuStore.setPageInfo(page, size, 1, items.length);
      }
    } catch (e: any) {
      menuError.value = e.message || "Lỗi tải menu";
    } finally {
      loadingMenu.value = false;
    }
  };

  // Load more items (for infinite scroll or pagination)
  const loadMoreItems = async (): Promise<void> => {
    if (menuStore.isLoadingMore) return;

    menuStore.isLoadingMore = true;
    try {
      const nextPage = menuStore.page + 1;
      const value = await menuApi.getAvailablePaged(nextPage, menuStore.size);
      const newItems: MenuItem[] = Array.isArray(value?.data?.content)
        ? value.data.content
        : Array.isArray(value?.content)
        ? value.content
        : Array.isArray(value?.data)
        ? value.data
        : Array.isArray(value)
        ? value
        : [];

      menuStore.appendItems(newItems);
      if (value?.data?.page?.totalPages !== undefined) {
        menuStore.setPageInfo(
          nextPage,
          menuStore.size,
          value.data.page.totalPages,
          value.data.page.totalElements || 0
        );
      }
    } catch (e: any) {
      menuError.value = e.message || "Lỗi tải thêm menu";
    } finally {
      menuStore.isLoadingMore = false;
    }
  };

  // Change page (traditional pagination)
  const changePage = async (page: number): Promise<void> => {
    await fetchMenuItems(page, menuStore.size);
  };

  // Change page size
  const changePageSize = async (size: number): Promise<void> => {
    await fetchMenuItems(0, size);
  };

  fetchMenuItems();

  // Methods
  const addNewItem = async () => {
    const { open } = useConfirm();
    const isConfirmed = await open({
      title: "Thêm món mới",
      message: "Bạn có chắc muốn thêm món mới?",
      confirmText: "Thêm",
      cancelText: "Hủy",
    });
    if (!isConfirmed) return;
    try {
      let imageUrl: string | undefined = undefined;
      if (newItemForm.value.imageFile) {
        imageUrl = await menuApi.uploadImage(newItemForm.value.imageFile);
      }

      const payload: MenuItemCreateRequest = {
        name: newItemForm.value.name,
        description: newItemForm.value.description,
        price: Number(newItemForm.value.price),
        imageUrl,
        categoryId: Number(newItemForm.value.category),
        isAvailable: true,
        isActive: true,
      };

      await menuApi.create(payload);
      const toast = useNuxtApp().$toast as typeof toastType;
      toast.success("Thêm món thành công!");
      await fetchMenuItems(0, menuStore.size); // Reset to first page
      newItemForm.value = {
        name: "",
        price: 0,
        category: "",
        description: "",
        imageFile: null,
      } as any;
      showAddItem.value = false;
    } catch (e: any) {
      const toast = useNuxtApp().$toast as typeof toastType;
      toast.error(e.message || "Lỗi khi thêm món!");
    }
  };

  // Sửa món ăn: gọi API và cập nhật store
  const updateItem = async () => {
    if (selectedItem.value) {
      const { open } = useConfirm();
      const isConfirmed = await open({
        title: "Lưu thay đổi",
        message: "Bạn có chắc muốn lưu thay đổi cho món này?",
        confirmText: "Lưu",
        cancelText: "Hủy",
      });
      if (!isConfirmed) return;
      try {
        await menuApi.update(selectedItem.value.id, selectedItem.value);
        menuStore.updateItem(selectedItem.value.id, selectedItem.value);
        await fetchMenuItems(menuStore.page, menuStore.size);
        const toast = useNuxtApp().$toast as typeof toastType;
        toast.success("Cập nhật thành công!");
        selectedItem.value = null;
      } catch (e: any) {
        const toast = useNuxtApp().$toast as typeof toastType;
        toast.error(e.message || "Lỗi cập nhật món!");
      }
    }
  };

  // Xóa món ăn: gọi API và cập nhật store
  const deleteItem = async (id: number | string) => {
    const { open } = useConfirm();
    const isConfirmed = await open({
      title: "Xóa món",
      message: "Bạn có chắc muốn xóa món này? Hành động không thể hoàn tác.",
      confirmText: "Xóa",
      cancelText: "Hủy",
      destructive: true,
    });
    if (!isConfirmed) return;
    try {
      await menuApi.delete(String(id));
      await fetchMenuItems(menuStore.page, menuStore.size);
      menuStore.removeItem(Number(id));
      const toast = useNuxtApp().$toast as typeof toastType;
      toast.success("Xóa món thành công!");
    } catch (e: any) {
      const toast = useNuxtApp().$toast as typeof toastType;
      toast.error(e.message || "Lỗi xóa món!");
    }
  };

  return {
    // Stores
    menuStore,
    staffStore,

    // Reactive data
    activeTab,
    selectedItem,
    selectedStaff,
    showAddItem,
    showAddStaff,
    loadingMenu,
    menuError,
    showEditItem,
    showStaffDetail,
    newItemForm,
    tabs,
    mockReviews,
    dashboardData,

    // Computed
    dashboardStats,

    // Methods
    addNewItem,
    updateItem,
    deleteItem,
    fetchMenuItems,
    loadMoreItems,
    changePage,
    changePageSize,
    fetchDashboard,
    fetchReviews,
    fetchStaff,
  };
};
