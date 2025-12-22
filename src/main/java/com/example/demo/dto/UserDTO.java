package com.example.demo.dto;

    public class UserDTO {
        private Long id;
        private String username;
        private String avatarUrl;

    public UserDTO(Long id, String username, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

