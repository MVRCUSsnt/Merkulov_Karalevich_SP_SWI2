package com.example.demo.service;

import com.example.demo.*;
import com.example.demo.dto.MessageDTO;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Message createMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        setUserAndRoom(message, messageDTO);
        return messageRepository.save(message);
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
    }

    public List<Message> getAllMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findAll(pageable).getContent();
    }

    public Message updateMessage(Long id, MessageDTO messageDTO) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        message.setContent(messageDTO.getContent());
        setUserAndRoom(message, messageDTO);
        return messageRepository.save(message);
    }

    public void deleteMessage(Long id, Principal principal) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));

        // Проверка на право удаления
        if (!message.getUsers().getUsername().equals(principal.getName())) {
            throw new IllegalArgumentException("You are not authorized to delete this message");
        }

        messageRepository.deleteById(id);
    }

    private void setUserAndRoom(Message message, MessageDTO messageDTO) {
        Users users = userRepository.findById(messageDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + messageDTO.getUserId()));
        message.setUsers(users);

        Room room = roomRepository.findById(messageDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + messageDTO.getRoomId()));
        message.setRoom(room);
    }
}
