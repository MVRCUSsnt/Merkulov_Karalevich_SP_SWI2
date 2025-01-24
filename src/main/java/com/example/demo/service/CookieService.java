package com.example.demo.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    public static void createAccessTokenCookie(HttpServletResponse response, String token, long cookieExpiry) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // true для HTTPS
                .path("/")
                .maxAge(cookieExpiry)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
//    public ResponseCookie setCookie (String token, long expiry) {
//        return ResponseCookie.from("accessToken", token)
//                .httpOnly(true)
//                .secure(false) // Включить true при продакшене?
//                .path("/")
//                .maxAge(expiry)
//                .build();
//    }
}

