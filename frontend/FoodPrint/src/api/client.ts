import { Platform } from 'react-native'

import { API_BASE_URL } from '../lib/env'
import { getSupabaseAccessToken, supabase } from '../lib/supabase'
import type { ApiResponse } from './types'

const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
}

type ApiRequestOptions = RequestInit & { skipAuth?: boolean; requireAuth?: boolean }

export function getEffectiveBaseUrl(): string {
    const base = API_BASE_URL.replace(/\/$/, '')
    if (Platform.OS === 'android' && (base.includes('localhost') || base.includes('127.0.0.1'))) {
        return base.replace(/localhost|127\.0\.0\.1/, '10.0.2.2')
    }
    return base
}

async function resolveAccessToken(): Promise<string | null> {
    let token = await getSupabaseAccessToken()
    if (token) {
        return token
    }

    const { error } = await supabase.auth.refreshSession()
    if (!error) {
        token = await getSupabaseAccessToken()
    }
    return token
}

async function parseApiResponse<T>(res: Response): Promise<ApiResponse<T>> {
    const raw = await res.text()
    const trimmed = raw.trim()

    if (!trimmed) {
        return {
            code: res.status,
            message: res.ok ? 'success' : res.statusText,
            data: null,
        }
    }

    try {
        const parsed = JSON.parse(trimmed) as unknown

        if (
            parsed &&
            typeof parsed === 'object' &&
            'code' in parsed &&
            'message' in parsed &&
            'data' in parsed
        ) {
            return parsed as ApiResponse<T>
        }

        if (
            parsed &&
            typeof parsed === 'object' &&
            'success' in parsed &&
            typeof (parsed as { success?: unknown }).success === 'boolean'
        ) {
            const ok = (parsed as { success: boolean }).success
            return {
                code: res.status,
                message: ok ? 'success' : 'failed',
                data: parsed as T,
            }
        }

        return {
            code: res.status,
            message: res.ok ? 'success' : res.statusText,
            data: parsed as T,
        }
    } catch {
        return {
            code: res.status,
            message: trimmed || res.statusText,
            data: null,
        }
    }
}

/**
 * Requests with JWT: automatically attach Authorization: Bearer <supabase_jwt>
 * Used to call the backend Spring Boot API（OAuth2 Resource Server + JWKS）
 */
export async function apiRequest<T = unknown>(
    path: string,
    options: ApiRequestOptions = {}
): Promise<ApiResponse<T>> {
    const { skipAuth, requireAuth, ...fetchOptions } = options
    const baseUrl = getEffectiveBaseUrl()
    const url = `${baseUrl}/${path.replace(/^\//, '')}`
    const headers: Record<string, string> = {
        ...defaultHeaders,
        ...(typeof fetchOptions.headers === 'object' && !(fetchOptions.headers instanceof Headers)
            ? (fetchOptions.headers as Record<string, string>)
            : {}),
    }

    console.log(`📡 API Request: ${fetchOptions.method || 'GET'} ${url}`)

    const requestToken = !skipAuth ? await resolveAccessToken() : null

    if (!skipAuth) {
        const token = requestToken
        console.log('🔐 Token retrieved:', token ? `${token.substring(0, 20)}...` : 'null')
        if (token) {
            headers['Authorization'] = `Bearer ${token}`
            console.log('✅ Authorization header set')
        } else if (requireAuth) {
            throw new ApiError(401, 'This endpoint requires login. Please sign in and try again.', {
                code: 401,
                message: 'Unauthorized: missing Bearer token',
                data: null,
            })
        } else {
            console.warn('⚠️ No token available, request will be sent without Authorization header')
        }
    } else {
        console.log('⏭️ skipAuth: true - Skip JWT authentication')
    }

    const doFetch = async (requestHeaders: HeadersInit): Promise<Response> => {
        return fetch(url, {
            ...fetchOptions,
            headers: requestHeaders,
        })
    }

    let res: Response
    try {
        res = await doFetch({ ...headers, ...fetchOptions.headers } as HeadersInit)
    } catch (e) {
        const msg =
            e instanceof TypeError && (e.message === 'Network request failed' || e.message === 'Failed to fetch')
                ? 'Network error. Check your connection and that the API server is running (e.g. use your computer’s IP instead of localhost on device).'
                : e instanceof Error
                  ? e.message
                  : 'Network request failed'
        throw new ApiError(0, msg, undefined)
    }

    if (res.status === 401 && !skipAuth) {
        const refreshedToken = await resolveAccessToken()
        if (refreshedToken && refreshedToken !== requestToken) {
            const retryHeaders: HeadersInit = {
                ...headers,
                ...fetchOptions.headers,
                Authorization: `Bearer ${refreshedToken}`,
            }
            try {
                res = await doFetch(retryHeaders)
            } catch (e) {
                const msg = e instanceof Error ? e.message : 'Network request failed during auth retry'
                throw new ApiError(0, msg, undefined)
            }
        }
    }

    const body = await parseApiResponse<T>(res)

    if (!res.ok) {
        const responseMessage = typeof body.message === 'string' ? body.message.trim() : ''
        const errorMessage =
            responseMessage ||
            (res.status === 401 && requestToken
                ? 'Unauthorized: token was sent but rejected by backend. Check backend SUPABASE_URL/JWKS and token issuer.'
                : res.statusText || `HTTP ${res.status}`)
        throw new ApiError(res.status, errorMessage, body)
    }

    return body
}

/** Convenience helper for GET requests */
export function apiGet<T>(path: string, options?: ApiRequestOptions) {
    return apiRequest<T>(path, { ...options, method: 'GET' })
}

/** Convenience helper for POST requests */
export function apiPost<T>(path: string, body?: unknown, options?: ApiRequestOptions) {
    return apiRequest<T>(path, {
        ...options,
        method: 'POST',
        body: body != null ? JSON.stringify(body) : undefined,
    })
}

/** Convenience helper for PUT requests */
export function apiPut<T>(path: string, body?: unknown, options?: ApiRequestOptions) {
    return apiRequest<T>(path, {
        ...options,
        method: 'PUT',
        body: body != null ? JSON.stringify(body) : undefined,
    })
}

/** Convenience helper for DELETE requests */
export function apiDelete<T>(path: string, options?: ApiRequestOptions) {
    return apiRequest<T>(path, { ...options, method: 'DELETE' })
}

export class ApiError extends Error {
    constructor(
        public status: number,
        message: string,
        public response?: ApiResponse<unknown>
    ) {
        super(message)
        this.name = 'ApiError'
    }
}
