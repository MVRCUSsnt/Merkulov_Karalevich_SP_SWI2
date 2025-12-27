package com.example.demo.dto;

public class RoomNotificationDTO {
    private Long roomId;
    private Long messageId;
    private String content;
    private String timestamp;
    private Long senderId;
    private String senderUsername;

    public RoomNotificationDTO(Long roomId, Long messageId, String content, String timestamp, Long senderId, String senderUsername) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.content = content;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
}