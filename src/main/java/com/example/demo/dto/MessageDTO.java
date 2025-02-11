package com.example.demo.dto;

public class MessageDTO {
        private Long messageId;
        private Long roomId;
        private String content;

        private String timestamp;

        private UserDTO userDTO;

    public MessageDTO(Long messageId, Long roomId, String content, String timestamp, UserDTO userDTO) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.content = content;
        this.timestamp = timestamp;
        this.userDTO = userDTO;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }
}
