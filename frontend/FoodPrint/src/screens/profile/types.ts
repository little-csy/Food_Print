import type MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'

export type GoalEntry = {
    current: string
    label: string
    progress: number
}

export type SettingsTitle =
    | 'Personal Information'
    | 'Body Metrics'
    | 'Health Goals'
    | 'Notifications'
    | 'Privacy & Security'
    | 'Password'

export type SettingsEntry = {
    description: string
    icon: keyof typeof MaterialCommunityIcons.glyphMap
    title: SettingsTitle
}

export type NotificationPreferences = {
    mealReminders: boolean
    streakAlerts: boolean
    weeklySummary: boolean
}

export type PrivacyPreferences = {
    analyticsSharing: boolean
    profileVisibility: boolean
}

export type ChallengeHighlight = {
    badgeIcon: string
    daysRemaining: number
    id: string
    progressPercent: number
    statusLabel: string
    streakLabel: string
    title: string
    todayCheckedIn: boolean
}

export type ChallengeSummary = {
    activeCount: number
    completedCount: number
    highlights: ChallengeHighlight[]
    longestStreak: number
}
