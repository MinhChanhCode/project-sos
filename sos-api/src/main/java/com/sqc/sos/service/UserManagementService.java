package com.sqc.sos.service;

import com.sqc.sos.dto.user.UserDetailResponse;
import com.sqc.sos.dto.user.UserProfileUpdateRequest;
import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.dto.user.UserResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.mapper.IUserMapper;
import com.sqc.sos.model.Role;
import com.sqc.sos.model.User;
import com.sqc.sos.repository.IRoleRepository;
import com.sqc.sos.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDetailResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        return toDetail(user);
    }

    public List<UserDetailResponse> listAll() {
        return userRepository.findAll().stream().map(this::toDetail).toList();
    }

    public UserDetailResponse getById(UUID id) {
        return toDetail(find(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXIST);
        }
        User user = userMapper.userRequestToUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(request.getRoles()));
        }
        userRepository.save(user);
        return userMapper.userToUserResponse(user);
    }

    @Transactional
    public UserDetailResponse updateProfile(UUID id, UserProfileUpdateRequest request) {
        User user = find(id);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        return toDetail(userRepository.save(user));
    }

    @Transactional
    public UserDetailResponse assignRoles(UUID id, List<String> roleNames) {
        User user = find(id);
        user.setRoles(resolveRoles(roleNames));
        return toDetail(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.delete(find(id));
    }

    private User find(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    private Set<Role> resolveRoles(List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            roles.add(role);
        }
        return roles;
    }

    private UserDetailResponse toDetail(User user) {
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
