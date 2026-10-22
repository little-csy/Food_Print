package com.foodprint.ai.repository;

import com.foodprint.ai.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieve the most recent chat messages for a user, ordered by time
     * descending.
     */
    List<ChatMessage> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
