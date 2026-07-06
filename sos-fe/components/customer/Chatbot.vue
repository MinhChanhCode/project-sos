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
          <span class="font-semibold">Tư vấn món ăn AI</span>
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

const props = defineProps<{ sessionId?: string; hidden?: boolean }>();

const open = ref(false);
const input = ref("");
const loading = ref(false);
const messages = ref<{ role: "user" | "bot"; text: string }[]>([
  { role: "bot", text: "Xin chào! Tôi có thể gợi ý món, trả lời FAQ nhà hàng, hỗ trợ gọi nhân viên và thanh toán. Bạn muốn gì?" },
]);
const messagesRef = ref<HTMLElement | null>(null);
const chatSessionId = ref(props.sessionId || "");
const quickQuestions = [
  "Combo cho 2 người dưới 200k",
  "Món không cay",
  "Tôi dị ứng hải sản",
  "Gọi nhân viên",
  "Tôi muốn thanh toán",
  "Món của tôi tới đâu rồi?",
  "Nhà hàng mở cửa lúc mấy giờ?",
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
  loading.value = true;
  try {
    const res = await chatApi.send({ sessionId: chatSessionId.value, message: text }) as { reply?: string };
    messages.value.push({ role: "bot", text: res.reply || "Xin lỗi, tôi chưa trả lời được." });
    nextTick(() => messagesRef.value?.scrollTo({ top: 9999, behavior: "smooth" }));
  } catch (e: any) {
    messages.value.push({ role: "bot", text: e.message || "Lỗi kết nối chatbot" });
  } finally {
    loading.value = false;
  }
};
</script>
