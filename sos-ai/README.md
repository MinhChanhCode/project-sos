# SOS AI Service

FastAPI microservice cho Chatbot AI, RAG và phân tích cảm xúc.

## Cài đặt

```bash
cd sos-ai
pip install -r requirements.txt
```

## Chạy

```bash
uvicorn main:app --reload --port 8000
```

## Biến môi trường (tùy chọn)

- `OPENAI_API_KEY` — bật OpenAI khi có key; không có thì thử Gemini hoặc dùng RAG local
- `OPENAI_MODEL` — mặc định `gpt-4o-mini`
- `GEMINI_API_KEY` — bật Gemini khi có key
- `GEMINI_MODEL` — mặc định `gemini-2.5-flash`

## API

| Endpoint | Mô tả |
|----------|--------|
| `POST /chat` | Chatbot tư vấn món |
| `POST /sentiment` | Phân tích cảm xúc |
| `POST /menu/sync` | Đồng bộ menu từ Spring Boot |
| `GET /health` | Health check |

Spring Boot gọi qua `ai.service.url=http://localhost:8000` trong `application.properties`.
