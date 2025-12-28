package com.example.demo.repositories;

import com.example.demo.PrivateMessage;
import com.example.demo.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <--- Не забудьте этот импорт
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long>, JpaSpecificationExecutor<PrivateMessage> {

    // Найти тех, кто писал мне
    @Query("SELECT DISTINCT m.sender FROM PrivateMessage m WHERE m.recipient.username = :username")
    List<Users> findSendersByRecipient(@Param("username") String username);

    // Найти тех, кому писал я
    @Query("SELECT DISTINCT m.recipient FROM PrivateMessage m WHERE m.sender.username = :username")
    List<Users> findRecipientsBySender(@Param("username") String username);

    // Наш умный метод для поиска переписки (Я->Он и Он->Я)
    @Query("SELECT p FROM PrivateMessage p WHERE " +
            "(p.sender = :user1 AND p.recipient = :user2) OR " +
            "(p.sender = :user2 AND p.recipient = :user1) " +
            "ORDER BY p.timestamp ASC")
    List<PrivateMessage> findChatHistory(@Param("user1") Users user1, @Param("user2") Users user2);
}