<template>
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row md:items-end gap-4">
      <div class="w-full md:w-96">
        <ULabel class="mb-1">Địa chỉ khách quét QR</ULabel>
        <UInput
          v-model="qrBaseUrl"
          placeholder="https://your-public-domain.com"
        />
        <div class="mt-2 rounded-md border px-3 py-2 text-xs" :class="qrModeClass">
          <div class="font-semibold">{{ qrModeTitle }}</div>
          <div class="mt-1">{{ qrModeMessage }}</div>
        </div>
      </div>

      <div class="w-full md:w-64">
        <ULabel class="mb-1">Chọn bàn</ULabel>
        <USelectMenu
          v-model="selectedTableIds"
          :options="tableOptions"
          option-attribute="label"
          value-attribute="value"
          multiple
          searchable
          placeholder="Chọn 1 hoặc nhiều bàn"
        />
      </div>

      <div class="w-full md:w-56">
        <ULabel class="mb-1">Kích thước QR (px)</ULabel>
        <USlider v-model="qrSize" :min="128" :max="1024" :step="16" />
        <div class="text-xs text-gray-500 mt-1">{{ qrSize }} px</div>
      </div>

      <div class="flex gap-2">
        <UButton
          color="primary"
          @click="printSelected"
          :disabled="selectedTableIds.length === 0"
        >
          <Icon name="lucide:printer" class="w-4 h-4 mr-1" /> In QR
        </UButton>
        <UButton
          variant="outline"
          @click="selectAll"
          :disabled="tables.length === 0"
        >
          Chọn tất cả
        </UButton>
        <UButton
          variant="ghost"
          @click="clearSelection"
          :disabled="selectedTableIds.length === 0"
        >
          Bỏ chọn
        </UButton>
      </div>
    </div>

    <div
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
    >
      <div
        v-for="t in selectedTables"
        :key="t.id"
        class="p-4 rounded-lg border bg-white dark:bg-gray-800 print:shadow-none"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="font-semibold">Bàn {{ t.tableNumber }}</div>
          <UBadge variant="soft">{{ t.id.slice(0, 8) }}</UBadge>
        </div>
        <div class="flex justify-center">
          <qrcode-vue
            :value="buildUrl(t.tableNumber)"
            :size="qrSize"
            level="H"
            render-as="svg"
            :margin="2"
          />
        </div>
        <div class="text-center text-xs text-gray-500 mt-2 break-all">
          {{ buildUrl(t.tableNumber) }}
        </div>
      </div>
    </div>

    <!-- Hidden print area -->
    <div ref="printAreaRef" class="hidden print:block">
      <div class="grid grid-cols-2 gap-6">
        <div
          v-for="t in selectedTables"
          :key="'p-' + t.id"
          class="break-inside-avoid"
        >
          <div class="text-center font-semibold mb-2">Bàn {{ t.tableNumber }}</div>
          <div class="flex justify-center">
            <qrcode-vue
              :value="buildUrl(t.tableNumber)"
              :size="qrSize"
              level="H"
              render-as="svg"
              :margin="2"
            />
          </div>
          <div class="text-center text-xs mt-2">{{ buildUrl(t.tableNumber) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import QrcodeVue from "qrcode.vue";
import { useRuntimeConfig } from "nuxt/app";
import { TableApi } from "@/api-service";
import { getStandardTableNumber, normalizeStandardTables } from "~/utils/tableLimits";

interface TableItemOpt {
  id: string;
  name: string;
  tableNumber: number;
}

const tables = ref<TableItemOpt[]>([]);
const selectedTableIds = ref<string[]>([]);
const qrSize = ref<number>(256);
const printAreaRef = ref<HTMLDivElement | null>(null);
const config = useRuntimeConfig();

const currentOrigin = () => {
  if (typeof window === "undefined") return "";
  return `${window.location.protocol}//${window.location.host}`;
};

const isPrivateHost = (origin: string) => {
  try {
    const { hostname } = new URL(origin);
    return (
      hostname === "localhost" ||
      hostname === "127.0.0.1" ||
      hostname.startsWith("192.168.") ||
      hostname.startsWith("10.") ||
      /^172\.(1[6-9]|2\d|3[0-1])\./.test(hostname)
    );
  } catch {
    return false;
  }
};

const defaultQrBaseUrl = () => {
  const origin = currentOrigin();
  if (origin && !isPrivateHost(origin)) return origin;
  return String(config.public.siteUrl || "").trim() || origin;
};

const qrBaseUrl = ref(defaultQrBaseUrl());

const tableOptions = computed(() =>
  tables.value.map((t) => ({ label: `Bàn ${t.tableNumber}`, value: t.id }))
);
const selectedTables = computed(() =>
  tables.value.filter((t) => selectedTableIds.value.includes(t.id))
);

const baseUrl = computed(() => qrBaseUrl.value.trim().replace(/\/+$/, ""));
const isPrivateQrUrl = computed(() => isPrivateHost(baseUrl.value));
const isLocalhostQrUrl = computed(() => {
  try {
    const { hostname } = new URL(baseUrl.value);
    return hostname === "localhost" || hostname === "127.0.0.1";
  } catch {
    return false;
  }
});
const qrModeTitle = computed(() => {
  if (!baseUrl.value) return "Chưa có địa chỉ QR";
  if (isLocalhostQrUrl.value) return "Không dùng localhost cho QR";
  if (isPrivateQrUrl.value) return "Chế độ Wi-Fi/LAN";
  return "Chế độ Public/4G/5G";
});
const qrModeMessage = computed(() => {
  if (!baseUrl.value) return "Nhập địa chỉ mà điện thoại khách có thể mở được.";
  if (isLocalhostQrUrl.value) {
    return "Điện thoại khác không mở được localhost. Hãy dùng IP LAN hoặc link public.";
  }
  if (isPrivateQrUrl.value) {
    return "QR này chỉ dùng cho thiết bị cùng Wi-Fi nhà hàng. Khách dùng 4G/5G cần link public dạng https://...loca.lt hoặc domain thật.";
  }
  return "QR này dùng được cho khách ngoài Wi-Fi nếu tunnel/domain public đang chạy.";
});
const qrModeClass = computed(() => {
  if (!baseUrl.value || isLocalhostQrUrl.value) {
    return "border-red-500/40 bg-red-500/10 text-red-200";
  }
  if (isPrivateQrUrl.value) {
    return "border-amber-500/40 bg-amber-500/10 text-amber-100";
  }
  return "border-green-500/40 bg-green-500/10 text-green-100";
});

const buildUrl = (tableNumber: number) =>
  `${baseUrl.value}/customer/table/${encodeURIComponent(String(tableNumber))}`;

const fetchTables = async () => {
  const list = await TableApi.list();
  const items = Array.isArray((list as any)?.data)
    ? (list as any).data
    : Array.isArray(list)
    ? list
    : [];
  tables.value = normalizeStandardTables(items as any[]).map((t: any) => ({
    id: String(t.id),
    name: String(t.name ?? t.number ?? t.id),
    tableNumber: getStandardTableNumber(t) || Number(String(t.name || "").replace(/\D+/g, "")),
  }));
};

const fetchPublicQrUrl = async () => {
  try {
    const res = await $fetch<{ url?: string; isPublic?: boolean }>(
      "/public-qr-url"
    );
    if (res?.url && res.isPublic) {
      qrBaseUrl.value = res.url.replace(/\/+$/, "");
    }
  } catch {
    // Keep the configured/default QR URL when no public tunnel file exists.
  }
};

const selectAll = () => {
  selectedTableIds.value = tables.value.map((t) => t.id);
};
const clearSelection = () => {
  selectedTableIds.value = [];
};

const printSelected = () => {
  // Ensure print uses the hidden area for clean layout
  window.print();
};

onMounted(() => {
  fetchPublicQrUrl();
  fetchTables();
});
</script>

<style scoped>
@media print {
  /* Hide everything except print area */
  :deep(
      body *:not(.print\:block):not(.print\:inline):not(.print\:inline-block)
    ) {
    /* no-op to avoid global override in scoped */
  }
}
</style>
