package com.sqc.sos.controller;

import com.nimbusds.jose.JOSEException;
import com.sqc.sos.dto.authenticate.IntrospectRequest;
import com.sqc.sos.dto.authenticate.LoginRequest;
import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.service.IAuthenticateService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    IAuthenticateService authenticateService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticateService.login(loginRequest));
    }

    @PostMapping("/introspect")
    public ResponseEntity<?> introspect(@RequestBody IntrospectRequest introspectRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(authenticateService.introspect(introspectRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest userRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(authenticateService.register(userRequest));
    }
}