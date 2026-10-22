package com.foodprint.challenge.repository;

import com.foodprint.challenge.entity.ChallengeCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeCheckinRepository extends JpaRepository<ChallengeCheckin, UUID> {

    /** All check-ins for a user challenge, sorted oldest first (for history display) */
    List<ChallengeCheckin> findByUserChallengeIdOrderByCheckinDateAsc(UUID userChallengeId);

    /** All check-ins for a user challenge, sorted newest first (for streak calculation) */
    List<ChallengeCheckin> findByUserChallengeIdOrderByCheckinDateDesc(UUID userChallengeId);

    /** Fetch the check-in record for a specific date, if it exists */
    Optional<ChallengeCheckin> findByUserChallengeIdAndCheckinDate(UUID userChallengeId, LocalDate checkinDate);

    /** Returns true if the user has already checked in on the given date */
    boolean existsByUserChallengeIdAndCheckinDate(UUID userChallengeId, LocalDate checkinDate);

    /** Deletes all check-in records for a given user challenge — used when the user resets a challenge run */
    void deleteAllByUserChallengeId(UUID userChallengeId);
}
