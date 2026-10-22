# FoodPrint Backend API Reference (Frontend Guide)

This document summarises all available backend APIs to help the frontend team integrate features.
Keep this file as the single source of truth — updated whenever the backend changes.

> **Auth note:** Every endpoint except `GET /api/challenges` requires
> `Authorization: Bearer <supabase-jwt>` in the request header.
> The backend resolves the user identity from the JWT automatically.
> **Never send a `userId` header** — that mechanism was removed in a previous release.

---

## 1. Authentication
```
Header: Authorization: Bearer <your-supabase-jwt-token>
```
All IDs returned and expected by the API are UUID strings (e.g. `"3fa85f64-5717-4562-b3fc-2c963f66afa6"`).
All responses use the unified envelope:
```json
{ "code": 200, "message": "...", "data": { ... } }
```
Errors use the same shape with `"code": 400` or `"code": 500`.

---

## 2. AI Module (`/api/ai`)
- **`POST /api/ai/analyze`**
  - Body: `{ "text": "optional", "imageUrl": "optional" }`
  - Returns AI food analysis. Pass the result to `/api/ai/log` to save it.
- **`POST /api/ai/log`**
  - Body: `{ "title": "str", "mealTime": "ISO-DateTime", "analysisResult": { ... } }`
  - One-click save for AI results. The user may edit calories/macros in the UI before sending.

---

## 3. User & Profile (`/api/users`)
- **`GET /api/users/me`** — Current user profile and body metrics.
- **`PUT /api/users/me`** — Update nickname and avatar URL.
- **`PUT /api/users/me/body-data`** — Update weight, height, age, gender, goal, `dailyCalorieTarget`, dietary preference.
- **`GET /api/users/me/stats`** — Total active days, goals met, achievements. Use for the Profile page header.

---

## 4. Food Logging (`/api/food-logs`)
- **`POST /api/food-logs/manual`**
  - Body: `{ "title": "str", "mealTime": "ISO-DateTime", "foods": [{ "nameEn": "...", "calories": 500, ... }] }`
  - Manual entry without AI.
- **`GET /api/food-logs/weekly-overview`** — Last 7 days. Each day: `{ date, calories, protein, fat, carbs }`.
- **`GET /api/food-logs/today`** — Today's meals for the Home screen.
  - Response item: `{ id, title, foodItems: string[], totalCalories, mealTime }`

---

## 5. Water Logging (`/api/water-logs`) ⬅ NEW

Track daily water intake. The default goal is **2000 ml per day**.

### POST `/api/water-logs` — Log a water entry
**Request body:**
```json
{
  "amountMl": 500.0,
  "note": "morning glass"   // optional
}
```
**Response `data`:**
```json
{
  "totalMl": 500.0,
  "totalCups": 2.1,
  "goalMl": 2000.0,
  "percentAchieved": 25,
  "logs": [
    { "id": "uuid", "amountMl": 500.0, "note": "morning glass", "loggedAt": "2026-04-16T08:00:00" }
  ]
}
```
- `percentAchieved` is capped at 100.
- `totalCups` uses the standard 237 ml per cup conversion.
- Returns 400 if `amountMl ≤ 0`.

### GET `/api/water-logs/today` — Today's water summary
Same `data` shape as the POST response above.

---

## 6. Exercise Logging (`/api/exercise-logs`) ⬅ NEW

Track daily exercise sessions and calories burned.

### POST `/api/exercise-logs` — Log an exercise session
**Request body:**
```json
{
  "activityType": "Running",
  "caloriesBurned": 300.0,
  "durationMin": 30,         // optional (Integer)
  "note": "morning jog"      // optional
}
```
**Response `data`:**
```json
{
  "totalCaloriesBurned": 300.0,
  "totalDurationMin": 30,
  "exercises": [
    {
      "id": "uuid",
      "activityType": "Running",
      "caloriesBurned": 300.0,
      "durationMin": 30,
      "note": "morning jog",
      "loggedAt": "2026-04-16T07:30:00"
    }
  ]
}
```
- Returns 400 if `activityType` is blank or `caloriesBurned ≤ 0`.

### GET `/api/exercise-logs/today` — Today's exercise summary
Same `data` shape as the POST response above.

---

## 7. Community & Feed (`/api/community`)
- **`GET /api/community/impact-metrics`** — Global leaderboard metrics.
- **`POST /api/community/posts`** — Create a post.
- **`GET /api/community/posts`** — Global public feed (paginated). Includes `authorNickname` and `authorAvatarUrl`.
- **`POST /api/community/posts/{id}/comments`** — Reply to a post.
- **`POST /api/community/posts/{id}/like`** / **`DELETE ...`** — Like / unlike.
- **`POST /api/community/users/{userId}/follow`** / **`DELETE ...`** — Follow / unfollow.
- **`GET /api/community/posts/following`** — Feed of posts from followed users only.
- **`POST /api/community/topics/{topicName}/join`** — Subscribe to a topic (e.g. `"GREEN_LIFE"`).
- **`GET /api/community/my-topics`** — Topics the current user has joined.
- **`DELETE /api/community/posts/{id}`** ⬅ NEW
  - Delete a post you own. Cascades to delete all comments/likes on that post.
  - Returns 403 if you are not the owner.
- **`DELETE /api/community/comments/{id}`** ⬅ NEW
  - Delete a comment you own.
  - Returns 403 if you are not the owner.

---

## 8. Challenge Module (`/api/challenges`) ⬅ SIGNIFICANTLY UPDATED

Full lifecycle for health challenges. Two new challenge types have been added, existing responses carry richer progress data, and a read-only status preview endpoint has been added.

### Challenge types now available

| `type`             | Title              | Goal                          | Duration |
|--------------------|--------------------|-------------------------------|----------|
| `CALORIE_CONTROL`  | Calorie Guardian   | Stay within 80–110% of personal calorie target | 7 days |
| `PROTEIN_CHAMPION` | Protein Champion   | ≥ 50 g protein/day            | 14 days  |
| `LOG_STREAK`       | Food Diary Hero    | ≥ 1 food log entry/day        | 7 days   |
| `LOW_CARB`         | Low-Carb Warrior   | ≤ 150 g carbs/day             | 14 days  |
| `LIGHT_EATER`      | Light Eater        | ≤ 1500 kcal/day (with ≥1 log) | 7 days   |
| `DAILY_WATER` ⬅ NEW | Hydration Hero     | ≥ 2000 ml water/day           | 7 days   |
| `CALORIE_BURN` ⬅ NEW | Active Lifestyle  | ≥ 200 kcal burned via exercise/day | 7 days |

> For `DAILY_WATER` check-ins to pass, the user must log water via `POST /api/water-logs` **before** calling checkin.
> For `CALORIE_BURN` check-ins to pass, the user must log exercise via `POST /api/exercise-logs` **before** calling checkin.

---

### GET `/api/challenges` — List all challenge templates (public, no auth required)
**Response `data`:** array of:
```json
{
  "id": "uuid",
  "title": "Hydration Hero",
  "description": "Drink at least 2000 ml...",
  "type": "DAILY_WATER",
  "durationDays": 7,
  "targetValue": 2000.0,
  "badgeIcon": "💧"
}
```

---

### POST `/api/challenges` — Create a custom challenge template ⬅ NEW
Auth required. Creates a brand new challenge template that users can join.

**Request body:**
```json
{
  "title": "My Custom Challenge",
  "description": "Do something awesome every day",
  "type": "MY_CUSTOM_TYPE",
  "duration_days": 10,
  "target_value": 500.0,
  "badge_icon": "⭐"
}
```
- `title` and `type` are required. `type` must be unique across all challenges.
- `duration_days` must be >= 1.
- `target_value` defaults to `0.0` if omitted.
- `badge_icon` defaults to `"🏆"` if omitted.

**Response `data`:** same shape as above (the created challenge template with its new `id`).

- Returns 400 (`DUPLICATE_TYPE`) if a challenge with the same `type` already exists.
- Returns 400 (`INVALID_REQUEST`) if required fields are missing or `duration_days <= 0`.

---

### POST `/api/challenges/{challengeId}/join` — Join a challenge
- Use the `id` from `GET /api/challenges`.
- **Re-join is supported.** If the user previously completed or abandoned this challenge, calling join again resets their progress (`completedDays`, `currentStreak`, dates) and sets the status back to `IN_PROGRESS`. No duplicate record is created.
- Returns 400 (`ALREADY_IN_PROGRESS`) if the user already has an active run of this challenge.

**Response `data`:** `UserChallengeResponse` (see schema below).

---

### GET `/api/challenges/my` — All user challenge records (all statuses)
**Response `data`:** array of `UserChallengeResponse` (without check-in history).

---

### GET `/api/challenges/my/active` — In-progress challenges only ⬅ ENRICHED
Returns only `IN_PROGRESS` challenges, each enriched with today's check-in status.
Use this to render the home-screen active challenge cards.

**Response `data`:** array of `UserChallengeResponse` (see schema below, `todayCheckedIn` / `todayPassed` are populated).

---

### GET `/api/challenges/my/{userChallengeId}` — Full detail for one challenge run
Includes the complete day-by-day check-in history sorted oldest first.

***Notice: userChallengeId is different from challengeId, it is a unique id for which user joined which challenge***

**Response `data`:** `UserChallengeResponse` with `checkins` array populated.

---

### GET `/api/challenges/my/{userChallengeId}/today-status` — Read-only today preview ⬅ UPDATED
Returns the user's current metric value and goal for today without creating a check-in record.
Use this to display a live progress bar or "ready to check in?" state on the challenge detail screen.

> **Now always includes `nutritionSnapshot`** with live data from food/water/exercise logs, even if already checked in.

**Response `data`:**
```json
{
  "checkedIn": false,
  "passed": null,
  "currentValue": 1200.0,
  "targetValue": 2000.0,
  "summary": "Today's water intake: 1200 ml (target: 2000 ml, ~5.1 cups) ❌",
  "daysRemaining": 5,
  "currentStreak": 2,
  "progressPercent": 42,
  "nutritionSnapshot": {
    "calories": 900.0,
    "protein_g": 40.0,
    "fat_g": 30.0,
    "carbs_g": 110.0,
    "fiber_g": 8.0,
    "water_ml": 1200.0,
    "calories_burned": 0.0,
    "food_log_count": 1
  }
}
```
- `passed` is `null` when not yet checked in, `true` or `false` once checked in.
- `currentValue` reflects the challenge-specific metric (water, burned calories, protein, etc.).
- `nutritionSnapshot` reflects **live** logged data and is always present.
- `progressPercent` = `completedDays / durationDays × 100`, capped at 100.

---

### POST `/api/challenges/my/{userChallengeId}/checkin` — Daily check-in
The backend reads today's food / water / exercise logs and evaluates the challenge criteria automatically.
**No request body needed.**

> **URL alias:** `POST /api/challenges/{userChallengeId}/checkin` (without `/my/`) is also supported for backward compatibility.

**Response `data`:**
```json
{
  "date": "2026-04-16",
  "passed": true,
  "actualValue": 2500.0,
  "note": "Today's water intake: 2500 ml (target: 2000 ml, ~10.5 cups) ✅",
  "completedDays": 3,
  "currentStreak": 3,
  "challengeCompleted": false,
  "message": "🔥 3-day streak! Only 4 more days to finish — keep it up!",
  "nutritionSnapshot": {
    "calories": 1850.0,
    "protein_g": 72.5,
    "fat_g": 55.0,
    "carbs_g": 230.0,
    "fiber_g": 18.0,
    "water_ml": 2500.0,
    "calories_burned": 320.0,
    "food_log_count": 3
  }
}
```
- `nutritionSnapshot` contains today's **full** nutrition and activity data — always populated regardless of challenge type.
- The frontend may use any field from `nutritionSnapshot` to display additional progress (e.g. show protein on a PROTEIN_CHAMPION card, water on a DAILY_WATER card).
- Returns 400 (`ALREADY_CHECKED_IN`) if already checked in today.
- Returns 400 (`CHALLENGE_NOT_ACTIVE`) if the challenge is not `IN_PROGRESS`.
- Returns 400 (`CHALLENGE_EXPIRED`) if today is past the challenge end date.

---

### DELETE `/api/challenges/my/{userChallengeId}` — Abandon a challenge
Returns 400 (`CHALLENGE_NOT_ACTIVE`) if already completed or abandoned.

---

### `UserChallengeResponse` full schema ⬅ UPDATED FIELDS
```json
{
  "id": "uuid",
  "userId": "uuid",
  "profileId": "uuid",
  "challenge": {
    "id": "uuid",
    "title": "Hydration Hero",
    "description": "...",
    "type": "DAILY_WATER",
    "durationDays": 7,
    "targetValue": 2000.0,
    "badgeIcon": "💧"
  },
  "status": "IN_PROGRESS",
  "startDate": "2026-04-16",
  "endDate": "2026-04-22",
  "completedDays": 2,
  "currentStreak": 2,
  "progressPercent": 28,
  "daysRemaining": 5,
  "todayCheckedIn": false,
  "todayPassed": null,
  "joinedAt": "2026-04-16T09:00:00",
  "completedAt": null,
  "checkins": []
}
```

**New / changed fields compared to previous version:**

| Field | Type | Description |
|-------|------|-------------|
| `progressPercent` | `int` (0–100) | `completedDays / durationDays × 100`. Use for progress bars. |
| `daysRemaining` | `int` | Calendar days until `endDate`. 0 = last day, negative = expired. |
| `todayCheckedIn` | `boolean` | Whether the user has already checked in today. |
| `todayPassed` | `Boolean` (nullable) | `true`/`false` if checked in, `null` if not yet. |
| `profileId` | `uuid` | Always equals `userId` in this system. |

---

## 9. Recommended Frontend Flow

### Home Screen — Active Challenge Cards
1. Call `GET /api/challenges/my/active`
2. For each card, display `challenge.title`, `badgeIcon`, `progressPercent`, `daysRemaining`, `currentStreak`
3. If `todayCheckedIn == true`: show ✅ or ❌ based on `todayPassed`
4. If `todayCheckedIn == false`: show a "Check In" button

### Challenge Detail Screen
1. Call `GET /api/challenges/my/{userChallengeId}` for history
2. Call `GET /api/challenges/my/{userChallengeId}/today-status` for the live progress bar
3. Show `currentValue / targetValue` as a progress bar using `today-status`
4. "Check In" button calls `POST .../checkin`; show the returned `message` as a toast

### Hydration / Exercise Challenge Workflow
```
User logs water → POST /api/water-logs
User logs exercise → POST /api/exercise-logs
User checks in → POST /api/challenges/my/{id}/checkin
Backend reads today's logs, evaluates, returns result automatically
```

### Joining a Challenge
1. Display challenge list from `GET /api/challenges`
2. "+" / "Join" button calls `POST /api/challenges/{id}/join`
3. After join, redirect to the challenge detail screen using the returned `id`

---

## 10. Error Codes Reference

| HTTP | Meaning |
|------|---------|
| 200  | Success |
| 400  | Business rule violation — see `message` for the user-facing text |
| 500  | Unexpected server error — report to backend team with the full response body |

**Challenge-specific 400 codes (`message` field explains the problem):**

| `errorCode` (in logs / internal) | When it occurs |
|----------------------------------|----------------|
| `ALREADY_IN_PROGRESS` | Tried to join a challenge already in progress |
| `ALREADY_CHECKED_IN` | Called checkin more than once on the same day |
| `CHALLENGE_NOT_ACTIVE` | Tried to checkin/abandon a completed or already-abandoned challenge |
| `CHALLENGE_EXPIRED` | Today is past the challenge `endDate` |
| `NOT_FOUND` | `userChallengeId` not found or belongs to a different user |
| `CHALLENGE_NOT_FOUND` | `challengeId` does not exist in the system |
| `DUPLICATE_TYPE` ⬅ NEW | Tried to create a challenge with a `type` that already exists |
| `INVALID_REQUEST` ⬅ NEW | Missing required field or invalid value (e.g. `duration_days <= 0`) |
