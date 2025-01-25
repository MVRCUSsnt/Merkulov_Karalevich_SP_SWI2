package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.MessageRepository;
import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.MessageDTO;
import com.example.demo.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    private MessageController messageController;
    private MessageRepository messageRepository;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        messageController = new MessageController(messageRepository, messageService);
    }

    @Test
    void testGetMessagesByRoomId() {
        // Arrange
        Room mockRoom = new Room();
        mockRoom.setId(1L);

        Users mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setAvatarUrl("avatar.png");

        Message message1 = new Message();
        message1.setId(1L);
        message1.setContent("Hello");
        message1.setRoom(mockRoom);
        message1.setUsers(mockUser);
        message1.setTimestamp(LocalDateTime.now());

        Message message2 = new Message();
        message2.setId(2L);
        message2.setContent("Hi");
        message2.setRoom(mockRoom);
        message2.setUsers(mockUser);
        message2.setTimestamp(LocalDateTime.now());

        List<Message> messages = Arrays.asList(message1, message2);
        when(messageRepository.findByRoomId(anyLong())).thenReturn(messages);

        // Act
        List<MessageDTO> response = messageController.getMessages(1L);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Hello", response.get(0).getContent());
        assertEquals("Hi", response.get(1).getContent());
        verify(messageRepository, times(1)).findByRoomId(anyLong());
    }


    @Test
    void testSendMessage() {
        // Arrange
        Message mockMessage = new Message();
        mockMessage.setId(1L);
        mockMessage.setContent("Hello");
        mockMessage.setTimestamp(LocalDateTime.now());

        when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        // Act
        Message response = messageController.sendMessage(mockMessage);

        // Assert
        assertNotNull(response);
        assertEquals(mockMessage.getId(), response.getId());
        assertEquals(mockMessage.getContent(), response.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testHandleInvalidRoomId() {
        // Arrange
        when(messageRepository.findByRoomId(anyLong())).thenReturn(new ArrayList<>());

        // Act
        List<MessageDTO> response = messageController.getMessages(999L);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.size());
        verify(messageRepository, times(1)).findByRoomId(anyLong());
    }
}
