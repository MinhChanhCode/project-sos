import { baseApiFetch } from "./BaseApiFetch";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId?: string;
  username?: string;
  fullName?: string;
  roles?: string[];
}

export const authApi = {
  login: async (payload: LoginRequest) => {
    const res = await baseApiFetch<any>("/auth/login", {
      method: "POST",
      body: payload,
      skipAuth: true,
    });
    return (res?.data ?? res) as LoginResponse;
  },
  me: async () => {
    const res = await baseApiFetch<any>("/api/v1/users/me");
    return res?.data ?? res;
  },
};
