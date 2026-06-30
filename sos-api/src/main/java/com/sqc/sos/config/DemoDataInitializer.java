package com.sqc.sos.config;

import com.sqc.sos.model.Role;
import com.sqc.sos.model.User;
import com.sqc.sos.repository.IRoleRepository;
import com.sqc.sos.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureRole("ADMIN");
        ensureRole("MANAGER");
        ensureRole("STAFF");
        ensureRole("KITCHEN");
        ensureUser("admin", "admin123", "Quản trị viên", Set.of("ADMIN"));
        ensureUser("staff", "staff123", "Nhân viên phục vụ", Set.of("STAFF"));
        ensureUser("kitchen", "kitchen123", "Nhân viên bếp", Set.of("KITCHEN"));
    }

    private void ensureRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
            log.info("Created role: {}", name);
        }
    }

    private void ensureUser(String username, String password, String fullName, Set<String> roleNames) {
        if (userRepository.findByUsername(username).isPresent()) return;
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(resolveRoles(roleNames))
                .build();
        userRepository.save(user);
        log.info("Created demo user: {}", username);
    }

    private Set<Role> resolveRoles(Set<String> names) {
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            roleRepository.findByName(name).ifPresent(roles::add);
        }
        return roles;
    }
}
