import { deriveOrderItemStatus } from "./formatters";

type AnyTable = Record<string, any>;

const sum = (items: any[], key: string) =>
  items.reduce((total, item) => total + Number(item?.[key] || 0), 0);

export const getTableItems = (table: AnyTable) => {
  const fromOrders = Array.isArray(table?.orders)
    ? table.orders.flatMap((order: any) => Array.isArray(order?.items) ? order.items : [])
    : [];
  return fromOrders.length ? fromOrders : (Array.isArray(table?.sessionItems) ? table.sessionItems : []);
};

export const getTableOrderSummary = (table: AnyTable) => {
  const items = getTableItems(table);
  const pending = sum(items, "pendingQuantity") || items.filter((i: any) => i.status === "pending" || deriveOrderItemStatus(i) === "PENDING").reduce((n: number, i: any) => n + Number(i.quantity || i.totalQuantity || 0), 0);
  const preparing = sum(items, "preparingQuantity") || items.filter((i: any) => i.status === "preparing" || deriveOrderItemStatus(i) === "PREPARING").reduce((n: number, i: any) => n + Number(i.quantity || i.totalQuantity || 0), 0);
  const ready = sum(items, "completedQuantity") || items.filter((i: any) => i.status === "completed" || i.status === "ready" || deriveOrderItemStatus(i) === "COMPLETED").reduce((n: number, i: any) => n + Number(i.quantity || i.totalQuantity || 0), 0);
  const served = sum(items, "servedQuantity") || items.filter((i: any) => i.status === "served" || deriveOrderItemStatus(i) === "SERVED").reduce((n: number, i: any) => n + Number(i.quantity || i.totalQuantity || 0), 0);
  const total = pending + preparing + ready + served || items.reduce((n: number, i: any) => n + Number(i.quantity || i.totalQuantity || 0), 0);
  const latestTime = items
    .map((item: any) => item.orderTime)
    .filter(Boolean)
    .sort((a: string, b: string) => new Date(b).getTime() - new Date(a).getTime())[0] || "";

  return {
    pending,
    preparing,
    ready,
    served,
    total,
    hasOrder: pending + preparing + ready > 0,
    latestTime,
  };
};

export const getTableDisplayStatus = (table: AnyTable) => {
  const raw = String(table?.tableStatus || table?.status || "").toUpperCase();
  const statusText = String(table?.status || "").toLowerCase();
  const summary = getTableOrderSummary(table);

  if (statusText.includes("thanh toán")) return "Chờ thanh toán";
  if (statusText.includes("dọn")) return "Cần dọn bàn";
  if (statusText.includes("sẵn sàng")) return "Món sẵn sàng";
  if (statusText.includes("chờ bếp")) return "Chờ bếp";
  if (statusText.includes("gọi món")) return "Đang gọi món";
  if (statusText.includes("có khách") || statusText.includes("đang đặt") || statusText.includes("phục vụ")) return "Có khách";

  if (raw === "EMPTY" || table?.isAvailable === true || table?.status === "trống") {
    if (!summary.total) return "Trống";
  }
  if (raw === "WAITING_PAYMENT" || table?.status === "thanh toán") return "Chờ thanh toán";
  if (raw === "NEEDS_CLEANING") return "Cần dọn bàn";
  if (summary.ready > 0) return "Món sẵn sàng";
  if (summary.preparing > 0) return "Chờ bếp";
  if (summary.pending > 0) return "Đang gọi món";
  if (raw === "SERVING" || table?.isAvailable === false) return "Có khách";
  return "Trống";
};

export const getTableDisplayColorClass = (status: string) => {
  const map: Record<string, string> = {
    "Trống": "bg-slate-100 text-slate-700 ring-slate-200",
    "Có khách": "bg-blue-100 text-blue-700 ring-blue-200",
    "Đang gọi món": "bg-amber-100 text-amber-800 ring-amber-200",
    "Chờ bếp": "bg-orange-100 text-orange-800 ring-orange-200",
    "Món sẵn sàng": "bg-emerald-100 text-emerald-800 ring-emerald-200",
    "Đang phục vụ": "bg-cyan-100 text-cyan-800 ring-cyan-200",
    "Chờ thanh toán": "bg-violet-100 text-violet-800 ring-violet-200",
    "Cần dọn bàn": "bg-rose-100 text-rose-800 ring-rose-200",
  };
  return map[status] || map["Trống"];
};
