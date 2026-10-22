import { apiGet, apiPut } from './client'
import type {
    ApiResponse,
    BodyDataRequest,
    UpdateProfileRequest,
    UserProfileDto,
    UserStatsDto,
} from './types'

const BASE = 'api/users'

/** GET /api/users/me - Get the current user (create automatically) */
export async function getMe(): Promise<ApiResponse<UserProfileDto>> {
    return apiGet<UserProfileDto>(`${BASE}/me`)
}

/** PUT /api/users/me - Update nickname / avatar */
export async function updateProfile(
    body: UpdateProfileRequest
): Promise<ApiResponse<UserProfileDto>> {
    return apiPut<UserProfileDto>(`${BASE}/me`, body)
}

/** PUT /api/users/me/body-data - Update body measurements */
export async function updateBodyData(
    body: BodyDataRequest
): Promise<ApiResponse<UserProfileDto>> {
    return apiPut<UserProfileDto>(`${BASE}/me/body-data`, body)
}

/** GET /api/users/me/stats - Get basic statistics for the current user */
export async function getMyStats(): Promise<ApiResponse<UserStatsDto>> {
    return apiGet<UserStatsDto>(`${BASE}/me/stats`)
}
