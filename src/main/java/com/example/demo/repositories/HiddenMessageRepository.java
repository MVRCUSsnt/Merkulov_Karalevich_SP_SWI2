package com.example.demo.repositories;

import com.example.demo.HiddenMessage;
import com.example.demo.Message;
import com.example.demo.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HiddenMessageRepository extends JpaRepository<HiddenMessage, Long> {
    List<HiddenMessage> findByUser(Users user);

    Optional<HiddenMessage> findByUserAndMessage(Users user, Message message);

    void deleteByMessage(Message message);
}