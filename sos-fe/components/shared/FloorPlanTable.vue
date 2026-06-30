<template>
  <button
    type="button"
    class="floor-table group"
    :class="{ 'floor-table--readonly': readonly }"
    @click="$emit('select')"
  >
    <span class="chair chair-top chair-a" />
    <span class="chair chair-top chair-b" />
    <span class="chair chair-right chair-a" />
    <span class="chair chair-right chair-b" />
    <span class="chair chair-bottom chair-a" />
    <span class="chair chair-bottom chair-b" />
    <span class="chair chair-left chair-a" />
    <span class="chair chair-left chair-b" />
    <span class="table-surface">
      <span class="table-number">{{ number }}</span>
      <span v-if="status && status !== 'EMPTY'" class="table-status">{{ status }}</span>
    </span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  number: number | string
  status?: string
  readonly?: boolean
}>()

defineEmits<{
  select: []
}>()
</script>

<style scoped>
.floor-table {
  position: relative;
  width: 128px;
  height: 128px;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.floor-table--readonly {
  cursor: pointer;
}

.table-surface {
  position: absolute;
  inset: 20px;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(148, 163, 184, 0.95);
  border-radius: 10px;
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 58%, #e5e7eb 100%);
  box-shadow:
    0 12px 24px rgba(2, 6, 23, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  color: #111827;
  transition: transform 150ms ease, box-shadow 150ms ease;
}

.floor-table:hover .table-surface {
  transform: translateY(-1px);
  box-shadow:
    0 16px 30px rgba(2, 6, 23, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.table-number {
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.table-status {
  margin-top: 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #64748b;
}

.chair {
  position: absolute;
  z-index: 1;
  width: 34px;
  height: 20px;
  border: 2px solid rgba(226, 232, 240, 0.95);
  border-radius: 6px;
  background: linear-gradient(145deg, rgba(226, 232, 240, 0.92), rgba(100, 116, 139, 0.55));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.chair-top {
  top: 0;
}

.chair-bottom {
  bottom: 0;
}

.chair-top.chair-a,
.chair-bottom.chair-a {
  left: 32px;
}

.chair-top.chair-b,
.chair-bottom.chair-b {
  right: 32px;
}

.chair-left,
.chair-right {
  width: 20px;
  height: 34px;
}

.chair-left {
  left: 0;
}

.chair-right {
  right: 0;
}

.chair-left.chair-a,
.chair-right.chair-a {
  top: 34px;
}

.chair-left.chair-b,
.chair-right.chair-b {
  bottom: 34px;
}
</style>
