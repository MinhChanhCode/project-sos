package com.sqc.sos.repository;

import com.sqc.sos.model.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    // Lấy tất cả yêu cầu theo trạng thái
    Page<ServiceRequest> findByStatusOrderByRequestedAtDesc(ServiceRequest.RequestStatus status, Pageable pageable);

    // Lấy yêu cầu theo bàn
    List<ServiceRequest> findByTableIdOrderByRequestedAtDesc(String tableId);

    // Lấy yêu cầu theo session
    List<ServiceRequest> findBySessionIdOrderByRequestedAtDesc(String sessionId);

    // Lấy yêu cầu đang chờ xử lý
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status IN ('PENDING', 'IN_PROGRESS') ORDER BY sr.priority DESC, sr.requestedAt ASC")
    List<ServiceRequest> findPendingRequests();

    // Lấy yêu cầu theo nhân viên được giao
    List<ServiceRequest> findByAssignedToOrderByRequestedAtDesc(String assignedTo);

    // Đếm yêu cầu đang chờ theo bàn
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.tableId = :tableId AND sr.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingRequestsByTable(@Param("tableId") String tableId);

    // Lấy yêu cầu theo loại và trạng thái
    Page<ServiceRequest> findByRequestTypeAndStatusOrderByRequestedAtDesc(
            ServiceRequest.RequestType requestType,
            ServiceRequest.RequestStatus status,
            Pageable pageable);
}










