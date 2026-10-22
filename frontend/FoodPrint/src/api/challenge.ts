import { apiDelete, apiGet, apiPost } from './client';
import type {
  ApiResponse,
  ChallengeResponse,
  CheckinResponse,
  CreateChallengeRequest,
  TodayStatusResponse,
  UserChallengeResponse,
} from './types';
import { supabase } from '../lib/supabase';

const BASE = 'api/challenges';

async function getCurrentUserId(): Promise<string> {
  const {
    data: { session },
  } = await supabase.auth.getSession();

  const userId = session?.user?.id;
  if (!userId) {
    throw new Error('This endpoint requires login. Please sign in and try again.');
  }
  return userId;
}

async function userIdHeader(): Promise<Record<string, string>> {
  return { userId: await getCurrentUserId() };
}

/** GET /api/challenges - Get all system challenge templates */
export async function getChallenges(): Promise<ApiResponse<ChallengeResponse[]>> {
  return apiGet<ChallengeResponse[]>(BASE, { requireAuth: true });
}

/** POST /api/challenges - Create a custom challenge (JWT authentication required) */
export async function createChallenge(
  data: CreateChallengeRequest
): Promise<ApiResponse<ChallengeResponse>> {
  return apiPost<ChallengeResponse>(BASE, data, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** GET /api/challenges/my/{userChallengeId}/today-status - Get challenge progress for today */
export async function getTodayStatus(
  userChallengeId: string
): Promise<ApiResponse<TodayStatusResponse>> {
  return apiGet<TodayStatusResponse>(`${BASE}/my/${userChallengeId}/today-status`, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** POST /api/challenges/{challengeId}/join - Join a challenge */
export async function joinChallenge(
  challengeId: string
): Promise<ApiResponse<UserChallengeResponse>> {
  return apiPost<UserChallengeResponse>(`${BASE}/${challengeId}/join`, undefined, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** GET /api/challenges/my - Get all my challenge records */
export async function getMyChallenges(): Promise<ApiResponse<UserChallengeResponse[]>> {
  return apiGet<UserChallengeResponse[]>(`${BASE}/my`, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** GET /api/challenges/my/active - Get my active challenges */
export async function getMyActiveChallenges(): Promise<ApiResponse<UserChallengeResponse[]>> {
  return apiGet<UserChallengeResponse[]>(`${BASE}/my/active`, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** GET /api/challenges/my/{userChallengeId} - Get challenge details including check-in history */
export async function getUserChallengeDetail(
  userChallengeId: string
): Promise<ApiResponse<UserChallengeResponse>> {
  return apiGet<UserChallengeResponse>(`${BASE}/my/${userChallengeId}`, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** POST /api/challenges/my/{userChallengeId}/checkin - Check in today */
export async function checkinChallenge(
  userChallengeId: string
): Promise<ApiResponse<CheckinResponse>> {
  return apiPost<CheckinResponse>(`${BASE}/my/${userChallengeId}/checkin`, undefined, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}

/** DELETE /api/challenges/my/{userChallengeId} - Abandon an active challenge */
export async function abandonChallenge(userChallengeId: string): Promise<ApiResponse<unknown>> {
  return apiDelete(`${BASE}/my/${userChallengeId}`, {
    requireAuth: true,
    headers: await userIdHeader(),
  });
}
