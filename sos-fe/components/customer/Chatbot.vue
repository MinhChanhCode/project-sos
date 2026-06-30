<template>
  <div class="fixed bottom-40 right-4 z-50">
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

const props = defineProps<{ sessionId?: string }>();

const open = ref(false);
const input = ref("");
const loading = ref(false);
const messages = ref<{ role: "user" | "bot"; text: string }[]>([
  { role: "bot", text: "Xin chào! Tôi có thể gợi ý món theo sở thích, ngân sách hoặc combo. Bạn muốn gì?" },
]);
const messagesRef = ref<HTMLElement | null>(null);
const chatSessionId = ref(props.sessionId || "");

onMounted(() => {
  if (!chatSessionId.value && process.client) {
    chatSessionId.value = crypto.randomUUID();
  }
});

const send = async () => {
  if (!input.value.trim() || loading.value) return;
  const text = input.value.trim();
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
