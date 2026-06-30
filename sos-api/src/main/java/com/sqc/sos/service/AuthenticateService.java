package com.sqc.sos.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sqc.sos.dto.authenticate.IntrospectRequest;
import com.sqc.sos.dto.authenticate.IntrospectResponse;
import com.sqc.sos.dto.authenticate.LoginRequest;
import com.sqc.sos.dto.authenticate.LoginResponse;
import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.dto.user.UserResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.mapper.IUserMapper;
import com.sqc.sos.model.User;
import com.sqc.sos.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticateService implements IAuthenticateService {
    IUserRepository userRepository;
    PasswordEncoder passwordEncoder;
    IUserMapper userMapper;

    @Value("${jwt.signerKey}")
    @NonFinal
    private String SIGNER_KEY;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String token = generateToken(user);
        log.info("User authenticated successfully: {}", request.getUsername());

        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(r -> r.getName()).toList();

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws ParseException, JOSEException {
        return IntrospectResponse.builder()
                .isValid(verifyJWT(introspectRequest.getToken()))
                .build();
    }

    @Override
    public UserResponse register(UserRequest userRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userMapper.userRequestToUser(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return userMapper.userToUserResponse(user);
    }

    // Method generateToken creates a JWT token with user information
    private String generateToken(User user) {
        // Create JWT header using HS512 (HMAC SHA-512) signing algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Create JWT claims (payload) containing user information
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Set JWT subject to user's login name
                .issuer("sqc.com") // Set JWT issuer to "sqc.com"
                .issueTime(new Date()) // Set JWT issue time to current time
                .expirationTime(new Date( // Set JWT expiration time to 1 hour from issue time
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                // Thêm một custom claim (thông tin tùy chỉnh) vào JWT, chứa thông tin về đối tượng Student
                .claim("scope", getRoles(user))
                .build(); // Xây dựng đối tượng JWTClaimsSet

        // Create payload from claims, convert claims object to JSON format
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // Create JWSObject from header and payload, combine them into JWS object
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Sign JWT using HMAC SHA-512 algorithm with secret key (SIGNER_KEY)
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

            // Convert JWS object to complete JWT string (header.payload.signature) and return
            return jwsObject.serialize();
        } catch (JOSEException e) {
            // If error occurs during JWT signing, throw RuntimeException
            throw new RuntimeException(e);
        }
    }

    // Method verifyJWT checks JWT token validity and authenticates it
    public boolean verifyJWT(String token)
            throws JOSEException, ParseException {
        // Create JWSVerifier object with HMAC SHA-512 algorithm to verify JWT signature
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // Parse JWT string into SignedJWT object
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Get JWT expiration time from claims (payload)
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Verify JWT signature, check if signature is valid
        var verified = signedJWT.verify(verifier);

        // Return authentication result:
        return verified && expiryTime.after(new Date());
    }

    public String getRoles(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        user.getRoles().forEach(role -> stringJoiner.add(role.getName()));

        return stringJoiner.toString();
    }
}
