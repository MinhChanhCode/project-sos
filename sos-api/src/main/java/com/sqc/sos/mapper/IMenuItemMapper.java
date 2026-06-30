package com.sqc.sos.mapper;

import com.sqc.sos.dto.menuitem.MenuItemRequest;
import com.sqc.sos.dto.menuitem.MenuItemResponse;
import com.sqc.sos.model.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IMenuItemMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MenuItemResponse toResponse(MenuItem menuItem);
    
    List<MenuItemResponse> toResponseList(List<MenuItem> menuItems);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isActive", expression = "java(request.getIsActive() != null ? request.getIsActive() : true)")
    @Mapping(target = "isAvailable", expression = "java(request.getIsAvailable() != null ? request.getIsAvailable() : true)")
    MenuItem toEntity(MenuItemRequest request);
} 