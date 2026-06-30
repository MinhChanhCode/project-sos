package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.servicerequest.ServiceRequestRequest;
import com.sqc.sos.dto.servicerequest.ServiceRequestResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.model.ServiceRequest;
import com.sqc.sos.service.IServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final IServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> createServiceRequest(
            @RequestBody ServiceRequestRequest request) {
        log.info("POST /service-requests - request: {}", request);
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo yêu cầu phục vụ thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getServiceRequestById(@PathVariable Long id) {
        log.info("GET /service-requests/{}", id);
        ServiceRequestResponse response = serviceRequestService.getServiceRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin yêu cầu thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServiceRequestResponse>>> getAllServiceRequests(Pageable pageable) {
        log.info("GET /service-requests - page: {}", pageable.getPageNumber());
        PageResponse<ServiceRequestResponse> page = serviceRequestService.getAllServiceRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách yêu cầu thành công"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<ServiceRequestResponse>>> getServiceRequestsByStatus(
            @PathVariable ServiceRequest.RequestStatus status,
            Pageable pageable) {
        log.info("GET /service-requests/status/{} - page: {}", status, pageable.getPageNumber());
        PageResponse<ServiceRequestResponse> page = serviceRequestService.getServiceRequestsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách yêu cầu theo trạng thái thành công"));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getServiceRequestsByTable(
            @PathVariable String tableId) {
        log.info("GET /service-requests/table/{}", tableId);
        List<ServiceRequestResponse> requests = serviceRequestService.getServiceRequestsByTable(tableId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Lấy danh sách yêu cầu theo bàn thành công"));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getServiceRequestsBySession(
            @PathVariable String sessionId) {
        log.info("GET /service-requests/session/{}", sessionId);
        List<ServiceRequestResponse> requests = serviceRequestService.getServiceRequestsBySession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Lấy danh sách yêu cầu theo session thành công"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getPendingServiceRequests() {
        log.info("GET /service-requests/pending");
        List<ServiceRequestResponse> requests = serviceRequestService.getPendingServiceRequests();
        return ResponseEntity.ok(ApiResponse.success(requests, "Lấy danh sách yêu cầu đang chờ thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateServiceRequestStatus(
            @PathVariable Long id,
            @RequestParam ServiceRequest.RequestStatus status,
            @RequestParam(required = false) String assignedTo) {
        log.info("PATCH /service-requests/{}/status - status: {}, assignedTo: {}", id, status, assignedTo);
        ServiceRequestResponse response = serviceRequestService.updateServiceRequestStatus(id, status, assignedTo);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái yêu cầu thành công"));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> assignServiceRequest(
            @PathVariable Long id,
            @RequestParam String assignedTo) {
        log.info("PATCH /service-requests/{}/assign - assignedTo: {}", id, assignedTo);
        ServiceRequestResponse response = serviceRequestService.assignServiceRequest(id, assignedTo);
        return ResponseEntity.ok(ApiResponse.success(response, "Giao yêu cầu thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteServiceRequest(@PathVariable Long id) {
        log.info("DELETE /service-requests/{}", id);
        serviceRequestService.deleteServiceRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa yêu cầu thành công"));
    }

    @GetMapping("/table/{tableId}/pending-count")
    public ResponseEntity<ApiResponse<Long>> getPendingRequestsCountByTable(@PathVariable String tableId) {
        log.info("GET /service-requests/table/{}/pending-count", tableId);
        long count = serviceRequestService.countPendingRequestsByTable(tableId);
        return ResponseEntity.ok(ApiResponse.success(count, "Lấy số lượng yêu cầu đang chờ thành công"));
    }
}










