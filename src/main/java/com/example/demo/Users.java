package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Set;

//@Getter
//@Setter
//@NoArgsConstructor
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "user_rooms", // Таблица связывания
            joinColumns = @JoinColumn(name = "user_id"), // Связь с таблицей users
            inverseJoinColumns = @JoinColumn(name = "room_id") // Связь с таблицей rooms
    )
    private Set<Room> rooms;

    public Set<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    @Column(nullable = false)
    private String password;


    @Column(nullable = true)
    private String avatarUrl; // Для аватара

    public Users() {
    }

    public Users(Long id, String username, String email, String password, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatarUrl = avatarUrl;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
