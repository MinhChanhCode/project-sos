# ProjectSOS Technical Report Index

Thu muc nay chua bo tai lieu ky thuat chi tiet de dung lam dau vao cho ChatGPT viet bao cao do an tot nghiep.

## Danh sach file

| File | Noi dung |
|---|---|
| `overview.md` | Tong quan project, muc tieu, kien truc, module, vai tro, luong du lieu |
| `technology.md` | Cong nghe backend, frontend, database, AI, realtime, auth, deploy, Docker, library |
| `architecture.md` | Kien truc source code theo backend/frontend/AI |
| `database.md` | Bang database, truong, khoa chinh, khoa ngoai, migration |
| `api.md` | Danh sach API, method, URL, request/response, role |
| `features.md` | Mo ta chi tiet cac chuc nang theo module |
| `business-flow.md` | Luong nghiep vu tu khach quet QR den thanh toan/danh gia |
| `uml.md` | Ma PlantUML cho use case, class, sequence, activity, state, deployment, component |
| `ai.md` | Phan tich chatbot AI, OpenAI, RAG, embedding, sentiment, chat flow |
| `security.md` | JWT, role, permission, authentication, authorization, CORS, validation |
| `realtime.md` | WebSocket/STOMP, topic, publish/subscribe |
| `deployment.md` | Docker, Railway, Vercel, env vars, CI/CD, cau hinh deploy |
| `statistics.md` | Thong ke source code, API, component, asset |
| `screens.md` | Danh sach man hinh, component, anh menu |
| `testing.md` | Chuc nang da co, chua hoan thien, bug/risk, test case, cai thien |

## Cach dung voi ChatGPT de viet bao cao

Co the dua tung file vao ChatGPT theo chuong:

1. Dung `overview.md`, `technology.md`, `architecture.md` de viet chuong gioi thieu va phan tich he thong.
2. Dung `database.md`, `api.md`, `features.md`, `business-flow.md` de viet chuong thiet ke va xay dung.
3. Dung `uml.md` de chen ma PlantUML vao phan phu luc hoac render thanh hinh khi can.
4. Dung `ai.md`, `security.md`, `realtime.md`, `deployment.md` cho cac muc ky thuat nang cao.
5. Dung `statistics.md`, `screens.md`, `testing.md` cho phan ket qua, kiem thu, danh gia va huong phat trien.

## Luu y khi viet bao cao

1. Neu bao cao can noi "RAG", nen mo ta dung hien trang: he thong co truy hoi menu theo token/context va co endpoint rebuild embeddings, nhung chua phai vector database hoan chinh neu chua bo sung index vector production.
2. Neu bao cao can noi deploy, nen ghi kien truc muc tieu: frontend Vercel, backend Railway, AI Railway, MySQL Railway cung region Singapore.
3. Neu bao cao can noi bao mat, nen ghi JWT role-based authorization va dong thoi neu diem can cai thien: rate limit, JWT secret qua env, gioi han CORS.
4. Neu bao cao can noi hieu nang, nen de cap toi uu anh menu, tranh cold start free tier va dat cung region.
