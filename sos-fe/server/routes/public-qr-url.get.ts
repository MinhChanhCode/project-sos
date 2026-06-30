import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";

const readFirstUrl = (filePath: string) => {
  if (!existsSync(filePath)) return "";
  const content = readFileSync(filePath, "utf8");
  const match = content.match(/https:\/\/[^\s]+/);
  return match?.[0] || "";
};

export default defineEventHandler(() => {
  const root = process.cwd();
  const url =
    readFirstUrl(resolve(root, "public-qr-url.txt")) ||
    readFirstUrl(resolve(root, "public-tunnel-url.txt"));

  return {
    url,
    isPublic: /^https:\/\//.test(url),
  };
});
