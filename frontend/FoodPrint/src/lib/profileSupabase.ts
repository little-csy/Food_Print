import { supabase } from './supabase'

export type OnboardingData = {
    activityLevel: string
    age: number
    dailyCalories: number
    dietPreference: string
    gender: string
    goal: string
    height: number
    weight: number
}

const METADATA_KEYS = {
    activityLevel: 'activityLevel',
    age: 'age',
    dailyCalorieTarget: 'dailyCalorieTarget',
    dietPreference: 'dietPreference',
    gender: 'gender',
    goal: 'goal',
    heightCm: 'heightCm',
    weightKg: 'weightKg',
} as const

/** Save onboarding profile to Supabase Auth user metadata (no Java backend needed). */
export async function saveOnboardingToSupabase(
    data: OnboardingData
): Promise<void> {
    const { error } = await supabase.auth.updateUser({
        data: {
            [METADATA_KEYS.heightCm]: data.height,
            [METADATA_KEYS.weightKg]: data.weight,
            [METADATA_KEYS.age]: data.age,
            [METADATA_KEYS.gender]: data.gender,
            [METADATA_KEYS.goal]: data.goal,
            [METADATA_KEYS.dailyCalorieTarget]: data.dailyCalories,
            [METADATA_KEYS.activityLevel]: data.activityLevel,
            [METADATA_KEYS.dietPreference]: data.dietPreference,
        },
    })
    if (error) throw error
}

/** Build profile from Supabase user metadata (same keys we write in saveOnboardingToSupabase). */
export function profileFromUserMetadata(
    metadata: Record<string, unknown> | undefined,
    email: string
): {
    activityLevel: 'low' | 'medium' | 'high'
    age: number
    dailyCalories: number
    dietPreference: string
    email: string
    gender: 'male' | 'female' | 'other'
    goal: 'lose' | 'maintain' | 'gain'
    height: number
    weight: number
} | null {
    if (!metadata || typeof metadata[METADATA_KEYS.heightCm] !== 'number') {
        return null
    }
    return {
        activityLevel:
            (metadata[METADATA_KEYS.activityLevel] as 'low' | 'medium' | 'high') ??
            'medium',
        age: Number(metadata[METADATA_KEYS.age]) ?? 0,
        dailyCalories:
            Number(metadata[METADATA_KEYS.dailyCalorieTarget]) ?? 2000,
        dietPreference:
            String(metadata[METADATA_KEYS.dietPreference] ?? 'none'),
        email,
        gender:
            (metadata[METADATA_KEYS.gender] as 'male' | 'female' | 'other') ??
            'other',
        goal:
            (metadata[METADATA_KEYS.goal] as 'lose' | 'maintain' | 'gain') ??
            'maintain',
        height: Number(metadata[METADATA_KEYS.heightCm]) ?? 0,
        weight: Number(metadata[METADATA_KEYS.weightKg]) ?? 0,
    }
}
