# 11. Realtime

## Cong nghe

- Backend: Spring WebSocket + STOMP.
- Frontend: SockJS + STOMP.js.
- Broker prefix: `/topic`.
- Application destination prefix: `/app`.
- Endpoint: `/ws`.

## Backend WebSocketConfig

```java
registry.enableSimpleBroker("/topic");
registry.setApplicationDestinationPrefixes("/app");
registry.addEndpoint("/ws")
  .setAllowedOriginPatterns(...)
  .withSockJS();
```

Client ket noi:

```text
{API_BASE}/ws
```

## Frontend realtime plugin

File:

```text
sos-fe/plugins/realtime.client.ts
```

Chuc nang:

- Tao SockJS connection.
- Tao STOMP client.
- Quan ly reconnect.
- Luu danh sach subscription.
- Tranh subscribe trung o STOMP-level.
- Fan-out mot topic cho nhieu handler.

## Topic chinh

| Topic | Publisher | Subscriber | Muc dich |
|---|---|---|---|
| `/topic/tables/{tableId}/cart` | CartRealtimeListener | Customer | Cart thay doi |
| `/topic/tables/{tableId}/ordered` | OrderedItemsRealtimeListener/RealtimeService | Customer/Staff | Mon da dat thay doi |
| `/topic/tables/{tableId}/order-items` | RealtimeService | Customer/Staff | Trang thai mon thay doi |
| `/topic/tables/{tableId}` | RealtimeService/ServiceRequestService | Customer/Staff | General table event |
| `/topic/management/tables` | TableStatusRealtimeListener | Admin/Staff | Trang thai ban thay doi |
| `/topic/management/order-items` | RealtimeService | Admin/Staff | Quan ly trang thai mon |
| `/topic/kitchen/orders` | RealtimeService | Kitchen | Don moi cho bep |
| `/topic/orders/{orderId}` | RealtimeService | Staff/Kitchen | Cap nhat order cu the |
| `/topic/server/orders-ready` | RealtimeService | Staff | Mon san sang phuc vu |
| `/topic/service-requests` | ServiceRequestService | Staff | Yeu cau phuc vu |
| `/topic/staff/chat` | StaffChatService | Staff | Tin nhan tu khach |
| `/topic/tables/{tableId}/staff-chat` | StaffChatService | Customer/Staff | Chat theo ban |
| `/topic/menu-items` | RealtimeService | Admin/Customer | Menu item thay doi |
| `/topic/reviews` | RealtimeService | Admin | Review moi |
| `/topic/payment` | RealtimeService | Customer/Staff/Admin | Thanh toan |

## Luong publish order item

```text
OrderItemService.updateStatus
  -> publish OrderItemStatusChangedEvent
  -> RealtimeService @EventListener
  -> convertAndSend:
      /topic/orders/{orderId}
      /topic/tables/{tableId}/order-items
      /topic/tables/{tableId}
      /topic/management/order-items
      /topic/server/orders-ready neu COMPLETED
```

## Luong publish cart

```text
CartService add/update/remove
  -> CartUpdatedEvent
  -> CartRealtimeListener
  -> /topic/tables/{tableId}/cart
```

## Luong table status

```text
TableManagementService/TableQueryService
  -> TableStatusChangedEvent
  -> TableStatusRealtimeListener
  -> /topic/management/tables
```

## Luong service request

```text
ServiceRequestService create/update/delete
  -> messagingTemplate.convertAndSend("/topic/service-requests", payload)
  -> messagingTemplate.convertAndSend("/topic/tables/{tableId}", payload)
```

## Frontend subscriptions

Customer:

- `/topic/tables/{tableId}/cart`
- `/topic/tables/{tableId}/ordered`
- `/topic/tables/{tableId}`
- `/topic/tables/{tableId}/order-items`
- `/topic/menu-items`
- `/topic/payment`

Staff:

- `/topic/server/orders-ready`
- `/topic/tables/{id}/ordered`
- `/topic/tables/{id}/order-items`
- `/topic/tables/{id}`
- `/topic/staff/chat`
- `/topic/payment`
- `/topic/management/tables`

Admin:

- `/topic/reviews`
- `/topic/menu-items`
- `/topic/payment`
- `/topic/management/tables`
- `/topic/management/order-items`

Kitchen:

- `/topic/kitchen/orders`
- `/topic/management/order-items`

## Diem manh

- Realtime phu hop nghiep vu nha hang.
- Topic theo table giup giam broadcast khong can thiet.
- Co management topics cho staff/admin.
- Frontend plugin tranh duplicate low-level subscriptions.

## Diem can cai thien

- WebSocket endpoint dang permitAll; production nen can nhac auth cho staff/admin topics.
- Chua thay phan quyen topic theo role.
- Can xu ly reconnect/backoff chi tiet hon neu deploy sau proxy.

