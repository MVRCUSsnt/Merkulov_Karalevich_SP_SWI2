package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private static final long EXPIRATION_TIME = 86400000; // 24 часа

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    //  Генерация токена
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    //  Проверка и извлечение имени пользователя из токена
    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Token expired");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new RuntimeException("Invalid signature");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    //  Новый метод: Извлечение имени пользователя из токена (заменяет `validateToken`)
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    //  Проверка, истек ли токен
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    //  Общий метод для парсинга токена
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
