package com.foodprint.challenge.repository;

import com.foodprint.challenge.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, UUID> {

    /** All participation records for a user, across all statuses */
    List<UserChallenge> findByUserId(UUID userId);

    /** Participation records filtered by status (e.g. "IN_PROGRESS") */
    List<UserChallenge> findByUserIdAndStatus(UUID userId, String status);

    /** Lookup by id + userId to prevent cross-user access */
    Optional<UserChallenge> findByIdAndUserId(UUID id, UUID userId);

    /** Lookup by id + profileId to prevent cross-user access (Supabase JWT sub == profileId) */
    Optional<UserChallenge> findByIdAndProfileId(UUID id, UUID profileId);

    /**
     * Look up any existing record for this profile + challenge regardless of status.
     * Used by joinChallenge upsert logic to reset abandoned/completed runs.
     */
    Optional<UserChallenge> findByProfileIdAndChallengeId(UUID profileId, UUID challengeId);
}
