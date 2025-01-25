package com.example.demo.controller;

import com.example.demo.Room;
import com.example.demo.dto.RoomDTO;
import com.example.demo.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ChatControllerTest {

    private ChatController chatController;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        chatController = new ChatController(roomService);
    }
    @Test
    void testGetChatByIdSuccess() {
        // Arrange
        Room mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setName("Test Room");

        when(roomService.getRoomById(anyLong())).thenReturn(mockRoom);

        // Act
        ResponseEntity<Room> responseEntity = chatController.getChatById(1L);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockRoom, responseEntity.getBody());
        verify(roomService, times(1)).getRoomById(anyLong());
    }

    @Test
    void testGetAllChatsSuccess() {
        // Arrange
        Room mockRoom1 = new Room();
        mockRoom1.setId(1L);
        mockRoom1.setName("Room 1");

        Room mockRoom2 = new Room();
        mockRoom2.setId(2L);
        mockRoom2.setName("Room 2");

        List<Room> mockRooms = Arrays.asList(mockRoom1, mockRoom2);

        when(roomService.getAllRooms(anyInt(), anyInt())).thenReturn(mockRooms);

        // Act
        ResponseEntity<List<Room>> responseEntity = chatController.getAllChats(0, 10);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockRooms, responseEntity.getBody());
        verify(roomService, times(1)).getAllRooms(anyInt(), anyInt());
    }

    @Test
    void testUpdateChatSuccess() {
        // Arrange
        RoomDTO roomDTO = new RoomDTO("Updated Room","Description");
        Room mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setName("Updated Room");

        when(roomService.updateRoom(anyLong(), any(RoomDTO.class))).thenReturn(mockRoom);

        // Act
        ResponseEntity<Room> responseEntity = chatController.updateChat(1L, roomDTO);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockRoom, responseEntity.getBody());
        verify(roomService, times(1)).updateRoom(anyLong(), any(RoomDTO.class));
    }

    @Test
    void testDeleteChatSuccess() {
        // Arrange
        doNothing().when(roomService).deleteRoom(anyLong());

        // Act
        ResponseEntity<Void> responseEntity = chatController.deleteChat(1L);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(roomService, times(1)).deleteRoom(anyLong());
    }
}
