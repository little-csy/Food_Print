# FoodPrint

A food logging and community challenge app that helps users track their diet, receive AI nutrition advice, share in the community, and participate in health challenges.

👉 **[Frontend Essentials] Quick Integration Guide**: [FRONTEND_API_REFERENCE.md](./FRONTEND_API_REFERENCE.md) summarizes all backend endpoints for frontend developers, ready to copy and integrate.

---

## Quick Start

### Prerequisites

- Java 17 + Maven
- Supabase project(Auth + PostgreSQL + Storage)
- Gemini API Key([How to obtain](https://aistudio.google.com/apikey))

### Configure Environment Variables

Create `.env` in `backend/` (**already listed in .gitignore and excluded from commits**):

```bash
# AI API Keys
export GEMINI_API_KEY=your_gemini_api_key        # Required
export USDA_API_KEY=DEMO_KEY                      # Optional; defaults to DEMO_KEY

# Supabase
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_DB_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres
export SUPABASE_DB_USER=postgres
export SUPABASE_DB_PASSWORD=your_db_password
export SUPABASE_ANON_KEY=your_anon_key
export SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

### Build and Run

```bash
cd backend

# Run mock tests (no API keys required)
mvn clean test -Dtest='!LiveDemoTest,!UserServiceIntegrationTest'

# Start the service (requires Supabase and a Gemini key)
source .env && mvn spring-boot:run

# Run live AI tests (requires a Gemini key)
export GEMINI_API_KEY=xxx && mvn test -Dtest="LiveDemoTest" -Dfoodprint.live=true
```

---

## Module Status Overview

| Module | Owner | Status | Description |
|------|--------|------|------|
| **AI analysis** | AI Team (tianheng) | ✅ Complete | Food recognition, nutrition lookup, dietary advice, diet roasts, intent routing, and chat history |
| **User** | hexuan ma | ✅ Complete | Profile CRUD, body measurements, OAuth2 JWT authentication, and statistics dashboard |
| **Food log** | minghui yang, Tianheng Shi | 🔧 Partially complete | POST creation and GET weekly calories; DELETE / PUT missing |
| **Security** | hexuan ma | ✅ Complete | OAuth2 Resource Server and JWKS validation |
| **Community** | Siyu Chen, Tianheng Shi | ✅ Complete | Posts, comments, likes, commenter avatars, global impact dashboard, **following and Following Feed**, and topic subscriptions |
| **Challenges** | — | ✅ Complete | Create / join / check-in / leaderboard stubs |
| **Storage** | — | ✅ Complete | Supabase Storage integration stub |

---

## API Endpoint Overview

### Authentication Method

All requests must include Supabase JWT: 
```
Authorization: Bearer <supabase_jwt_token>
```

### User Module ✅

| Method | Path | Description | Request body |
|------|------|------|--------|
| GET | `/api/users/me` | Get current user (create automatically) | — |
| PUT | `/api/users/me` | Update nickname / avatar | `{ nickname, avatarUrl }` |
| PUT | `/api/users/me/body-data` | Update body measurements | `{ heightCm, weightKg, age, gender... }` |
| GET | `/api/users/me/stats` | Get user achievements and active-day summary | — |

### Food Module 🔧

| Method | Path | Description | Request body |
|------|------|------|--------|
| POST | `/api/food-logs` | Create a food log (calls AI) | `{ text, imageUrl, date }` |
| GET | `/api/food-logs/weekly-overview` | Daily calorie histogram data for the past seven days | `?endDate=2026-04-09` (optional) |
| GET | `/api/food-logs?date=2026-01-01` | Query by date | 
| PUT | `/api/food-logs/{id}` | Update a record |
| DELETE | `/api/food-logs/{id}` | Delete a record |

> ⚠️ Note: `FoodController` currently reads the user ID from `@RequestHeader("userId")`, while `UserController` uses `@AuthenticationPrincipal Jwt`. Standardize on the latter.

### AI Module ✅

| Method | Path | Description | Request body |
|------|------|------|--------|
| POST | `/api/ai/analyze` | Unified analysis: food, advice, roasts, or chat | `{ text, imageUrl, userContext }` |
| POST | `/api/ai/analyze/image` | Direct image recognition (bypasses routing) | `multipart/form-data: image` |

> 💡 **About General Chat (Chat)**: 
> When the frontend sends free-form text such as "I am so tired today" to `/api/ai/analyze`, the backend classifies it as `GENERAL_CHAT` and calls Gemini for a reply.
> The database automatically saves the latest 10 chat messages, associated with the `userId` from JWT. **The frontend does not need to send conversation history**; send text normally to preserve context.

### Community Module 🔧

| Method | Path | Description |
|------|------|------|
| GET | `/api/community/impact-metrics` | Get global metrics: total registered users and total pounds lost |
| POST | `/api/community/posts` | Create a post |
| GET | `/api/community/posts` | Get Feed |
| POST | `/api/community/posts/{id}/comments` | Comments |
| POST | `/api/community/posts/{id}/like` | Like |
| DELETE | `/api/community/posts/{id}/like` | Unlike a post |

### Challenge Module ✅ Complete

| Method | Path | Description |
|------|------|------|
| POST | `/api/challenges` | Create a challenge |
| GET | `/api/challenges` | List active challenges |
| POST | `/api/challenges/{id}/join` | Join a challenge |
| POST | `/api/challenges/{id}/checkin` | Daily check-in |
| GET | `/api/challenges/{id}/leaderboard` | Leaderboard |

---

## Unified response format

```json
// Success
{ "code": 200, "message": "success", "data": { ... } }

// Error
{ "code": 400, "message": "Text and image cannot both be empty", "data": null }
```

> Note: `UserController` currently returns `ResponseEntity<User>` directly rather than `ApiResponse<User>`; standardize this later.

---

## Project structure

```
backend/src/main/java/com/foodprint/
├── ai/                    ← AI module (✅ complete)
│   ├── config/            # AIConfig, WebClientConfig
│   ├── controller/        # POST /api/ai/analyze, /api/ai/analyze/image
│   ├── dto/               # AIResponse, FoodAnalysisResult, UserNutritionContext
│   ├── exception/         # AIServiceException
│   └── service/           # AIRequestRouter, GeminiService, FoodAnalysisService,
│                          # NutritionLookupService, DietAdviceService, NutritionPostProcessor
│
├── user/                  ← hexuan ma(✅ Complete)
│   ├── controller/        # GET/PUT /api/users/me
│   ├── dto/               # UserProfileRequest, BodyDataRequest
│   ├── entity/            # User (profiles Table, UUID id)
│   ├── repository/        # UserRepository
│   └── service/           # UserService
│
├── food/                  ← minghui yang(🔧 Partially complete)
│   ├── controller/        # POST /api/food-logs(Implemented)
│   ├── dto/               # CreateFoodLogRequest
│   ├── entity/            # FoodLog, NutritionData
│   ├── repository/        # FoodLogRepository, NutritionDataRepository
│   └── service/           # FoodLoggingService(Call AIRequestRouter)
│
├── community/             ← ✅ Complete
├── challenge/             ← ✅ Complete
├── config/                # SecurityConfig (OAuth2 JWKS), SupabaseConfig
├── security/              # JwtAuthenticationFilter (replaced by SecurityConfig)
├── storage/               ← ✅ Complete
├── exception/             # GlobalExceptionHandler
├── middleware/             # LoggingFilter
└── util/                  # ApiResponse, Constants
```

---

## Backend Engineer API Usage Guide

### Call the AI Module (Internal Java Calls)

```java
@Autowired
private AIRequestRouter aiRouter;

// Image URL -> food analysis
AIResponse response = aiRouter.routeImageUrl(imageUrl);

// Text -> intent routing (food logging / dietary advice / meal planning / diet roast / general chat)
// Note: general chat automatically saves the latest 10 messages to preserve context.
AIResponse response = aiRouter.routeText(text, userNutritionContext);

// Binary image data -> food analysis
AIResponse response = aiRouter.routeImage(bytes, "image/jpeg");
```

**import Path:**
```java
import com.foodprint.ai.service.AIRequestRouter;
import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.UserNutritionContext;
import com.foodprint.ai.dto.FoodAnalysisResult;
```

### Get User Data (Called by Other Modules)

```java
@Autowired
private UserService userService;

// Get the user (String userId is converted internally to UUID)
User user = userService.getUserById(userId);
// user.getHeightCm(), user.getWeightKg(), user.getGoal() ...
```

---

## Testing

| Test Files | Type | Covers |
|---------|------|------|
| `AIControllerTest` | WebMvc Mock | 6 AI controller endpoints |
| `AIRequestRouterTest` | Unit Mock | Routing logic |
| `FoodAnalysisServiceTest` | Unit Mock | Food analysis pipeline |
| `GeminiServiceTest` | Mock WebServer | Gemini API |
| `NutritionLookupServiceTest` | Mock WebServer | USDA Query |
| `NutritionPostProcessorTest` | Pure Unit | Data validation |
| `DietAdviceServiceTest` | Unit Mock | Dietary advice |
| `LiveDemoTest` | Live API | End-to-end tests (key required) |
| `UserServiceIntegrationTest` | Integration tests | User CRUD (database required) |

```bash
# 41 mock tests requiring no external services
mvn clean test -Dtest='!LiveDemoTest,!UserServiceIntegrationTest'
```

---

## Documentation

| Documentation | Content |
|------|------|
| [BACKEND_DESIGN_WITH_AI.md](BACKEND_DESIGN_WITH_AI.md) | Complete backend design: AI integration, database design, and task assignments |
| [backend/docs/AI_MODULE_WALKTHROUGH.md](backend/docs/AI_MODULE_WALKTHROUGH.md) | Detailed AI module walkthrough |
| [backend/food/food-api.md](backend/src/main/java/com/foodprint/food/food-api.md) | Food module API documentation |
| [BACKEND_DESIGN.md](BACKEND_DESIGN.md) | Original design (retained for reference; partially outdated) |

---

## Known Issues & TODO

- [ ] Read the user ID in `FoodController` through `@AuthenticationPrincipal Jwt` instead of `@RequestHeader("userId")`.
- [ ] `UserController` Responses should be wrapped consistently in `ApiResponse<User>`
- [ ] Food module: missing GET (query by date), PUT (update), and DELETE endpoints.
- [ ] Food module: fields such as todayCalories in `createFoodLog` are currently 0; implement aggregation.
- [ ] Community module: all TODO.
- [ ] Challenge module: all TODO.
- [ ] Storage module: Supabase Storage integration.
- [ ] JwtAuthenticationFilter is an old stub replaced by SecurityConfig; consider deleting it.
