package com.blood_bank.auth_service.security;

import com.blood_bank.auth_service.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiry}")
    private long expiry;

    private Key getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(Long userId, Role role){
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role",role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expiry))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expiry * 7))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUserId(String token){
        return extractClaims(token).getSubject();
    }

   public String extractRole(String token){
        return extractClaims(token).get("role",String.class);
   }

   public boolean isTokenExpired(String token){
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
   }

}
