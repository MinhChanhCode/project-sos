package com.sqc.sos.mapper;

import com.sqc.sos.dto.servicerequest.ServiceRequestRequest;
import com.sqc.sos.dto.servicerequest.ServiceRequestResponse;
import com.sqc.sos.model.ServiceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IServiceRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "requestedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    ServiceRequest toEntity(ServiceRequestRequest request);

    ServiceRequestResponse toResponse(ServiceRequest entity);
}










