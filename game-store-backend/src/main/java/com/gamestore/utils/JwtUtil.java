package com.gamestore.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gamestore.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(User user) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiration);
        
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole())
                .withIssuedAt(now)
                .withExpiresAt(expire)
                .sign(Algorithm.HMAC256(secret));
    }
    
    public DecodedJWT verify(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        return verifier.verify(token);
    }
    
    public Long getUserId(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userId").asLong();
    }
    
    public String getUsername(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("username").asString();
    }
    
    public Integer getRole(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("role").asInt();
    }
}
