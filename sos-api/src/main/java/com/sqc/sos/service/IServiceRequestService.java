package com.sqc.sos.service;

import com.sqc.sos.dto.servicerequest.ServiceRequestRequest;
import com.sqc.sos.dto.servicerequest.ServiceRequestResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.model.ServiceRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IServiceRequestService {

    ServiceRequestResponse createServiceRequest(ServiceRequestRequest request);

    ServiceRequestResponse getServiceRequestById(Long id);

    PageResponse<ServiceRequestResponse> getAllServiceRequests(Pageable pageable);

    PageResponse<ServiceRequestResponse> getServiceRequestsByStatus(ServiceRequest.RequestStatus status,
            Pageable pageable);

    List<ServiceRequestResponse> getServiceRequestsByTable(String tableId);

    List<ServiceRequestResponse> getServiceRequestsBySession(String sessionId);

    List<ServiceRequestResponse> getPendingServiceRequests();

    ServiceRequestResponse updateServiceRequestStatus(Long id, ServiceRequest.RequestStatus status, String assignedTo);

    ServiceRequestResponse assignServiceRequest(Long id, String assignedTo);

    void deleteServiceRequest(Long id);

    long countPendingRequestsByTable(String tableId);
}










