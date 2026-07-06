# 9. AI

## Tong quan AI trong ProjectSOS

AI duoc tach thanh service rieng `sos-ai` bang FastAPI. Backend Spring Boot khong goi OpenAI truc tiep ma goi AI service thong qua `AI_SERVICE_URL`.

```text
Frontend Chatbot.vue
  -> Backend /api/v1/chat
      -> ChatService
          -> sos-ai /chat
              -> OpenAI neu co key
              -> Local token RAG fallback
```

## LLM va OpenAI

Trong `sos-ai/main.py`, ham `call_openai`:

- Doc `OPENAI_API_KEY`.
- Neu khong co key: tra `None`.
- Neu co key: khoi tao `OpenAI(api_key=api_key)`.
- Goi Chat Completions:
  - Model mac dinh: `OPENAI_MODEL` hoac `gpt-4o-mini`.
  - System prompt: "Ban la tu van mon an nha hang Viet Nam. Menu..."
  - User prompt: cau hoi cua khach.

Bien moi truong:

```env
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini
```

## Prompt

Prompt hien tai trong OpenAI call:

```text
Ban la tu van mon an nha hang Viet Nam. Menu:
{context}
```

Context hien tai trong endpoint `/chat`:

```python
context = "\n".join(f"- {m['name']}: {m['price']}đ" for m in MENU_ITEMS)
```

Nhan xet:

- Prompt ngan gon, de chay.
- Context OpenAI hien moi gom ten mon va gia, chua dua day du `description`, `tasteTags`, `spicyLevel`, `ingredients`, `allergens`, `pairing`.
- Local RAG lai dung nhieu truong metadata hon OpenAI context.
- Diem can cai thien: dua full metadata vao context LLM de cau tra loi thong minh hon.

## RAG

### Kieu RAG hien tai

Code dung **local token RAG**, khong dung vector database thuc su.

Thanh phan:

- `MENU_ITEMS`: list dict trong bo nho.
- `SEARCH_INDEX`: list token counter.
- `normalize`, `tokenize`, `item_text`.
- `rebuild_search_index`.
- `rag_search`.

### Truong duoc dua vao token search

`item_text` gom:

- name
- description
- categoryName
- type
- tasteTags
- ingredients
- allergens
- suitableFor
- pairing

### Bo loc ngu nghia

`rag_search` co cac bo loc:

- Ngan sach: `extract_budget`.
- Khong cay: `no_spicy`.
- An chay: `wants_vegetarian`.
- Di ung: `allergy_terms`.
- Con ban/active: `available_items`.
- Do cay: `spicyLevel`.
- Do uong/mon chinh/combo: `is_drink`, `is_main`, `is_combo`.

### Cham diem

`item_score` cong diem theo:

- Token query nam trong text mon.
- Query co combo va item la combo.
- Query can do uong va item la drink.
- Query can mon no/mon chinh va item la main.
- Query khong cay va item spicy 0.
- Query cay va item spicy >= 2.
- Query an chay va item vegetarian.

## Embedding va Vector Search

Project co migration `menu_embeddings` va endpoint `/embeddings/rebuild`, nhung code hien tai:

- Khong tao embedding vector so hoc.
- Khong goi embedding API.
- Khong dung vector database.
- `/embeddings/rebuild` thuc chat rebuild token index va tra `engine: local-token-rag`.

Ket luan cho bao cao:

> He thong co thiet ke huong toi RAG/embedding thong qua bang `menu_embeddings`, nhung phien ban hien tai moi hien thuc local token retrieval in-memory. Vector search la diem mo rong tuong lai.

## Tool Calling

Code hien tai **khong su dung OpenAI tool calling**. Chatbot khong goi tool de lay database truc tiep; thay vao do backend sync menu sang AI service bang `/menu/sync`.

## Context

Context cua chatbot den tu:

1. Menu fallback mac dinh trong `MENU_ITEMS`.
2. Menu active duoc backend dong bo qua `/menu/sync`.
3. Cau hoi hien tai cua user.
4. Mot phan logic session id de luu lich su trong backend `chat_histories`.

## Conversation Memory

Backend luu lich su chat:

- Entity: `ChatHistory`.
- Bang: `chat_histories`.
- API: `GET /api/v1/chat/history/{sessionId}`.

Tuy nhien, khi goi `sos-ai /chat`, code hien tai chi gui message hien tai, chua gui lich su hoi thoai gan day vao LLM. Do do memory dang la **persisted history** nhung chua phai **conversational context injection**.

## Chat Flow

```text
User message
  -> Chatbot.vue
  -> chatApi.send({sessionId, message})
  -> POST /api/v1/chat
  -> ChatService.chat()
  -> callExternalAi()
      -> POST sos-ai /chat
      -> OpenAI or local RAG
  -> Neu external AI loi: buildLocalRagReply() backend
  -> Luu ChatHistory
  -> Tra reply cho frontend
```

## Sentiment Analysis

Co 2 lop sentiment:

### AI service

Endpoint:

```text
POST /sentiment
```

Ham `analyze_sentiment` dem keyword:

- Positive: ngon, tuyet, tot, hai long, xuat sac, nhanh, than thien, great, good, excellent.
- Negative: te, do, cham, lau, man, nguoi, kem, bad, slow, terrible, disappoint.

### Backend ReviewService

Khi tao review:

1. Luu review.
2. Goi `sos-ai /sentiment` neu co `AI_SERVICE_URL`.
3. Neu AI loi hoac khong cau hinh, fallback keyword trong backend.
4. Luu `SentimentResult`.

## Diem manh

- Tach AI service rieng nen de deploy/scale rieng.
- Co fallback khi OpenAI loi hoac khong co key.
- Menu metadata duoc thiet ke kha tot cho tu van khau vi.
- Co endpoint sync menu tu backend sang AI.
- Co history chat va sentiment storage.

## Han che va cai thien

- OpenAI context chua dua day du metadata.
- Chua co vector embedding that.
- Chua co conversation context khi goi AI.
- Chua log exception OpenAI trong `call_openai`, kho debug khi key sai.
- CORS AI dang allow all.
- Chua co rate limit cho chatbot.

## De xuat nang cap

- Dua full metadata mon vao OpenAI prompt.
- Gui 5-10 message gan nhat lam conversation memory.
- Dung Responses API/Chat Completions voi JSON structured output neu can.
- Them embedding vector cho menu va vector DB nhe.
- Them moderation/rate limiting.
- Them logging loi OpenAI.
- Cache ket qua sync menu va tinh content hash.

