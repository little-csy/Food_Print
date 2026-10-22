/**
 * Environment Variable Configuration
 * Expo loads the project root .env into process.env (requires app.config.js or a Babel plugin)
 * During development, run source .env before startup, or use EAS / a local .env
 */
const getEnv = (key: string, fallback = ''): string => {
    if (typeof process !== 'undefined' && process.env?.[key] != null) {
        return String(process.env[key]).trim()
    }
    return fallback
}

/** Supabase project URL from Supabase Dashboard → Settings → API */
export const SUPABASE_URL =
    getEnv('EXPO_PUBLIC_SUPABASE_URL') || getEnv('SUPABASE_URL', '')

/** Public Supabase anon key for client Auth */
export const SUPABASE_ANON_KEY =
    getEnv('EXPO_PUBLIC_SUPABASE_ANON_KEY') || getEnv('SUPABASE_ANON_KEY', '')

/** Backend API base URL (Spring Boot service) */
export const API_BASE_URL =
    getEnv('EXPO_PUBLIC_API_BASE_URL') ||
    getEnv('API_BASE_URL', 'http://localhost:8080')
