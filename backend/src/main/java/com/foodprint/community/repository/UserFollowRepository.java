package com.foodprint.community.repository;

import java.util.UUID;

import com.foodprint.community.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    Optional<UserFollow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    List<UserFollow> findByFollowerId(UUID followerId);
    List<UserFollow> findByFolloweeId(UUID followeeId);
}
