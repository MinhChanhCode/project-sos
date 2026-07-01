package com.sqc.sos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> {
            request
                    .requestMatchers("/auth/login", "/auth/introspect", "/health/**", "/health").permitAll()
                    .requestMatchers("/ws/**", "/ws").permitAll()
                    // Customer public APIs
                    .requestMatchers("/api/v1/menu-items/**").permitAll()
                    .requestMatchers("/api/v1/categories/**").permitAll()
                    .requestMatchers("/api/v1/carts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/tables/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders/session/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").permitAll()
                    .requestMatchers("/api/v1/service-requests/**").permitAll()
                    .requestMatchers("/api/v1/images/view/**").permitAll()
                    .requestMatchers("/api/v1/reviews").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/reviews").permitAll()
                    .requestMatchers("/api/v1/chat/**").permitAll()
                    .requestMatchers("/api/v1/customer-sessions/**").permitAll()
                    .requestMatchers("/api/v1/staff-chat/**").permitAll()
                    .requestMatchers("/api/v1/qr-codes/token/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                    // Admin / Manager
                    .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/areas/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/tables/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/tables/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/tables/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/tables/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/shifts/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/assignments/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/invoices/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                    .requestMatchers("/api/v1/qr-codes/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/order-items/**").hasAnyRole("ADMIN", "MANAGER", "STAFF", "KITCHEN")
                    .requestMatchers("/auth/register").hasRole("ADMIN")
                    .anyRequest().authenticated();
        });

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(cors -> {});
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
