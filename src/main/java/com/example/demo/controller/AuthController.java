package com.example.demo.controller;

import com.example.demo.Users;
import com.example.demo.dto.UserRegistrationDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CookieService;
import com.example.demo.service.UserService;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.CookieService.*;


import java.security.Principal;
import java.util.List;

import static com.example.demo.service.CookieService.createAccessTokenCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private static final long COOKIE_EXPIRY = 7 * 24 * 60 * 60; // 7 дней
    private final CookieService cookieService;
    public CookieService getCookieService() {
        return cookieService;
    }


    public AuthController(UserService userService, JwtUtil jwtUtil, CookieService cookieService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cookieService = cookieService;
    }

    // Авторизация и генерация токена
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        // Аутентификация пользователя
        Users user = userService.authenticateAndGetUser(loginRequest);
        String token = jwtUtil.generateToken(user.getUsername());

        // Создание и установка cookie через cookieService
        createAccessTokenCookie(response, token, 3600);

        // Возвращаем имя пользователя как строку
        return ResponseEntity.ok(user.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO, HttpServletResponse response) {
        // Создание пользователя
        Users user = userService.createUser(userRegistrationDTO);

        // Генерация токена
        String token = jwtUtil.generateToken(user.getUsername());

        // Установка токена в cookie
        createAccessTokenCookie(response, token, COOKIE_EXPIRY);

        // Возврат имени пользователя (или другого необходимого ответа)
        return ResponseEntity.status(HttpStatus.CREATED).body(user.getUsername());
    }


    // Получение информации о пользователе по ID
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        Users user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        return ResponseEntity.ok(user);
    }

    // Получение списка всех пользователей
    @GetMapping
    public ResponseEntity<List<Users>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    // Обновление пользователя
    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRegistrationDTO));
    }

    // Удаление пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Получение профиля текущего пользователя
    @GetMapping("/profile")
    public ResponseEntity<Users> getProfile(Principal principal) {
        Users user = userService.getUserByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }
}
