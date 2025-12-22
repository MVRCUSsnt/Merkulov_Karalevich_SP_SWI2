package com.example.demo.repositories;

import com.example.demo.PrivateMessage;
import com.example.demo.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long>, JpaSpecificationExecutor<PrivateMessage> {
    List<PrivateMessage> findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(
            Users sender, Users recipient, Users recipient2, Users sender2
    );
}

