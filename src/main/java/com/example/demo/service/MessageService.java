package com.example.demo.service;

import com.example.demo.*;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.PrivateMessageRepository;
import com.example.demo.repositories.RoomRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository,
                          PrivateMessageRepository privateMessageRepository, UserRepository userRepository,
                          RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Message createMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setAttachmentUrl(messageDTO.getAttachmentUrl());
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
        message.setAttachmentUrl(messageDTO.getAttachmentUrl());
        setUserAndRoom(message, messageDTO);
        return messageRepository.save(message);
    }


    private void setUserAndRoom(Message message, MessageDTO messageDTO) {
        Users users = userRepository.findById(messageDTO.getUserDTO().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + messageDTO.getMessageId()));
        message.setUsers(users);

        Room room = roomRepository.findById(messageDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + messageDTO.getRoomId()));
        message.setRoom(room);
    }

    public List<PrivateMessageDTO> getPrivateMessages(String currentUsername, String recipientUsername) {
        Users sender = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        Users recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        List<PrivateMessage> messages = privateMessageRepository
                .findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(sender, recipient, recipient, sender);

        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public PrivateMessageDTO sendPrivateMessage(PrivateMessageDTO messageDTO, String senderUsername) {
        Users sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        Users recipient = userRepository.findById(messageDTO.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        PrivateMessage message = new PrivateMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(messageDTO.getContent());

        privateMessageRepository.save(message);

        PrivateMessageDTO response = new PrivateMessageDTO(
                recipient.getId(),
                sender.getUsername(),
                recipient.getUsername(),
                message.getContent(),
                message.getTimestamp().toString()
        );

        // Отправляем WebSocket-сообщение конкретному получателю
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(), "/queue/messages", response
        );

        return response;
    }

    public void deletePrivateMessage(Long messageId, String username) {
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Проверяем, является ли пользователь отправителем сообщения
        if (!message.getSender().getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not authorized to delete this message");
        }

        privateMessageRepository.deleteById(messageId);
    }

    public List<PrivateMessageDTO> getFilteredMessages(String filter) {
        if (filter == null || filter.isEmpty()) {
            return privateMessageRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .toList();
        }

        Specification<PrivateMessage> spec = createSpecificationFromFilter(filter);
        return privateMessageRepository.findAll(spec).stream()
                .map(this::convertToDTO)
                .toList();
    }

    private Specification<PrivateMessage> createSpecificationFromFilter(String filter) {
        return (Root<PrivateMessage> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String[] conditions = filter.split(" AND ");
            for (String condition : conditions) {
                if (condition.contains(" eq ")) {
                    String[] parts = condition.split(" eq ");
                    predicates.add(cb.equal(root.get(parts[0].trim()), parts[1].trim().replace("'", "")));
                } else if (condition.contains(" ct ")) {
                    String[] parts = condition.split(" ct ");
                    predicates.add(cb.like(root.get(parts[0].trim()), "%" + parts[1].trim().replace("'", "") + "%"));
                } else if (condition.contains(" gt ")) {
                    String[] parts = condition.split(" gt ");
                    predicates.add(cb.greaterThan(root.get(parts[0].trim()), parts[1].trim().replace("'", "")));
                } else if (condition.contains(" lt ")) {
                    String[] parts = condition.split(" lt ");
                    predicates.add(cb.lessThan(root.get(parts[0].trim()), parts[1].trim().replace("'", "")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PrivateMessageDTO convertToDTO(PrivateMessage message) {
        return new PrivateMessageDTO(
                message.getRecipient().getId(),
                message.getSender().getUsername(),
                message.getRecipient().getUsername(),
                message.getContent(),
                message.getTimestamp().atOffset(java.time.ZoneOffset.UTC).toString()
        );
    }
}
