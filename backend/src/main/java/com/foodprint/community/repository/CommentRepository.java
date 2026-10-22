package com.foodprint.community.repository;
import java.util.UUID;

import com.foodprint.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Find comments by postId ordered by createdAt
    List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId);

    /** Delete all comments associated with a post */
    void deleteByPostId(UUID postId);
}