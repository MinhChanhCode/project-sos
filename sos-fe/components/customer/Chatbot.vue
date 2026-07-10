<template>
  <div v-if="!hidden" class="fixed bottom-56 right-4 z-30">
    <UButton
      v-if="!open"
      icon="i-lucide-message-circle"
      size="lg"
      color="orange"
      class="rounded-full shadow-lg"
      @click="open = true"
    />
    <UCard v-else class="w-80 sm:w-96 shadow-xl">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <span class="font-semibold">Tư vấn món ăn AI</span>
            <p v-if="aiStatus" class="text-[11px] text-gray-400">{{ aiStatus }}</p>
          </div>
          <UButton variant="ghost" size="xs" icon="i-lucide-x" @click="open = false" />
        </div>
      </template>
      <div ref="messagesRef" class="h-64 overflow-y-auto space-y-2 mb-3 text-sm">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          :class="msg.role === 'user' ? 'text-right' : 'text-left'"
        >
          <span
            class="inline-block px-3 py-2 rounded-lg max-w-[90%] whitespace-pre-wrap"
            :class="msg.role === 'user' ? 'bg-orange-500 text-white' : 'bg-gray-100 dark:bg-gray-700'"
          >
            {{ msg.text }}
          </span>
        </div>
        <p v-if="loading" class="text-gray-400 text-xs">Đang suy nghĩ...</p>
      </div>
      <div v-if="lastSuggestedItems.length" class="mb-3 space-y-2">
        <button
          v-for="item in lastSuggestedItems"
          :key="item.id"
          type="button"
          class="flex w-full items-center justify-between rounded-lg border border-orange-100 bg-orange-50 px-3 py-2 text-left text-xs text-gray-900 transition hover:bg-orange-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-orange-900/50 dark:bg-orange-950/30 dark:text-white"
          :disabled="item.isAvailable === false"
          @click="emit('addToCart', normalizeSuggestedItem(item))"
        >
          <span class="min-w-0">
            <span class="block truncate font-semibold">{{ item.name }}</span>
            <span class="text-orange-600 dark:text-orange-300">
              {{ item.isAvailable === false ? 'Hết món' : formatMoney(Number(item.promotionalPrice || item.price || 0)) }}
            </span>
          </span>
          <span class="ml-2 rounded-full bg-orange-500 px-2 py-1 font-semibold text-white">{{ item.isAvailable === false ? 'Hết' : 'Thêm' }}</span>
        </button>
      </div>
      <div class="mb-3 flex gap-2 overflow-x-auto pb-1">
        <UButton
          v-for="question in quickQuestions"
          :key="question"
          size="xs"
          variant="soft"
          color="orange"
          class="shrink-0"
          :disabled="loading"
          @click="sendQuick(question)"
        >
          {{ question }}
        </UButton>
      </div>
      <form class="flex gap-2" @submit.prevent="send">
        <UInput v-model="input" placeholder="Hỏi về món ăn..." class="flex-1" />
        <UButton type="submit" :loading="loading" size="sm">Gửi</UButton>
      </form>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { chatApi } from "~/api-service/ExtendedApi";
import { nextTick, onMounted, ref } from "vue";

const props = defineProps<{
  sessionId?: string;
  tableId?: string;
  tableNumber?: string;
  customerName?: string;
  hidden?: boolean;
}>();
const emit = defineEmits<{
  addToCart: [item: any];
  requestPayment: [];
}>();

const open = ref(false);
const input = ref("");
const loading = ref(false);
const messages = ref<{ role: "user" | "bot"; text: string }[]>([
  { role: "bot", text: "Xin chào! Tôi có thể gợi ý món, trả lời FAQ nhà hàng, hỗ trợ gọi nhân viên và thanh toán. Bạn muốn gì?" },
]);
const messagesRef = ref<HTMLElement | null>(null);
const chatSessionId = ref(props.sessionId || "");
const lastSuggestedItems = ref<any[]>([]);
const aiStatus = ref("");
const quickQuestions = [
  "Món không cay",
  "Món cho trẻ em",
  "Combo 3 người dưới 300k",
  "Món dưới 50k",
  "Tôi dị ứng hải sản, nên ăn gì?",
  "Đồ uống hợp với món chính",
  "Món no bụng",
  "Món ăn nhẹ",
  "Món đang khuyến mãi",
  "Giỏ hàng của tôi có gì?",
  "Món của tôi tới đâu rồi?",
  "Tôi muốn thanh toán",
];

onMounted(() => {
  if (!chatSessionId.value && process.client) {
    chatSessionId.value = crypto.randomUUID();
  }
});

const send = async () => {
  if (!input.value.trim() || loading.value) return;
  const text = input.value.trim();
  await sendMessage(text);
};

const sendQuick = async (text: string) => {
  if (loading.value) return;
  open.value = true;
  await sendMessage(text);
};

const sendMessage = async (text: string) => {
  messages.value.push({ role: "user", text });
  input.value = "";
  lastSuggestedItems.value = [];
  loading.value = true;
  try {
    const res = await chatApi.send({
      sessionId: chatSessionId.value,
      tableId: props.tableId,
      tableNumber: props.tableNumber,
      customerName: props.customerName,
      message: text,
    }) as { reply?: string; intent?: string; suggestedItems?: any[]; actions?: any[]; llmUsed?: boolean; llmProvider?: string; fallbackReason?: string };
    lastSuggestedItems.value = isMenuIntent(res.intent) && Array.isArray(res.suggestedItems) ? res.suggestedItems : [];
    aiStatus.value = res.llmUsed
      ? `Đang dùng ${res.llmProvider === 'gemini' ? 'Gemini' : res.llmProvider === 'openai' ? 'OpenAI' : 'AI'} + dữ liệu menu thật`
      : res.fallbackReason
        ? "Đang dùng dữ liệu nội bộ của nhà hàng"
        : "";
    messages.value.push({ role: "bot", text: res.reply || "Xin lỗi, tôi chưa trả lời được." });
    if (Array.isArray(res.actions) && res.actions.some((action) => action?.type === "REQUEST_PAYMENT")) {
      emit("requestPayment");
    }
    nextTick(() => messagesRef.value?.scrollTo({ top: 9999, behavior: "smooth" }));
  } catch (e: any) {
    messages.value.push({ role: "bot", text: e.message || "Lỗi kết nối chatbot" });
  } finally {
    loading.value = false;
  }
};

const formatMoney = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value || 0);

const isMenuIntent = (intent?: string) =>
  [
    "MENU_RECOMMENDATION",
    "MENU_PRICE",
    "MENU_AVAILABILITY",
    "COMBO",
    "BEST_SELLER",
    "PROMOTION",
    "BUDGET_MENU",
    "CATEGORY_QUERY",
    "KIDS_FRIENDLY",
    "NO_SPICY",
    "LOW_SPICY",
    "VEGETARIAN",
    "ALLERGY_SAFE",
    "DRINK_PAIRING",
  ].includes(String(intent || ""));

const normalizeSuggestedItem = (item: any) => ({
  id: Number(item.id),
  name: String(item.name || ""),
  price: Number(item.promotionalPrice || item.price || 0),
  originalPrice: Number(item.originalPrice || item.price || 0),
  promotionalPrice: item.promotionalPrice,
  imageUrl: String(item.imageUrl || ""),
  categoryId: String(item.categoryId || ""),
  description: String(item.description || ""),
  badges: [],
  rating: Number(item.rating || 0),
  prepTime: item.prepTimeMinutes ? `${item.prepTimeMinutes} phút` : "",
  isAvailable: item.isAvailable !== false,
  orders: Number(item.orders || 0),
  quantity: 1,
});
</script>
