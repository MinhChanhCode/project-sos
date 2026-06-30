package com.sqc.sos.dto.qr;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrCodeResponse {
    Long id;
    UUID tableId;
    String codeUrl;
    String qrToken;
    String qrImageBase64;
}
