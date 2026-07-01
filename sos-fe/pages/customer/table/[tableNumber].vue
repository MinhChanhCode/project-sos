<script setup lang="ts">
import { navigateTo, useRoute } from "nuxt/app";

const route = useRoute();
const tableNumber = String(route.params.tableNumber || "").trim();

await navigateTo({
  path: "/customer",
  query: tableNumber ? { tableNumber } : {},
}, { replace: true });
</script>
