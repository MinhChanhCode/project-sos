package com.sqc.sos;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@SpringBootTest
class StudentApplicationTests {
	@Value("${jwt.signerKey}")
	private String SIGNER_KEY;

	@Test
	public void getToken() throws ParseException, JOSEException {
//		String token = generateToken("QuangNN");
//
//		System.out.println("Token: " + token);
		System.out.println("Check token: " + verifyJWT("eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJzcWMuY29tIiwic3ViIjoiS2hhbmdMRCIsImV4cCI6MTc0ODUyNTc1NCwic3R1ZGVudCI6IlRoaSIsImlhdCI6MTc0ODUyMjE1NH0.BfaBdv9VOV8ZW1CC-GxoEMCeJSd1SL45DsK6zmAkPyLeadyyDP_QI0YzPHaTNkEm97nBdK_EwSV8s1bjFpk8Kg"));
	}

	// Method generateToken creates a JWT token with user information
	private String generateToken(String username) {
		// Create JWT header using HS512 (HMAC SHA-512) signing algorithm
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

		// Create JWT claims (payload) containing user information
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
				.subject(username) // Set JWT subject to user's login name
				.issuer("sqc.com") // Set JWT issuer to "sqc.com"
				.issueTime(new Date()) // Set JWT issue time to current time
				.expirationTime(new Date( // Set JWT expiration time to 1 hour from issue time
						Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
				))
				// Add a custom claim containing Student object information
				.claim("student", "Thi")
				.build(); // Build JWTClaimsSet object

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
}