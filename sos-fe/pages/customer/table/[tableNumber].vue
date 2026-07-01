<script setup lang="ts">
import { navigateTo, useRoute } from "nuxt/app";

const route = useRoute();
const tableNumber = String(route.params.tableNumber || "").trim();
const query = {
  ...route.query,
  ...(tableNumber ? { tableNumber } : {}),
};

await navigateTo({
  path: "/customer",
  query,
}, { replace: true });
</script>
