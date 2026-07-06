type PublicApiRule = {
  method: string;
  pattern: RegExp;
};

const PUBLIC_API_RULES: PublicApiRule[] = [
  { method: "GET", pattern: /^\/api\/v1\/menu-items(?:\/|\?|$)/ },
  { method: "GET", pattern: /^\/api\/v1\/categories(?:\/|\?|$)/ },
  { method: "GET", pattern: /^\/api\/v1\/tables(?:\/|\?|$)/ },
  { method: "GET", pattern: /^\/api\/v1\/orders(?:\/|\?|$)/ },
  { method: "POST", pattern: /^\/api\/v1\/orders\/session(?:\/|\?|$)/ },
  { method: "ANY", pattern: /^\/api\/v1\/carts(?:\/|\?|$)/ },
  { method: "ANY", pattern: /^\/api\/v1\/service-requests(?:\/|\?|$)/ },
  { method: "ANY", pattern: /^\/api\/v1\/customer-sessions(?:\/|\?|$)/ },
  { method: "ANY", pattern: /^\/api\/v1\/staff-chat(?:\/|\?|$)/ },
  { method: "ANY", pattern: /^\/api\/v1\/chat(?:\/|\?|$)/ },
  { method: "GET", pattern: /^\/api\/v1\/qr-codes\/token(?:\/|\?|$)/ },
  { method: "POST", pattern: /^\/api\/v1\/invoices\/public\/session(?:\/|\?|$)/ },
  { method: "POST", pattern: /^\/api\/v1\/reviews(?:\/|\?|$)/ },
  { method: "GET", pattern: /^\/api\/v1\/images\/view(?:\/|\?|$)/ },
];

export const isPublicApiRequest = (url: string, method = "GET") => {
  const normalizedMethod = String(method || "GET").toUpperCase();
  const normalizedUrl = String(url || "").replace(/^https?:\/\/[^/]+/i, "");
  return PUBLIC_API_RULES.some(
    (rule) =>
      (rule.method === "ANY" || rule.method === normalizedMethod) &&
      rule.pattern.test(normalizedUrl)
  );
};
