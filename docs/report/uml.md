# UML

Tài liệu này chỉ chứa mã PlantUML, không sinh hình ảnh. Có thể copy từng khối vào PlantUML, IntelliJ PlantUML plugin, VS Code PlantUML extension hoặc plantuml.com để render.

## Use Case Diagram

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor "Khach hang" as Customer
actor "Nhan vien phuc vu" as Staff
actor "Nhan vien bep" as Kitchen
actor "Quan ly" as Manager
actor "Quan tri vien" as Admin
actor "AI Service" as AI

rectangle "ProjectSOS" {
  usecase "Quet QR vao ban" as UC_QR
  usecase "Xem menu" as UC_Menu
  usecase "Tim kiem / loc mon" as UC_Search
  usecase "Them mon vao gio" as UC_Cart
  usecase "Xac nhan dat mon" as UC_Order
  usecase "Yeu cau ho tro" as UC_Service
  usecase "Chat voi nhan vien" as UC_StaffChat
  usecase "Chatbot goi y mon" as UC_AIChat
  usecase "Thanh toan" as UC_Payment
  usecase "Danh gia" as UC_Review

  usecase "Dang nhap" as UC_Login
  usecase "Quan ly ban / so do tang" as UC_Tables
  usecase "Quan ly menu" as UC_MenuAdmin
  usecase "Quan ly danh muc" as UC_Categories
  usecase "Quan ly don / mon" as UC_OrderAdmin
  usecase "Cap nhat trang thai mon" as UC_ItemStatus
  usecase "Quan ly nhan vien" as UC_Employees
  usecase "Quan ly ca lam / phan cong" as UC_Shifts
  usecase "Quan ly hoa don" as UC_Invoices
  usecase "Xem dashboard" as UC_Dashboard
  usecase "Quan ly danh gia" as UC_ReviewAdmin
  usecase "Phan tich cam xuc danh gia" as UC_Sentiment
  usecase "Dong bo menu sang AI" as UC_AISync
}

Customer --> UC_QR
Customer --> UC_Menu
Customer --> UC_Search
Customer --> UC_Cart
Customer --> UC_Order
Customer --> UC_Service
Customer --> UC_StaffChat
Customer --> UC_AIChat
Customer --> UC_Payment
Customer --> UC_Review

Staff --> UC_Login
Staff --> UC_Tables
Staff --> UC_OrderAdmin
Staff --> UC_ItemStatus
Staff --> UC_Service
Staff --> UC_StaffChat
Staff --> UC_Invoices

Kitchen --> UC_Login
Kitchen --> UC_OrderAdmin
Kitchen --> UC_ItemStatus

Manager --> UC_Login
Manager --> UC_Tables
Manager --> UC_MenuAdmin
Manager --> UC_Categories
Manager --> UC_OrderAdmin
Manager --> UC_Employees
Manager --> UC_Shifts
Manager --> UC_Invoices
Manager --> UC_Dashboard
Manager --> UC_ReviewAdmin
Manager --> UC_AISync

Admin --> UC_Login
Admin --> UC_Tables
Admin --> UC_MenuAdmin
Admin --> UC_Categories
Admin --> UC_OrderAdmin
Admin --> UC_Employees
Admin --> UC_Shifts
Admin --> UC_Invoices
Admin --> UC_Dashboard
Admin --> UC_ReviewAdmin
Admin --> UC_AISync

UC_AIChat --> AI
UC_Sentiment --> AI
UC_AISync --> AI
UC_ReviewAdmin --> UC_Sentiment
UC_Order --> UC_Cart
UC_Payment --> UC_Invoices
@enduml
```

## Class Diagram

```plantuml
@startuml
skinparam classAttributeIconSize 0

abstract class BaseEntity {
  +Long id
  +LocalDateTime createdAt
  +LocalDateTime updatedAt
}

class User {
  +String username
  +String password
  +String email
  +String fullName
  +String phone
  +Boolean active
}

class Role {
  +String name
}

class Employee {
  +String code
  +String fullName
  +String phone
  +String position
}

class StaffShift {
  +LocalDateTime startTime
  +LocalDateTime endTime
}

class Assignment {
  +String note
}

class Area {
  +String name
  +String description
}

class TableEntity {
  +String tableNumber
  +Integer capacity
  +TableStatus status
  +Integer positionX
  +Integer positionY
}

enum TableStatus {
  AVAILABLE
  OCCUPIED
  RESERVED
  CLEANING
  OUT_OF_SERVICE
}

class Category {
  +String name
  +String description
  +Boolean active
}

class MenuItem {
  +String name
  +String description
  +BigDecimal price
  +String imageUrl
  +Boolean available
  +Boolean active
  +Boolean promotion
  +String tasteTags
  +String aiDescription
}

class CustomerSession {
  +String sessionId
  +String customerName
  +Boolean active
}

class Cart {
  +String sessionId
  +Boolean active
}

class CartItem {
  +Integer quantity
  +String note
}

class Order {
  +String orderCode
  +String status
  +BigDecimal totalAmount
}

class OrderItem {
  +Integer pendingQuantity
  +Integer preparingQuantity
  +Integer readyQuantity
  +Integer servedQuantity
  +Integer completedQuantity
  +String note
}

enum OrderItemStatus {
  PENDING
  PREPARING
  READY
  SERVED
  CANCELLED
}

class Payment {
  +BigDecimal amount
  +String method
  +String status
}

class Invoice {
  +String invoiceCode
  +BigDecimal totalAmount
  +String paymentStatus
}

class QrCode {
  +String token
  +String qrUrl
  +Boolean active
}

class Review {
  +Integer rating
  +String comment
  +String customerName
}

class SentimentResult {
  +String label
  +Double score
  +String summary
}

class ServiceRequest {
  +String type
  +String status
  +String message
}

class StaffChatMessage {
  +String senderType
  +String message
}

class ChatHistory {
  +String sessionId
  +String question
  +String answer
}

BaseEntity <|-- User
BaseEntity <|-- Role
BaseEntity <|-- Employee
BaseEntity <|-- StaffShift
BaseEntity <|-- Assignment
BaseEntity <|-- Area
BaseEntity <|-- TableEntity
BaseEntity <|-- Category
BaseEntity <|-- MenuItem
BaseEntity <|-- CustomerSession
BaseEntity <|-- Cart
BaseEntity <|-- CartItem
BaseEntity <|-- Order
BaseEntity <|-- OrderItem
BaseEntity <|-- Payment
BaseEntity <|-- Invoice
BaseEntity <|-- QrCode
BaseEntity <|-- Review
BaseEntity <|-- SentimentResult
BaseEntity <|-- ServiceRequest
BaseEntity <|-- StaffChatMessage
BaseEntity <|-- ChatHistory

User "many" -- "many" Role
Employee "1" -- "many" StaffShift
Employee "1" -- "many" Assignment
Area "1" -- "many" TableEntity
Category "1" -- "many" MenuItem
TableEntity "1" -- "many" CustomerSession
TableEntity "1" -- "many" Cart
TableEntity "1" -- "many" Order
TableEntity "1" -- "many" QrCode
TableEntity "1" -- "many" ServiceRequest
TableEntity "1" -- "many" StaffChatMessage
CustomerSession "1" -- "many" ChatHistory
CustomerSession "1" -- "many" ServiceRequest
Cart "1" -- "many" CartItem
CartItem "many" -- "1" MenuItem
Order "1" -- "many" OrderItem
OrderItem "many" -- "1" MenuItem
Order "1" -- "many" Payment
Order "1" -- "1" Invoice
Review "1" -- "0..1" SentimentResult
@enduml
```

## Sequence Diagram - Khach dat mon qua QR

```plantuml
@startuml
actor "Khach hang" as Customer
participant "Nuxt Frontend" as FE
participant "CustomerSession API" as SessionAPI
participant "Menu API" as MenuAPI
participant "Cart API" as CartAPI
participant "Order API" as OrderAPI
database "MySQL" as DB
participant "WebSocket Broker" as WS
participant "Kitchen/Staff UI" as StaffUI

Customer -> FE: Quet QR /customer/table/{tableNumber}
FE -> SessionAPI: POST /api/v1/customer-sessions
SessionAPI -> DB: Tao hoac lay session
DB --> SessionAPI: sessionId
SessionAPI --> FE: SessionResponse

FE -> MenuAPI: GET /api/v1/menu-items/active
MenuAPI -> DB: Lay menu dang ban
DB --> MenuAPI: Danh sach mon
MenuAPI --> FE: MenuResponse[]

Customer -> FE: Them mon vao gio
FE -> CartAPI: POST /api/v1/carts/session/{sessionId}/items
CartAPI -> DB: Cap nhat cart/cart_items
CartAPI -> WS: Publish /topic/tables/{tableId}/cart
WS --> StaffUI: Gio hang thay doi
CartAPI --> FE: CartResponse

Customer -> FE: Xac nhan dat mon
FE -> OrderAPI: POST /api/v1/orders/session/{sessionId}/confirm
OrderAPI -> DB: Tao order va order_items
OrderAPI -> WS: Publish kitchen/management/order topics
WS --> StaffUI: Don moi
OrderAPI --> FE: OrderResponse
@enduml
```

## Sequence Diagram - Chatbot AI

```plantuml
@startuml
actor "Khach hang" as Customer
participant "Chatbot.vue" as Chatbot
participant "Chat API Backend" as ChatAPI
participant "ChatService" as ChatService
participant "sos-ai FastAPI" as AI
participant "OpenAI API" as OpenAI
database "MySQL" as DB

Customer -> Chatbot: Nhap cau hoi ve mon an
Chatbot -> ChatAPI: POST /api/v1/chat
ChatAPI -> ChatService: Xu ly tin nhan
ChatService -> AI: POST /chat
AI -> AI: rag_search(MENU_ITEMS)
alt Co OPENAI_API_KEY
  AI -> OpenAI: Chat completion voi context menu
  OpenAI --> AI: Cau tra loi tu LLM
else Khong co key hoac loi
  AI -> AI: build_rag_reply heuristic
end
AI --> ChatService: answer, recommendations
ChatService -> DB: Luu ChatHistory
ChatService --> ChatAPI: ChatResponse
ChatAPI --> Chatbot: Cau tra loi
Chatbot --> Customer: Hien thi goi y mon
@enduml
```

## Activity Diagram - Quy trinh nha hang

```plantuml
@startuml
start
:Khach quet QR tren ban;
:Frontend tao hoac lay customer session;
:Khach xem menu;
if (Muon hoi chatbot?) then (Co)
  :Gui tin nhan toi Chat API;
  :AI tim mon phu hop va tra loi;
endif
:Khach them mon vao gio;
if (Gio hang hop le?) then (Co)
  :Xac nhan dat mon;
  :Backend tao order va order item;
  :WebSocket thong bao bep va nhan vien;
  :Bep xu ly mon;
  repeat
    :Cap nhat trang thai item;
    :Frontend khach/nhan vien nhan realtime;
  repeat while (Con mon chua phuc vu?) is (Co)
  :Nhan vien tao hoa don;
  :Khach thanh toan;
  :Khach gui danh gia;
  :Backend gui danh gia sang AI phan tich cam xuc;
else (Khong)
  :Thong bao loi gio hang;
endif
stop
@enduml
```

## State Diagram - Trang thai mon trong don

```plantuml
@startuml
[*] --> PENDING
PENDING --> PREPARING: Bep bat dau lam
PREPARING --> READY: Mon da san sang
READY --> SERVED: Nhan vien da phuc vu
PENDING --> CANCELLED: Huy mon
PREPARING --> CANCELLED: Huy neu duoc phep
SERVED --> [*]
CANCELLED --> [*]
@enduml
```

## State Diagram - Trang thai ban

```plantuml
@startuml
[*] --> AVAILABLE
AVAILABLE --> OCCUPIED: Khach vao ban / tao session
AVAILABLE --> RESERVED: Dat truoc
OCCUPIED --> CLEANING: Ket thuc phuc vu
CLEANING --> AVAILABLE: Don ban xong
RESERVED --> OCCUPIED: Khach den
AVAILABLE --> OUT_OF_SERVICE: Bao tri
OUT_OF_SERVICE --> AVAILABLE: Mo lai ban
@enduml
```

## Deployment Diagram

```plantuml
@startuml
node "Client Browser" as Browser {
  artifact "Nuxt/Vue UI"
}

cloud "Vercel" as Vercel {
  node "sos-fe" {
    artifact "Frontend static/SSR deployment"
    artifact "Vercel Functions / proxy routes"
  }
}

cloud "Railway" as Railway {
  node "sos-api" {
    artifact "Spring Boot REST API"
    artifact "WebSocket STOMP endpoint"
  }
  node "sos-ai" {
    artifact "FastAPI AI service"
    artifact "OpenAI client"
  }
  database "Railway MySQL" as MySQL
}

cloud "OpenAI" as OpenAI {
  artifact "Chat model"
}

Browser --> Vercel: HTTPS
Vercel --> Railway: REST API / WebSocket
Railway --> MySQL: JDBC
Railway --> OpenAI: HTTPS API
Browser --> Railway: Optional direct WebSocket/API if configured
@enduml
```

## Component Diagram

```plantuml
@startuml
package "sos-fe Nuxt" {
  [Pages]
  [Components]
  [Pinia Stores]
  [API Services]
  [WebSocket Client]
}

package "sos-api Spring Boot" {
  [Controllers]
  [Services]
  [Repositories]
  [Entities]
  [Security Config]
  [WebSocket Config]
  [AI Client]
}

package "sos-ai FastAPI" {
  [Chat Endpoint]
  [Sentiment Endpoint]
  [Menu Sync Endpoint]
  [RAG Search]
  [Prompt Builder]
  [OpenAI Client]
}

database "MySQL" {
  [Restaurant Schema]
}

cloud "OpenAI API" {
  [LLM]
}

[Pages] --> [Components]
[Components] --> [Pinia Stores]
[Pinia Stores] --> [API Services]
[Components] --> [WebSocket Client]
[API Services] --> [Controllers]
[WebSocket Client] --> [WebSocket Config]
[Controllers] --> [Services]
[Services] --> [Repositories]
[Repositories] --> [Entities]
[Repositories] --> [Restaurant Schema]
[Services] --> [AI Client]
[AI Client] --> [Chat Endpoint]
[AI Client] --> [Sentiment Endpoint]
[Chat Endpoint] --> [RAG Search]
[Chat Endpoint] --> [Prompt Builder]
[Prompt Builder] --> [OpenAI Client]
[OpenAI Client] --> [LLM]
@enduml
```
