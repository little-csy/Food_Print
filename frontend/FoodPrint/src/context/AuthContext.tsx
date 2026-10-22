import AsyncStorage from '@react-native-async-storage/async-storage'
import {
    createContext,
    type PropsWithChildren,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from 'react'

import type { UserProfileDto } from '../api/types'
import { getMe } from '../api/user'
import { supabase } from '../lib/supabase'

type AuthActionResult = {
    success: boolean
    message?: string
}

type AuthContextValue = {
    isAuthenticated: boolean
    /** When true, Supabase is in a recovery session: complete the password change before entering the main app tabs */
    isPasswordRecovery: boolean
    hasCompletedOnboarding: boolean
    isLoading: boolean
    /** Supabase Auth user.id for food-logs and other endpoints requiring a userId header */
    authUserId: string | null
    userProfile: UserProfile | null
    userEmail: string | null
    completeOnboarding: () => Promise<void>
    login: (email: string, password: string) => Promise<AuthActionResult>
    register: (email: string, password: string) => Promise<AuthActionResult>
    logout: () => Promise<void>
    updateProfile: (profile: UserProfile) => Promise<void>
}

export type UserProfile = {
    avatarUrl?: string | null
    createdAt?: string
    email: string
    goal: 'lose' | 'maintain' | 'gain'
    gender: 'male' | 'female' | 'other'
    height: number
    nickname?: string
    weight: number
    age: number
    activityLevel: 'low' | 'medium' | 'high'
    dietPreference: string
    dailyCalories: number
}

const AUTH_SESSION_KEY = '@foodprint/auth_session'
const AUTH_PROFILE_KEY = '@foodprint/auth_profile'
const AUTH_ONBOARDING_KEY = '@foodprint/auth_onboarding_completed'
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function getGender(
    value: string | null | undefined,
    fallback: UserProfile['gender']
): UserProfile['gender'] {
    return value === 'male' || value === 'female' || value === 'other'
        ? value
        : fallback
}

function getGoal(
    value: string | null | undefined,
    fallback: UserProfile['goal']
): UserProfile['goal'] {
    return value === 'lose' || value === 'maintain' || value === 'gain'
        ? value
        : fallback
}

function getPositiveNumber(
    value: number | null | undefined,
    fallback: number
): number {
    return typeof value === 'number' && Number.isFinite(value) && value > 0
        ? value
        : fallback
}

function getOptionalText(
    value: string | null | undefined,
    fallback: string
): string {
    return typeof value === 'string' && value.trim().length > 0
        ? value.trim()
        : fallback
}

function getOptionalNullableText(
    value: string | null | undefined,
    fallback: string | null
): string | null {
    return typeof value === 'string' && value.trim().length > 0
        ? value.trim()
        : fallback
}

export function mergeUserProfileDto(
    dto: UserProfileDto,
    fallbackEmail: string,
    existingProfile: UserProfile | null = null
): UserProfile {
    return {
        activityLevel: existingProfile?.activityLevel ?? 'medium',
        age: getPositiveNumber(dto.age, existingProfile?.age ?? 0),
        avatarUrl: getOptionalNullableText(
            dto.avatarUrl,
            existingProfile?.avatarUrl ?? null
        ),
        createdAt: dto.createdAt ?? existingProfile?.createdAt,
        dailyCalories: getPositiveNumber(
            dto.dailyCalorieTarget,
            existingProfile?.dailyCalories ?? 2000
        ),
        dietPreference: getOptionalText(
            dto.dietaryPreference,
            existingProfile?.dietPreference ?? 'none'
        ),
        email: getOptionalText(dto.email, existingProfile?.email ?? fallbackEmail),
        gender: getGender(dto.gender, existingProfile?.gender ?? 'other'),
        goal: getGoal(dto.goal, existingProfile?.goal ?? 'maintain'),
        height: getPositiveNumber(dto.heightCm, existingProfile?.height ?? 0),
        nickname: getOptionalText(dto.nickname, existingProfile?.nickname ?? ''),
        weight: getPositiveNumber(dto.weightKg, existingProfile?.weight ?? 0),
    }
}

type PasswordStrength = {
    minLength: boolean
    hasUppercase: boolean
    hasLowercase: boolean
    hasNumber: boolean
    hasSpecial: boolean
    score: number
    isStrong: boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function isValidEmail(email: string) {
    return EMAIL_REGEX.test(email.trim().toLowerCase())
}

export function evaluatePasswordStrength(password: string): PasswordStrength {
    const result: PasswordStrength = {
        minLength: password.length >= 8,
        hasUppercase: /[A-Z]/.test(password),
        hasLowercase: /[a-z]/.test(password),
        hasNumber: /\d/.test(password),
        hasSpecial: /[^A-Za-z0-9]/.test(password),
        score: 0,
        isStrong: false,
    }

    result.score = [
        result.minLength,
        result.hasUppercase,
        result.hasLowercase,
        result.hasNumber,
        result.hasSpecial,
    ].filter(Boolean).length
    result.isStrong = result.score === 5

    return result
}

export function AuthProvider({ children }: PropsWithChildren) {
    const [hasCompletedOnboarding, setHasCompletedOnboarding] = useState(true)
    const [isLoading, setIsLoading] = useState(true)
    const [authUserId, setAuthUserId] = useState<string | null>(null)
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null)
    const [userEmail, setUserEmail] = useState<string | null>(null)
    const [isPasswordRecovery, setIsPasswordRecovery] = useState(false)

    const initializeUserProfile = useCallback(
        async (
            fallbackEmail: string,
            existingProfile: UserProfile | null = null
        ): Promise<UserProfile | null> => {
            try {
                const res = await getMe()
                const dto = res?.data ?? (res as unknown as UserProfileDto)
                if (dto && typeof dto === 'object' && ('id' in dto || 'email' in dto)) {
                    const mergedProfile = mergeUserProfileDto(
                        dto as UserProfileDto,
                        fallbackEmail,
                        existingProfile
                    )
                    setUserProfile(mergedProfile)
                    try {
                        await AsyncStorage.setItem(
                            AUTH_PROFILE_KEY,
                            JSON.stringify(mergedProfile)
                        )
                    } catch {
                        // profile Persistence failures do not block session initialization
                    }
                    return mergedProfile
                }
            } catch {
                // Keep local fallback profile data if the API fails
            }

            if (existingProfile) {
                setUserProfile(existingProfile)
            }

            return existingProfile
        },
        []
    )

    useEffect(() => {
        async function restoreSession() {
            try {
                await supabase.auth.refreshSession()
                const {
                    data: { session },
                } = await supabase.auth.getSession()
                setAuthUserId(session?.user?.id ?? null)
                if (session?.user?.email) {
                    setUserEmail(session.user.email)
                }

                const [onboardingRaw, profileRaw] = await Promise.all([
                    AsyncStorage.getItem(AUTH_ONBOARDING_KEY),
                    AsyncStorage.getItem(AUTH_PROFILE_KEY),
                ])

                const storedProfile = profileRaw
                    ? (JSON.parse(profileRaw) as UserProfile)
                    : null

                if (session?.user?.email) {
                    await initializeUserProfile(session.user.email, storedProfile)
                } else if (storedProfile) {
                    setUserProfile(storedProfile)
                }

                if (onboardingRaw === 'false') {
                    setHasCompletedOnboarding(false)
                } else {
                    setHasCompletedOnboarding(true)
                }
            } catch {
                await AsyncStorage.removeItem(AUTH_SESSION_KEY)
                await AsyncStorage.removeItem(AUTH_ONBOARDING_KEY)
                setAuthUserId(null)
                setUserEmail(null)
                setIsPasswordRecovery(false)
                setHasCompletedOnboarding(true)
            } finally {
                setIsLoading(false)
            }
        }

        void restoreSession()

        const {
            data: { subscription },
        } = supabase.auth.onAuthStateChange((event, session) => {
            setAuthUserId(session?.user?.id ?? null)
            setUserEmail(session?.user?.email ?? null)
            if (event === 'PASSWORD_RECOVERY') {
                setIsPasswordRecovery(true)
            } else if (event === 'SIGNED_OUT') {
                setIsPasswordRecovery(false)
            } else if (event === 'SIGNED_IN') {
                setIsPasswordRecovery(false)
                if (session?.user?.email) {
                    void initializeUserProfile(session.user.email)
                }
            }
        })
        return () => {
            subscription.unsubscribe()
        }
    }, [initializeUserProfile])

    const login = useCallback(
        async (email: string, password: string): Promise<AuthActionResult> => {
            const normalizedEmail = email.trim().toLowerCase()
            const { data, error } = await supabase.auth.signInWithPassword({
                email: normalizedEmail,
                password,
            })
            if (error) {
                const msg =
                    /network|fetch|failed|connection/i.test(error.message)
                        ? 'Network error. Check your connection and try again.'
                        : error.message || 'Login failed'
                return { success: false, message: msg }
            }
            setUserEmail(data.user?.email ?? normalizedEmail)
            setAuthUserId(data.user?.id ?? null)
            setIsPasswordRecovery(false)
            setHasCompletedOnboarding(true)
            await AsyncStorage.setItem(AUTH_ONBOARDING_KEY, 'true')
            if (data.user?.email) {
                await initializeUserProfile(data.user.email)
            }
            return { success: true }
        },
        [initializeUserProfile]
    )

    const register = useCallback(
        async (email: string, password: string): Promise<AuthActionResult> => {
            const normalizedEmail = email.trim().toLowerCase()
            const { data, error } = await supabase.auth.signUp({
                email: normalizedEmail,
                password,
            })
            if (error) {
                const msg =
                    /network|fetch|failed|connection/i.test(error.message)
                        ? 'Network error. Check your connection and try again.'
                        : error.message || 'Registration failed'
                return { success: false, message: msg }
            }
            if (!data.session) {
                return {
                    success: true,
                    message: 'Please check your email and click the link to verify your account',
                }
            }
            const sessionEmail = data.user?.email ?? normalizedEmail
            setUserEmail(sessionEmail)
            setAuthUserId(data.user?.id ?? null)
            setIsPasswordRecovery(false)
            setHasCompletedOnboarding(false)
            setUserProfile(null)
            await AsyncStorage.setItem(AUTH_ONBOARDING_KEY, 'false')
            await initializeUserProfile(sessionEmail)
            return { success: true }
        },
        [initializeUserProfile]
    )

    const logout = useCallback(async () => {
        setHasCompletedOnboarding(true)
        setAuthUserId(null)
        setIsPasswordRecovery(false)
        setUserEmail(null)
        try {
            await supabase.auth.signOut()
            await AsyncStorage.removeItem(AUTH_SESSION_KEY)
        } catch {
            // Do not block logout
        }
    }, [])

    const updateProfile = useCallback(async (profile: UserProfile) => {
        setUserProfile(profile)
        try {
            await AsyncStorage.setItem(
                AUTH_PROFILE_KEY,
                JSON.stringify(profile)
            )
        } catch {
            // Profile persistence failures do not block onboarding
        }
    }, [])

    const completeOnboarding = useCallback(async () => {
        setHasCompletedOnboarding(true)
        try {
            await AsyncStorage.setItem(AUTH_ONBOARDING_KEY, 'true')
        } catch {
            // Persistence failures do not block navigation
        }
    }, [])

    const value = useMemo<AuthContextValue>(
        () => ({
            isAuthenticated: !!userEmail,
            isPasswordRecovery,
            hasCompletedOnboarding,
            isLoading,
            authUserId,
            userProfile,
            userEmail,
            completeOnboarding,
            login,
            register,
            logout,
            updateProfile,
        }),
        [
            authUserId,
            completeOnboarding,
            hasCompletedOnboarding,
            isLoading,
            isPasswordRecovery,
            login,
            logout,
            register,
            updateProfile,
            userEmail,
            userProfile,
        ]
    )

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}
