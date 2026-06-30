<template>
  <div>
    <h2 class="text-lg font-semibold mb-4">Thao tác nhanh</h2>
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <UButton 
        variant="outline" 
        class="h-20 flex-col"
        @click="$emit('action', 'kitchen')"
      >
        <Icon name="lucide:chef-hat" class="w-6 h-6 mb-2" />
        <span class="text-sm">Kiểm tra bếp</span>
      </UButton>
      <UButton 
        variant="outline" 
        class="h-20 flex-col"
        @click="$emit('action', 'drinks')"
      >
        <Icon name="lucide:coffee" class="w-6 h-6 mb-2" />
        <span class="text-sm">Đồ uống</span>
      </UButton>
      <UButton 
        variant="outline" 
        class="h-20 flex-col"
        @click="$emit('action', 'clean')"
      >
        <Icon name="lucide:utensils" class="w-6 h-6 mb-2" />
        <span class="text-sm">Dọn bàn</span>
      </UButton>
      <UButton 
        variant="outline" 
        class="h-20 flex-col"
        @click="$emit('action', 'message')"
      >
        <Icon name="lucide:message-circle" class="w-6 h-6 mb-2" />
        <span class="text-sm">Tin nhắn</span>
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  action: ['kitchen' | 'drinks' | 'clean' | 'message']
}>()
</script>