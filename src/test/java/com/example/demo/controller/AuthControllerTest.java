/*
package com.example.demo.controller;

import com.example.demo.Users;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserRegistrationDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CookieService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthController authController;
    private UserService userService;
    private JwtUtil jwtUtil;
    private CookieService cookieService;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtUtil = mock(JwtUtil.class);
        cookieService = mock(CookieService.class);
        response = mock(HttpServletResponse.class);
        authController = new AuthController(userService, jwtUtil, cookieService);
    }

    @Test
    void testRegisterSuccess() {
        // Arrange
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("testuser", "testuser@example.com", "password123");
        Users mockUser = new Users();
        mockUser.setUsername("testuser");

        when(userService.createUser(any(UserRegistrationDTO.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("mockToken");

        // Act
        ResponseEntity<String> responseEntity = authController.register(userRegistrationDTO, response);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("testuser", responseEntity.getBody());
        verify(userService, times(1)).createUser(any(UserRegistrationDTO.class));
        verify(jwtUtil, times(1)).generateToken("testuser");
        verify(cookieService, times(1));
    }

    @Test
    void testRegisterFailValidation() {
        // Arrange
        UserRegistrationDTO invalidUser = new UserRegistrationDTO("", "invalidemail", "");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> authController.register(invalidUser, response));
        assertNotNull(exception);
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        Users mockUser = new Users();
        mockUser.setUsername("testuser");

        when(userService.authenticateAndGetUser(any(LoginRequest.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("mockToken");

        // Act
        ResponseEntity<String> responseEntity = authController.login(loginRequest, response);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("testuser", responseEntity.getBody());
        verify(userService, times(1)).authenticateAndGetUser(any(LoginRequest.class));
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testLoginFailInvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("invaliduser", "wrongpassword");

        when(userService.authenticateAndGetUser(any(LoginRequest.class))).thenThrow(new IllegalArgumentException("Invalid credentials"));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> authController.login(loginRequest, response));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        ResponseEntity<String> responseEntity = handler.handleResourceNotFoundException(exception);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("User not found", responseEntity.getBody());
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        ResponseEntity<String> responseEntity = handler.handleGenericException(exception);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("Unexpected error"));
    }
}
*/
