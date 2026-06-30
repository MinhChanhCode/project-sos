import { defineStore } from "pinia";

export interface AuthUser {
  userId: string;
  username: string;
  fullName?: string;
  roles: string[];
}

const TOKEN_KEY = "sos_token";
const USER_KEY = "sos_user";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: null as string | null,
    user: null as AuthUser | null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    hasRole: (state) => (role: string) =>
      state.user?.roles?.includes(role) ?? false,
    hasAnyRole: (state) => (roles: string[]) =>
      roles.some((r) => state.user?.roles?.includes(r)),
  },
  actions: {
    loadFromStorage() {
      if (!process.client) return;
      this.token = localStorage.getItem(TOKEN_KEY);
      const raw = localStorage.getItem(USER_KEY);
      if (raw) {
        try {
          this.user = JSON.parse(raw);
        } catch {
          this.user = null;
        }
      }
    },
    setAuth(token: string, user: AuthUser) {
      this.token = token;
      this.user = user;
      if (process.client) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      if (process.client) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
    },
    getAuthHeaders(): Record<string, string> {
      return this.token ? { Authorization: `Bearer ${this.token}` } : {};
    },
  },
});

export const getStoredToken = (): string | null => {
  if (!process.client) return null;
  return localStorage.getItem(TOKEN_KEY);
};
