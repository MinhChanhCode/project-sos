import { baseApiFetch } from "./BaseApiFetch";

const baseUrl = "/api/v1/service-requests";

export type ServiceRequestType =
  | "GENERAL_SERVICE"
  | "FOOD_REQUEST"
  | "CLEANING"
  | "PAYMENT"
  | "OTHER";

export type ServiceRequestStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "DONE"
  | "CANCELLED";

export type ServiceRequestPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export interface ServiceRequest {
  id: number;
  tableId: string;
  tableName: string;
  sessionId: string;
  requestType: ServiceRequestType;
  description: string;
  status: ServiceRequestStatus;
  priority: ServiceRequestPriority;
  requestedAt: string;
  startedAt?: string;
  completedAt?: string;
  assignedTo?: string;
  notes?: string;
}

export interface ServiceRequestCreateRequest {
  tableId: string;
  tableName: string;
  sessionId: string;
  requestType: ServiceRequestType;
  description: string;
  priority?: ServiceRequestPriority;
  notes?: string;
}

export const serviceRequestApi = {
  create: async (payload: ServiceRequestCreateRequest) => {
    const res = await baseApiFetch<any>(`${baseUrl}`, {
      method: "POST",
      body: payload,
    });
    return (res?.data ?? res) as ServiceRequest;
  },

  getById: async (id: number | string) => {
    const res = await baseApiFetch<any>(`${baseUrl}/${id}`);
    return (res?.data ?? res) as ServiceRequest;
  },

  getAll: async (page = 0, size = 20) => {
    const res = await baseApiFetch<any>(`${baseUrl}`, {
      query: { page, size },
    });
    return res;
  },

  getByStatus: async (status: ServiceRequestStatus, page = 0, size = 20) => {
    const res = await baseApiFetch<any>(`${baseUrl}/status/${status}`, {
      query: { page, size },
    });
    return res;
  },

  getByTable: async (tableId: string) => {
    const res = await baseApiFetch<any>(`${baseUrl}/table/${tableId}`);
    return (res?.data ?? res) as ServiceRequest[];
  },

  getBySession: async (sessionId: string) => {
    const res = await baseApiFetch<any>(`${baseUrl}/session/${sessionId}`);
    return (res?.data ?? res) as ServiceRequest[];
  },

  getPending: async () => {
    const res = await baseApiFetch<any>(`${baseUrl}/pending`);
    return (res?.data ?? res) as ServiceRequest[];
  },

  updateStatus: async (
    id: number | string,
    status: ServiceRequestStatus,
    assignedTo?: string
  ) => {
    const res = await baseApiFetch<any>(`${baseUrl}/${id}/status`, {
      method: "PATCH",
      query: { status, ...(assignedTo && { assignedTo }) },
    });
    return (res?.data ?? res) as ServiceRequest;
  },

  assign: async (id: number | string, assignedTo: string) => {
    const res = await baseApiFetch<any>(`${baseUrl}/${id}/assign`, {
      method: "PATCH",
      query: { assignedTo },
    });
    return (res?.data ?? res) as ServiceRequest;
  },

  delete: async (id: number | string) => {
    await baseApiFetch<void>(`${baseUrl}/${id}`, { method: "DELETE" });
  },
};
