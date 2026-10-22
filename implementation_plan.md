# Challenge Module Stabilization & Test Fix Plan

This plan addresses the "Re-join" logic error in the Challenge module and fixes the `UnnecessaryStubbingException` blocking the build in the new test suite.

## User Review Required

> [!IMPORTANT]
> **Join Logic Change**: I will change `joinChallenge` from a "strict insert" to an "upsert-style" logic. If a user has previously abandoned or completed a challenge, re-joining will **reset** their existing record instead of creating a duplicate. This aligns with the database's unique constraint on `(profile_id, challenge_id)`.

> [!NOTE]
> **Test Strictness**: I will apply `lenient()` to security stubbing in the new tests. This resolves the `UnnecessaryStubbingException` without degrading test quality.

## Proposed Changes

### Challenge Module

#### [MODIFY] [ChallengeService.java](file:///Users/tianhengshi/FoodPrint/backend/src/main/java/com/foodprint/challenge/service/ChallengeService.java)
- Refactor `joinChallenge` to use `userChallengeRepository.findByProfileIdAndChallengeId`.
- If a record exists:
    - If `status.equals("IN_PROGRESS")`, throw `ALREADY_IN_PROGRESS`.
    - Otherwise, **reset** `startDate`, `endDate`, `completedDays`, `currentStreak`, and set `status = "IN_PROGRESS"`.
- Remove the `try-catch` block that hides the `DataIntegrityViolationException`.

#### [MODIFY] [UserChallengeRepository.java](file:///Users/tianhengshi/FoodPrint/backend/src/main/java/com/foodprint/challenge/repository/UserChallengeRepository.java)
- Add `Optional<UserChallenge> findByProfileIdAndChallengeId(UUID profileId, UUID challengeId)`.

### New Modules Alignment (Water & Exercise)

#### [MODIFY] [WaterLog.java](file:///Users/tianhengshi/FoodPrint/backend/src/main/java/com/foodprint/water/entity/WaterLog.java)
#### [MODIFY] [ExerciseLog.java](file:///Users/tianhengshi/FoodPrint/backend/src/main/java/com/foodprint/exercise/entity/ExerciseLog.java)
- Update `@Column(name = "user_id")` to `@Column(name = "profile_id")` to match the hardware-aligned Supabase schema.

### Test Suite Fixes (Mockito Strictness)

#### [MODIFY] [ExerciseLogServiceTest.java](file:///Users/tianhengshi/FoodPrint/backend/src/test/java/com/foodprint/exercise/service/ExerciseLogServiceTest.java)
#### [MODIFY] [WaterLogServiceTest.java](file:///Users/tianhengshi/FoodPrint/backend/src/test/java/com/foodprint/water/service/WaterLogServiceTest.java)
#### [MODIFY] [ChallengeServiceTest.java](file:///Users/tianhengshi/FoodPrint/backend/src/test/java/com/foodprint/challenge/service/ChallengeServiceTest.java)
- Update code in `mockAuth` or setup to use `lenient().when(...)` for security context stubbing to prevent build failures during validation-focused tests.

## Verification Plan

### Automated Tests
- Run `mvn test` specifically for the affected modules.
- Expected: All 11 currently failing tests should pass.

### Manual Verification
1. **Join Challenge**: Join a challenge for the first time -> Success.
2. **Abandon**: Abandon that challenge -> Success (status becomes ABANDONED).
3. **Re-join**: Click Join again on the same challenge -> Success (status becomes IN_PROGRESS again).
4. **Check-in**: Perform a check-in on the re-joined challenge -> Success.
