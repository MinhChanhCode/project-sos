package com.sqc.sos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sqc.sos.dto.qr.QrCodeResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.QrCode;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.repository.IQrCodeRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final IQrCodeRepository qrCodeRepository;
    private final ITableRepository tableRepository;

    @Value("${app.qr-base-url:${app.frontend-url:http://localhost:3000}}")
    private String qrBaseUrl;

    public List<QrCodeResponse> listByTable(UUID tableId) {
        return qrCodeRepository.findByTableId(tableId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public QrCodeResponse generate(UUID tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
        var existing = qrCodeRepository.findFirstByTableIdAndIsActiveTrue(tableId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String url = buildPublicCustomerUrl(table, token);
        QrCode qr = QrCode.builder()
                .tableId(tableId)
                .codeUrl(url)
                .qrToken(token)
                .isActive(true)
                .build();
        return toResponse(qrCodeRepository.save(qr));
    }

    public QrCodeResponse resolveByToken(String token) {
        QrCode qr = qrCodeRepository.findByQrTokenAndIsActiveTrue(token)
                .orElseThrow(() -> new AppException(ErrorCode.QR_NOT_FOUND));
        return toResponse(qr);
    }

    private QrCodeResponse toResponse(QrCode qr) {
        String codeUrl = buildPublicCustomerUrl(qr.getTableId(), qr.getQrToken(), qr.getCodeUrl());
        return QrCodeResponse.builder()
                .id(qr.getId())
                .tableId(qr.getTableId())
                .codeUrl(codeUrl)
                .qrToken(qr.getQrToken())
                .qrImageBase64(generateBase64(codeUrl))
                .build();
    }

    private String buildPublicCustomerUrl(TableEntity table, String qrToken) {
        return buildPublicCustomerUrl(table.getId(), qrToken, null);
    }

    private String buildPublicCustomerUrl(UUID tableId, String qrToken, String fallbackUrl) {
        String baseUrl = normalizeBaseUrl(qrBaseUrl);
        TableEntity table = tableRepository.findById(tableId).orElse(null);
        Integer tableNumber = table != null ? getStandardTableNumber(table.getName()) : null;
        if (tableNumber != null) {
            return baseUrl
                    + "/customer/table/" + tableNumber
                    + "?tableId=" + tableId
                    + "&tableNumber=" + tableNumber
                    + (qrToken != null && !qrToken.isBlank() ? "&qrCode=" + qrToken : "");
        }
        if (fallbackUrl != null && !fallbackUrl.isBlank() && !isLocalUrl(fallbackUrl)) {
            return fallbackUrl;
        }
        return baseUrl
                + "/customer?tableId=" + tableId
                + (qrToken != null && !qrToken.isBlank() ? "&qrCode=" + qrToken : "");
    }

    private String normalizeBaseUrl(String value) {
        String baseUrl = value == null || value.isBlank() ? "http://localhost:3000" : value.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private Integer getStandardTableNumber(String name) {
        if (name == null) return null;
        String normalized = name.trim().replaceAll("\\s+", " ");
        if (!normalized.matches("(?i)^Bàn \\d+$")) return null;
        int number = Integer.parseInt(normalized.replaceAll("\\D+", ""));
        return number >= 1 ? number : null;
    }

    private boolean isLocalUrl(String url) {
        return url.contains("localhost")
                || url.contains("127.0.0.1")
                || url.matches(".*://192\\.168\\..*")
                || url.matches(".*://10\\..*")
                || url.matches(".*://172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }

    private String generateBase64(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
}
