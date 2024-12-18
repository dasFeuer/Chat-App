package com.barun.ChatApp.repositories;

import com.barun.ChatApp.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrderByTimestampAsc(Long senderId, Long receiverId);

    List<ChatMessage> findByReceiverIdAndSenderIdNotOrderByTimestampAsc(Long receiverId, Long senderId);
}

