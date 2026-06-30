package com.sqc.sos.repository;

import com.sqc.sos.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IQrCodeRepository extends JpaRepository<QrCode, Long> {
    Optional<QrCode> findByQrTokenAndIsActiveTrue(String qrToken);
    List<QrCode> findByTableId(UUID tableId);
    Optional<QrCode> findFirstByTableIdAndIsActiveTrue(UUID tableId);
}
