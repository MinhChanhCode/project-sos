import localtunnel from "localtunnel";
import { writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const port = Number(process.env.PORT || 3000);
const subdomain = process.env.PUBLIC_TUNNEL_SUBDOMAIN || undefined;
const currentDir = dirname(fileURLToPath(import.meta.url));
const outputFile = resolve(currentDir, "..", "public-tunnel-url.txt");

const tunnel = await localtunnel({ port, subdomain, local_host: "127.0.0.1" });

console.log("");
console.log("Public QR URL:");
console.log(tunnel.url);
console.log("");
await writeFile(outputFile, `${tunnel.url}\n`, "utf8");
console.log(`Saved URL to: ${outputFile}`);
console.log("");
try {
  const password = await fetch("https://loca.lt/mytunnelpassword").then((res) =>
    res.text()
  );
  await writeFile(outputFile, `${tunnel.url}\nPassword: ${password.trim()}\n`, "utf8");
  console.log("If localtunnel asks for a password, use:");
  console.log(password.trim());
  console.log("");
} catch {
  console.log("If localtunnel asks for a password, open https://loca.lt/mytunnelpassword on this laptop.");
  console.log("");
}
console.log("Use this in Admin QR Manager:");
console.log(`${tunnel.url}/customer?tableId=...`);
console.log("");
console.log("Keep this window open while customers are ordering.");

tunnel.on("close", () => {
  console.log("Public tunnel closed.");
});

process.on("SIGINT", () => tunnel.close());
process.on("SIGTERM", () => tunnel.close());
