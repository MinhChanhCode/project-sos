<script setup lang="ts">
const route = useRoute();
const tableNumber = String(route.params.tableNumber || "").trim();

await navigateTo({
  path: "/customer",
  query: tableNumber ? { tableNumber } : {},
}, { replace: true });
</script>
