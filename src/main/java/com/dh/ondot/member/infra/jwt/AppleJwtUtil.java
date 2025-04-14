package com.dh.ondot.member.infra.jwt;

import com.dh.ondot.member.core.AppleProperties;
import com.dh.ondot.member.core.exception.ApplePrivateKeyLoadFailedException;
import com.dh.ondot.member.core.exception.AppleSignatureInvalidException;
import com.dh.ondot.member.core.exception.AppleUserParseFailedException;
import com.dh.ondot.member.core.exception.OauthUserFetchFailedException;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.infra.dto.ApplePublicKeyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 *  Apple ID Token 서명 검증 & sub/email 추출
 *  내부에서 Apple Public Key을 조회해서 최종적으로 UserInfo를 반환
 */
@Component
@RequiredArgsConstructor
public class AppleJwtUtil {
    private final AppleProperties appleProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateClientSecret() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        return Jwts.builder()
                .header()
                    .add("alg", "ES256")
                    .add("kid", appleProperties.keyId())
                    .and()
                .issuer(appleProperties.teamId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .audience()
                    .add(appleProperties.audience())
                    .and()
                .subject(appleProperties.clientId())
                .signWith(getPrivateKey())
                .compact();
    }

    /**
     * Apple JWT 서명에 사용할 비공개 키를 클래스패스에서 로드
     * @return Apple JWT 서명에 사용될 RSA 비공개 키
     */
    private PrivateKey getPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("auth/ApplePrivateKey.p8");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 PEMParser pemParser = new PEMParser(reader)
            ) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();

                return converter.getPrivateKey(privateKeyInfo);
            }
        } catch (Exception e) {
            throw new ApplePrivateKeyLoadFailedException();
        }
    }

    public UserInfo parseIdToken(ApplePublicKeyResponse appleKeys, String idToken) {
        try {
            // id_token 헤더 파싱 (kid, alg)
            String[] tokenParts = idToken.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, String> headerMap = objectMapper.readValue(headerJson, Map.class);

            // kid, alg에 맞는 PublicKey 찾기
            ApplePublicKeyResponse.Key matchedKey = appleKeys
                    .getMatchedKey(headerMap.get("kid"), headerMap.get("alg"))
                    .orElseThrow(() -> new OauthUserFetchFailedException(OauthProvider.APPLE.name()));

            // RSA PublicKey 구성
            byte[] nBytes = Base64.getUrlDecoder().decode(matchedKey.n());
            byte[] eBytes = Base64.getUrlDecoder().decode(matchedKey.e());
            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance(matchedKey.kty());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Claims claims = parseJwt(idToken, publicKey);
            String sub = claims.get("sub", String.class);
            String email = claims.get("email", String.class);

            if (sub == null || email == null) {
                throw new AppleUserParseFailedException();
            }

            return new UserInfo(sub, email);
        } catch (Exception ex) {
            throw new OauthUserFetchFailedException(OauthProvider.APPLE.name());
        }
    }

    private Claims parseJwt(String token, PublicKey publicKey) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            throw new AppleSignatureInvalidException();
        } catch (Exception e) {
            throw new AppleUserParseFailedException();
        }
    }
}
