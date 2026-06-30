// Using runtime plugin typing from Nuxt; fallback to any if type missing
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import { defineNuxtPlugin, useRuntimeConfig } from "#app";

type StompClient = any;

export default defineNuxtPlugin(() => {
  let stomp: StompClient | null = null;
  let connected = false;
  // Allow multiple handlers per destination
  const subscribers = new Map<string, Set<(payload: any) => void>>();
  // Track which destinations are actively subscribed on STOMP to avoid duplicate low-level subs
  const activeStompSubscriptions = new Set<string>();

  const connect = async () => {
    if (connected) return;
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const SockJS = (await import("sockjs-client")).default;
    const { Client } = await import("@stomp/stompjs");

    const {
      public: { wsBase },
    } = useRuntimeConfig();
    const socketUrl = String(wsBase || "/ws")
      .replace(/^wss:\/\//i, "https://")
      .replace(/^ws:\/\//i, "http://");
    const socket = new SockJS(socketUrl);
    const client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 2000,
      onConnect: () => {
        connected = true;
        // Re-subscribe once per destination and fan-out to all handlers
        activeStompSubscriptions.clear();
        for (const dest of subscribers.keys()) {
          client.subscribe(dest, (msg: any) => {
            const handlers = subscribers.get(dest);
            if (!handlers || handlers.size === 0) return;
            try {
              const payload = JSON.parse(msg.body);
              for (const h of handlers) {
                try {
                  h(payload);
                } catch {
                  /* ignore handler error */
                }
              }
            } catch {
              /* ignore parse error */
            }
          });
          activeStompSubscriptions.add(dest);
        }
      },
      onStompError: () => {
        connected = false;
      },
      onWebSocketClose: () => {
        connected = false;
      },
    });
    client.activate();
    stomp = client;
  };

  const subscribe = async (
    destination: string,
    handler: (payload: any) => void
  ) => {
    // Register handler in a Set per destination
    if (!subscribers.has(destination)) {
      subscribers.set(destination, new Set());
    }
    subscribers.get(destination)!.add(handler);

    // Ensure connection
    if (!stomp || !connected) {
      await connect();
      return;
    }

    // If not yet subscribed at STOMP level for this destination, subscribe once and fan-out
    if (!activeStompSubscriptions.has(destination)) {
      stomp.subscribe(destination, (msg: any) => {
        const handlers = subscribers.get(destination);
        if (!handlers || handlers.size === 0) return;
        try {
          const payload = JSON.parse(msg.body);
          for (const h of handlers) {
            try {
              h(payload);
            } catch {
              /* ignore handler error */
            }
          }
        } catch {
          /* ignore parse error */
        }
      });
      activeStompSubscriptions.add(destination);
    }
  };

  const unsubscribe = (
    destination: string,
    handler?: (payload: any) => void
  ) => {
    const set = subscribers.get(destination);
    if (!set) return;
    if (handler) {
      set.delete(handler);
    } else {
      set.clear();
    }
  };

  const onTableCleared = async (tableId: string, clear: () => void) => {
    await subscribe(`/topic/tables/${tableId}`, (payload: any) => {
      if (payload?.type === "TABLE_CLEARED") clear();
    });
  };

  const onServiceRequest = async (handler: (payload: any) => void) => {
    await subscribe("/topic/service-requests", handler);
  };

  const onTableServiceRequest = async (
    tableId: string,
    handler: (payload: any) => void
  ) => {
    await subscribe(`/topic/tables/${tableId}`, handler);
  };

  return {
    provide: {
      realtime: {
        subscribe,
        unsubscribe,
        onTableCleared,
        onServiceRequest,
        onTableServiceRequest,
        onKitchenOrders: async (handler: (payload: any) => void) => {
          await subscribe("/topic/kitchen/orders", handler);
        },
        onOrder: async (orderId: string, handler: (payload: any) => void) => {
          await subscribe(`/topic/orders/${orderId}`, handler);
        },
      },
    },
  };
});
