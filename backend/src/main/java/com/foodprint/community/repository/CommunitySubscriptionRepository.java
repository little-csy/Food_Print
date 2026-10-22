package com.foodprint.community.repository;

import java.util.UUID;

import com.foodprint.community.entity.CommunitySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunitySubscriptionRepository extends JpaRepository<CommunitySubscription, UUID> {
    List<CommunitySubscription> findByUserId(UUID userId);
    Optional<CommunitySubscription> findByUserIdAndTopicName(UUID userId, String topicName);
}
