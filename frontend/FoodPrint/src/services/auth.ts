// src/services/auth.ts
// Temporary mock authentication service for quick AI feature testing
// TODO: Replace with real Supabase authentication integration

/**
 * Temporary mock token for testing
 * In production, get a real token from Supabase or another authentication system
 */
export function getMockToken(): string {
    // Return a mock token for development testing only
    // In a real environment, this must be a valid JWT
    return 'mock-jwt-token-for-testing'
}

/**
 * Get the user token (currently mocked)
 * TODO: Integrate Supabase authentication
 */
export async function getUserToken(): Promise<string | null> {
    try {
        // TODO: In the actual app, retrieve it as follows:
        // const { data: { session } } = await supabaseClient.auth.getSession();
        // return session?.access_token || null;

        // Currently return a mock token for testing
        return getMockToken()
    } catch {
        return null
    }
}

/**
 * Check whether the user is logged in
 */
export async function isUserLoggedIn(): Promise<boolean> {
    const token = await getUserToken()
    return token !== null && token !== 'mock-jwt-token-for-testing'
}

/**
 * Token validation for development testing
 * Delete this function in production
 */
export function isDevelopmentMode(): boolean {
    return __DEV__ || process.env.NODE_ENV === 'development'
}
