package com.sqc.sos.dto.orderitem;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemStatusResponse {
    private String code;
    private String name;
} 