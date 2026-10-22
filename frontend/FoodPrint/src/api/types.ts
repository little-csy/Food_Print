/**
 * Shared response format and DTO types matching the backend
 * @see Backend README for the unified response format
 */

/** Unified backend response:{ code, message, data } */
export interface ApiResponse<T = unknown> {
    code: number
    message: string
    data: T | null
}

// ---------- User Module ----------
export interface UserProfileDto {
    id: string
    email?: string
    nickname?: string | null
    avatarUrl?: string | null
    heightCm?: number | null
    weightKg?: number | null
    age?: number | null
    gender?: string | null
    goal?: string | null
    dailyCalorieTarget?: number | null
    dietaryPreference?: string | null
    createdAt?: string
    updatedAt?: string
}

export interface UserStatsDto {
    achievements: number
    goalsMet: number
    daysActive: number
}

export interface BodyDataRequest {
    heightCm?: number
    weightKg?: number
    age?: number
    gender?: string
    goal?: string
    dailyCalorieTarget?: number
    dietaryPreference?: string
}

export interface UpdateProfileRequest {
    nickname?: string
    avatarUrl?: string
}

// ---------- Food Module ----------
/**
 * Create / update a food log (data write only; the frontend assembles this object after calling /api/ai)
 */
export interface CreateFoodLogRequest {
    text?: string
    imageUrl?: string
    date?: string // ISO date e.g. 2026-01-01
    /** Original user description (may differ from the text display name) */
    originalText?: string | null
    recognizedFoods?: string[]
    nutritionData?: NutritionDataDto | null
    aiConfidence?: number
}

export interface NutritionDataDto {
    calories?: number
    protein?: number
    proteinG?: number
    fat?: number
    carbs?: number
    fiber?: number
    [key: string]: number | undefined
}

/**
 * GET /api/food-logs/today — Home Simplified meal list for the day
 */
export interface TodayFoodLogItemDto {
    id: string
    title: string
    foodItems: string[]
    totalCalories: number
    /** ISO-8601 */
    mealTime: string
    /** Grams; matches weekly overview fields and falls back to the daily overview entry when missing */
    totalProtein?: number
    totalFat?: number
    totalCarbs?: number
}

export interface FoodLogDto {
    id: string
    userId: string
    /** Meal time supplied by the frontend when saving */
    mealTime?: string
    /** Log date currently returned by the backend */
    logDate?: string
    /** Original user input / AI recognition text */
    originalText?: string | null
    aiConfidence?: number
    /** Compatible with legacy fields */
    date?: string
    text?: string | null
    imageUrl?: string | null
    recognizedFoods?: string[]
    nutritionData?: NutritionDataDto | null
    /** Consistent with manual entry; GET /today may also return per-item nutrition */
    foods?: FoodNutritionDetail[]
    /** Complete saved AI analysis (macros are often in summary or foods) */
    analysisResult?: FoodAnalysisResult | null
    todayCalories?: number
    todayProtein?: number
    todayFat?: number
    todayCarbs?: number
    createdAt?: string
    updatedAt?: string
}

export interface TodaySummaryDto {
    date: string
    totalCalories: number
    totalProtein?: number
    totalFat?: number
    totalCarbs?: number
    logCount?: number
}

/** Daily bar chart data from the weekly overview endpoint */
export interface DailyCalorieBarDto {
    date: string
    totalCalories: number
    totalProtein?: number
    totalFat?: number
    totalCarbs?: number
    logCount?: number
}

/**
 * GET /api/food-logs/weekly-overview data may be an array or an object with a days field
 */
export type WeeklyOverviewPayload =
    | DailyCalorieBarDto[]
    | { days?: DailyCalorieBarDto[] }

// ---------- AI Module ----------
export interface UserNutritionContext {
    heightCm?: number
    weightKg?: number
    age?: number
    gender?: string
    goal?: string
    dailyCalorieTarget?: number
}

export interface AnalyzeRequest {
    text?: string
    imageUrl?: string
    userContext?: UserNutritionContext
}

export interface FoodNutritionDetail {
    nameEn?: string
    nameZh?: string
    portionAmount?: number
    portionUnit?: string
    calories?: number
    proteinG?: number
    fatG?: number
    carbsG?: number
    fiberG?: number
    dataSource?: string
    flagged?: boolean
    flagReason?: string
}

export interface MealSummary {
    totalCalories?: number
    totalProteinG?: number
    totalFatG?: number
    totalCarbsG?: number
    totalFiberG?: number
    foodCount?: number
}

export interface FoodAnalysisResult {
    foods?: FoodNutritionDetail[]
    summary?: MealSummary
    confidence?: number
}

/** POST /api/ai/log - Save AI analysis results, including edits made in the UI */
export interface AiLogSaveRequest {
    title: string
    /** ISO 8601 Meal time */
    mealTime: string
    analysisResult: FoodAnalysisResult
}

/** POST /api/food-logs/manual - Manual entry without AI */
export interface ManualFoodLogRequest {
    title: string
    mealTime: string
    foods: FoodNutritionDetail[]
}

export interface AIResponseDto {
    type?:
        | 'FOOD_ANALYSIS'
        | 'DIET_ADVICE'
        | 'DIET_PLAN'
        | 'DIET_ROAST'
        | 'CHAT'
        | 'PROFILE_NEEDED'
    foodAnalysis?: FoodAnalysisResult
    adviceText?: string
    profileIncomplete?: boolean
    profilePrompt?: string
}

// ---------- Community Module ----------
export interface CommentResponse {
    id: number
    postId: number
    userId: string
    authorNickname?: string | null
    authorAvatarUrl?: string | null
    content: string
    createdAt: string
    updatedAt?: string
}

export interface PostResponse {
    id: number
    userId: string
    authorNickname?: string | null
    authorAvatarUrl?: string | null
    title: string
    content: string
    imageUrl?: string | null
    likeCount: number
    createdAt: string
    updatedAt?: string
    comments?: CommentResponse[]
    likedByCurrentUser: boolean
}

export interface CreatePostRequest {
    title: string
    content: string
    imageUrl?: string | null
}

export interface CreateCommentRequest {
    content: string
}

export interface ImpactMetricsResponse {
    totalUsers: number
    totalWeightLost: number
}

// ---------- Water Module ----------
export interface WaterLogEntryDto {
    id: string
    amountMl: number
    note?: string | null
    loggedAt: string
}

export interface WaterTodayDto {
    totalMl: number
    totalCups: number
    goalMl: number
    percentAchieved: number
    logs: WaterLogEntryDto[]
}

// ---------- Exercise Module ----------
export interface ExerciseEntryDto {
    id: string
    activityType: string
    caloriesBurned: number
    durationMin?: number | null
    note?: string | null
    loggedAt: string
}

export interface ExerciseTodayDto {
    totalCaloriesBurned: number
    totalDurationMin: number
    exercises: ExerciseEntryDto[]
}

// ---------- Challenge Module ----------
export interface ChallengeTemplateDto {
    id: string
    title: string
    description?: string | null
    type: string
    durationDays: number
    targetValue: number
    badgeIcon?: string | null
}

export interface NutritionSnapshotDto {
    calories: number
    protein_g: number
    fat_g: number
    carbs_g: number
    fiber_g: number
    water_ml: number
    calories_burned: number
    food_log_count: number
}

export interface ChallengeCheckinDto {
    date: string
    passed: boolean
    actualValue: number
    note?: string | null
    completedDays?: number
    currentStreak?: number
    challengeCompleted?: boolean
    nutritionSnapshot?: NutritionSnapshotDto | null
}

export interface UserChallengeDto {
    id: string
    userId: string
    profileId?: string
    challenge: ChallengeTemplateDto
    status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED' | string
    startDate: string
    endDate: string
    completedDays: number
    currentStreak: number
    progressPercent: number
    daysRemaining?: number
    todayCheckedIn?: boolean
    todayPassed?: boolean | null
    joinedAt?: string
    completedAt?: string | null
    checkins?: ChallengeCheckinDto[]
}

export interface TodayStatusDto {
    checkedIn: boolean
    passed?: boolean | null
    currentValue: number
    targetValue: number
    summary: string
    daysRemaining: number
    currentStreak: number
    progressPercent: number
    nutritionSnapshot?: NutritionSnapshotDto | null
}

export type NutritionSnapshot = NutritionSnapshotDto
export type TodayStatusResponse = TodayStatusDto

export interface ChallengeResponse {
    id: string
    title: string
    description: string
    type: string
    durationDays: number
    targetValue: number
    badgeIcon?: string | null
    createdAt?: string
}

export interface CheckinResponse {
    date: string
    passed: boolean
    actualValue: number
    note: string
    completedDays: number
    currentStreak: number
    challengeCompleted: boolean
    message?: string | null
}

export interface UserChallengeResponse {
    id: string
    userId: string
    profileId?: string
    challenge: ChallengeResponse
    status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED' | string
    startDate: string
    endDate: string
    completedDays: number
    currentStreak: number
    progressPercent: number
    daysRemaining?: number
    todayCheckedIn?: boolean
    todayPassed?: boolean | null
    joinedAt?: string
    completedAt?: string | null
    checkins?: CheckinResponse[]
}

export interface CreateChallengeRequest {
    title: string
    description: string
    type: string
    duration_days: number
    target_value?: number
    badge_icon?: string
}
