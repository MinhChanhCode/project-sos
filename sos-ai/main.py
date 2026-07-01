"""
SOS AI Service - Chatbot, RAG, Sentiment Analysis
Run: uvicorn main:app --reload --port 8000
"""
from __future__ import annotations

import os
import re
from collections import Counter
from typing import Optional

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(title="SOS AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory menu store (synced via /menu/sync from Spring Boot or manual)
MENU_ITEMS: list[dict] = [
    {"id": 1, "name": "Trà sữa trân châu", "price": 35000, "description": "Đồ uống ngọt béo, có trân châu dai"},
    {"id": 2, "name": "Cà phê sữa đá", "price": 25000, "description": "Cà phê Việt Nam pha sữa đặc, uống lạnh"},
    {"id": 3, "name": "Sinh tố xoài", "price": 40000, "description": "Sinh tố trái cây mát, vị ngọt tự nhiên"},
    {"id": 4, "name": "Gỏi cuốn tôm thịt", "price": 42000, "description": "Khai vị nhẹ, nhiều rau, không cay"},
    {"id": 5, "name": "Chả giò hải sản", "price": 48000, "description": "Khai vị chiên giòn nhân tôm mực"},
    {"id": 6, "name": "Salad bò lúc lắc", "price": 59000, "description": "Khai vị rau xanh cùng bò mềm"},
    {"id": 7, "name": "Khoai tây chiên phô mai", "price": 35000, "description": "Món ăn nhẹ giòn, hợp đi nhóm"},
    {"id": 8, "name": "Phở bò tái", "price": 50000, "description": "Món chính truyền thống, nước dùng thanh, không cay"},
    {"id": 9, "name": "Bún bò Huế", "price": 60000, "description": "Món chính vị đậm, hơi cay"},
    {"id": 10, "name": "Cơm gà xối mỡ", "price": 55000, "description": "Món chính no bụng, gà giòn"},
    {"id": 11, "name": "Cơm tấm sườn bì chả", "price": 68000, "description": "Món chính kiểu Sài Gòn, sườn nướng thơm"},
    {"id": 12, "name": "Mì xào bò", "price": 62000, "description": "Món chính dễ ăn, bò mềm và rau cải"},
    {"id": 13, "name": "Lẩu Thái hải sản", "price": 189000, "description": "Món chính cho nhóm, vị chua cay"},
    {"id": 14, "name": "Bánh mì chảo", "price": 58000, "description": "Món chính nóng, có bò, trứng, pate"},
    {"id": 15, "name": "Trà tắc xí muội", "price": 28000, "description": "Đồ uống chua ngọt, giải khát"},
    {"id": 16, "name": "Nước ép cam", "price": 35000, "description": "Đồ uống trái cây tươi, giàu vitamin C"},
    {"id": 17, "name": "Matcha latte", "price": 42000, "description": "Đồ uống trà xanh pha sữa béo nhẹ"},
    {"id": 18, "name": "Sinh tố bơ", "price": 45000, "description": "Đồ uống béo mịn từ bơ sáp"},
    {"id": 19, "name": "Combo cơm gà và trà tắc", "price": 78000, "description": "Combo một người, gồm món chính và đồ uống"},
    {"id": 20, "name": "Combo phở bò và nước cam", "price": 82000, "description": "Combo một người, nhẹ bụng và không cay"},
    {"id": 21, "name": "Combo khai vị 3 món", "price": 119000, "description": "Combo nhóm nhỏ gồm gỏi cuốn, chả giò và khoai tây"},
    {"id": 22, "name": "Combo gia đình 4 người", "price": 329000, "description": "Combo nhóm có lẩu, món chính, khai vị và đồ uống"},
]


class ChatRequest(BaseModel):
    session_id: Optional[str] = None
    message: str


class ChatResponse(BaseModel):
    session_id: str
    reply: str


class SentimentRequest(BaseModel):
    text: str


class SentimentResponse(BaseModel):
    sentiment: str
    confidence: float


class MenuSyncRequest(BaseModel):
    items: list[dict]


SEARCH_INDEX: list[dict] = []


def as_text(value: object) -> str:
    return str(value or "").strip()


def normalize(text: object) -> str:
    value = as_text(text).lower()
    return (
        value.replace("khong", "không")
        .replace("cay qua", "cay quá")
        .replace("an chay", "ăn chay")
        .replace("hai san", "hải sản")
    )


def item_text(item: dict) -> str:
    fields = [
        "name",
        "description",
        "categoryName",
        "type",
        "tasteTags",
        "ingredients",
        "allergens",
        "suitableFor",
        "pairing",
    ]
    return normalize(" ".join(as_text(item.get(field)) for field in fields))


def tokenize(text: str) -> list[str]:
    return [token for token in re.split(r"[^0-9a-zA-ZÀ-ỹ]+", normalize(text)) if len(token) > 1]


def rebuild_search_index() -> int:
    global SEARCH_INDEX
    SEARCH_INDEX = [
        {
            "id": item.get("id"),
            "tokens": Counter(tokenize(item_text(item))),
            "text": item_text(item),
        }
        for item in MENU_ITEMS
    ]
    return sum(sum(row["tokens"].values()) for row in SEARCH_INDEX)


def extract_budget(text: str) -> Optional[int]:
    lower = normalize(text)
    for match in re.finditer(r"(\d+)\s*(k|nghìn|ngàn|000)?", lower):
        amount = int(match.group(1))
        unit = match.group(2)
        if unit in {"k", "nghìn", "ngàn"}:
            amount *= 1000
        elif unit == "000":
            amount *= 1000
        if amount >= 10000:
            return amount
    return None


def extract_people(text: str) -> Optional[int]:
    lower = normalize(text)
    match = re.search(r"(\d+)\s*(người|nguoi|khách|khach|ban)", lower)
    if match:
        return int(match.group(1))
    return None


def wants_vegetarian(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["ăn chay", "chay", "vegetarian", "vegan"])


def allergy_terms(text: str) -> list[str]:
    lower = normalize(text)
    terms = []
    for term in ["hải sản", "sữa", "đậu phộng", "trứng", "bò", "gà"]:
        if term in lower and any(prefix in lower for prefix in ["dị ứng", "di ung", "không ăn", "khong an", "tránh", "tranh"]):
            terms.append(term)
    return terms


def item_price(item: dict) -> float:
    promo = item.get("promotionalPrice")
    price = promo if promo not in (None, "", 0) else item.get("price", 0)
    try:
        return float(price)
    except (TypeError, ValueError):
        return 0


def is_drink(item: dict) -> bool:
    text = item_text(item)
    return item.get("type") == "DRINK" or any(term in text for term in ["đồ uống", "trà", "cà phê", "sinh tố", "nước ép", "latte"])


def is_combo(item: dict) -> bool:
    return item.get("type") == "COMBO" or "combo" in normalize(item.get("name"))


def is_main(item: dict) -> bool:
    return item.get("type") == "MAIN" or any(term in item_text(item) for term in ["món chính", "cơm", "phở", "bún", "lẩu", "mì", "bánh mì"])


def available_items(items: list[dict]) -> list[dict]:
    return [
        item for item in items
        if item.get("isActive", True) is not False and item.get("isAvailable", True) is not False
    ]


def item_score(query: str, item: dict) -> int:
    lower = normalize(query)
    text = item_text(item)
    score = 0
    for token in set(tokenize(lower)):
        if token in text:
            score += 2
    if "combo" in lower and is_combo(item):
        score += 8
    if any(term in lower for term in ["nước", "uống", "giải khát"]) and is_drink(item):
        score += 6
    if any(term in lower for term in ["no", "món chính", "ăn chính"]) and is_main(item):
        score += 6
    if "không cay" in lower and int(item.get("spicyLevel") or 0) == 0:
        score += 5
    if "cay" in lower and "không cay" not in lower and int(item.get("spicyLevel") or 0) >= 2:
        score += 4
    if wants_vegetarian(lower) and item.get("isVegetarian") is True:
        score += 8
    return score


def rag_search(query: str, items: list[dict]) -> list[dict]:
    lower = normalize(query)
    budget = extract_budget(lower)
    no_spicy = "không cay" in lower or "khong cay" in lower or "not spicy" in lower
    allergies = allergy_terms(lower)
    results: list[tuple[int, dict]] = []
    for item in available_items(items):
        price = item_price(item)
        text = item_text(item)
        if budget and price > budget:
            continue
        if no_spicy and int(item.get("spicyLevel") or 0) > 0:
            continue
        if wants_vegetarian(lower) and item.get("isVegetarian") is not True:
            continue
        if any(term in text for term in allergies):
            continue
        score = item_score(lower, item)
        if score > 0:
            results.append((score, item))
    if not results:
        results = [(item_score(lower, item), item) for item in available_items(items)]
    return [item for _, item in sorted(results, key=lambda row: (-row[0], item_price(row[1])))[:5]]


def build_combo(message: str, items: list[dict]) -> list[dict]:
    lower = normalize(message)
    budget = extract_budget(lower)
    people = extract_people(lower) or 1
    candidates = rag_search(message, items)
    combos = [item for item in candidates if is_combo(item)]
    if combos:
        return combos[:3]

    mains = [item for item in available_items(items) if is_main(item)]
    drinks = [item for item in available_items(items) if is_drink(item)]
    picked: list[dict] = []
    remaining = budget or 9999999

    for main in sorted(mains, key=item_price):
        if len([x for x in picked if is_main(x)]) >= people:
            break
        if item_score(lower, main) >= 0 and item_price(main) <= remaining:
            picked.append(main)
            remaining -= item_price(main)

    for drink in sorted(drinks, key=item_price):
        if len([x for x in picked if is_drink(x)]) >= people:
            break
        if item_price(drink) <= remaining:
            picked.append(drink)
            remaining -= item_price(drink)

    return picked[: max(3, people * 2)]


def build_rag_reply(message: str) -> str:
    lower = normalize(message)
    wants_combo = any(term in lower for term in ["combo", "set", "đi nhóm", "di nhom", "mấy người", "người"])
    matched = build_combo(message, MENU_ITEMS) if wants_combo else rag_search(message, MENU_ITEMS)
    if not matched:
        return "Xin chào! Hiện chưa có món phù hợp. Bạn có thể hỏi về combo, món dưới 100.000đ, hoặc món không cay."
    lines = ["Dựa trên menu nhà hàng, tôi gợi ý:"]
    for m in matched:
        reasons = []
        if m.get("tasteTags"):
            reasons.append(f"vị {m.get('tasteTags')}")
        if m.get("spicyLevel") is not None:
            reasons.append(f"độ cay {m.get('spicyLevel')}/3")
        if m.get("suitableFor"):
            reasons.append(as_text(m.get("suitableFor")))
        if m.get("pairing"):
            reasons.append(f"hợp với {m.get('pairing')}")
        reason_text = "; ".join(reasons[:3])
        suffix = f" - {reason_text}" if reason_text else ""
        lines.append(f"- {m['name']} ({int(item_price(m)):,}đ): {m.get('description', '')}{suffix}")
    if wants_combo:
        total = sum(item_price(item) for item in matched)
        lines.append(f"Tổng gợi ý khoảng {int(total):,}đ. Bạn có thể thêm ngân sách hoặc số người để tôi tối ưu lại combo.")
    else:
        lines.append("Bạn có thể nói thêm ngân sách, số người, mức cay hoặc món cần tránh để tôi lọc chính xác hơn.")
    return "\n".join(lines)


def analyze_sentiment(text: str) -> SentimentResponse:
    lower = text.lower()
    positive = ["ngon", "tuyệt", "tốt", "hài lòng", "xuất sắc", "nhanh", "thân thiện", "great", "good", "excellent"]
    negative = ["tệ", "dở", "chậm", "lâu", "mặn", "nguội", "kém", "bad", "slow", "terrible", "disappoint"]
    pos = sum(1 for w in positive if w in lower)
    neg = sum(1 for w in negative if w in lower)
    if pos > neg:
        return SentimentResponse(sentiment="POSITIVE", confidence=min(0.95, 0.6 + pos * 0.1))
    if neg > pos:
        return SentimentResponse(sentiment="NEGATIVE", confidence=min(0.95, 0.6 + neg * 0.1))
    return SentimentResponse(sentiment="NEUTRAL", confidence=0.7)


def call_openai(message: str, context: str) -> Optional[str]:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        return None
    try:
        from openai import OpenAI
        client = OpenAI(api_key=api_key)
        resp = client.chat.completions.create(
            model=os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
            messages=[
                {"role": "system", "content": f"Bạn là tư vấn món ăn nhà hàng Việt Nam. Menu:\n{context}"},
                {"role": "user", "content": message},
            ],
            max_tokens=500,
        )
        return resp.choices[0].message.content
    except Exception:
        return None


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest):
    session_id = req.session_id or "default"
    context = "\n".join(f"- {m['name']}: {m['price']}đ" for m in MENU_ITEMS)
    reply = call_openai(req.message, context) or build_rag_reply(req.message)
    return ChatResponse(session_id=session_id, reply=reply)


@app.post("/sentiment", response_model=SentimentResponse)
def sentiment(req: SentimentRequest):
    return analyze_sentiment(req.text)


@app.post("/menu/sync")
def sync_menu(req: MenuSyncRequest):
    global MENU_ITEMS
    MENU_ITEMS = req.items
    token_count = rebuild_search_index()
    return {"synced": len(MENU_ITEMS), "indexedTokens": token_count}


@app.post("/embeddings/rebuild")
def rebuild_embeddings():
    token_count = rebuild_search_index()
    return {"status": "ok", "count": len(MENU_ITEMS), "indexedTokens": token_count, "engine": "local-token-rag"}


rebuild_search_index()
