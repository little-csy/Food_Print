import AsyncStorage from '@react-native-async-storage/async-storage'
import { createClient } from '@supabase/supabase-js'
import type { Database } from '../types/supabase'
import { SUPABASE_ANON_KEY, SUPABASE_URL } from './env'

const isSupabaseConfigured =
    Boolean(SUPABASE_URL && SUPABASE_ANON_KEY) &&
    !SUPABASE_URL.startsWith('https://placeholder') &&
    SUPABASE_ANON_KEY !== 'placeholder-anon-key'

if (!isSupabaseConfigured) {
    console.warn(
        '[FoodPrint] SUPABASE_URL / SUPABASE_ANON_KEY is missing or still a placeholder. Supabase sign-in and APIs may fail. Set EXPO_PUBLIC_SUPABASE_URL and EXPO_PUBLIC_SUPABASE_ANON_KEY in .env.'
    )
}

/** In-memory fallback when native AsyncStorage is unavailable, such as some Web / emulator environments; avoids "Native module is null" errors */
const memoryStorage: Record<string, string> = {}
const safeStorage = {
    getItem: async (key: string): Promise<string | null> => {
        try {
            return await AsyncStorage.getItem(key)
        } catch {
            return memoryStorage[key] ?? null
        }
    },
    setItem: async (key: string, value: string): Promise<void> => {
        try {
            await AsyncStorage.setItem(key, value)
        } catch {
            memoryStorage[key] = value
        }
    },
    removeItem: async (key: string): Promise<void> => {
        try {
            await AsyncStorage.removeItem(key)
        } catch {
            delete memoryStorage[key]
        }
    },
}

/** Supabase client: Auth and optional Realtime / Storage / PostgREST; safeStorage avoids crashes when native AsyncStorage is unavailable */
export const supabase = createClient<Database>(
    SUPABASE_URL || 'https://placeholder.supabase.co',
    SUPABASE_ANON_KEY || 'placeholder-anon-key',
    {
        auth: {
            storage: safeStorage,
            autoRefreshToken: true,
            persistSession: true,
            detectSessionInUrl: false,
        },
    }
)

export { isSupabaseConfigured }

/** Get the current session access_token for backend API Authorization: Bearer authentication */
export async function getSupabaseAccessToken(): Promise<string | null> {
    const {
        data: { session },
    } = await supabase.auth.getSession()

    if (session?.access_token) {
        return session.access_token
    }

    const { error } = await supabase.auth.refreshSession()
    if (error) {
        return null
    }

    const {
        data: { session: refreshedSession },
    } = await supabase.auth.getSession()
    return refreshedSession?.access_token ?? null
}
