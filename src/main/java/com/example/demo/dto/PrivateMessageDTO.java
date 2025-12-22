package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PrivateMessageDTO {
    @NotNull
    private Long recipientId;
    private String senderUsername;
    private String recipientUsername;
    @NotBlank
    private String content;

    private String timestamp;

    public PrivateMessageDTO(Long recipientId, String senderUsername, String recipientUsername, String content, String timestamp) {
        this.recipientId = recipientId;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}


