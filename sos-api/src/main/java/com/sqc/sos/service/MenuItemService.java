package com.sqc.sos.service;

import com.sqc.sos.dto.menuitem.MenuItemRequest;
import com.sqc.sos.dto.menuitem.MenuItemResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.mapper.IMenuItemMapper;
import com.sqc.sos.model.Category;
import com.sqc.sos.model.MenuItem;
import com.sqc.sos.repository.ICategoryRepository;
import com.sqc.sos.repository.IMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemService implements IMenuItemService {
    //
    private final IMenuItemRepository menuItemRepository;
    private final ICategoryRepository categoryRepository;
    private final com.sqc.sos.repository.IOrderItemRepository orderItemRepository;
    private final IMenuItemMapper menuItemMapper;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuItemResponse> getAllMenuItems(String name, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findByNameContainingIgnoreCase(name, pageable);
        return new PageResponse<>(page.map(menuItemMapper::toResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuItemResponse> getActiveMenuItems(Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findByIsActiveTrue(pageable);
        return new PageResponse<>(page.map(menuItemMapper::toResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuItemResponse> getAvailableMenuItems(Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findByIsAvailableTrueAndIsActiveTrue(pageable);
        return new PageResponse<>(page.map(menuItemMapper::toResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuItemResponse> getMenuItemsByCategory(Long categoryId, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return new PageResponse<>(page.map(menuItemMapper::toResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuItemResponse> searchMenuItems(String keyword, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.searchByKeyword(keyword, pageable);
        return new PageResponse<>(page.map(menuItemMapper::toResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        return menuItemMapper.toResponse(menuItem);
    }
    
    @Override
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        
        MenuItem menuItem = menuItemMapper.toEntity(request);
        menuItem.setCategory(category);
        
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toResponse(savedMenuItem);
    }
    
    @Override
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem existingMenuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        
        // Validate category exists if category is being updated
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            existingMenuItem.setCategory(category);
        }
        
        // Update fields
        if (request.getName() != null) {
            existingMenuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingMenuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            existingMenuItem.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            existingMenuItem.setImageUrl(request.getImageUrl());
        }
        if (request.getIsAvailable() != null) {
            existingMenuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsActive() != null) {
            existingMenuItem.setIsActive(request.getIsActive());
        }
        // Optional inventory fields from request (if your DTO has them later)
        // if (request.getStockQuantity() != null) existingMenuItem.setStockQuantity(request.getStockQuantity());
        // if (request.getIsLimitedStock() != null) existingMenuItem.setIsLimitedStock(request.getIsLimitedStock());
        
        MenuItem updatedMenuItem = menuItemRepository.save(existingMenuItem);
        return menuItemMapper.toResponse(updatedMenuItem);
    }
    
    @Override
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        
        menuItemRepository.delete(menuItem);
    }
    
    @Override
    public void toggleAvailability(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        
        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        menuItemRepository.save(menuItem);
    }
    
    @Override
    public void toggleActiveStatus(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        
        menuItem.setIsActive(!menuItem.getIsActive());
        menuItemRepository.save(menuItem);
    }

    // ===== Promotions embedded in MenuItem =====
    @Override
    public void upsertPromotion(Long menuItemId, BigDecimal promotionalPrice, LocalDateTime endDate) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        if (promotionalPrice == null || promotionalPrice.compareTo(BigDecimal.ZERO) <= 0 || promotionalPrice.compareTo(menuItem.getPrice()) >= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        menuItem.setOriginalPrice(menuItem.getPrice());
        menuItem.setPromotionalPrice(promotionalPrice);
        menuItem.setPromotionEndDate(endDate);
        menuItemRepository.save(menuItem);
    }

    @Override
    public void removePromotion(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        menuItem.setOriginalPrice(null);
        menuItem.setPromotionalPrice(null);
        menuItem.setPromotionEndDate(null);
        menuItemRepository.save(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> listPromotions(int limit, LocalDateTime now) {
        LocalDateTime ts = (now != null) ? now : LocalDateTime.now();
        List<MenuItem> list = menuItemRepository.findActivePromotions(ts);
        if (limit > 0 && list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list.stream().map(menuItemMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> listBestSellers(int limit, LocalDateTime from) {
        LocalDateTime fromTs = from;
        var pageable = PageRequest.of(0, Math.max(1, limit <= 0 ? 8 : limit));
        List<MenuItem> items = orderItemRepository.findBestSellerMenuItems(fromTs, pageable);
        return items.stream().map(menuItemMapper::toResponse).toList();
    }
} 