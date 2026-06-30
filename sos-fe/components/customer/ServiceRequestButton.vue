<template>
  <div class="fixed bottom-24 right-4 z-50">
    <!-- Service Request Button -->
    <UButton
      @click="showServiceRequestModal = true"
      color="orange"
      size="lg"
      class="rounded-full bg-orange-500 shadow-lg shadow-orange-500/25 transition hover:bg-orange-600 hover:shadow-xl"
    >
      <Icon name="lucide:phone" class="w-5 h-5 mr-2" />
      Gọi phục vụ
      <UBadge
        v-if="serviceRequests.length > 0"
        :value="serviceRequests.length"
        color="blue"
        size="sm"
        class="ml-2"
      />
    </UButton>

    <!-- Service Request Modal -->
    <UModal v-model="showServiceRequestModal">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">Gọi phục vụ & Lịch sử yêu cầu</h3>
            <UButton
              @click="showServiceRequestModal = false"
              variant="ghost"
              size="sm"
              square
            >
              <Icon name="lucide:x" class="w-4 h-4" />
            </UButton>
          </div>
        </template>

        <!-- Content Sections -->
        <div class="space-y-6">
          <!-- Gọi phục vụ Section -->
          <div>
            <h4
              class="text-md font-semibold text-gray-900 dark:text-gray-100 mb-4 flex items-center"
            >
              <Icon name="lucide:phone" class="w-5 h-5 mr-2 text-blue-600" />
              Gọi phục vụ
            </h4>
            <form @submit.prevent="submitServiceRequest" class="space-y-4">
              <!-- Request Type -->
              <UFormGroup label="Loại yêu cầu" name="requestType">
                <USelect
                  v-model="form.requestType"
                  :options="requestTypeOptions"
                  placeholder="Chọn loại yêu cầu"
                  required
                />
              </UFormGroup>

              <!-- Description -->
              <UFormGroup label="Mô tả yêu cầu" name="description">
                <UTextarea
                  v-model="form.description"
                  placeholder="Mô tả chi tiết yêu cầu của bạn..."
                  :rows="3"
                  required
                />
              </UFormGroup>

              <!-- Priority -->
              <UFormGroup label="Mức độ ưu tiên" name="priority">
                <USelect
                  v-model="form.priority"
                  :options="priorityOptions"
                  placeholder="Chọn mức độ ưu tiên"
                />
              </UFormGroup>

              <!-- Notes -->
              <UFormGroup label="Ghi chú thêm" name="notes">
                <UTextarea
                  v-model="form.notes"
                  placeholder="Ghi chú bổ sung (không bắt buộc)..."
                  :rows="2"
                />
              </UFormGroup>
            </form>
          </div>

          <!-- Lịch sử yêu cầu Section -->
          <div>
            <div class="flex items-center justify-between mb-4">
              <h4
                class="text-md font-semibold text-gray-900 dark:text-gray-100 flex items-center"
              >
                <Icon
                  name="lucide:history"
                  class="w-5 h-5 mr-2 text-blue-600"
                />
                Lịch sử yêu cầu
                <UBadge
                  v-if="serviceRequests.length > 0"
                  :value="serviceRequests.length"
                  color="blue"
                  size="sm"
                  class="ml-2"
                />
              </h4>
              <UButton
                @click="fetchServiceRequests"
                variant="outline"
                size="sm"
                :loading="submitting"
              >
                <Icon name="lucide:refresh-cw" class="w-4 h-4 mr-1" />
                Làm mới
              </UButton>
            </div>
            <div class="space-y-4">
              <div v-if="serviceRequests.length === 0" class="text-center py-8">
                <Icon
                  name="lucide:inbox"
                  class="w-12 h-12 mx-auto text-gray-400 mb-2"
                />
                <p class="text-gray-500">Chưa có yêu cầu phục vụ nào</p>
              </div>

              <div
                v-for="request in serviceRequests"
                :key="request.id"
                class="border rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <!-- Header with type and status -->
                <div class="flex items-center justify-between mb-3">
                  <div class="flex items-center space-x-2">
                    <Icon
                      :name="getRequestTypeIcon(request.requestType)"
                      class="w-5 h-5 text-blue-600"
                    />
                    <span
                      class="font-semibold text-gray-900 dark:text-gray-100"
                    >
                      {{ getRequestTypeLabel(request.requestType) }}
                    </span>
                  </div>
                  <UBadge
                    :color="getStatusColor(request.status)"
                    variant="solid"
                    size="sm"
                  >
                    {{ getStatusLabel(request.status) }}
                  </UBadge>
                </div>

                <!-- Description -->
                <p
                  class="text-sm text-gray-700 dark:text-gray-300 mb-3 leading-relaxed"
                >
                  {{ request.description }}
                </p>

                <!-- Priority -->
                <div class="flex items-center space-x-2 mb-3">
                  <Icon name="lucide:flag" class="w-4 h-4 text-orange-500" />
                  <span class="text-xs text-gray-600 dark:text-gray-400">
                    Mức độ: {{ getPriorityLabel(request.priority) }}
                  </span>
                </div>

                <!-- Notes if exists -->
                <div
                  v-if="request.notes"
                  class="mb-3 p-2 bg-gray-50 dark:bg-gray-800 rounded text-xs text-gray-600 dark:text-gray-400"
                >
                  <strong>Ghi chú:</strong> {{ request.notes }}
                </div>

                <!-- Timestamps and staff info -->
                <div
                  class="flex items-center justify-between text-xs text-gray-500 border-t pt-2"
                >
                  <div class="flex flex-col space-y-1">
                    <span class="flex items-center space-x-1">
                      <Icon name="lucide:clock" class="w-3 h-3" />
                      <span>{{ formatDateTime(request.requestedAt) }}</span>
                    </span>
                    <span
                      v-if="request.startedAt"
                      class="flex items-center space-x-1"
                    >
                      <Icon name="lucide:play" class="w-3 h-3 text-blue-500" />
                      <span
                        >Bắt đầu: {{ formatDateTime(request.startedAt) }}</span
                      >
                    </span>
                    <span
                      v-if="request.completedAt"
                      class="flex items-center space-x-1"
                    >
                      <Icon
                        name="lucide:check"
                        class="w-3 h-3 text-green-500"
                      />
                      <span
                        >Hoàn thành:
                        {{ formatDateTime(request.completedAt) }}</span
                      >
                    </span>
                  </div>
                  <div v-if="request.assignedTo" class="text-right">
                    <div class="flex items-center space-x-1 text-blue-600">
                      <Icon name="lucide:user" class="w-3 h-3" />
                      <span>{{ request.assignedTo }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex space-x-2">
            <UButton
              @click="showServiceRequestModal = false"
              variant="outline"
              class="flex-1"
            >
              Đóng
            </UButton>
            <UButton
              @click="submitServiceRequest" 
              :loading="submitting"
              color="orange"
              class="flex-1"
            >
              <Icon name="lucide:phone" class="w-4 h-4 mr-2" />
              Gửi yêu cầu
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useNuxtApp } from "nuxt/app";
import type { toast as toastType } from "vue3-toastify";
import {
  serviceRequestApi,
  type ServiceRequest,
  type ServiceRequestCreateRequest,
} from "~/api-service/ServiceRequestApi";
import { TableApi } from "@/api-service";
import { useCustomer } from "~/composables/useCustomer";

interface Props {
  tableId: string | null;
  tableName: string;
  sessionId: string;
}

const props = defineProps<Props>();

// Reactive data
const showServiceRequestModal = ref(false);
const submitting = ref(false);
const serviceRequests = ref<ServiceRequest[]>([]);
const pendingCount = ref(0);

// Watch for sessionId changes
const previousSessionId = ref<string | undefined>(props.sessionId);

// Debounce and de-dup helpers
let fetchDebounceTimer: any = null;
const debouncedFetchServiceRequests = () => {
  if (fetchDebounceTimer) clearTimeout(fetchDebounceTimer);
  fetchDebounceTimer = setTimeout(() => {
    fetchServiceRequests();
  }, 200);
};
// Global seen set to avoid duplicate toasts across HMR/remounts
if (typeof window !== "undefined" && !(window as any).__seenServiceReqToasts) {
  (window as any).__seenServiceReqToasts = new Set<string>();
}

// Form data
const form = ref<ServiceRequestCreateRequest>({
  tableId: props.tableId || "",
  tableName: props.tableName,
  sessionId: "",
  requestType: "GENERAL_SERVICE",
  description: "",
  priority: "MEDIUM",
  notes: "",
});

// Options
const requestTypeOptions = [
  { label: "Dịch vụ chung", value: "GENERAL_SERVICE" },
  { label: "Yêu cầu về món ăn", value: "FOOD_REQUEST" },
  { label: "Dọn dẹp", value: "CLEANING" },
  { label: "Thanh toán", value: "PAYMENT" },
  { label: "Khác", value: "OTHER" },
];

const priorityOptions = [
  { label: "Thấp", value: "LOW" },
  { label: "Trung bình", value: "MEDIUM" },
  { label: "Cao", value: "HIGH" },
  { label: "Khẩn cấp", value: "URGENT" },
];

// Methods
const submitServiceRequest = async () => {
  if (!form.value.description.trim()) {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error("Vui lòng nhập mô tả yêu cầu");
    return;
  }

  // Check if tableId is valid
  if (!props.tableId || props.tableId === 'null') {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error("Không thể gửi yêu cầu: thiếu thông tin bàn");
    return;
  }

  submitting.value = true;
  try {
    form.value.tableId = props.tableId;
    form.value.sessionId = props.sessionId || "";
    // Ensure tableName is filled from server before sending
    try {
      const detail: any = await TableApi.getDetail(String(props.tableId));
      const resolvedName = detail?.tableName || detail?.name || "";
      if (resolvedName) form.value.tableName = resolvedName;
    } catch {}
    await serviceRequestApi.create(form.value);

    const toast = useNuxtApp().$toast as typeof toastType;
    toast.success("Yêu cầu phục vụ đã được gửi!");

    // Reset form
    form.value.description = "";
    form.value.notes = "";

    // Refresh service requests
    await fetchServiceRequests();
  } catch (error: any) {
    const toast = useNuxtApp().$toast as typeof toastType;
    toast.error(error.message || "Lỗi khi gửi yêu cầu");
  } finally {
    submitting.value = false;
  }
};

const fetchServiceRequests = async () => {
  try {
    // Check if tableId is valid before making API call
    if (!props.tableId || props.tableId === 'null') {
      console.warn('TableId is null or invalid, skipping service request fetch');
      serviceRequests.value = [];
      pendingCount.value = 0;
      return;
    }

    // Use getByTable instead of getBySession since sessionId might be undefined initially
    const requests = await serviceRequestApi.getByTable(props.tableId);

    // Filter by sessionId if available, otherwise show all for the table
    let filteredRequests = requests;
    if (props.sessionId) {
      filteredRequests = requests.filter(
        (r) => r.sessionId === props.sessionId
      );
    }

    serviceRequests.value = filteredRequests;
    pendingCount.value = filteredRequests.filter(
      (r) => r.status === "PENDING" || r.status === "IN_PROGRESS"
    ).length;
  } catch (error) {
    console.error("Error fetching service requests:", error);
  }
};

const setupRealtimeSubscription = () => {
  const nuxt = useNuxtApp() as any;

  if (nuxt?.$realtime && props.tableId && props.tableId !== 'null') {
    nuxt.$realtime.onTableServiceRequest(props.tableId, (payload: any) => {
      if (
        payload?.type === "SERVICE_REQUEST_CREATED" ||
        payload?.type === "SERVICE_REQUEST_UPDATED"
      ) {
        // De-dup event toasts by event key
        try {
          const seen: Set<string> = (window as any).__seenServiceReqToasts;
          const id = payload?.data?.id ?? payload?.id ?? "unknown";
          const status = payload?.data?.status ?? payload?.status ?? "";
          const key = `${payload.type}:${id}:${status}`;
          if (!seen.has(key)) {
            seen.add(key);
            const toast = useNuxtApp().$toast as typeof toastType;
            if (payload.type === "SERVICE_REQUEST_CREATED") {
              toast.info("Yêu cầu phục vụ mới đã được tạo");
            } else {
              toast.info("Trạng thái yêu cầu đã được cập nhật");
            }
          }
        } catch (error) {
          console.error("Error in toast handling:", error);
        }

        // Debounce fetch to prevent repeated calls
        debouncedFetchServiceRequests();
      }
    });
  }
};

// Helper functions
const getRequestTypeLabel = (type: string) => {
  const option = requestTypeOptions.find((opt) => opt.value === type);
  return option?.label || type;
};

const getRequestTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    GENERAL_SERVICE: "lucide:settings",
    FOOD_REQUEST: "lucide:utensils",
    CLEANING: "lucide:sparkles",
    PAYMENT: "lucide:credit-card",
    OTHER: "lucide:help-circle",
  };
  return icons[type] || "lucide:help-circle";
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

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString("vi-VN");
};

// Lifecycle
onMounted(async () => {
  // Fetch service requests first
  await fetchServiceRequests();

  // Setup realtime subscription
  setupRealtimeSubscription();

  // Sync tableName from server
  try {
    const detail: any = await TableApi.getDetail(String(props.tableId));
    const resolvedName = detail?.tableName || detail?.name || "";
    if (resolvedName) {
      form.value.tableName = resolvedName;
    }
  } catch (error) {
    console.error("Error fetching table detail:", error);
  }

  // Fetch again after a short delay to ensure we have the latest data
  setTimeout(async () => {
    await fetchServiceRequests();
  }, 1000);

  // Set up interval to check for sessionId changes
  setInterval(async () => {
    if (props.sessionId !== previousSessionId.value) {
      previousSessionId.value = props.sessionId;
      await fetchServiceRequests();
    }
  }, 500);
});

// Expose methods
defineExpose({
  fetchServiceRequests,
});
</script>
