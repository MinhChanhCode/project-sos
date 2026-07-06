# 12. Deploy

## Kien truc deploy de xuat

```text
Vercel
  -> sos-fe

Railway
  -> sos-api
  -> sos-ai
  -> MySQL
```

Ly do:

- Frontend Vercel phu hop Nuxt/Vue.
- Backend, AI va MySQL nen o cung Railway/Singapore de giam latency.
- Khong nen tach backend Render va MySQL Railway neu khong can thiet.

## Frontend Vercel

Thu muc:

```text
sos-fe
```

Build settings:

```text
Install Command: npm install
Build Command: npm run build
Framework: Nuxt.js
Root Directory: sos-fe
```

`vercel.json`:

```json
{
  "buildCommand": "npm run build",
  "installCommand": "npm install",
  "framework": "nuxtjs"
}
```

Neu can function region:

```json
"regions": ["sin1"]
```

Environment variables:

```env
NUXT_PUBLIC_API_BASE=https://project-sos-api.up.railway.app
NUXT_PUBLIC_WS_BASE=wss://project-sos-api.up.railway.app/ws
NUXT_PUBLIC_SITE_URL=https://project-sos.vercel.app
```

## Backend Railway

Service:

```text
sos-api
```

Root directory:

```text
sos-api
```

Builder:

```text
Dockerfile
```

Environment variables:

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:mysql://MYSQLHOST:MYSQLPORT/MYSQLDATABASE?allowPublicKeyRetrieval=true&useSSL=false
DATABASE_USERNAME=MYSQLUSER
DATABASE_PASSWORD=MYSQLPASSWORD
FRONTEND_ORIGIN=https://project-sos.vercel.app
APP_FRONTEND_URL=https://project-sos.vercel.app
QR_BASE_URL=https://project-sos.vercel.app
AI_SERVICE_URL=https://project-sos-ai.up.railway.app
```

Can luu y:

- Railway thuong dung bien `PORT`; code hien tai trong `application.properties` dang `server.port=8080`. Nen doi sang `server.port=${PORT:8080}` khi deploy Railway production.
- `jwt.signerKey` nen dua ra env var de an toan hon.

## AI Railway

Service:

```text
sos-ai
```

Root directory:

```text
sos-ai
```

Build command:

```text
pip install -r requirements.txt
```

Start command:

```text
uvicorn main:app --host 0.0.0.0 --port $PORT
```

Environment variables:

```env
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini
```

Neu khong co `OPENAI_API_KEY`, service van chay local token RAG fallback.

## MySQL Railway

Service:

```text
MySQL
```

Region nen cung voi backend/AI:

```text
Southeast Asia / Singapore
```

Backend lay:

- host
- port
- database
- username
- password

va ghep JDBC URL.

## Docker backend

`sos-api/Dockerfile`:

1. Build bang JDK 17.
2. Copy Gradle wrapper, source, build jar.
3. Runtime bang JRE 17.
4. Chay:

```text
java -Xms64m -Xmx384m -jar app.jar
```

## CI/CD

Hien tai CI/CD dua tren platform:

- Vercel auto deploy khi push GitHub.
- Railway auto deploy service khi push GitHub neu connected.
- Render config ton tai `render.yaml` nhung kien truc de xuat la khong dung Render.

## Ngat Render

De bo Render khoi luong:

1. Vercel environment variables khong con `onrender.com`.
2. Backend env `AI_SERVICE_URL` khong con `onrender.com`.
3. Render service suspend/delete.
4. GitHub auto-deploy Render tat.
5. File `sos-api/render.yaml` co the giu lam tham khao hoac xoa trong mot commit rieng neu chac chan khong dung Render.

## Health checks

Backend:

```text
GET /health
GET /health/readiness
GET /health/liveness
```

AI:

```text
GET /health
```

Frontend:

```text
https://project-sos.vercel.app
```

## Checklist deploy dung

- Vercel `NUXT_PUBLIC_API_BASE` tro ve Railway API.
- Vercel `NUXT_PUBLIC_WS_BASE` dung `wss://.../ws`.
- Railway API health UP.
- Railway AI health OK.
- Railway MySQL cung region voi API.
- Frontend Network tab khong con `localhost`, `127.0.0.1`, `onrender.com`.

