package com.example.demo.dto;

public class MessageDTO {
    private Long userId;
    private Long roomId;
    private String content;

    private String timestamp;

    private UserDTO userDTO;

    public MessageDTO(Long userId, Long roomId, String content, String timestamp, UserDTO userDTO) {
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
