package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class AvatarUpdateDTO {
    @NotBlank
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
