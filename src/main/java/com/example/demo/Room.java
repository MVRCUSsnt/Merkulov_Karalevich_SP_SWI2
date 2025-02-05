package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToMany(mappedBy = "rooms")
    @JsonIgnore
    private Set<Users> users = new HashSet<>();

    private String description;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    @JsonIgnore
    private Users creator;

    public Room(Long id, String name, Set<Users> users, String description, Users creator) {
        this.id = id;
        this.name = name;
        this.users = users;
        this.description = description;
        this.creator = creator;
    }

    public Room() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Users> getUsers() {
        return users;
    }

    public void setUsers(Set<Users> users) {
        this.users = users;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Users getCreator() {
        return creator;
    }

    public void setCreator(Users creator) {
        this.creator = creator;
    }

}