# ProjectSOS Public Deployment

## Recommended Setup

- Frontend: Vercel, domain `https://sos-fe.vercel.app`
- Backend: Render, Railway, Fly.io, or VPS, domain `https://sos-api.onrender.com`
- Database: MySQL online on Railway/Render/VPS
- QR base URL: public frontend URL, never `localhost`, `127.0.0.1`, or LAN IP

## Frontend Environment

Set these variables on Vercel or Netlify:

```env
VITE_API_BASE_URL=https://sos-api.onrender.com/api
VITE_WS_URL=wss://sos-api.onrender.com/ws
VITE_PUBLIC_APP_URL=https://sos-fe.vercel.app

NUXT_PUBLIC_API_BASE=https://sos-api.onrender.com
NUXT_PUBLIC_WS_BASE=wss://sos-api.onrender.com/ws
NUXT_PUBLIC_SITE_URL=https://sos-fe.vercel.app
```

The app normalizes `VITE_API_BASE_URL` ending in `/api`, so existing calls like `/api/v1/...` still work.

## Backend Environment

Set these variables on Render/Railway/Fly/VPS:

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:mysql://your-mysql-host:3306/sos?allowPublicKeyRetrieval=true&useSSL=true
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password
FRONTEND_ORIGIN=https://sos-fe.vercel.app
APP_FRONTEND_URL=https://sos-fe.vercel.app
QR_BASE_URL=https://sos-fe.vercel.app
AI_SERVICE_URL=https://your-ai-service.example.com
```

If you change domain later, update only `FRONTEND_ORIGIN`, `APP_FRONTEND_URL`, `QR_BASE_URL`, and the frontend `VITE_PUBLIC_APP_URL` / `NUXT_PUBLIC_SITE_URL`.

## QR Format

Admin QR now uses public table-number URLs:

```text
https://sos-fe.vercel.app/customer/table/1
https://sos-fe.vercel.app/customer/table/20
```

The route redirects internally to the Customer page with `tableNumber`, then the app resolves the real table ID from the backend.

## Local vs Production

Local:

```env
SOS_API_TARGET=http://127.0.0.1:8080
NUXT_PUBLIC_API_BASE=
NUXT_PUBLIC_WS_BASE=/ws
NUXT_PUBLIC_SITE_URL=http://127.0.0.1:3000
```

Production:

```env
NUXT_PUBLIC_API_BASE=https://sos-api.onrender.com
NUXT_PUBLIC_WS_BASE=wss://sos-api.onrender.com/ws
NUXT_PUBLIC_SITE_URL=https://sos-fe.vercel.app
```

## Final Test Checklist

1. Open frontend public URL.
2. Login Admin, Staff, Kitchen.
3. Admin verifies exactly 20 tables.
4. Admin opens QR and confirms all URLs use `https://.../customer/table/{number}`.
5. Scan QR using 4G/5G.
6. Customer enters name and places order.
7. Kitchen receives order realtime.
8. Staff receives notification and can chat with Customer.
9. Kitchen updates item status.
10. Customer and Staff see realtime status changes.
11. Customer submits review.
12. Admin sees review with customer name, table number, rating, comment, and sentiment if available.
