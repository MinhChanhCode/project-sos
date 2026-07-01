package com.sqc.sos.service;

import com.sqc.sos.dto.menuitem.MenuItemResponse;

public class MenuItemChangedEvent {
    private final String action;
    private final MenuItemResponse item;

    public MenuItemChangedEvent(String action, MenuItemResponse item) {
        this.action = action;
        this.item = item;
    }

    public String getAction() {
        return action;
    }

    public MenuItemResponse getItem() {
        return item;
    }
}
