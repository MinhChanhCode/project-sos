import { baseApiFetch } from "./BaseApiFetch";

const unwrap = <T>(res: any): T => (res?.data ?? res) as T;

export const dashboardApi = {
  get: () => baseApiFetch<any>("/api/v1/dashboard").then(unwrap),
};

export const areaApi = {
  list: () => baseApiFetch<any>("/api/v1/areas").then(unwrap),
  create: (body: any) =>
    baseApiFetch<any>("/api/v1/areas", { method: "POST", body }).then(unwrap),
  update: (id: number, body: any) =>
    baseApiFetch<any>(`/api/v1/areas/${id}`, { method: "PUT", body }).then(unwrap),
  delete: (id: number) =>
    baseApiFetch<void>(`/api/v1/areas/${id}`, { method: "DELETE" }),
};

export const categoryApi = {
  list: () => baseApiFetch<any>("/api/v1/categories").then(unwrap),
  create: (body: any) =>
    baseApiFetch<any>("/api/v1/categories", { method: "POST", body }).then(unwrap),
  update: (id: number, body: any) =>
    baseApiFetch<any>(`/api/v1/categories/${id}`, { method: "PUT", body }).then(unwrap),
  delete: (id: number) =>
    baseApiFetch<void>(`/api/v1/categories/${id}`, { method: "DELETE" }),
};

export const employeeApi = {
  list: () => baseApiFetch<any>("/api/v1/employees").then(unwrap),
  create: (body: any) =>
    baseApiFetch<any>("/api/v1/employees", { method: "POST", body }).then(unwrap),
  update: (id: string, body: any) =>
    baseApiFetch<any>(`/api/v1/employees/${id}`, { method: "PUT", body }).then(unwrap),
  delete: (id: string) =>
    baseApiFetch<void>(`/api/v1/employees/${id}`, { method: "DELETE" }),
  listShifts: () => baseApiFetch<any>("/api/v1/shifts").then(unwrap),
  createShift: (body: any) =>
    baseApiFetch<any>("/api/v1/shifts", { method: "POST", body }).then(unwrap),
  assign: (employeeId: string, areaId: number, shiftId?: number) =>
    baseApiFetch<any>("/api/v1/assignments", {
      method: "POST",
      query: { employeeId, areaId, shiftId },
    }).then(unwrap),
};

export const invoiceApi = {
  list: () => baseApiFetch<any>("/api/v1/invoices").then(unwrap),
  create: (orderId: number) =>
    baseApiFetch<any>(`/api/v1/invoices/order/${orderId}`, { method: "POST" }).then(unwrap),
  confirmPayment: (body: { orderId: number; method: string; amount: number }) =>
    baseApiFetch<any>("/api/v1/invoices/payment/confirm", { method: "POST", body }).then(unwrap),
  getByOrder: (orderId: number) =>
    baseApiFetch<any>(`/api/v1/invoices/order/${orderId}`).then(unwrap),
  createForSession: (sessionId: string) =>
    baseApiFetch<any>(`/api/v1/invoices/public/session/${encodeURIComponent(sessionId)}`, { method: "POST", skipAuth: true }).then(unwrap),
};

export const reviewApi = {
  list: () => baseApiFetch<any>("/api/v1/reviews").then(unwrap),
  create: (body: { tableId?: string; sessionId?: string; customerName?: string; rating: number; comment?: string }) =>
    baseApiFetch<any>("/api/v1/reviews", { method: "POST", body, skipAuth: true }).then(unwrap),
  sentimentSummary: () =>
    baseApiFetch<any>("/api/v1/reviews/sentiment-summary").then(unwrap),
};

export const customerSessionApi = {
  save: (body: { sessionId?: string; tableId: string; customerName: string }) =>
    baseApiFetch<any>("/api/v1/customer-sessions", { method: "POST", body, skipAuth: true }).then(unwrap),
  get: (sessionId: string) =>
    baseApiFetch<any>(`/api/v1/customer-sessions/${sessionId}`, { skipAuth: true }).then(unwrap),
};

export const staffChatApi = {
  send: (body: { tableId: string; sessionId?: string; customerName?: string; sender: "CUSTOMER" | "STAFF"; message: string }) =>
    baseApiFetch<any>("/api/v1/staff-chat", { method: "POST", body, skipAuth: true }).then(unwrap),
  history: (tableId: string) =>
    baseApiFetch<any>(`/api/v1/staff-chat/table/${tableId}`, { skipAuth: true }).then(unwrap),
};

export const chatApi = {
  send: (body: { sessionId?: string; message: string }) =>
    baseApiFetch<any>("/api/v1/chat", { method: "POST", body, skipAuth: true }).then(unwrap),
  history: (sessionId: string) =>
    baseApiFetch<any>(`/api/v1/chat/history/${sessionId}`, { skipAuth: true }).then(unwrap),
};

export const qrCodeApi = {
  generate: (tableId: string) =>
    baseApiFetch<any>(`/api/v1/qr-codes/table/${tableId}/generate`, { method: "POST" }).then(unwrap),
  resolve: (token: string) =>
    baseApiFetch<any>(`/api/v1/qr-codes/token/${encodeURIComponent(token)}`, { skipAuth: true }).then(unwrap),
};
