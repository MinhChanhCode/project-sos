// utils/id.ts
export function generateId(): string {
  // Nếu môi trường hỗ trợ Web Crypto
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }

  // Nếu không hỗ trợ thì fallback sang cách khác
  return Math.random().toString(36).substring(2) + Date.now().toString(36);
}
