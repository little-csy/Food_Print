package com.foodprint.community.repository;

import java.util.UUID;

import com.foodprint.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    // Check if user liked the post
    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    // Count likes for a post
    long countByPostId(UUID postId);

    /** Delete all likes associated with a post */
    void deleteByPostId(UUID postId);
}