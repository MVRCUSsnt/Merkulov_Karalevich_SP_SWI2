package com.example.demo.controller;

import com.example.demo.Users;
import com.example.demo.dto.UserRegistrationDTO;
import com.example.demo.dto.AvatarUpdateDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CookieService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;
    private static final long COOKIE_EXPIRY = 7 * 24 * 60 * 60; // 7 дней

    public AuthController(UserService userService, JwtUtil jwtUtil, CookieService cookieService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cookieService = cookieService;
    }

    //  Авторизация и установка `HttpOnly` куки
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        // Аутентифицируем пользователя
        Users user = userService.authenticateAndGetUser(loginRequest);

        // Генерируем JWT-токен
        String token = jwtUtil.generateToken(user.getUsername());

        // Устанавливаем токен в HttpOnly куки
        cookieService.createAccessTokenCookie(response, token, COOKIE_EXPIRY);

        // Формируем JSON-ответ с ID и username
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", user.getId());
        responseBody.put("username", user.getUsername());
        responseBody.put("avatarUrl", user.getAvatarUrl());
        responseBody.put("email", user.getEmail());

        return ResponseEntity.ok(responseBody);
    }


    // Регистрация нового пользователя и установка `HttpOnly` куки
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO, HttpServletResponse response) {
        // Создание пользователя
        Users user = userService.createUser(userRegistrationDTO);

        // Генерация токена
        String token = jwtUtil.generateToken(user.getUsername());

        // Установка токена в HttpOnly куки
        cookieService.createAccessTokenCookie(response, token, COOKIE_EXPIRY);

        // Формируем JSON-ответ с ID и username
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", user.getId());
        responseBody.put("username", user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }


    // Проверка авторизации (`HttpOnly` куки) и получение текущего пользователя
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(value = "accessToken", required = false) String token) {
        if (token == null || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String username = jwtUtil.extractUsername(token);
        Users user = userService.getUserByUsername(username);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", user.getId());
        responseBody.put("username", user.getUsername());
        responseBody.put("avatarUrl", user.getAvatarUrl());
        responseBody.put("email", user.getEmail());

        return ResponseEntity.ok(responseBody);
    }

    @PutMapping("/avatar")
    public ResponseEntity<Users> updateAvatar(@RequestBody @Valid AvatarUpdateDTO avatarUpdateDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Users updatedUser = userService.updateAvatar(principal.getName(), avatarUpdateDTO.getAvatarUrl());
        return ResponseEntity.ok(updatedUser);
    }




    //  Выход (удаление `HttpOnly` куки)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Удаляем куки

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out");
    }

    //  Получение информации о пользователе по ID
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        Users user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        return ResponseEntity.ok(user);
    }

    //  Получение списка всех пользователей (с пагинацией)
    @GetMapping
    public ResponseEntity<List<Users>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    //  Обновление профиля пользователя
    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRegistrationDTO));
    }

    //  Удаление пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    //  Получение профиля текущего пользователя
    @GetMapping("/profile")
    public ResponseEntity<Users> getProfile(@CookieValue(value = "accessToken", required = false) String token) {
        if (token == null || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = jwtUtil.extractUsername(token);
        Users user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
