package com.foodprint.community.repository;

import java.util.UUID;

import com.foodprint.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // Find posts by userId
    List<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Find all posts ordered by createdAt desc for feed
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllOrderByCreatedAtDesc();

    // Paginated feed
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findPostsForFeed(@Param("offset") int offset, @Param("limit") int limit);

    // Find following feed
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds);
}