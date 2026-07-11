"""
SOS AI Service - Chatbot, RAG, Sentiment Analysis
Run: uvicorn main:app --reload --port 8000
"""
from __future__ import annotations

import os
import re
import json
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter
from pathlib import Path
from typing import Any, Optional

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
    {"id": 1, "name": "Trà sữa trân châu", "price": 35000, "description": "Đồ uống ngọt béo, có trân châu dai", "type": "DRINK", "spicyLevel": 0, "allergens": "sữa", "suitableFor": "trẻ em, ăn nhẹ", "pairing": "món cay, món chiên", "isAvailable": True, "isActive": True},
    {"id": 2, "name": "Cà phê sữa đá", "price": 25000, "description": "Cà phê Việt Nam pha sữa đặc, uống lạnh", "type": "DRINK", "spicyLevel": 0, "allergens": "sữa", "suitableFor": "người lớn", "pairing": "bánh mì chảo", "isAvailable": True, "isActive": True},
    {"id": 3, "name": "Sinh tố xoài", "price": 40000, "description": "Sinh tố trái cây mát, vị ngọt tự nhiên", "type": "DRINK", "spicyLevel": 0, "allergens": "", "suitableFor": "trẻ em, giải khát", "pairing": "món chính không cay", "isAvailable": True, "isActive": True},
    {"id": 4, "name": "Gỏi cuốn tôm thịt", "price": 42000, "description": "Khai vị nhẹ, nhiều rau, không cay", "type": "APPETIZER", "spicyLevel": 0, "allergens": "tôm, hải sản", "suitableFor": "ăn nhẹ, không cay", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 5, "name": "Chả giò hải sản", "price": 48000, "description": "Khai vị chiên giòn nhân tôm mực", "type": "APPETIZER", "spicyLevel": 0, "allergens": "hải sản, tôm, mực", "suitableFor": "ăn nhẹ, đi nhóm", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 6, "name": "Salad bò lúc lắc", "price": 59000, "description": "Khai vị rau xanh cùng bò mềm", "type": "APPETIZER", "spicyLevel": 0, "allergens": "bò", "suitableFor": "ăn nhẹ, nhiều rau", "pairing": "nước ép cam", "isAvailable": True, "isActive": True},
    {"id": 7, "name": "Khoai tây chiên phô mai", "price": 35000, "description": "Món ăn nhẹ giòn, hợp đi nhóm", "type": "APPETIZER", "spicyLevel": 0, "allergens": "sữa", "suitableFor": "trẻ em, ăn nhẹ, đi nhóm", "pairing": "trà tắc, nước ép cam", "isAvailable": True, "isActive": True},
    {"id": 8, "name": "Phở bò tái", "price": 50000, "description": "Món chính truyền thống, nước dùng thanh, không cay", "type": "MAIN", "spicyLevel": 0, "allergens": "bò", "suitableFor": "món no bụng, không cay", "pairing": "nước ép cam", "isAvailable": True, "isActive": True},
    {"id": 9, "name": "Bún bò Huế", "price": 60000, "description": "Món chính vị đậm, hơi cay", "type": "MAIN", "spicyLevel": 2, "allergens": "bò", "suitableFor": "người thích cay", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 10, "name": "Cơm gà xối mỡ", "price": 55000, "description": "Món chính no bụng, gà giòn", "type": "MAIN", "spicyLevel": 0, "allergens": "gà", "suitableFor": "món no bụng, dễ ăn", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 11, "name": "Cơm tấm sườn bì chả", "price": 68000, "description": "Món chính kiểu Sài Gòn, sườn nướng thơm", "type": "MAIN", "spicyLevel": 0, "allergens": "trứng", "suitableFor": "món no bụng", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 12, "name": "Mì xào bò", "price": 62000, "description": "Món chính dễ ăn, bò mềm và rau cải", "type": "MAIN", "spicyLevel": 0, "allergens": "bò", "suitableFor": "món no bụng, dễ ăn", "pairing": "nước ép cam", "isAvailable": True, "isActive": True},
    {"id": 13, "name": "Lẩu Thái hải sản", "price": 189000, "description": "Món chính cho nhóm, vị chua cay", "type": "MAIN", "spicyLevel": 3, "allergens": "hải sản, tôm, mực, cá", "suitableFor": "nhóm bạn, người thích cay", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 14, "name": "Bánh mì chảo", "price": 58000, "description": "Món chính nóng, có bò, trứng, pate", "type": "MAIN", "spicyLevel": 0, "allergens": "bò, trứng", "suitableFor": "món no bụng", "pairing": "cà phê sữa đá", "isAvailable": True, "isActive": True},
    {"id": 15, "name": "Trà tắc xí muội", "price": 28000, "description": "Đồ uống chua ngọt, giải khát", "type": "DRINK", "spicyLevel": 0, "allergens": "", "suitableFor": "giải khát, đi nhóm", "pairing": "món chính, món chiên, món cay", "promotionalPrice": 24000, "isAvailable": True, "isActive": True},
    {"id": 16, "name": "Nước ép cam", "price": 35000, "description": "Đồ uống trái cây tươi, giàu vitamin C", "type": "DRINK", "spicyLevel": 0, "allergens": "", "suitableFor": "trẻ em, giải khát", "pairing": "phở bò, cơm gà, mì xào", "isAvailable": True, "isActive": True},
    {"id": 17, "name": "Matcha latte", "price": 42000, "description": "Đồ uống trà xanh pha sữa béo nhẹ", "type": "DRINK", "spicyLevel": 0, "allergens": "sữa", "suitableFor": "ăn nhẹ, tráng miệng", "pairing": "khoai tây chiên phô mai", "isAvailable": True, "isActive": True},
    {"id": 18, "name": "Sinh tố bơ", "price": 45000, "description": "Đồ uống béo mịn từ bơ sáp", "type": "DRINK", "spicyLevel": 0, "allergens": "sữa", "suitableFor": "trẻ em, tráng miệng", "pairing": "món chính không cay", "isAvailable": True, "isActive": True},
    {"id": 19, "name": "Combo cơm gà và trà tắc", "price": 78000, "description": "Combo một người, gồm món chính và đồ uống", "type": "COMBO", "spicyLevel": 0, "allergens": "gà", "suitableFor": "một người, no bụng", "pairing": "đã gồm đồ uống", "promotionalPrice": 69000, "isAvailable": True, "isActive": True},
    {"id": 20, "name": "Combo phở bò và nước cam", "price": 82000, "description": "Combo một người, nhẹ bụng và không cay", "type": "COMBO", "spicyLevel": 0, "allergens": "bò", "suitableFor": "một người, không cay", "pairing": "đã gồm đồ uống", "promotionalPrice": 76000, "isAvailable": True, "isActive": True},
    {"id": 21, "name": "Combo khai vị 3 món", "price": 119000, "description": "Combo nhóm nhỏ gồm gỏi cuốn, chả giò và khoai tây", "type": "COMBO", "spicyLevel": 0, "allergens": "hải sản, tôm, mực, sữa", "suitableFor": "nhóm nhỏ, ăn nhẹ", "pairing": "trà tắc", "isAvailable": True, "isActive": True},
    {"id": 22, "name": "Combo gia đình 4 người", "price": 329000, "description": "Combo nhóm có lẩu, món chính, khai vị và đồ uống", "type": "COMBO", "spicyLevel": 3, "allergens": "hải sản, tôm, mực", "suitableFor": "gia đình 4 người", "pairing": "đã gồm đồ uống", "isAvailable": True, "isActive": True},
]
MENU_SYNCED = False


class ChatRequest(BaseModel):
    session_id: Optional[str] = None
    table_id: Optional[str] = None
    table_number: Optional[str] = None
    customer_name: Optional[str] = None
    order_id: Optional[int] = None
    message: str
    context: dict[str, Any] = Field(default_factory=dict)


class ChatResponse(BaseModel):
    session_id: str
    reply: str
    intent: str = "FAQ"
    suggestedItems: list[dict] = Field(default_factory=list)
    actions: list[dict] = Field(default_factory=list)
    usedTools: list[str] = Field(default_factory=list)
    memoryUpdated: bool = False
    llmUsed: bool = False
    llmProvider: Optional[str] = None
    fallbackReason: Optional[str] = None


class SentimentRequest(BaseModel):
    text: str


class SentimentResponse(BaseModel):
    sentiment: str
    confidence: float


class MenuSyncRequest(BaseModel):
    items: list[dict]


SEARCH_INDEX: list[dict] = []
KNOWLEDGE_BASE_PATH = Path(__file__).with_name("knowledge_base.json")
CONVERSATION_MEMORY: dict[str, dict[str, Any]] = {}
MAX_MEMORY_MESSAGES = 12
RESTAURANT_PROFILE = {
    "name": "Bếp Mẹ Hương",
    "brand": "Gọi Món",
    "openingHours": "08:00 - 22:00 mỗi ngày",
    "style": "Nhà hàng gia đình hiện đại, khách quét QR tại bàn để gọi món.",
    "payment": "Bill có QR thanh toán demo; nhân viên xác nhận trên hệ thống.",
    "support": "Khách có thể nhắn nhân viên hoặc gọi dịch vụ ngay trên giao diện khách hàng.",
}


def load_knowledge_base() -> list[dict]:
    try:
        with KNOWLEDGE_BASE_PATH.open("r", encoding="utf-8") as f:
            data = json.load(f)
            return data if isinstance(data, list) else []
    except Exception:
        return []


KNOWLEDGE_BASE = load_knowledge_base()


def as_text(value: object) -> str:
    return str(value or "").strip()


def normalize(text: object) -> str:
    value = as_text(text).lower()
    return (
        value.replace("khong", "không")
        .replace("cay qua", "cay quá")
        .replace("an chay", "ăn chay")
        .replace("hai san", "hải sản")
        .replace("do uong", "đồ uống")
        .replace("nuoc", "nước")
        .replace("goi nhan vien", "gọi nhân viên")
        .replace("thanh toan", "thanh toán")
        .replace("hoa don", "hóa đơn")
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


def effective_spicy_level(item: dict) -> int:
    value = item.get("spicyLevel")
    if value not in (None, ""):
        try:
            return int(value)
        except (TypeError, ValueError):
            pass
    text = item_text(item)
    if "không cay" in text or "khong cay" in text:
        return 0
    if any(term in text for term in ["cay nhẹ", "cay nhe", "hơi cay", "hoi cay", "sa tế", "sa te"]):
        return 2
    if any(term in text for term in ["cay", "ớt", "ot", "lẩu thái", "lau thai", "bún bò huế", "bun bo hue"]):
        return 3
    return 0


def effective_allergen_text(item: dict) -> str:
    text = item_text(item)
    detected = []
    for term in ["hải sản", "tôm", "mực", "cua", "cá", "ốc", "nghêu", "sò", "sữa", "đậu phộng", "trứng", "bò", "gà"]:
        if term in text:
            detected.append(term)
    return " ".join(detected)


def effective_type(item: dict) -> str:
    item_type = as_text(item.get("type")).upper()
    if item_type:
        return item_type
    text = item_text(item)
    name = normalize(item.get("name"))
    if "combo" in name or "combo" in text or "set" in text:
        return "COMBO"
    if any(term in text for term in ["đồ uống", "nước", "trà", "cà phê", "sinh tố", "latte", "giải khát"]):
        return "DRINK"
    if any(term in text for term in ["khai vị", "ăn nhẹ", "salad", "gỏi", "khoai", "chả giò"]):
        return "APPETIZER"
    return "MAIN"


def faq_text(item: dict) -> str:
    return normalize(" ".join(as_text(item.get(field)) for field in ["question", "answer", "tags"]))


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


def has_request_menu(context: dict[str, Any]) -> bool:
    menu = context.get("menu")
    return isinstance(menu, list) and any(isinstance(item, dict) for item in menu)


def detect_intent(message: str) -> str:
    lower = normalize(message)
    if any(term in lower for term in ["thanh toán", "bill", "hóa đơn", "qr thanh toán", "chuyển khoản", "tính tiền"]):
        return "PAYMENT_HELP"
    if any(term in lower for term in ["gọi nhân viên", "nhân viên", "phục vụ", "hỗ trợ", "gọi phục vụ"]):
        return "CALL_STAFF"
    if any(term in lower for term in ["giỏ hàng", "xóa món", "thêm món", "đặt món", "order", "đang có gì"]):
        return "CART_HELP"
    if any(term in lower for term in ["tới đâu", "làm xong", "món của tôi", "đơn của tôi", "trạng thái đơn", "chờ lâu", "ra chưa"]):
        return "ORDER_STATUS"
    if any(term in lower for term in ["còn không", "hết chưa", "còn món", "hết món"]):
        return "MENU_AVAILABILITY"
    if any(term in lower for term in ["combo", "set", "nhóm", "gia đình"]) or extract_people(lower):
        return "COMBO"
    if allergy_terms(lower):
        return "ALLERGY_SAFE"
    if wants_kids_friendly(lower):
        return "KIDS_FRIENDLY"
    if wants_no_spicy(lower):
        return "NO_SPICY"
    if wants_low_spicy(lower):
        return "LOW_SPICY"
    if wants_vegetarian(lower):
        return "VEGETARIAN"
    if wants_drink_pairing(lower):
        return "DRINK_PAIRING"
    if any(term in lower for term in ["mở cửa", "giờ", "địa chỉ", "địa điểm", "ở đâu", "vi tri", "vị trí", "wifi", "xuất hóa đơn", "hủy món", "đổi món", "phí dịch vụ", "review", "đánh giá"]):
        return "FAQ"
    if any(term in lower for term in [
        "món", "ăn", "uống", "đồ uống", "nước", "bia", "combo", "set", "cay", "chay",
        "dị ứng", "ngân sách", "người", "gợi ý", "recommend", "trẻ em", "bé",
        "no bụng", "ăn nhẹ", "healthy", "đậm đà", "thanh nhẹ", "chua ngọt", "giòn",
        "món chính", "khai vị", "lẩu", "nướng", "best seller", "bán chạy"
    ]):
        if any(term in lower for term in ["còn", "hết", "còn không", "hết chưa"]):
            return "MENU_AVAILABILITY"
        if any(term in lower for term in ["bán chạy", "best seller", "ngon nhất", "món ngon"]):
            return "BEST_SELLER"
        if any(term in lower for term in ["khuyến mãi", "giảm giá", "promotion"]):
            return "PROMOTION"
        if any(term in lower for term in ["combo", "set", "nhóm", "gia đình"]) or extract_people(lower):
            return "COMBO"
        if any(term in lower for term in ["duoi", "dưới"]) or extract_budget(lower):
            return "BUDGET_MENU"
        if any(term in lower for term in ["giá", "bao nhiêu", "mấy tiền"]):
            return "MENU_PRICE"
        if wants_kids_friendly(lower):
            return "KIDS_FRIENDLY"
        if wants_no_spicy(lower):
            return "NO_SPICY"
        if wants_low_spicy(lower):
            return "LOW_SPICY"
        if wants_vegetarian(lower):
            return "VEGETARIAN"
        if allergy_terms(lower):
            return "ALLERGY_SAFE"
        if wants_drink_pairing(lower):
            return "DRINK_PAIRING"
        if query_category_kind(lower):
            return "CATEGORY_QUERY"
        return "MENU_RECOMMENDATION"
    if any(term in lower for term in ["duoi", "dưới"]) or extract_budget(lower):
        return "BUDGET_MENU"
    if any(term in lower for term in ["giá", "bao nhiêu tiền", "mấy tiền", "bao nhiêu"]):
        return "MENU_PRICE"
    if any(term in lower for term in ["bitcoin", "chứng khoán", "code", "lập trình", "chính trị", "bóng đá", "thời tiết"]):
        return "OUT_OF_SCOPE"
    return "FAQ"


def get_context_menu(context: dict[str, Any]) -> list[dict]:
    menu = context.get("menu")
    if isinstance(menu, list) and menu:
        return [item for item in menu if isinstance(item, dict)]
    return MENU_ITEMS if MENU_SYNCED else []


def get_memory(session_id: str) -> dict[str, Any]:
    memory = CONVERSATION_MEMORY.setdefault(session_id, {
        "messages": [],
        "preferences": {},
        "lastSuggestedItems": [],
        "lastIntent": None,
    })
    return memory


def update_memory(session_id: str, req: ChatRequest, intent: str, reply: str, suggested_items: list[dict]) -> bool:
    memory = get_memory(session_id)
    messages = memory.setdefault("messages", [])
    messages.append({"role": "user", "content": req.message})
    messages.append({"role": "assistant", "content": reply})
    memory["messages"] = messages[-MAX_MEMORY_MESSAGES:]
    memory["lastIntent"] = intent
    if suggested_items:
        memory["lastSuggestedItems"] = suggested_items[:5]
    preferences = memory.setdefault("preferences", {})
    allergies = allergy_terms(req.message)
    if allergies:
        preferences["allergies"] = sorted(set(preferences.get("allergies", []) + allergies))
    budget = extract_budget(req.message)
    if budget:
        preferences["budget"] = budget
    people = extract_people(req.message)
    if people:
        preferences["people"] = people
    lower = normalize(req.message)
    if "không cay" in lower:
        preferences["spicy"] = "none"
    elif "cay" in lower:
        preferences["spicy"] = "spicy"
    return True


def memory_context(session_id: str) -> str:
    memory = get_memory(session_id)
    prefs = memory.get("preferences") or {}
    last_items = memory.get("lastSuggestedItems") or []
    lines = []
    if prefs:
        lines.append(f"Sở thích đã nhớ: {json.dumps(prefs, ensure_ascii=False)}")
    if last_items:
        lines.append("Món vừa gợi ý: " + ", ".join(as_text(item.get("name")) for item in last_items[:5]))
    recent = memory.get("messages") or []
    if recent:
        lines.append("Hội thoại gần đây:")
        for msg in recent[-6:]:
            lines.append(f"- {msg.get('role')}: {msg.get('content')}")
    return "\n".join(lines)


def tool_get_table_context(req: ChatRequest) -> tuple[str, dict]:
    table = req.context.get("table") or {}
    table_number = req.table_number or req.context.get("tableNumber") or table.get("tableName") or table.get("name")
    return "get_table_context", {
        "tableId": req.table_id or req.context.get("tableId") or table.get("tableId"),
        "tableNumber": table_number,
        "customerName": req.customer_name or req.context.get("customerName"),
        "status": table.get("status"),
        "activeOrderId": table.get("activeOrderId"),
        "sessionItems": table.get("sessionItems") or [],
    }


def tool_get_cart(req: ChatRequest) -> tuple[str, dict]:
    return "get_cart", req.context.get("cart") or {"items": [], "totalItems": 0, "totalAmount": 0}


def tool_get_order_status(req: ChatRequest) -> tuple[str, dict]:
    table = req.context.get("table") or {}
    orders = req.context.get("orders") or []
    items = table.get("sessionItems") or []
    return "get_order_status", {
        "orders": orders,
        "items": items,
        "activeOrderId": table.get("activeOrderId"),
        "tableStatus": table.get("status"),
    }


def tool_search_faq(message: str) -> tuple[str, dict]:
    faq = match_faq(message)
    return "search_faq", faq or {}


def current_menu_items_by_id(items: list[dict]) -> dict[str, dict]:
    return {
        str(item.get("id")): item
        for item in active_items(items)
        if item.get("id") not in (None, "")
    }


def remap_memory_items_to_current_menu(last_items: list[dict], items: list[dict]) -> list[dict]:
    by_id = current_menu_items_by_id(items)
    current_items = []
    for item in last_items:
        current = by_id.get(str(item.get("id")))
        if current and current.get("isAvailable", True) is not False:
            current_items.append(current)
    return current_items


def tool_search_menu(message: str, items: list[dict], intent: str, session_id: str) -> tuple[str, list[dict]]:
    memory = get_memory(session_id)
    last_items = remap_memory_items_to_current_menu(memory.get("lastSuggestedItems") or [], items)
    lower = normalize(message)
    if any(term in lower for term in ["món đó", "loại đó", "cái đó", "rẻ hơn", "ít cay hơn"]) and last_items:
        if "rẻ hơn" in lower:
            previous_prices = [item_price(item) for item in last_items if item_price(item) > 0]
            if not previous_prices:
                return "get_menu_items", rag_search(message, items)
            limit_price = min(previous_prices)
            matched = [item for item in available_items(items) if item_price(item) and item_price(item) < limit_price]
            return "get_menu_items", sorted(matched, key=item_price)[:5]
        return "get_menu_items", last_items[:5]
    if intent in {"COMBO"}:
        return "get_combo_items", build_combo(message, items)
    if intent in {"BEST_SELLER"}:
        ranked = sorted(available_items(items), key=lambda item: (item.get("orders") or item.get("rating") or 0), reverse=True)
        return "get_best_sellers", ranked[:5] or rag_search(message, items)
    if intent in {"PROMOTION"}:
        promos = [item for item in available_items(items) if item.get("promotionalPrice")]
        return "get_promotions", promos[:5]
    if intent == "MENU_AVAILABILITY":
        return "get_menu_availability", rag_search(message, items, include_unavailable=True)
    return "get_menu_items", rag_search(message, items)


def build_tool_plan(req: ChatRequest, intent: str) -> tuple[list[str], dict[str, Any]]:
    items = get_context_menu(req.context)
    used_tools: list[str] = []
    tool_data: dict[str, Any] = {}

    if intent in {"ORDER_STATUS"}:
        name, data = tool_get_order_status(req)
        used_tools.append(name)
        tool_data[name] = data
    elif intent in {"CART_HELP"}:
        name, data = tool_get_cart(req)
        used_tools.append(name)
        tool_data[name] = data
    elif intent in {"PAYMENT_HELP", "CALL_STAFF", "SERVICE_REQUEST"}:
        for tool in [tool_get_table_context, tool_get_cart]:
            name, data = tool(req)
            used_tools.append(name)
            tool_data[name] = data
        name, data = tool_search_faq(req.message)
        used_tools.append(name)
        tool_data[name] = data
    elif intent in {"FAQ"}:
        name, data = tool_search_faq(req.message)
        used_tools.append(name)
        tool_data[name] = data
    elif intent == "OUT_OF_SCOPE":
        pass
    else:
        name, data = tool_search_menu(req.message, items, intent, req.session_id or "default")
        used_tools.append(name)
        tool_data[name] = data
        table_name, table_data = tool_get_table_context(req)
        used_tools.append(table_name)
        tool_data[table_name] = table_data
    return used_tools, tool_data


def format_money(value: object) -> str:
    price = item_price({"price": value})
    return f"{int(price):,}đ" if price else "chưa cập nhật giá"


def build_menu_context(items: list[dict], limit: int = 80) -> str:
    lines = []
    for item in active_items(items)[:limit]:
        fields = [
            f"Tên: {item.get('name')}",
            f"Giá: {format_money(item.get('promotionalPrice') or item.get('price'))}",
            f"Danh mục: {item.get('categoryName') or item.get('type') or 'khác'}",
            f"Trạng thái: {'còn món' if item.get('isAvailable', True) is not False else 'hết món'}",
        ]
        optional_fields = [
            ("Mô tả", item.get("description")),
            ("Khẩu vị", item.get("tasteTags")),
            ("Độ cay", effective_spicy_level(item)),
            ("Nguyên liệu", item.get("ingredients")),
            ("Dị ứng", item.get("allergens")),
            ("Phù hợp", item.get("suitableFor")),
            ("Gợi ý kèm", item.get("pairing")),
            ("Thời gian chuẩn bị", f"{item.get('prepTimeMinutes')} phút" if item.get("prepTimeMinutes") else None),
        ]
        fields.extend(f"{label}: {value}" for label, value in optional_fields if value not in (None, ""))
        lines.append(" | ".join(fields))
    return "\n".join(f"- {line}" for line in lines)


def build_menu_catalog(items: list[dict], limit: int = 150) -> str:
    grouped: dict[str, list[dict]] = {}
    for item in active_items(items)[:limit]:
        category = as_text(item.get("categoryName") or item.get("type") or "Khác") or "Khác"
        grouped.setdefault(category, []).append(item)

    lines = []
    for category, category_items in grouped.items():
        lines.append(f"[{category}]")
        for item in category_items[:30]:
            status = "còn" if item.get("isAvailable", True) is not False else "hết"
            tags = []
            tags.append(f"cay {effective_spicy_level(item)}/3")
            if item.get("isVegetarian") is True:
                tags.append("chay")
            if item.get("tasteTags"):
                tags.append(as_text(item.get("tasteTags")))
            if item.get("suitableFor"):
                tags.append(as_text(item.get("suitableFor")))
            tag_text = f" | {'; '.join(tags[:4])}" if tags else ""
            lines.append(
                f"- {item.get('name')} - {format_money(item.get('promotionalPrice') or item.get('price'))} - {status}{tag_text}"
            )
    return "\n".join(lines)


def build_faq_context() -> str:
    return "\n".join(
        f"- Hỏi: {item.get('question')}\n  Đáp: {item.get('answer')}"
        for item in KNOWLEDGE_BASE
    )


def match_faq(message: str) -> Optional[dict]:
    tokens = set(tokenize(message))
    best_score = 0
    best_item = None
    for item in KNOWLEDGE_BASE:
        score = sum(1 for token in tokens if token in faq_text(item))
        if score > best_score:
            best_score = score
            best_item = item
    return best_item if best_score >= 2 else None


def fallback_faq_reply(message: str) -> str:
    lower = normalize(message)
    if any(term in lower for term in ["xin chào", "hello", "hi", "chào bạn"]):
        return "Xin chào! Mình có thể tư vấn món ăn, kiểm tra giỏ hàng, xem trạng thái món, gọi nhân viên, hỗ trợ thanh toán hoặc trả lời thông tin nhà hàng."
    if any(term in lower for term in ["mở cửa", "giờ", "mấy giờ", "thời gian", "open", "đóng cửa"]):
        return f"Nhà hàng mở cửa từ {RESTAURANT_PROFILE['openingHours']}."
    if any(term in lower for term in ["địa chỉ", "địa điểm", "ở đâu", "vị trí", "vi tri", "address"]):
        return "Nhà hàng ở 30 Trần Quang Diệu, Quy Nhơn, Bình Định."
    if "wifi" in lower:
        return "Bạn vui lòng nhắn nhân viên trên màn hình hoặc hỏi trực tiếp nhân viên để được cung cấp wifi của nhà hàng."
    if any(term in lower for term in ["xuất hóa đơn", "invoice", "hóa đơn đỏ", "vat"]):
        return "Bạn có thể yêu cầu nhân viên hỗ trợ xuất hóa đơn. Bill trên hệ thống có danh sách món, số lượng, đơn giá và tổng tiền."
    if any(term in lower for term in ["hủy món", "đổi món", "cancel"]):
        return "Nếu món chưa được bếp xử lý, hãy nhắn nhân viên ngay để được hỗ trợ hủy hoặc đổi món. Khi món đã chế biến, nhà hàng có thể không hỗ trợ hủy."
    if any(term in lower for term in ["phí dịch vụ", "giảm giá", "khuyến mãi", "service fee"]):
        return "Nếu có phí dịch vụ, giảm giá hoặc khuyến mãi, bill sẽ hiển thị rõ trước khi thanh toán. Bạn cũng có thể nhắn nhân viên để xác nhận thêm."
    if any(term in lower for term in ["đánh giá", "review", "phản hồi", "góp ý"]):
        return "Sau khi dùng bữa, bạn có thể gửi đánh giá trên màn hình khách hàng để nhà hàng cải thiện chất lượng phục vụ."
    return "Mình có thể trả lời thông tin nhà hàng như giờ mở cửa, địa chỉ, wifi, hóa đơn, thanh toán, gọi nhân viên hoặc trạng thái món. Bạn muốn hỏi phần nào?"


def is_generic_menu_reply(reply: Optional[str]) -> bool:
    if not reply:
        return False
    lower = normalize(reply)
    generic_phrases = [
        "dựa trên menu",
        "dua tren menu",
        "dựa trên dữ liệu menu",
        "dua tren du lieu menu",
        "tôi gợi ý",
        "toi goi y",
        "gợi ý các món",
        "goi y cac mon",
        "gợi ý món",
        "goi y mon",
        "món phù hợp từ menu",
        "mon phu hop tu menu",
    ]
    return any(phrase in lower for phrase in generic_phrases)


def should_keep_deterministic_reply(intent: str, deterministic_reply: Optional[str], llm_reply: Optional[str]) -> bool:
    if not deterministic_reply:
        return False
    strict_menu_intents = {
        "BUDGET_MENU", "ALLERGY_SAFE", "NO_SPICY", "LOW_SPICY", "KIDS_FRIENDLY",
        "VEGETARIAN", "MENU_AVAILABILITY", "CATEGORY_QUERY", "DRINK_PAIRING",
        "COMBO", "PROMOTION", "BEST_SELLER", "MENU_RECOMMENDATION"
    }
    if intent in strict_menu_intents:
        return True
    protected_intents = {"FAQ", "PAYMENT_HELP", "CALL_STAFF", "CART_HELP", "ORDER_STATUS", "OUT_OF_SCOPE"}
    if intent in protected_intents:
        return True
    return is_generic_menu_reply(llm_reply)


def is_menu_intent(intent: str) -> bool:
    return intent in {
        "MENU_RECOMMENDATION", "MENU_PRICE", "MENU_AVAILABILITY", "COMBO", "BEST_SELLER",
        "PROMOTION", "BUDGET_MENU", "CATEGORY_QUERY", "KIDS_FRIENDLY", "NO_SPICY",
        "LOW_SPICY", "VEGETARIAN", "ALLERGY_SAFE", "DRINK_PAIRING"
    }


def llm_reply_uses_menu_data(reply: Optional[str], suggested_items: list[dict]) -> bool:
    if not reply:
        return False
    if not suggested_items:
        return True
    lower = normalize(reply)
    return any(normalize(item.get("name")) in lower for item in suggested_items if item.get("name"))


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
    match = re.search(r"(\d+)\s*(người|nguoi|khách|khach|bạn|ban)", lower)
    if match:
        return int(match.group(1))
    return None


def wants_vegetarian(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["ăn chay", "chay", "vegetarian", "vegan"])


def wants_no_spicy(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["không cay", "khong cay", "không ăn cay", "khong an cay", "not spicy"])


def wants_low_spicy(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["ít cay", "it cay", "cay nhẹ", "cay nhe"])


def wants_kids_friendly(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["trẻ em", "tre em", "bé", "con nít", "em bé", "kids", "kid"])


def wants_drink_pairing(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["đồ uống", "uống gì", "nước nào", "thức uống"]) and any(
        term in lower for term in ["hợp", "kèm", "với", "ăn cùng", "pairing"]
    )


def wants_food_only(text: str) -> bool:
    lower = normalize(text)
    has_food_word = any(term in lower for term in ["món", "ăn gì", "ăn nhẹ", "no bụng", "món chính", "khai vị"])
    has_drink_word = any(term in lower for term in ["đồ uống", "nước", "uống", "trà", "cà phê", "sinh tố", "latte"])
    return has_food_word and not has_drink_word


def query_category_kind(text: str) -> Optional[str]:
    lower = normalize(text)
    category_terms = [
        ("DRINK", ["đồ uống", "nước giải khát", "bia", "cà phê", "trà", "sinh tố", "nước ép"]),
        ("COMBO", ["combo", "set"]),
        ("MAIN", ["món chính", "ăn chính", "cơm", "phở", "bún", "mì"]),
        ("APPETIZER", ["khai vị", "ăn nhẹ", "món nhẹ"]),
        ("GRILL", ["đồ nướng", "món nướng", "nướng"]),
        ("HOTPOT", ["lẩu"]),
    ]
    for kind, terms in category_terms:
        if any(term in lower for term in terms):
            return kind
    return None


def wants_light_food(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["thanh nhẹ", "nhẹ bụng", "ăn nhẹ", "ít dầu", "ít dầu mỡ", "healthy", "không ngấy"])


def wants_filling_food(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["no", "no bụng", "rất đói", "ăn no", "chắc bụng"])


def wants_rich_flavor(text: str) -> bool:
    lower = normalize(text)
    return any(term in lower for term in ["đậm đà", "chua ngọt", "béo", "giòn", "nóng hổi", "trời mưa", "dễ ăn"])


def allergy_terms(text: str) -> list[str]:
    lower = normalize(text)
    terms = []
    avoidance_prefixes = [
        "dị ứng", "di ung", "không ăn", "khong an", "tránh", "tranh",
        "không có", "khong co", "không chứa", "khong chua", "không dùng", "khong dung",
        "không muốn", "khong muon", "loại bỏ", "loai bo"
    ]
    for term in ["hải sản", "sữa", "đậu phộng", "trứng", "bò", "gà", "tôm", "mực", "cua", "cá", "ốc"]:
        if term in lower and any(prefix in lower for prefix in avoidance_prefixes):
            terms.append(term)
    return terms


def contains_restricted_term(item: dict, terms: list[str]) -> bool:
    text = item_text(item) + " " + effective_allergen_text(item)
    if "hải sản" in terms and any(term in text for term in ["hải sản", "tôm", "mực", "cua", "cá", "ốc", "nghêu", "sò"]):
        return True
    return any(term in text for term in terms)


def item_reason_from_description(item: dict, query: str, intent: str) -> str:
    lower = normalize(query)
    text = item_text(item)
    reasons = []
    price = item_price(item)
    budget = extract_budget(lower)
    allergies = allergy_terms(lower)
    if budget and price <= budget:
        reasons.append(f"dưới {format_money(budget)}")
    if wants_no_spicy(lower) and effective_spicy_level(item) == 0:
        reasons.append("mô tả/độ cay phù hợp không cay")
    elif wants_low_spicy(lower) and effective_spicy_level(item) <= 1:
        reasons.append("ít cay")
    if wants_kids_friendly(lower):
        if effective_spicy_level(item) == 0:
            reasons.append("không cay, hợp trẻ em hơn")
        if any(term in text for term in ["dễ ăn", "thanh nhẹ", "trái cây", "sữa", "nước ép", "sinh tố"]):
            reasons.append("mô tả dễ ăn/thanh nhẹ")
    if wants_light_food(lower) and any(term in text for term in ["thanh nhẹ", "nhẹ", "healthy", "rau", "salad", "gỏi", "ít dầu"]):
        reasons.append("mô tả nhẹ bụng")
    if wants_filling_food(lower) and (is_main(item) or is_combo(item)):
        reasons.append("phù hợp ăn no")
    if allergies and not contains_restricted_term(item, allergies):
        reasons.append("không thấy thành phần cần tránh trong mô tả/nguyên liệu")
    if wants_drink_pairing(lower) and is_drink(item):
        pairing = as_text(item.get("pairing"))
        reasons.append(f"hợp dùng kèm {pairing}" if pairing else "đồ uống dùng kèm phù hợp")
    if item.get("tasteTags"):
        reasons.append(as_text(item.get("tasteTags")))
    if item.get("suitableFor"):
        reasons.append(as_text(item.get("suitableFor")))
    return "; ".join(reasons[:3])


def item_price(item: dict) -> float:
    promo = item.get("promotionalPrice")
    price = promo if promo not in (None, "", 0) else item.get("price", 0)
    try:
        return float(price)
    except (TypeError, ValueError):
        return 0


def active_items(items: list[dict]) -> list[dict]:
    return [item for item in items if item.get("isActive", True) is not False]


def is_drink(item: dict) -> bool:
    text = item_text(item)
    item_type = effective_type(item)
    if item_type == "COMBO":
        return False
    return item_type == "DRINK" or any(term in text for term in ["đồ uống", "trà", "cà phê", "sinh tố", "nước ép", "latte"])


def is_combo(item: dict) -> bool:
    return effective_type(item) == "COMBO" or "combo" in normalize(item.get("name"))


def is_main(item: dict) -> bool:
    return effective_type(item) == "MAIN" or any(term in item_text(item) for term in ["món chính", "cơm", "phở", "bún", "lẩu", "mì", "bánh mì"])


def matches_category_kind(item: dict, kind: Optional[str]) -> bool:
    if not kind:
        return True
    text = item_text(item)
    item_type = effective_type(item)
    if kind == "DRINK":
        return is_drink(item)
    if kind == "COMBO":
        return is_combo(item)
    if kind == "MAIN":
        return is_main(item)
    if kind == "APPETIZER":
        return not is_drink(item) and (item_type in {"APPETIZER", "STARTER"} or any(term in text for term in ["khai vị", "ăn nhẹ", "salad", "gỏi", "khoai", "chả giò"]))
    if kind == "GRILL":
        return "GRILL" in item_type or any(term in text for term in ["nướng", "đồ nướng"])
    if kind == "HOTPOT":
        return "HOTPOT" in item_type or "lẩu" in text
    return True


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
    if any(term in lower for term in ["combo", "set", "nhóm", "gia đình", "nhiều người"]) and is_combo(item):
        score += 8
    if any(term in lower for term in ["nước", "uống", "giải khát"]) and is_drink(item):
        score += 6
    if any(term in lower for term in ["no", "món chính", "ăn chính"]) and is_main(item):
        score += 6
    if wants_no_spicy(lower) and effective_spicy_level(item) == 0:
        score += 8
    elif wants_low_spicy(lower) and effective_spicy_level(item) <= 1:
        score += 5
    if "cay" in lower and "không cay" not in lower and effective_spicy_level(item) >= 2:
        score += 4
    if wants_vegetarian(lower) and item.get("isVegetarian") is True:
        score += 8
    if wants_kids_friendly(lower):
        spicy_level = effective_spicy_level(item)
        if spicy_level == 0:
            score += 8
        if any(term in text for term in ["trẻ em", "bé", "không cay", "thanh nhẹ", "dễ ăn", "sữa", "nước ép", "sinh tố", "cơm", "gà"]):
            score += 6
    category = query_category_kind(lower)
    if category and matches_category_kind(item, category):
        score += 9
    if wants_drink_pairing(lower) and is_drink(item):
        score += 12
        pairing = as_text(item.get("pairing")).lower()
        if any(term in lower for term in tokenize(pairing)):
            score += 4
    if wants_light_food(lower) and any(term in text for term in ["thanh nhẹ", "nhẹ", "healthy", "rau", "salad", "gỏi", "nước ép", "trà", "không ngấy", "ít dầu"]):
        score += 7
    if wants_filling_food(lower) and (is_main(item) or is_combo(item) or any(term in text for term in ["no", "cơm", "phở", "bún", "mì", "lẩu", "combo"])):
        score += 8
    if wants_rich_flavor(lower):
        for term in ["đậm đà", "chua ngọt", "béo", "giòn", "nóng", "dễ ăn"]:
            if term in lower and term in text:
                score += 6
    return score


def rag_search(query: str, items: list[dict], include_unavailable: bool = False) -> list[dict]:
    lower = normalize(query)
    budget = extract_budget(lower)
    no_spicy = wants_no_spicy(lower)
    low_spicy = wants_low_spicy(lower) or wants_kids_friendly(lower)
    allergies = allergy_terms(lower)
    category = query_category_kind(lower)
    drink_pairing = wants_drink_pairing(lower)
    food_only = wants_food_only(lower) and not drink_pairing
    results: list[tuple[int, dict]] = []
    candidates = active_items(items) if include_unavailable else available_items(items)
    for item in candidates:
        price = item_price(item)
        text = item_text(item)
        if budget and price > budget:
            continue
        if category and not matches_category_kind(item, category):
            continue
        if food_only and is_drink(item):
            continue
        if drink_pairing and not is_drink(item):
            continue
        if no_spicy and effective_spicy_level(item) > 0:
            continue
        if low_spicy and effective_spicy_level(item) > 1:
            continue
        if wants_vegetarian(lower) and item.get("isVegetarian") is not True:
            continue
        if contains_restricted_term(item, allergies):
            continue
        score = item_score(lower, item)
        if score > 0:
            results.append((score, item))
    if not results:
        fallback_candidates = [
            item for item in candidates
            if (not category or matches_category_kind(item, category))
            and (not food_only or not is_drink(item))
            and (not drink_pairing or is_drink(item))
            and (not budget or item_price(item) <= budget)
            and (not no_spicy or effective_spicy_level(item) <= 0)
            and (not low_spicy or effective_spicy_level(item) <= 1)
            and (not wants_vegetarian(lower) or item.get("isVegetarian") is True)
            and not contains_restricted_term(item, allergies)
        ]
        if not fallback_candidates and (budget or allergies or no_spicy or low_spicy or wants_vegetarian(lower)):
            return []
        fallback_candidates = fallback_candidates or candidates
        results = [(item_score(lower, item), item) for item in fallback_candidates]
    return [item for _, item in sorted(results, key=lambda row: (-row[0], item_price(row[1])))[:8]]


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

    target_mains = max(1, people)
    target_drinks = max(1, people)

    for main in sorted(mains, key=lambda item: (-item_score(lower, item), item_price(item))):
        if len([x for x in picked if is_main(x)]) >= people:
            break
        if item_score(lower, main) >= 0 and item_price(main) <= remaining:
            picked.append(main)
            remaining -= item_price(main)

    for drink in sorted(drinks, key=lambda item: (-item_score(lower, item), item_price(item))):
        if len([x for x in picked if is_drink(x)]) >= people:
            break
        if item_price(drink) <= remaining:
            picked.append(drink)
            remaining -= item_price(drink)

    if len([x for x in picked if is_main(x)]) < target_mains:
        for main in sorted(mains, key=item_price):
            if main not in picked and item_price(main) <= remaining:
                picked.append(main)
                remaining -= item_price(main)
            if len([x for x in picked if is_main(x)]) >= target_mains:
                break
    if len([x for x in picked if is_drink(x)]) < target_drinks:
        for drink in sorted(drinks, key=item_price):
            if drink not in picked and item_price(drink) <= remaining:
                picked.append(drink)
                remaining -= item_price(drink)
            if len([x for x in picked if is_drink(x)]) >= target_drinks:
                break

    return picked[: max(4, people * 2)]


def build_rag_reply(message: str) -> str:
    lower = normalize(message)
    intent = detect_intent(message)
    if intent == "OUT_OF_SCOPE":
        return "Tôi là trợ lý AI của nhà hàng ProjectSOS. Tôi có thể giúp bạn chọn món, xem hướng dẫn đặt món, gọi nhân viên hoặc hỗ trợ thanh toán."
    if intent in {"FAQ", "PAYMENT_HELP", "CALL_STAFF", "CART_HELP", "ORDER_STATUS"}:
        faq = match_faq(message)
        if faq:
            return str(faq.get("answer"))
        if intent == "ORDER_STATUS":
            return "Bạn có thể xem trạng thái món ngay trên màn hình đặt món. Nếu món chờ lâu, hãy bấm Nhắn nhân viên hoặc Gọi dịch vụ để nhân viên kiểm tra giúp bạn."
        if intent == "CALL_STAFF":
            return "Bạn hãy bấm nút Nhắn nhân viên hoặc Gọi dịch vụ ở góc dưới màn hình. Nhân viên sẽ nhận thông báo theo bàn của bạn."
        if intent == "PAYMENT_HELP":
            return "Bạn có thể bấm Yêu cầu thanh toán để xem bill và QR thanh toán demo. Nhân viên sẽ xác nhận sau khi bạn thanh toán."
        if intent == "FAQ":
            return fallback_faq_reply(message)
        if intent == "CART_HELP":
            return "Bạn có thể chọn món trong menu rồi bấm thêm vào giỏ. Khi cần kiểm tra giỏ hàng, hãy bấm biểu tượng giỏ hàng ở cuối màn hình."

    wants_combo = any(term in lower for term in ["combo", "set", "đi nhóm", "di nhom", "mấy người", "người"])
    matched = build_combo(message, MENU_ITEMS) if wants_combo else rag_search(message, MENU_ITEMS)
    if not matched:
        return "Xin chào! Hiện chưa có món phù hợp. Bạn có thể hỏi về combo, món dưới 100.000đ, hoặc món không cay."
    lines = ["Dựa trên menu nhà hàng, tôi gợi ý:"]
    for m in matched:
        reasons = []
        if m.get("tasteTags"):
            reasons.append(f"vị {m.get('tasteTags')}")
        reasons.append(f"độ cay {effective_spicy_level(m)}/3")
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


def has_llm_config() -> bool:
    return bool(os.getenv("OPENAI_API_KEY") or os.getenv("GEMINI_API_KEY"))


def build_llm_prompt(message: str, context: str, catalog: str, intent: str, tool_data: dict[str, Any], session_id: str) -> str:
    restaurant = json.dumps(RESTAURANT_PROFILE, ensure_ascii=False)
    return f"""
Bạn là nhân viên tư vấn AI của nhà hàng Bếp Mẹ Hương trong hệ thống ProjectSOS/Gọi Món.
Vai trò của bạn giống một nhân viên phục vụ giỏi: hiểu menu, hỏi đúng nhu cầu, gợi ý món phù hợp và hướng dẫn khách thao tác trên màn hình.

NGUYÊN TẮC BẮT BUỘC:
- Luôn trả lời bằng tiếng Việt tự nhiên, thân thiện, ngắn gọn, có cảm giác đang nói với khách tại bàn.
- Chỉ dùng dữ liệu có trong CONTEXT, TOOL DATA, FAQ và bộ nhớ hội thoại. Không bịa tên món, giá, trạng thái còn/hết, hóa đơn hoặc trạng thái đơn.
- Chỉ gợi ý món còn hoạt động. Nếu món hết, được nói là hết món và gợi ý món thay thế đang còn.
- Nếu khách hỏi về dị ứng, luôn nhắc khách xác nhận lại với nhân viên trước khi dùng món.
- Nếu thiếu thông tin quan trọng như số người, ngân sách, mức cay hoặc món cần tránh, hãy hỏi lại đúng 1 câu ngắn.
- Nếu câu hỏi ngoài phạm vi nhà hàng/menu/order/cart/thanh toán/dịch vụ, hãy lịch sự giới hạn phạm vi và gợi ý hỏi về món hoặc gọi nhân viên.
- Nếu TOOL DATA có draftReply, hãy viết lại tự nhiên hơn nhưng phải giữ nguyên dữ liệu thật: tên món, giá, số lượng, trạng thái.
- Chủ động hiểu các kiểu hỏi: món không cay, ít cay, trẻ em, gia đình, combo 2/3/4 người, nhóm bạn, dưới ngân sách, món no bụng, món ăn nhẹ, món healthy, món đậm đà, món theo danh mục, đồ uống kèm, món khuyến mãi, món hết/còn, dị ứng/kiêng món.
- Khi tư vấn đồ uống kèm, ưu tiên đồ uống còn món và giải thích hợp với món chính/lẩu/nướng/cơm ở điểm nào.
- Khi tư vấn ngân sách hoặc combo, nêu tổng tiền dự kiến nếu gợi ý nhiều món.

Intent hiện tại: {intent}

THÔNG TIN NHÀ HÀNG:
{restaurant}

CONTEXT RAG / MENU LIÊN QUAN:
{context}

CATALOG MENU ĐẦY ĐỦ HƠN THEO DANH MỤC:
{catalog}

TOOL DATA THẬT:
{json.dumps(tool_data, ensure_ascii=False, default=str)[:12000]}

BỘ NHỚ HỘI THOẠI:
{memory_context(session_id)}

FAQ NHÀ HÀNG:
{build_faq_context()}

Yêu cầu trả lời:
- Nếu có gợi ý món: nêu 2-4 món tốt nhất, kèm giá và lý do ngắn.
- Nếu có thao tác: nói rõ khách bấm nút nào trên giao diện.
- Không kết thúc bằng câu chung chung như "tôi có thể giúp gì thêm" nếu không cần.
""".strip()


def call_openai(message: str, system_prompt: str) -> tuple[Optional[str], Optional[str]]:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        return None, "openai_key_missing"
    try:
        from openai import OpenAI
        client = OpenAI(api_key=api_key)
        resp = client.chat.completions.create(
            model=os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message},
            ],
            max_tokens=500,
            temperature=0.35,
        )
        return resp.choices[0].message.content, None
    except Exception as exc:
        return None, f"openai_error:{type(exc).__name__}"


def call_gemini(message: str, system_prompt: str) -> tuple[Optional[str], Optional[str]]:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return None, "gemini_key_missing"
    model = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
    url = (
        "https://generativelanguage.googleapis.com/v1beta/models/"
        + urllib.parse.quote(model, safe="-_.")
        + ":generateContent?key="
        + urllib.parse.quote(api_key, safe="")
    )
    payload = {
        "contents": [
            {
                "role": "user",
                "parts": [
                    {
                        "text": f"{system_prompt}\n\nCâu hỏi của khách: {message}"
                    }
                ],
            }
        ],
        "generationConfig": {
            "temperature": 0.45,
            "maxOutputTokens": 700,
        },
    }
    try:
        data = json.dumps(payload).encode("utf-8")
        request = urllib.request.Request(
            url,
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(request, timeout=15) as response:
            parsed = json.loads(response.read().decode("utf-8"))
        candidates = parsed.get("candidates") or []
        if not candidates:
            return None, "gemini_empty_candidates"
        parts = (((candidates[0] or {}).get("content") or {}).get("parts") or [])
        text = "\n".join(str(part.get("text", "")) for part in parts if part.get("text"))
        return (text.strip() or None), None
    except urllib.error.HTTPError as exc:
        return None, f"gemini_http_error:{exc.code}"
    except (urllib.error.URLError, TimeoutError) as exc:
        return None, f"gemini_network_error:{type(exc).__name__}"
    except Exception as exc:
        return None, f"gemini_error:{type(exc).__name__}"


def call_llm(message: str, context: str, catalog: str, intent: str, tool_data: dict[str, Any], session_id: str) -> tuple[Optional[str], Optional[str], Optional[str]]:
    system_prompt = build_llm_prompt(message, context, catalog, intent, tool_data, session_id)
    errors: list[str] = []
    if os.getenv("OPENAI_API_KEY"):
        text, error = call_openai(message, system_prompt)
        if text:
            return text, "openai", None
        if error:
            errors.append(error)
    if os.getenv("GEMINI_API_KEY"):
        text, error = call_gemini(message, system_prompt)
        if text:
            return text, "gemini", None
        if error:
            errors.append(error)
    return None, None, ";".join(errors) if errors else "no_llm_key_configured"


def summarize_order_status(data: dict) -> str:
    items = data.get("items") or []
    orders = data.get("orders") or []
    if not items and not orders:
        return "Hiện tại mình chưa thấy đơn đang xử lý cho bàn này. Nếu bạn vừa đặt món, vui lòng chờ vài giây hoặc nhắn nhân viên kiểm tra giúp."
    pending = preparing = ready = served = 0
    lines = []
    for item in items:
        name = item.get("menuItemName") or item.get("name") or "Món"
        p = int(item.get("pendingQuantity") or 0)
        pr = int(item.get("preparingQuantity") or 0)
        r = int(item.get("completedQuantity") or 0)
        s = int(item.get("servedQuantity") or 0)
        pending += p
        preparing += pr
        ready += r
        served += s
        status_parts = []
        if p:
            status_parts.append(f"{p} chờ bếp")
        if pr:
            status_parts.append(f"{pr} đang chế biến")
        if r:
            status_parts.append(f"{r} sẵn sàng")
        if s:
            status_parts.append(f"{s} đã phục vụ")
        if status_parts:
            lines.append(f"- {name}: {', '.join(status_parts)}")
    summary = f"Đơn của bạn hiện có {pending} món chờ bếp, {preparing} món đang chế biến, {ready} món sẵn sàng và {served} món đã phục vụ."
    return "\n".join([summary, *lines[:6]])


def summarize_cart(data: dict) -> str:
    items = data.get("items") or []
    if not items:
        return "Giỏ hàng của bạn hiện đang trống. Bạn có thể chọn món trong menu rồi bấm thêm vào giỏ."
    lines = ["Giỏ hàng hiện tại của bạn:"]
    for item in items[:8]:
        lines.append(f"- {item.get('name')}: x{item.get('quantity')} - {format_money(item.get('subtotal') or item.get('unitPrice'))}")
    lines.append(f"Tổng cộng: {format_money(data.get('totalAmount'))}")
    return "\n".join(lines)


def build_actions(intent: str, suggested_items: list[dict]) -> list[dict]:
    actions = []
    for item in suggested_items[:3]:
        if item.get("id") and item.get("isAvailable", True) is not False:
            actions.append({
                "type": "ADD_TO_CART",
                "label": f"Thêm {item.get('name')} vào giỏ",
                "menuItemId": item.get("id"),
            })
    if intent == "PAYMENT_HELP":
        actions.append({"type": "REQUEST_PAYMENT", "label": "Yêu cầu thanh toán"})
    if intent in {"CALL_STAFF", "SERVICE_REQUEST"}:
        actions.append({"type": "CALL_STAFF", "label": "Nhắn nhân viên"})
    return actions


def build_tool_reply(req: ChatRequest, intent: str, tool_data: dict[str, Any], suggested_items: list[dict]) -> Optional[str]:
    if intent == "OUT_OF_SCOPE":
        return "Tôi là trợ lý AI của nhà hàng ProjectSOS. Tôi có thể giúp bạn chọn món, xem giỏ hàng, kiểm tra đơn, gọi nhân viên hoặc hỗ trợ thanh toán."
    if intent == "ORDER_STATUS":
        return summarize_order_status(tool_data.get("get_order_status") or {})
    if intent == "CART_HELP" and any(term in normalize(req.message) for term in ["giỏ", "cart", "có gì"]):
        return summarize_cart(tool_data.get("get_cart") or {})
    if intent == "FAQ":
        faq = tool_data.get("search_faq") or {}
        if faq.get("answer"):
            return str(faq.get("answer"))
        return fallback_faq_reply(req.message)
    if intent == "CALL_STAFF":
        return "Bạn có thể bấm nút Nhắn nhân viên hoặc Gọi dịch vụ trên màn hình. Mình cũng khuyên bạn nhắn nhân viên để được hỗ trợ trực tiếp tại bàn."
    if intent == "PAYMENT_HELP":
        return "Bạn hãy bấm Yêu cầu thanh toán để xem bill và QR thanh toán. Nhân viên sẽ xác nhận sau khi bạn thanh toán."
    menu_filter_intents = {
        "MENU_AVAILABILITY", "MENU_PRICE", "BUDGET_MENU", "CATEGORY_QUERY", "MENU_RECOMMENDATION",
        "COMBO", "BEST_SELLER", "PROMOTION", "KIDS_FRIENDLY", "NO_SPICY", "LOW_SPICY",
        "VEGETARIAN", "ALLERGY_SAFE", "DRINK_PAIRING"
    }
    if intent in menu_filter_intents and not suggested_items:
        if allergy_terms(req.message):
            return "Mình chưa thấy món nào trong thực đơn hiện tại đáp ứng điều kiện tránh dị ứng đó dựa trên mô tả/nguyên liệu. Bạn nên nhắn nhân viên để xác nhận an toàn trước khi gọi món."
        budget = extract_budget(req.message)
        if budget:
            return f"Mình chưa thấy món nào phù hợp dưới {format_money(budget)} trong thực đơn hiện tại. Bạn có thể tăng ngân sách một chút hoặc hỏi nhân viên món thay thế."
        return "Mình chưa thấy món nào phù hợp với điều kiện đó trong thực đơn hiện tại. Bạn có thể đổi tiêu chí như ngân sách, mức cay hoặc danh mục món."
    if intent in {"MENU_AVAILABILITY", "MENU_PRICE", "BUDGET_MENU", "CATEGORY_QUERY"} and suggested_items:
        lines = ["Mình tìm thấy các món phù hợp trong menu hiện tại:"]
        for item in suggested_items[:5]:
            status = "còn món" if item.get("isAvailable", True) is not False else "hết món"
            reason = item_reason_from_description(item, req.message, intent)
            reason_text = f" - {reason}" if reason else ""
            lines.append(f"- {item.get('name')} - {format_money(item.get('promotionalPrice') or item.get('price'))} ({status}){reason_text}")
        return "\n".join(lines)
    if intent in {
        "MENU_RECOMMENDATION", "COMBO", "BEST_SELLER", "PROMOTION", "KIDS_FRIENDLY",
        "NO_SPICY", "LOW_SPICY", "VEGETARIAN", "ALLERGY_SAFE", "DRINK_PAIRING"
    } and suggested_items:
        lower = normalize(req.message)
        intro = "Mình gợi ý các món phù hợp từ menu hiện tại:"
        if intent == "COMBO" or extract_people(lower):
            people = extract_people(lower)
            intro = f"Mình gợi ý combo cho {people} người:" if people else "Mình gợi ý combo/nhóm món phù hợp:"
        elif wants_kids_friendly(lower):
            intro = "Mình gợi ý các món dễ ăn, ít/không cay cho trẻ em:"
        elif intent == "NO_SPICY" or wants_no_spicy(lower):
            intro = "Mình gợi ý các món không cay trong menu hiện tại:"
        elif intent == "LOW_SPICY" or wants_low_spicy(lower):
            intro = "Mình gợi ý các món ít cay hoặc không cay:"
        elif intent == "VEGETARIAN":
            intro = "Mình gợi ý các món chay/phù hợp ăn chay đang có:"
        elif intent == "ALLERGY_SAFE":
            intro = "Mình lọc các món phù hợp hơn với thông tin dị ứng/kiêng món của bạn:"
        elif intent == "DRINK_PAIRING":
            intro = "Mình gợi ý đồ uống hợp để dùng kèm:"
        elif intent == "BEST_SELLER":
            intro = "Mình gợi ý các món nổi bật/bán chạy trong menu:"
        elif intent == "PROMOTION":
            intro = "Mình gợi ý các món đang có giá ưu đãi:"
        lines = [intro]
        total = 0
        for item in suggested_items[:6]:
            price = item_price(item)
            total += price
            reason = item_reason_from_description(item, req.message, intent)
            if not reason:
                reason = f"độ cay {effective_spicy_level(item)}/3"
            reason = f" - {reason}" if reason else ""
            lines.append(f"- {item.get('name')} - {format_money(price)}{reason}")
        if intent == "COMBO" or extract_people(lower):
            lines.append(f"Tổng gợi ý khoảng {format_money(total)}. Bạn có thể bấm thêm từng món vào giỏ.")
        else:
            lines.append("Bạn có thể nói thêm ngân sách, số người hoặc món cần tránh để mình lọc kỹ hơn.")
        if allergy_terms(lower):
            lines.append("Lưu ý: với dị ứng, bạn nên xác nhận lại với nhân viên trước khi dùng món.")
        return "\n".join(lines)
    return None


def constrain_suggested_items_to_menu(suggested_items: list[dict], all_menu_items: list[dict], intent: str) -> list[dict]:
    by_id = current_menu_items_by_id(all_menu_items)
    constrained = []
    include_unavailable = intent == "MENU_AVAILABILITY"
    seen = set()
    for item in suggested_items:
        current = by_id.get(str(item.get("id")))
        if not current:
            continue
        if not include_unavailable and current.get("isAvailable", True) is False:
            continue
        if intent == "NO_SPICY" and effective_spicy_level(current) != 0:
            continue
        if intent == "LOW_SPICY" and effective_spicy_level(current) > 1:
            continue
        item_id = str(current.get("id"))
        if item_id in seen:
            continue
        seen.add(item_id)
        constrained.append(current)
    return constrained


@app.get("/health")
def health():
    return {
        "status": "ok",
        "menuItems": len(MENU_ITEMS),
        "menuSynced": MENU_SYNCED,
        "knowledgeBase": len(KNOWLEDGE_BASE),
        "llmConfigured": has_llm_config(),
        "openaiConfigured": bool(os.getenv("OPENAI_API_KEY")),
        "geminiConfigured": bool(os.getenv("GEMINI_API_KEY")),
    }


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest):
    session_id = req.session_id or "default"
    req.session_id = session_id
    intent = detect_intent(req.message)
    all_menu_items = get_context_menu(req.context)
    if is_menu_intent(intent) and not all_menu_items:
        reply = "Mình chưa có dữ liệu thực đơn hiện tại để tư vấn chính xác. Vui lòng đồng bộ menu từ hệ thống quản lý rồi hỏi lại nhé."
        return ChatResponse(
            session_id=session_id,
            reply=reply,
            intent=intent,
            suggestedItems=[],
            actions=[],
            usedTools=["menu_context_missing"],
            memoryUpdated=update_memory(session_id, req, intent, reply, []),
            llmUsed=False,
            llmProvider=None,
            fallbackReason="menu_not_synced_or_missing_context",
        )
    used_tools, tool_data = build_tool_plan(req, intent)
    suggested_items = []
    for value in tool_data.values():
        if isinstance(value, list):
            suggested_items = [item for item in value if isinstance(item, dict)]
            break

    suggested_items = constrain_suggested_items_to_menu(suggested_items, all_menu_items, intent)
    menu_context = build_menu_context(suggested_items or all_menu_items, limit=24)
    menu_catalog = build_menu_catalog(all_menu_items, limit=150)
    deterministic_reply = build_tool_reply(req, intent, tool_data, suggested_items)
    if deterministic_reply:
        tool_data["draftReply"] = deterministic_reply
    tool_data["menuSummary"] = req.context.get("menuSummary") or {
        "totalItems": len(active_items(all_menu_items)),
        "availableItems": len(available_items(all_menu_items)),
    }
    # Deterministic answers are built from real backend/database context.
    # Do not let Gemini/OpenAI rewrite them, because that is where generic repeated answers creep in.
    needs_llm = has_llm_config() and intent != "OUT_OF_SCOPE" and deterministic_reply is None
    reply = deterministic_reply
    llm_used = False
    llm_provider = None
    fallback_reason = None
    if needs_llm:
        llm_reply, llm_provider, fallback_reason = call_llm(req.message, menu_context, menu_catalog, intent, tool_data, session_id)
        if llm_reply:
            if should_keep_deterministic_reply(intent, deterministic_reply, llm_reply):
                reply = deterministic_reply
                fallback_reason = "kept_deterministic_reply_over_generic_menu_llm"
            elif is_menu_intent(intent) and not llm_reply_uses_menu_data(llm_reply, suggested_items):
                reply = build_tool_reply(req, intent, tool_data, suggested_items) or build_rag_reply(req.message)
                llm_used = False
                fallback_reason = "discarded_llm_reply_not_grounded_in_menu"
            else:
                reply = llm_reply
                llm_used = True
        else:
            reply = deterministic_reply
    elif not has_llm_config():
        fallback_reason = "no_llm_key_configured"
    if not reply:
        previous_menu = MENU_ITEMS
        try:
            if req.context.get("menu"):
                globals()["MENU_ITEMS"] = get_context_menu(req.context)
            reply = build_rag_reply(req.message)
            fallback_reason = fallback_reason or "local_rag_fallback"
        finally:
            globals()["MENU_ITEMS"] = previous_menu

    actions = build_actions(intent, suggested_items)
    memory_updated = update_memory(session_id, req, intent, reply, suggested_items)
    return ChatResponse(
        session_id=session_id,
        reply=reply,
        intent=intent,
        suggestedItems=suggested_items[:5],
        actions=actions,
        usedTools=used_tools,
        memoryUpdated=memory_updated,
        llmUsed=llm_used,
        llmProvider=llm_provider,
        fallbackReason=fallback_reason,
    )


@app.post("/sentiment", response_model=SentimentResponse)
def sentiment(req: SentimentRequest):
    return analyze_sentiment(req.text)


@app.post("/menu/sync")
def sync_menu(req: MenuSyncRequest):
    global MENU_ITEMS, MENU_SYNCED
    MENU_ITEMS = req.items
    MENU_SYNCED = True
    token_count = rebuild_search_index()
    return {"synced": len(MENU_ITEMS), "indexedTokens": token_count}


@app.post("/embeddings/rebuild")
def rebuild_embeddings():
    token_count = rebuild_search_index()
    return {"status": "ok", "count": len(MENU_ITEMS), "indexedTokens": token_count, "engine": "local-token-rag"}


rebuild_search_index()
