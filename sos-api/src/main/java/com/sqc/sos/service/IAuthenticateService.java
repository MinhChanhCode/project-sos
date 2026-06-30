package com.sqc.sos.service;

import com.nimbusds.jose.JOSEException;
import com.sqc.sos.dto.authenticate.IntrospectRequest;
import com.sqc.sos.dto.authenticate.IntrospectResponse;
import com.sqc.sos.dto.authenticate.LoginRequest;
import com.sqc.sos.dto.authenticate.LoginResponse;
import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.dto.user.UserResponse;

import java.text.ParseException;

public interface IAuthenticateService {
    LoginResponse login(LoginRequest loginRequest);

    IntrospectResponse introspect(IntrospectRequest introspectRequest) throws ParseException, JOSEException;

    UserResponse register(UserRequest userRequest);
}
