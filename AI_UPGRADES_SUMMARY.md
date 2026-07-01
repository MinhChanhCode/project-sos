# ProjectSOS AI Upgrade Summary

## Muc tieu

Goi nang cap nay lam cho AI chatbot va phan review gan chat hon voi du lieu that cua nha hang:

- Menu co them metadata cho AI hieu khau vi, do cay, di ung, mon an kem va thoi gian chuan bi.
- Backend co endpoint dong bo menu sang AI service.
- Chatbot AI goi y mon/combo theo ngan sach, so nguoi, do cay, an chay va di ung.
- Review sentiment uu tien goi AI service, neu AI tat thi fallback ve rule cu.

## Da thay doi

### 0. Security va do sach project

- API menu/category chi public cho `GET`; tao/sua/xoa menu va category yeu cau `ADMIN` hoac `MANAGER`.
- Endpoint dong bo AI `POST /api/v1/ai/menu/sync` yeu cau `ADMIN` hoac `MANAGER`.
- Sua repository `MenuItem` ve dung kieu khoa chinh `Long`.
- Doi health check sang `SOS Restaurant Ordering API`.
- Xoa project Spring cu long trong `sos-api/sos-api` de tranh nham voi backend chinh.
- Don README/backend khoi cac noi dung Student/Class cu.

### 1. Menu AI metadata

Bang `menu_items` co them cac cot:

- `type`: MAIN, DRINK, APPETIZER, DESSERT, COMBO.
- `taste_tags`: vi mon, vi du `ngot`, `beo`, `chua`, `cay`, `thanh nhe`.
- `spicy_level`: muc cay 0-3.
- `ingredients`: nguyen lieu chinh.
- `allergens`: tac nhan di ung nhu hai san, sua, dau phong.
- `suitable_for`: phu hop voi ai/tinh huong nao.
- `pairing`: mon/nuoc uong nen di kem.
- `is_vegetarian`: co phu hop an chay khong.
- `prep_time_minutes`: thoi gian chuan bi du kien.

Migration moi: `sos-api/src/main/resources/db/migration/V1.0.13__menu_ai_metadata.sql`.

### 2. Dong bo menu sang AI

Backend them service `MenuAiSyncService` va controller `AiSyncController`.

Endpoint:

```http
POST /api/v1/ai/menu/sync
```

Yeu cau quyen: `ADMIN` hoac `MANAGER`.

Khi admin tao/sua/xoa mon, doi trang thai mon, hoac cap nhat khuyen mai, backend se tu thu dong bo menu sang `sos-ai`. Neu AI service dang tat, thao tac menu van thanh cong va backend chi ghi warning.

### 3. Chatbot AI menu-aware

File thay doi: `sos-ai/main.py`.

Chatbot gio hieu them:

- Ngan sach: `100k`, `200.000`, `duoi 50000`.
- So nguoi: `2 nguoi`, `4 khach`.
- Khau vi: `khong cay`, `cay`, `ngot`, `beo`, `thanh nhe`.
- An chay: `an chay`, `vegetarian`.
- Di ung/can tranh: `di ung hai san`, `khong an sua`.
- Combo/di nhom: tu tao goi mon chinh + do uong neu khong co combo san.

Endpoint van giu:

```http
POST /chat
POST /menu/sync
POST /embeddings/rebuild
POST /sentiment
```

`/embeddings/rebuild` hien tao chi muc token noi bo va tra ve `indexedTokens`.

### 4. Review sentiment dung AI

File thay doi: `sos-api/src/main/java/com/sqc/sos/service/ReviewService.java`.

Khi khach gui review:

1. Backend goi `sos-ai /sentiment`.
2. Neu AI tra ve ket qua, luu sentiment va confidence vao database.
3. Neu AI service loi/tat, backend dung rule cu de khong lam hong workflow review.

### 5. Admin UI cho metadata AI

Man hinh them/sua mon trong admin co them nhom truong `Du lieu AI tu van mon`:

- Loai mon.
- Do cay.
- Thoi gian chuan bi.
- An chay.
- Tag khau vi.
- Nguyen lieu.
- Di ung/can tranh.
- Phu hop voi.
- Goi y di kem.

Nhung truong nay duoc gui ve backend va tu dong dong bo sang AI service khi menu thay doi.

## Cach su dung

### Chay AI service

```powershell
cd D:\ProjectSOS\sos-ai
.\run-ai.ps1
```

Hoac chay truc tiep neu da co Python environment:

```powershell
uvicorn main:app --reload --port 8000
```

### Chay backend voi AI service

```powershell
cd D:\ProjectSOS\sos-api
$env:AI_SERVICE_URL='http://127.0.0.1:8000'
.\gradlew.bat --no-daemon bootRun
```

Backend doc config:

```properties
ai.service.url=${AI_SERVICE_URL:http://localhost:8000}
```

### Dong bo menu thu cong sang AI

Dang nhap admin/manager, lay token, roi goi:

```http
POST http://localhost:8080/api/v1/ai/menu/sync
Authorization: Bearer <token>
```

Ket qua mau:

```json
{
  "data": {
    "synced": true,
    "count": 36,
    "aiResponse": {
      "synced": 36,
      "indexedTokens": 1200
    }
  }
}
```

### Hoi chatbot

Trong giao dien khach hang, bam `Tu van mon an AI`, hoi cac cau nhu:

- `2 nguoi 200k khong cay thi an gi?`
- `toi di ung hai san, goi mon nao duoc?`
- `goi y combo cho 4 nguoi`
- `mon nao thanh nhe kem nuoc uong?`
- `toi an chay co mon gi?`

### Them metadata khi tao/sua mon

API `POST/PUT /api/v1/menu-items` co the gui them:

```json
{
  "name": "Pho bo tai",
  "description": "Pho bo nuoc dung thanh, khong cay",
  "price": 50000,
  "categoryId": 1,
  "type": "MAIN",
  "tasteTags": "thanh nhe, dam vi",
  "spicyLevel": 0,
  "ingredients": "banh pho, bo tai, hanh, rau thom",
  "allergens": "",
  "suitableFor": "bua sang, bua chinh ca nhan, khach khong an cay",
  "pairing": "nuoc ep cam, tra tac xi muoi",
  "isVegetarian": false,
  "prepTimeMinutes": 10
}
```

## Kiem tra da thuc hien

- `sos-api`: `.\gradlew.bat --no-daemon compileJava` thanh cong.
- `sos-api`: `.\gradlew.bat --no-daemon testClasses` thanh cong.
- `sos-ai`: `python -m py_compile sos-ai/main.py` thanh cong bang Python runtime bundled cua Codex.
- `sos-fe`: `.\node_modules\.bin\nuxt.cmd typecheck` thanh cong.
