package com.showbook.back.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.showbook.back.dto.RefreshToken;
import com.showbook.back.security.dto.GeneratedToken;
import com.showbook.back.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


import java.util.Date;

import static com.showbook.back.common.constants.ErrorCode.UNAUTHORIZED_USER;

@Slf4j
@Component
public class JwtTokenUtil {

    private final String SECRET_KEY;
    private final Integer ACCESS_EXPIRATION_TIME;
    private final Integer REFRESH_EXPIRATION_TIME;

    public static final String PREFIX = "Bearer_"; // 띄어쓰기 있어야 한다
    private static final String ISSUER = "shook";
    public static final String HEADER_STRING = "Authorization";

    private final RefreshTokenService refreshTokenService;

    public JwtTokenUtil(@Value("${SECRET_KEY}") String SECRET_KEY,
                        @Value("${ACCESS_EXPIRATION_TIME}")Integer ACCESS_EXPIRATION_TIME,
                        @Value("${REFRESH_EXPIRATION_TIME}")Integer REFRESH_EXPIRATION_TIME,
                        RefreshTokenService refreshTokenService){
        this.SECRET_KEY = SECRET_KEY;
        this.ACCESS_EXPIRATION_TIME = ACCESS_EXPIRATION_TIME;
        this.REFRESH_EXPIRATION_TIME = REFRESH_EXPIRATION_TIME;
        this.refreshTokenService = refreshTokenService;
    }

    public GeneratedToken generateTokens(Long memberId) {
        String accessToken = createAccessToken(memberId);
        String refreshToken = createRefreshToken(memberId);

        refreshTokenService.saveTokenInfo(memberId,accessToken,refreshToken); // 토큰이 생성되면 redis에도 저장

        return  new GeneratedToken(accessToken,refreshToken);
    }

    public String createAccessToken(Long memberId) {
        String accessToken = JWT.create()
                .withSubject("accessToken") // 토큰 제목
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME)) // access Token 만료시간
                .withIssuer(ISSUER) // 발급자
                .withClaim("id",memberId)
                .sign(Algorithm.HMAC256(SECRET_KEY));

        return PREFIX + accessToken;
    }

    public String createRefreshToken(Long memberId) {
        String refreshToken =JWT.create()
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .withIssuer(ISSUER)
                .withClaim("id",memberId)
                .sign(Algorithm.HMAC256(SECRET_KEY));

        return PREFIX + refreshToken;
    }

    public boolean existsRefreshToken(String accessToken) {
        log.info("JwtTokenUtil - existsRefreshToken");
        RefreshToken token = refreshTokenService.findRefreshTokenByAccessToken(accessToken);
        if (token == null)
            return false;

        return true;

    }

    public boolean isTokenValid(String jwtToken) { // 토큰의 만료시간 검증
        try {
            DecodedJWT decodedJWT = verify(jwtToken);
            return decodedJWT != null && !decodedJWT.getExpiresAt().before(new Date());
        } catch (TokenExpiredException e) {
            log.error("JwtTokenUtil -> 토큰 시간이 만료됨! {}",e.getMessage());
            return false;
        }
    }


    public DecodedJWT verify(String token) {
        try {
            // HMAC256 알고리즘을 사용하여 JWT를 확인하기 위한 JWTVerifier를 생성
            token = token.replace(PREFIX, "");
            JWTVerifier jwtVerifier =JWT.require(Algorithm.HMAC256(SECRET_KEY)).withIssuer(ISSUER).build();
            return jwtVerifier.verify(token);
        } catch (Exception e) {
            log.error("JwtTokenUtil.verify에서 예외 - {}",e.getMessage());
            return null;
        }
    }

    public Long getMemberId(String token) {
        token = token.replace(PREFIX,"");
        DecodedJWT decodedJWT = verify(token);

        if (decodedJWT != null) {
            return decodedJWT.getClaim("id").asLong();
        } else {
            throw null;
        }
    }

    public String getEmail(String token) {
        token = token.replace(PREFIX,"");
        DecodedJWT decodedJWT = verify(token);

        if (decodedJWT != null) {
            return decodedJWT.getClaim("email").asString();
        } else {
            return null;
        }
    }

    public String getRole(String token) {
        token = token.replace(PREFIX,"");
        DecodedJWT decodedJWT = verify(token);

        if (decodedJWT != null) {
            return decodedJWT.getClaim("email").asString();
        } else {
            return null;
        }
    }



}
