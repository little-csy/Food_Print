import { apiDelete, apiGet, apiPost } from './client'
import type {
    ApiResponse,
    CommentResponse,
    CreateCommentRequest,
    CreatePostRequest,
    ImpactMetricsResponse,
    PostResponse,
} from './types'

const BASE = 'api/community'

/** GET /api/community/impact-metrics - Get community impact metrics */
export async function getImpactMetrics(): Promise<ApiResponse<ImpactMetricsResponse>> {
    return apiGet<ImpactMetricsResponse>(`${BASE}/impact-metrics`)
}

/** POST /api/community/posts - Create a post */
export async function createPost(
    body: CreatePostRequest
): Promise<ApiResponse<PostResponse>> {
    return apiPost<PostResponse>(`${BASE}/posts`, body, { requireAuth: true })
}

/** GET /api/community/posts?page=0&size=10 - Get Feed */
export async function getFeed(page = 0, size = 10): Promise<ApiResponse<PostResponse[]>> {
    return apiGet<PostResponse[]>(`${BASE}/posts?page=${page}&size=${size}`, { requireAuth: true })
}

/** POST /api/community/posts/{id}/comments - Add a comment */
export async function addComment(
    postId: string | number,
    body: CreateCommentRequest
): Promise<ApiResponse<CommentResponse>> {
    return apiPost<CommentResponse>(`${BASE}/posts/${postId}/comments`, body, { requireAuth: true })
}

/** POST /api/community/posts/{id}/like - Like a post */
export async function likePost(postId: string | number): Promise<ApiResponse<unknown>> {
    return apiPost(`${BASE}/posts/${postId}/like`, undefined, { requireAuth: true })
}

/** DELETE /api/community/posts/{id}/like - Unlike a post */
export async function unlikePost(postId: string | number): Promise<ApiResponse<unknown>> {
    return apiDelete(`${BASE}/posts/${postId}/like`, { requireAuth: true })
}

/** POST /api/community/users/{userId}/follow - Follow a user */
export async function followUser(userId: string): Promise<ApiResponse<unknown>> {
    return apiPost(`${BASE}/users/${userId}/follow`, undefined, { requireAuth: true })
}

/** DELETE /api/community/users/{userId}/follow - Unfollow a user */
export async function unfollowUser(userId: string): Promise<ApiResponse<unknown>> {
    return apiDelete(`${BASE}/users/${userId}/follow`, { requireAuth: true })
}

/** GET /api/community/posts/following - Get followed users Feed */
export async function getFollowingFeed(): Promise<ApiResponse<PostResponse[]>> {
    return apiGet<PostResponse[]>(`${BASE}/posts/following`, { requireAuth: true })
}

/** POST /api/community/topics/{topicName}/join - Join a topic */
export async function joinTopic(topicName: string): Promise<ApiResponse<unknown>> {
    return apiPost(`${BASE}/topics/${encodeURIComponent(topicName)}/join`, undefined, { requireAuth: true })
}

/** GET /api/community/my-topics - Get my topics */
export async function getMyTopics(): Promise<ApiResponse<string[]>> {
    return apiGet<string[]>(`${BASE}/my-topics`, { requireAuth: true })
}

/** DELETE /api/community/posts/{id} - Delete a post */
export async function deletePost(postId: string | number): Promise<ApiResponse<unknown>> {
    return apiDelete(`${BASE}/posts/${postId}`, { requireAuth: true })
}

/** DELETE /api/community/comments/{id} - Delete a comment */
export async function deleteComment(
    commentId: string | number
): Promise<ApiResponse<unknown>> {
    return apiDelete(`${BASE}/comments/${commentId}`, { requireAuth: true })
}
