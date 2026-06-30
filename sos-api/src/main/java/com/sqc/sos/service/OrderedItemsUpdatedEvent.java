package com.sqc.sos.service;

import java.util.UUID;

public class OrderedItemsUpdatedEvent {
    private final UUID tableId;
    public OrderedItemsUpdatedEvent(UUID tableId) { this.tableId = tableId; }
    public UUID getTableId() { return tableId; }
}
