<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <h2 class="text-xl font-semibold">Quản lý yêu cầu phục vụ</h2>
      <div class="flex space-x-2">
        <UButton @click="refreshRequests" variant="outline" size="sm">
          <Icon name="lucide:refresh-cw" class="w-4 h-4 mr-2" />
          Làm mới
        </UButton>
        <UButton
          @click="showAllRequests = !showAllRequests"
          variant="outline"
          size="sm"
        >
          <Icon name="lucide:list" class="w-4 h-4 mr-2" />
          {{ showAllRequests ? "Chỉ hiện chờ" : "Tất cả" }}
        </UButton>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <UCard>
        <div class="text-center">
          <div class="text-2xl font-bold text-yellow-600">
            {{ stats.pending }}
          </div>
          <div class="text-sm text-gray-600">Bàn chờ xử lý</div>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-600">
            {{ stats.inProgress }}
          </div>
          <div class="text-sm text-gray-600">Bàn đang xử lý</div>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <div class="text-2xl font-bold text-green-600">{{ stats.done }}</div>
          <div class="text-sm text-gray-600">Bàn hoàn thành</div>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <div class="text-2xl font-bold text-gray-600">{{ stats.total }}</div>
          <div class="text-sm text-gray-600">Tổng bàn có yêu cầu</div>
        </div>
      </UCard>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-8">
      <UIcon name="lucide:loader-2" class="w-8 h-8 animate-spin" />
    </div>

    <!-- Service Requests List -->
    <div v-else class="space-y-4">
      <UCard
        v-for="request in filteredRequests"
        :key="request.id"
        class="hover:shadow-md transition-shadow cursor-pointer"
        @click="showTableDetail(request.tableId)"
      >
        <div class="p-4">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-1">
                <h3 class="font-semibold">
                  {{
                    request.tableName && request.tableName.trim().length
                      ? request.tableName
                      : `Bàn ${request.tableId}`
                  }}
                </h3>
                <UBadge
                  :color="getPriorityColor(request.priority)"
                  variant="solid"
                  size="xs"
                >
                  {{ getPriorityLabel(request.priority) }}
                </UBadge>
                <UBadge :color="getStatusColor(request.status)" variant="solid">
                  {{ getStatusLabel(request.status) }}
                </UBadge>
                <UBadge
                  v-if="getTableRequestCount(request.tableId) > 1"
                  color="gray"
                  variant="outline"
                  size="xs"
                >
                  {{ getTableRequestCount(request.tableId) }} yêu cầu
                </UBadge>
              </div>
              <p class="text-sm text-gray-600 mb-2">
                {{ getRequestTypeLabel(request.requestType) }}
              </p>
              <p class="text-gray-800">{{ request.description }}</p>
              <div v-if="request.notes" class="text-sm text-gray-500 mt-1">
                Ghi chú: {{ request.notes }}
              </div>
            </div>
            <div class="text-right text-sm text-gray-500">
              <div>{{ formatDateTime(request.requestedAt) }}</div>
              <div v-if="request.assignedTo" class="text-blue-600">
                NV: {{ request.assignedTo }}
              </div>
            </div>
          </div>
        </div>
      </UCard>

      <!-- Empty State -->
      <div
        v-if="filteredRequests.length === 0"
        class="text-center py-8 text-gray-500"
      >
        <Icon
          name="lucide:inbox"
          class="w-12 h-12 mx-auto mb-4 text-gray-300"
        />
        <p>Không có yêu cầu phục vụ nào</p>
      </div>
    </div>

    <!-- Table Detail Modal -->
    <UModal v-model="showTableDetailModal">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">
              Chi tiết
              {{ selectedTableDetail?.tableName || `Bàn ${selectedTableId}` }}
            </h3>
            <UButton
              @click="showTableDetailModal = false"
              variant="ghost"
              size="sm"
              square
            >
              <Icon name="lucide:x" class="w-4 h-4" />
            </UButton>
          </div>
        </template>

        <div v-if="loadingTableDetail" class="flex justify-center py-8">
          <UIcon name="lucide:loader-2" class="w-8 h-8 animate-spin" />
        </div>

        <div v-else-if="selectedTableDetail" class="space-y-6">
          <!-- Service Requests for this table -->
          <div>
            <h4 class="font-semibold mb-3">Yêu cầu phục vụ</h4>
            <div class="space-y-3">
              <div
                v-for="request in tableServiceRequests"
                :key="request.id"
                class="border rounded-lg p-3 hover:bg-gray-50 dark:hover:bg-gray-800"
              >
                <div class="flex items-start justify-between mb-2">
                  <div class="flex-1">
                    <div class="flex items-center space-x-2 mb-1">
                      <UBadge
                        :color="getPriorityColor(request.priority)"
                        variant="solid"
                        size="xs"
                      >
                        {{ getPriorityLabel(request.priority) }}
                      </UBadge>
                      <UBadge
                        :color="getStatusColor(request.status)"
                        variant="solid"
                        size="xs"
                      >
                        {{ getStatusLabel(request.status) }}
                      </UBadge>
                    </div>
                    <p class="text-sm text-gray-600 mb-1">
                      {{ getRequestTypeLabel(request.requestType) }}
                    </p>
                    <p class="text-sm">{{ request.description }}</p>
                    <div
                      v-if="request.notes"
                      class="text-xs text-gray-500 mt-1"
                    >
                      Ghi chú: {{ request.notes }}
                    </div>
                  </div>
                  <div class="text-right text-xs text-gray-500">
                    <div>{{ formatDateTime(request.requestedAt) }}</div>
                    <div v-if="request.assignedTo" class="text-blue-600">
                      NV: {{ request.assignedTo }}
                    </div>
                  </div>
                </div>

                <!-- Action Buttons for each request -->
                <div class="flex items-center space-x-2 pt-2 border-t">
                  <UButton
                    v-if="request.status === 'PENDING'"
                    @click="updateRequestStatus(request.id, 'IN_PROGRESS')"
                    color="blue"
                    size="xs"
                  >
                    <Icon name="lucide:play" class="w-3 h-3 mr-1" />
                    Bắt đầu
                  </UButton>

                  <UButton
                    v-if="request.status === 'IN_PROGRESS'"
                    @click="updateRequestStatus(request.id, 'DONE')"
                    color="green"
                    size="xs"
                  >
                    <Icon name="lucide:check" class="w-3 h-3 mr-1" />
                    Hoàn thành
                  </UButton>

                  <UButton
                    v-if="request.status === 'PENDING'"
                    @click="updateRequestStatus(request.id, 'CANCELLED')"
                    color="red"
                    size="xs"
                    variant="outline"
                  >
                    <Icon name="lucide:x" class="w-3 h-3 mr-1" />
                    Hủy
                  </UButton>

                  <UButton
                    v-if="!request.assignedTo"
                    @click="assignRequest(request.id)"
                    color="orange"
                    size="xs"
                    variant="outline"
                  >
                    <Icon name="lucide:user-plus" class="w-3 h-3 mr-1" />
                    Nhận việc
                  </UButton>
                </div>
              </div>

              <div
                v-if="tableServiceRequests.length === 0"
                class="text-center py-4 text-gray-500"
              >
                <Icon
                  name="lucide:inbox"
                  class="w-8 h-8 mx-auto mb-2 text-gray-300"
                />
                <p class="text-sm">Không có yêu cầu phục vụ nào</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-gray-500">
          <Icon
            name="lucide:alert-circle"
            class="w-12 h-12 mx-auto mb-4 text-gray-300"
          />
          <p>Không tìm thấy thông tin bàn</p>
        </div>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useNuxtApp } from "nuxt/app";
import type { toast as toastType } from "vue3-toastify";
import { serviceRequestApi, type ServiceRequest } from "@/api-service";

// Reactive data
const loading = ref(false);
const serviceRequests = ref<ServiceRequest[]>([]);
const showAllRequests = ref(false);

// Table detail modal
const showTableDetailModal = ref(false);
const selectedTableId = ref<string>("");
const selectedTableDetail = ref<any>(null);
const loadingTableDetail = ref(false);

// De-dup notifications and debounce fetch to avoid repeated toasts and calls
let fetchDebounceTimer: any = null;
const debouncedFetchServiceRequests = () => {
  if (fetchDebounceTimer) clearTimeout(fetchDebounceTimer);
  fetchDebounceTimer = setTimeout(() => {
    fetchServiceRequests();
  }, 200);
};
if (typeof window !== "undefined" && !(window as any).__seenServiceReqToasts) {
  (window as any).__seenServiceReqToasts = new Set<string>();
}

// Computed
const filteredRequests = computed<ServiceRequest[]>(() => {
  let requests = serviceRequests.value;

  // Filter by status if not showing all
  if (!showAllRequests.value) {
    requests = requests.filter(
      (r: ServiceRequest) =>
        r.status === "PENDING" || r.status === "IN_PROGRESS"
    );
  }

  // Group by table and get the first request for each table
  const tableGroups = new Map<string, ServiceRequest>();

  requests.forEach((request) => {
    const tableKey = request.tableId;

    // If this table doesn't have a request yet, or this request is earlier
    if (
      !tableGroups.has(tableKey) ||
      new Date(request.requestedAt) <
        new Date(tableGroups.get(tableKey)!.requestedAt)
    ) {
      tableGroups.set(tableKey, request);
    }
  });

  // Convert back to array and sort by requestedAt (ascending)
  const groupedRequests = Array.from(tableGroups.values());
  return groupedRequests.sort(
    (a, b) =>
      new Date(a.requestedAt).getTime() - new Date(b.requestedAt).getTime()
  );
});

const stats = computed(() => {
  const requests: ServiceRequest[] = serviceRequests.value as ServiceRequest[];

  // Group by table to count unique tables
  const tableGroups = new Map<string, ServiceRequest>();
  requests.forEach((request) => {
    const tableKey = request.tableId;
    if (
      !tableGroups.has(tableKey) ||
      new Date(request.requestedAt) <
        new Date(tableGroups.get(tableKey)!.requestedAt)
    ) {
      tableGroups.set(tableKey, request);
    }
  });

  const groupedRequests = Array.from(tableGroups.values());

  return {
    pending: groupedRequests.filter(
      (r: ServiceRequest) => r.status === "PENDING"
    ).length,
    inProgress: groupedRequests.filter(
      (r: ServiceRequest) => r.status === "IN_PROGRESS"
    ).length,
    done: groupedRequests.filter((r: ServiceRequest) => r.status === "DONE")
      .length,
    total: groupedRequests.length,
  };
});

// Methods
const fetchServiceRequests = async () => {
  loading.value = true;
  try {
    const res = await serviceRequestApi.getPending();
    const list = Array.isArray((res as any)?.data)
      ? (res as any).data
      : Array.isArray((res as any)?.content)
      ? (res as any).content
      : Array.isArray(res)
      ? (res as any)
      : [];
    serviceRequests.value = list as any;
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error(error.message || "Lỗi khi tải yêu cầu phục vụ");
  } finally {
    loading.value = false;
  }
};

const refreshRequests = async () => {
  await fetchServiceRequests();
  const toast = useNuxtApp().$toast as typeof toastType;
  toast.success("Đã làm mới danh sách");
};

const updateRequestStatus = async (id: number, status: string) => {
  try {
    await serviceRequestApi.updateStatus(id, status as any);
    await fetchServiceRequests();

    // Refresh table detail if modal is open
    if (showTableDetailModal.value && selectedTableId.value) {
      await fetchTableDetail(selectedTableId.value);
    }

    const toast = useNuxtApp().$toast as typeof toastType;
    toast.success("Cập nhật trạng thái thành công");
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error(error.message || "Lỗi khi cập nhật trạng thái");
  }
};

const assignRequest = async (id: number) => {
  try {
    // Giả sử có thông tin nhân viên hiện tại
    const currentStaff = "Nhân viên hiện tại"; // Có thể lấy từ store hoặc context
    await serviceRequestApi.assign(id, currentStaff);
    await fetchServiceRequests();

    // Refresh table detail if modal is open
    if (showTableDetailModal.value && selectedTableId.value) {
      await fetchTableDetail(selectedTableId.value);
    }

    const toast = useNuxtApp().$toast as typeof toastType;
    toast.success("Đã nhận yêu cầu");
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error(error.message || "Lỗi khi nhận yêu cầu");
  }
};

// Table detail methods
const showTableDetail = async (tableId: string) => {
  selectedTableId.value = tableId;
  showTableDetailModal.value = true;
  await fetchTableDetail(tableId);
};

const fetchTableDetail = async (tableId: string) => {
  loadingTableDetail.value = true;
  try {
    // Import TableApi
    const { TableApi } = await import("@/api-service");
    const detail = await TableApi.getDetail(tableId);
    selectedTableDetail.value = detail;
  } catch (error: any) {
    console.error("Error fetching table detail:", error);
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error("Lỗi khi tải thông tin bàn");
  } finally {
    loadingTableDetail.value = false;
  }
};

const setupRealtimeSubscription = () => {
  const nuxt = useNuxtApp() as any;
  if (nuxt?.$realtime) {
    nuxt.$realtime.onServiceRequest((payload: any) => {
      if (
        payload?.type === "SERVICE_REQUEST_CREATED" ||
        payload?.type === "SERVICE_REQUEST_UPDATED"
      ) {
        // De-dup toasts by event key
        try {
          const seen: Set<string> = (window as any).__seenServiceReqToasts;
          const id = payload?.data?.id ?? payload?.id ?? "unknown";
          const status = payload?.data?.status ?? payload?.status ?? "";
          const key = `${payload.type}:${id}:${status}`;
          if (!seen.has(key)) {
            seen.add(key);
            const toast = useNuxtApp().$toast as typeof toastType;
            if (payload.type === "SERVICE_REQUEST_CREATED") {
              toast.info(
                `Yêu cầu mới từ ${
                  payload.data.tableName || "Bàn " + payload.data.tableId
                }`
              );
            }
          }
        } catch {}
        // Debounce fetch
        debouncedFetchServiceRequests();
      }
    });
  }
};

// Helper functions
const getRequestTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    GENERAL_SERVICE: "Dịch vụ chung",
    FOOD_REQUEST: "Yêu cầu về món ăn",
    CLEANING: "Dọn dẹp",
    PAYMENT: "Thanh toán",
    OTHER: "Khác",
  };
  return labels[type] || type;
};

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: "Chờ xử lý",
    IN_PROGRESS: "Đang xử lý",
    DONE: "Hoàn thành",
    CANCELLED: "Đã hủy",
  };
  return labels[status] || status;
};

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    PENDING: "yellow",
    IN_PROGRESS: "blue",
    DONE: "green",
    CANCELLED: "red",
  };
  return colors[status] || "gray";
};

const getPriorityLabel = (priority: string) => {
  const labels: Record<string, string> = {
    LOW: "Thấp",
    MEDIUM: "Trung bình",
    HIGH: "Cao",
    URGENT: "Khẩn cấp",
  };
  return labels[priority] || priority;
};

const getPriorityColor = (priority: string) => {
  const colors: Record<string, string> = {
    LOW: "gray",
    MEDIUM: "blue",
    HIGH: "orange",
    URGENT: "red",
  };
  return colors[priority] || "gray";
};

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString("vi-VN");
};

// Helper function to count requests per table
const getTableRequestCount = (tableId: string) => {
  return serviceRequests.value.filter(
    (r: ServiceRequest) => r.tableId === tableId
  ).length;
};

// Computed property for table service requests
const tableServiceRequests = computed(() => {
  if (!selectedTableId.value) return [];
  return serviceRequests.value
    .filter((r: ServiceRequest) => r.tableId === selectedTableId.value)
    .sort(
      (a, b) =>
        new Date(a.requestedAt).getTime() - new Date(b.requestedAt).getTime()
    );
});

// Lifecycle
let refreshTimer: any;
onMounted(async () => {
  await fetchServiceRequests();
  setupRealtimeSubscription();
  refreshTimer = setInterval(() => {
    if (!loading.value) {
      fetchServiceRequests();
    }
  }, 30000);
});

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});
</script>
