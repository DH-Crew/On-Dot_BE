package com.dh.ondot.member.infra.jwt

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.core.AppleProperties
import com.dh.ondot.member.core.exception.ApplePrivateKeyLoadFailedException
import com.dh.ondot.member.core.exception.AppleSignatureInvalidException
import com.dh.ondot.member.core.exception.AppleUserParseFailedException
import com.dh.ondot.member.core.exception.OauthUserFetchFailedException
import com.dh.ondot.member.domain.dto.UserInfo
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.infra.dto.ApplePublicKeyResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.Date

/**
 *  Apple ID Token 서명 검증 & sub/email 추출
 *  내부에서 Apple Public Key을 조회해서 최종적으로 UserInfo를 반환
 */
@Component
class AppleJwtUtil(
    private val appleProperties: AppleProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper()

    fun generateClientSecret(): String {
        val expiration = TimeUtils.nowSeoulDateTime().plusMinutes(5)
        val clientSecret = Jwts.builder()
            .header()
                .add("alg", "ES256")
                .add("kid", appleProperties.keyId)
                .add("typ", "JWT")
                .and()
            .issuer(appleProperties.teamId)
            .issuedAt(Date.from(TimeUtils.nowSeoulInstant()))
            .expiration(Date.from(TimeUtils.toInstant(expiration)))
            .audience()
                .add(appleProperties.audience)
                .and()
            .subject(appleProperties.clientId)
            .signWith(getPrivateKey())
            .compact()

        log.info("Generated client_secret: {}", clientSecret)
        return clientSecret
    }

    /**
     * Apple JWT 서명에 사용할 비공개 키를 클래스패스에서 로드
     * @return Apple JWT 서명에 사용될 RSA 비공개 키
     */
    private fun getPrivateKey(): PrivateKey {
        try {
            val resource = ClassPathResource("auth/ApplePrivateKey.p8")
            InputStreamReader(resource.inputStream, StandardCharsets.UTF_8).use { reader ->
                PEMParser(reader).use { pemParser ->
                    val converter = JcaPEMKeyConverter()
                    val privateKeyInfo = pemParser.readObject() as PrivateKeyInfo
                    return converter.getPrivateKey(privateKeyInfo)
                }
            }
        } catch (e: Exception) {
            throw ApplePrivateKeyLoadFailedException()
        }
    }

    fun parseIdToken(appleKeys: ApplePublicKeyResponse, idToken: String): UserInfo {
        try {
            // id_token 헤더 파싱 (kid, alg)
            val tokenParts = idToken.split(".")
            val headerJson = String(Base64.getUrlDecoder().decode(tokenParts[0]))
            val headerMap: Map<String, String> = objectMapper.readValue(
                headerJson,
                object : TypeReference<Map<String, String>>() {},
            )

            // kid, alg에 맞는 PublicKey 찾기
            val matchedKey = appleKeys.getMatchedKey(headerMap["kid"]!!, headerMap["alg"]!!)
                ?: throw OauthUserFetchFailedException(OauthProvider.APPLE.name)

            // RSA PublicKey 구성
            val nBytes = Base64.getUrlDecoder().decode(matchedKey.n)
            val eBytes = Base64.getUrlDecoder().decode(matchedKey.e)
            val n = BigInteger(1, nBytes)
            val e = BigInteger(1, eBytes)

            val publicKeySpec = RSAPublicKeySpec(n, e)
            val keyFactory = KeyFactory.getInstance(matchedKey.kty)
            val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

            val claims = parseJwt(idToken, publicKey)
            val sub = claims.get("sub", String::class.java)
            val email = claims.get("email", String::class.java)

            if (sub == null || email == null) {
                throw AppleUserParseFailedException()
            }

            return UserInfo(sub, email)
        } catch (ex: Exception) {
            throw OauthUserFetchFailedException(OauthProvider.APPLE.name)
        }
    }

    private fun parseJwt(token: String, publicKey: PublicKey): Claims {
        try {
            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: SignatureException) {
            throw AppleSignatureInvalidException()
        } catch (e: Exception) {
            throw AppleUserParseFailedException()
        }
    }
}
