<template>
  <div class="fixed bottom-24 right-3 z-50 sm:right-4">
    <UButton
      v-if="!open"
      icon="i-lucide-message-square"
      size="lg"
      color="blue"
      class="relative rounded-full bg-orange-500 shadow-lg shadow-orange-500/25 hover:bg-orange-600"
      :disabled="!tableId"
      @click="open = true"
    >
      Nhắn nhân viên
      <span
        v-if="unreadStaffReplies > 0"
        class="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1 text-[11px] font-bold text-white ring-2 ring-white"
      >
        {{ unreadStaffReplies }}
      </span>
    </UButton>
    <UCard v-else class="w-[calc(100vw-1.5rem)] max-w-sm shadow-xl sm:w-96">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-semibold">Nhắn nhân viên · {{ tableName }}</span>
          <UButton variant="ghost" size="xs" icon="i-lucide-x" @click="open = false" />
        </div>
      </template>

      <div ref="messagesRef" class="h-64 overflow-y-auto space-y-2 mb-3 text-sm">
        <div
          v-for="message in messages"
          :key="message.id"
          :class="message.sender === 'CUSTOMER' ? 'text-right' : 'text-left'"
        >
          <span
            class="inline-block px-3 py-2 rounded-lg max-w-[90%] whitespace-pre-wrap"
            :class="message.sender === 'CUSTOMER' ? 'bg-blue-500 text-white' : 'bg-gray-100 dark:bg-gray-700'"
          >
            {{ message.message }}
          </span>
          <div class="mt-1 text-[10px] text-gray-400">
            {{ formatTime(message.createdAt) }}
          </div>
        </div>
        <p v-if="messages.length === 0" class="text-center text-xs text-gray-400">
          Gửi tin nhắn để nhân viên hỗ trợ bạn.
        </p>
      </div>

      <form class="flex gap-2" @submit.prevent="send">
        <UInput v-model="input" placeholder="Nhập tin nhắn..." class="flex-1" />
        <UButton type="submit" :loading="sending" size="sm">Gửi</UButton>
      </form>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from "vue";
import { useNuxtApp } from "nuxt/app";
import { staffChatApi } from "~/api-service/ExtendedApi";
import { useNotificationSound } from "~/utils/notificationSound";

const props = defineProps<{
  tableId: string
  tableName: string
  sessionId: string
  customerName: string
}>();

const open = ref(false);
const input = ref("");
const sending = ref(false);
const messages = ref<any[]>([]);
const messagesRef = ref<HTMLElement | null>(null);
const nuxt = useNuxtApp() as any;
const unreadStaffReplies = ref(0);
const { playNotificationSound } = useNotificationSound();

const scrollBottom = () => nextTick(() => messagesRef.value?.scrollTo({ top: 99999, behavior: "smooth" }));
const formatTime = (value?: string) => (value ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) : "");

const loadHistory = async () => {
  if (!props.tableId) return;
  try {
    messages.value = (await staffChatApi.history(props.tableId)) as any[];
    scrollBottom();
  } catch (error: any) {
    nuxt.$toast?.error?.(error?.message || "Không thể tải tin nhắn");
  }
};

const send = async () => {
  if (!input.value.trim() || !props.tableId || sending.value) return;
  sending.value = true;
  try {
    const saved = await staffChatApi.send({
      tableId: props.tableId,
      sessionId: props.sessionId,
      customerName: props.customerName,
      sender: "CUSTOMER",
      message: input.value.trim(),
    }) as any;
    if (!messages.value.some((message) => message.id === saved.id)) {
      messages.value.push(saved);
    }
    input.value = "";
    scrollBottom();
  } catch (error: any) {
    nuxt.$toast?.error?.(error?.message || "Không thể gửi tin nhắn");
  } finally {
    sending.value = false;
  }
};

const setupRealtime = () => {
  if (!props.tableId || !nuxt?.$realtime) return;
  nuxt.$realtime.subscribe(`/topic/tables/${props.tableId}/staff-chat`, (payload: any) => {
    const message = payload?.data;
    if (payload?.type === "STAFF_CHAT_MESSAGE" && message && !messages.value.some((m) => m.id === message.id)) {
      messages.value.push(message);
      if (message.sender === "STAFF" && !open.value) {
        unreadStaffReplies.value += 1;
        playNotificationSound();
      }
      scrollBottom();
    }
  });
};

watch(open, (value) => {
  if (value) unreadStaffReplies.value = 0;
});

watch(() => props.tableId, () => {
  loadHistory();
  setupRealtime();
});

onMounted(() => {
  loadHistory();
  setupRealtime();
});
</script>
