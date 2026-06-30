package com.sqc.sos.service;

import com.sqc.sos.dto.area.AreaRequest;
import com.sqc.sos.dto.area.AreaResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.Area;
import com.sqc.sos.repository.IAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final IAreaRepository areaRepository;

    public List<AreaResponse> listAll() {
        return areaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AreaResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public AreaResponse create(AreaRequest request) {
        Area area = Area.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(areaRepository.save(area));
    }

    @Transactional
    public AreaResponse update(Long id, AreaRequest request) {
        Area area = find(id);
        if (request.getName() != null) area.setName(request.getName());
        if (request.getDescription() != null) area.setDescription(request.getDescription());
        if (request.getIsActive() != null) area.setIsActive(request.getIsActive());
        return toResponse(areaRepository.save(area));
    }

    @Transactional
    public void delete(Long id) {
        areaRepository.delete(find(id));
    }

    private Area find(Long id) {
        return areaRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));
    }

    private AreaResponse toResponse(Area area) {
        return AreaResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .description(area.getDescription())
                .isActive(area.getIsActive())
                .build();
    }
}
