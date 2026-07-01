<template>
  <UModal v-model="isOpen" :ui="{ width: 'max-w-4xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold">Tin nhắn khách hàng</h2>
          <UButton variant="ghost" size="xs" icon="i-lucide-refresh-cw" @click="loadAll" />
        </div>
      </template>

      <div class="grid gap-4 md:grid-cols-[220px_1fr]">
        <div class="space-y-2 max-h-96 overflow-y-auto">
          <button
            v-for="table in tables"
            :key="table.id"
            type="button"
            class="w-full rounded-lg border px-3 py-2 text-left text-sm"
            :class="selectedTableId === table.id ? 'border-blue-500 bg-blue-50 text-blue-700 dark:bg-blue-950' : 'hover:bg-gray-50 dark:hover:bg-gray-800'"
            @click="selectTable(table.id)"
          >
            <div class="font-semibold">{{ table.number }}</div>
            <div class="text-xs text-gray-500">{{ tableMessages(table.id).length }} tin nhắn</div>
          </button>
        </div>

        <div>
          <div class="h-80 overflow-y-auto rounded-lg border p-3 space-y-2">
            <div
              v-for="message in activeMessages"
              :key="message.id"
              :class="message.sender === 'STAFF' ? 'text-right' : 'text-left'"
            >
              <div class="text-[11px] text-gray-400">
                {{ message.customerName || message.tableName || 'Khách' }} · {{ formatTime(message.createdAt) }}
              </div>
              <span
                class="inline-block max-w-[80%] rounded-lg px-3 py-2 text-sm"
                :class="message.sender === 'STAFF' ? 'bg-blue-500 text-white' : 'bg-gray-100 dark:bg-gray-800'"
              >
                {{ message.message }}
              </span>
            </div>
            <p v-if="!activeMessages.length" class="py-12 text-center text-sm text-gray-400">
              Chưa có tin nhắn cho bàn này
            </p>
          </div>
          <form class="mt-3 flex gap-2" @submit.prevent="reply">
            <UInput v-model="input" class="flex-1" placeholder="Trả lời khách..." :disabled="!selectedTableId" />
            <UButton type="submit" :loading="sending" :disabled="!selectedTableId">Gửi</UButton>
          </form>
        </div>
      </div>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useNuxtApp } from "nuxt/app";
import { staffChatApi } from "~/api-service/ExtendedApi";

const props = defineProps<{
  modelValue: boolean
  tables: Array<{ id: string; number: string }>
}>();

const emit = defineEmits<{ "update:modelValue": [value: boolean] }>();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const selectedTableId = ref("");
const messages = ref<any[]>([]);
const input = ref("");
const sending = ref(false);
const nuxt = useNuxtApp() as any;

const tableMessages = (tableId: string) => messages.value.filter((message) => String(message.tableId) === String(tableId));
const activeMessages = computed(() => tableMessages(selectedTableId.value));
const selectedTable = computed(() => props.tables.find((table) => table.id === selectedTableId.value));
const formatTime = (value?: string) => value ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) : "";

const selectTable = async (tableId: string) => {
  selectedTableId.value = tableId;
  try {
    const history = (await staffChatApi.history(tableId)) as any[];
    const otherMessages = messages.value.filter((message) => String(message.tableId) !== String(tableId));
    messages.value = [...otherMessages, ...history];
  } catch (error: any) {
    nuxt.$toast?.error?.(error?.message || "Không thể tải tin nhắn");
  }
};

const loadAll = async () => {
  if (!props.tables.length) return;
  if (!selectedTableId.value) selectedTableId.value = props.tables[0].id;
  await selectTable(selectedTableId.value);
};

const reply = async () => {
  if (!input.value.trim() || !selectedTableId.value || sending.value) return;
  sending.value = true;
  try {
    const saved = await staffChatApi.send({
      tableId: selectedTableId.value,
      sender: "STAFF",
      message: input.value.trim(),
    }) as any;
    if (!messages.value.some((message) => message.id === saved.id)) messages.value.push(saved);
    input.value = "";
  } catch (error: any) {
    nuxt.$toast?.error?.(error?.message || "Không thể gửi tin nhắn");
  } finally {
    sending.value = false;
  }
};

const setupRealtime = () => {
  if (!nuxt?.$realtime) return;
  nuxt.$realtime.subscribe("/topic/staff/chat", (payload: any) => {
    const message = payload?.data;
    if (payload?.type === "STAFF_CHAT_MESSAGE" && message && !messages.value.some((m) => m.id === message.id)) {
      messages.value.push(message);
    }
  });
};

watch(() => props.modelValue, (open) => {
  if (open) loadAll();
});

onMounted(setupRealtime);
</script>
