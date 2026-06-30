package com.sqc.sos.service;

import java.util.UUID;

public class CartUpdatedEvent {
    private final UUID tableId;
    public CartUpdatedEvent(UUID tableId) { this.tableId = tableId; }
    public UUID getTableId() { return tableId; }
}
