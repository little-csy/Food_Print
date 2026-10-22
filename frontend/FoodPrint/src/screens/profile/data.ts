import type { UserProfile } from '../../context/AuthContext'
import type {
    ChallengeHighlight,
    ChallengeSummary,
    GoalEntry,
    SettingsEntry,
} from './types'

export const DEFAULT_PROFILE_AVATAR_URL =
    'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop'

export function getDisplayName(
    nickname: string | null | undefined,
    email: string | null
) {
    if (nickname?.trim()) {
        return nickname.trim()
    }

    if (!email) {
        return 'FoodPrint User'
    }

    const localPart = email.split('@')[0] ?? ''
    const words = localPart
        .split(/[._-]/)
        .map((part) => part.trim())
        .filter(Boolean)

    if (words.length === 0) {
        return email
    }

    return words
        .map((word) => word[0]?.toUpperCase() + word.slice(1).toLowerCase())
        .join(' ')
}

export function formatMemberSince(createdAt: string | undefined) {
    if (!createdAt) {
        return 'Member since recently'
    }

    const date = new Date(createdAt)
    if (Number.isNaN(date.getTime())) {
        return 'Member since recently'
    }

    const monthNames = [
        'Jan',
        'Feb',
        'Mar',
        'Apr',
        'May',
        'Jun',
        'Jul',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec',
    ]

    return `Member since ${monthNames[date.getUTCMonth()]} ${date.getUTCFullYear()}`
}

export function formatActivityLevel(level: 'low' | 'medium' | 'high' | undefined) {
    if (!level) {
        return 'Moderate activity'
    }

    if (level === 'low') {
        return 'Light activity'
    }

    if (level === 'high') {
        return 'High activity'
    }

    return 'Moderate activity'
}

export function formatGoal(goal: 'lose' | 'maintain' | 'gain' | undefined) {
    if (!goal) {
        return 'Maintain weight'
    }

    if (goal === 'lose') {
        return 'Lose weight'
    }

    if (goal === 'gain') {
        return 'Gain muscle'
    }

    return 'Maintain weight'
}

export function formatDietaryPreference(value: string | undefined) {
    const normalized = value?.trim().toLowerCase()
    if (!normalized || normalized === 'none') {
        return 'No dietary preference'
    }

    return normalized
        .split(/[_\s-]+/)
        .filter(Boolean)
        .map((part) => part[0]?.toUpperCase() + part.slice(1))
        .join(' ')
}

export function getGoals(userProfile: UserProfile | null): GoalEntry[] {
    const calorieTarget = userProfile?.dailyCalories ?? 2000
    const proteinTarget = Math.max(90, Math.round((userProfile?.weight ?? 75) * 2))
    const dietPreference = formatDietaryPreference(userProfile?.dietPreference)

    return [
        {
            current: `${calorieTarget} kcal`,
            label: 'Daily Calorie Target',
            progress: calorieTarget > 0 ? 1 : 0.3,
        },
        {
            current: `${proteinTarget} g`,
            label: 'Protein Intake',
            progress: userProfile?.weight ? 1 : 0.4,
        },
        {
            current: dietPreference,
            label: 'Dietary Preference',
            progress: userProfile?.dietPreference ? 1 : 0.35,
        },
    ]
}

export function formatChallengeSummarySubtitle(summary: ChallengeSummary) {
    if (summary.activeCount === 0) {
        return 'No active challenges yet.'
    }

    return `${summary.activeCount} active | ${summary.completedCount} completed | longest streak ${summary.longestStreak} days`
}

export function buildChallengeStatusLabel(highlight: ChallengeHighlight) {
    const checkinLabel = highlight.todayCheckedIn ? 'Checked in today' : 'Check-in pending'
    const daysLabel =
        highlight.daysRemaining > 0
            ? `${highlight.daysRemaining} days left`
            : highlight.daysRemaining === 0
              ? 'Final day'
              : 'Ends today'
    return `${checkinLabel} · ${daysLabel}`
}

export function getSettingsEntries(userProfile: UserProfile | null): SettingsEntry[] {
    const weight = userProfile?.weight
    const height = userProfile?.height
    const calorieTarget = userProfile?.dailyCalories

    return [
        {
            description: 'Nickname, avatar, and account identity',
            icon: 'account-circle-outline',
            title: 'Personal Information',
        },
        {
            description:
                typeof weight === 'number' &&
                weight > 0 &&
                typeof height === 'number' &&
                height > 0
                    ? `Weight: ${weight}kg | Height: ${height}cm`
                    : 'Weight, height, age, and gender',
            icon: 'scale-bathroom',
            title: 'Body Metrics',
        },
        {
            description:
                userProfile?.goal && typeof calorieTarget === 'number' && calorieTarget > 0
                    ? `${formatGoal(userProfile.goal)} | ${calorieTarget} kcal target`
                    : 'Goal, calorie target, and diet preference',
            icon: 'heart-pulse',
            title: 'Health Goals',
        },
        {
            description: 'Manage alerts and reminders',
            icon: 'bell-outline',
            title: 'Notifications',
        },
        {
            description: 'Control your data',
            icon: 'shield-check-outline',
            title: 'Privacy & Security',
        },
        {
            description: 'Change your password',
            icon: 'lock-outline',
            title: 'Password',
        },
    ]
}
