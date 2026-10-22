import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import { Image, ScrollView, View } from 'react-native'
import { Card, IconButton, ProgressBar, Text } from 'react-native-paper'

import type { ImpactMetricsResponse, UserStatsDto } from '../../api/types'
import { buildChallengeStatusLabel, formatChallengeSummarySubtitle } from './data'
import { styles } from './styles'
import type { ChallengeSummary, GoalEntry } from './types'

const MOCK_STORE_BANNER_URI =
    'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=80&auto=format&fit=crop'

type ProfileOverviewProps = {
    avatarUrl: string
    challengeSummary: ChallengeSummary
    dashboardLoading: boolean
    dashboardNotice: string | null
    displayName: string
    goals: GoalEntry[]
    impactMetrics: ImpactMetricsResponse | null
    memberSinceLabel: string
    onOpenSettings: () => void
    palette: ReturnType<typeof import('./styles').getProfilePalette>
    stats: UserStatsDto | null
    themeColors: {
        onSurfaceVariant: string
        primary: string
    }
    userEmail: string | null
}

export function ProfileOverview({
    avatarUrl,
    challengeSummary,
    dashboardLoading,
    dashboardNotice,
    displayName,
    goals,
    impactMetrics,
    memberSinceLabel,
    onOpenSettings,
    palette,
    stats,
    themeColors,
    userEmail,
}: ProfileOverviewProps) {
    const statsItems = [
        {
            icon: 'trophy-outline' as const,
            label: 'Achievements',
            value: String(stats?.achievements ?? 0),
        },
        {
            icon: 'target' as const,
            label: 'Goals Met',
            value: String(stats?.goalsMet ?? 0),
        },
        {
            icon: 'calendar-month-outline' as const,
            label: 'Days Active',
            value: String(stats?.daysActive ?? 0),
        },
    ]
    const bannerMessage =
        dashboardNotice ??
        (dashboardLoading
            ? 'Syncing live profile data from the backend.'
            : 'Live community stats from FoodPrint backend.')
    const formatCompactValue = (
        value: number | null | undefined,
        suffix = '',
        fallback = '—'
    ) => {
        const numericValue =
            typeof value === 'number' ? value : value == null ? NaN : Number(value)

        if (!Number.isFinite(numericValue) || numericValue < 0) {
            return fallback
        }

        const rounded = Math.round(numericValue)
        let formatted = String(rounded)

        if (rounded >= 1_000_000) {
            const compact = Math.round((rounded / 100_000)) / 10
            formatted = `${Number.isInteger(compact) ? compact.toFixed(0) : compact.toFixed(1)}M`
        } else if (rounded >= 1_000) {
            const compact = Math.round((rounded / 100)) / 10
            formatted = `${Number.isInteger(compact) ? compact.toFixed(0) : compact.toFixed(1)}K`
        }

        return `${formatted}${suffix}`
    }
    const totalUsersValue = dashboardLoading && !impactMetrics
        ? '...'
        : formatCompactValue(impactMetrics?.totalUsers)
    const totalWeightLostValue = dashboardLoading && !impactMetrics
        ? '...'
        : formatCompactValue(impactMetrics?.totalWeightLost, ' lbs')

    return (
        <ScrollView
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
        >
            <View style={[styles.banner, { backgroundColor: palette.bannerSurface }]}>
                <View style={styles.bannerTitleRow}>
                    <MaterialCommunityIcons color="#FFFFFF" name="trending-up" size={20} />
                    <Text style={styles.bannerTitle}>FoodPrint Community Impact</Text>
                </View>
                <Text style={styles.bannerMeta}>{bannerMessage}</Text>

                <View style={styles.bannerStats}>
                    <View style={styles.bannerStatBlock}>
                        <Text style={styles.bannerStatValue}>{totalUsersValue}</Text>
                        <Text style={styles.bannerStatLabel}>
                            Registered users in the community
                        </Text>
                    </View>
                    <View style={styles.bannerStatBlock}>
                        <Text style={styles.bannerStatValue}>{totalWeightLostValue}</Text>
                        <Text style={styles.bannerStatLabel}>
                            Total weight lost together
                        </Text>
                    </View>
                </View>
            </View>

            <Card style={[styles.sectionCard, { backgroundColor: palette.surface }]}>
                <Card.Content style={styles.sectionContent}>
                    <View style={styles.profileHeader}>
                        <Image source={{ uri: avatarUrl }} style={styles.profileImage} />
                        <View style={styles.profileCopy}>
                            <Text style={styles.profileName}>{displayName}</Text>
                            <Text style={[styles.profileMeta, { color: palette.mutedText }]}>
                                {userEmail ?? 'No email available'}
                            </Text>
                            <Text style={[styles.profileMeta, { color: palette.mutedText }]}>
                                {memberSinceLabel}
                            </Text>
                        </View>
                        <IconButton
                            icon={() => (
                                <MaterialCommunityIcons
                                    color={themeColors.onSurfaceVariant}
                                    name="account-outline"
                                    size={24}
                                />
                            )}
                            onPress={onOpenSettings}
                        />
                    </View>

                    <View style={styles.statsGrid}>
                        {statsItems.map((item) => (
                            <View key={item.label} style={styles.statItem}>
                                <View
                                    style={[
                                        styles.statIcon,
                                        { backgroundColor: palette.accentSurface },
                                    ]}
                                >
                                    <MaterialCommunityIcons
                                        color={themeColors.primary}
                                        name={item.icon}
                                        size={24}
                                    />
                                </View>
                                <Text style={styles.statValue}>{item.value}</Text>
                                <Text style={[styles.statLabel, { color: palette.mutedText }]}>
                                    {item.label}
                                </Text>
                            </View>
                        ))}
                    </View>
                </Card.Content>
            </Card>

            <Card style={[styles.sectionCard, { backgroundColor: palette.surface }]}>
                <Card.Content style={styles.sectionContent}>
                    <Text style={styles.sectionTitle}>Current Goals</Text>
                    <View style={styles.goalList}>
                        {goals.map((goal) => (
                            <View key={goal.label} style={styles.goalBlock}>
                                <View style={styles.goalHeader}>
                                    <Text style={styles.goalLabel}>{goal.label}</Text>
                                    <Text
                                        style={[
                                            styles.goalCurrent,
                                            { color: palette.detailHighlight },
                                        ]}
                                    >
                                        {goal.current}
                                    </Text>
                                </View>
                                <ProgressBar
                                    color={palette.activeBar}
                                    progress={goal.progress}
                                    style={[
                                        styles.progressBar,
                                        { backgroundColor: palette.progressTrack },
                                    ]}
                                />
                            </View>
                        ))}
                    </View>
                </Card.Content>
            </Card>



            <Card style={[styles.sectionCard, { backgroundColor: palette.surface }]}>
                <Card.Content style={styles.sectionContent}>
                    <View style={styles.sectionHeader}>
                        <Text style={styles.sectionTitle}>Active Challenges</Text>
                        <MaterialCommunityIcons
                            color={palette.detailHighlight}
                            name="trophy-outline"
                            size={20}
                        />
                    </View>

                    <Text style={[styles.sectionCaption, { color: palette.mutedText }]}>
                        {formatChallengeSummarySubtitle(challengeSummary)}
                    </Text>

                    <View style={styles.challengeStatsRow}>
                        {[
                            {
                                label: 'Active',
                                value: String(challengeSummary.activeCount),
                            },
                            {
                                label: 'Completed',
                                value: String(challengeSummary.completedCount),
                            },
                            {
                                label: 'Best Streak',
                                value: `${challengeSummary.longestStreak}d`,
                            },
                        ].map((item) => (
                            <View
                                key={item.label}
                                style={[
                                    styles.challengeStatCard,
                                    {
                                        backgroundColor: palette.detailAccent,
                                        borderColor: palette.detailBorder,
                                    },
                                ]}
                            >
                                <Text style={styles.challengeStatValue}>{item.value}</Text>
                                <Text
                                    style={[
                                        styles.challengeStatLabel,
                                        { color: palette.mutedText },
                                    ]}
                                >
                                    {item.label}
                                </Text>
                            </View>
                        ))}
                    </View>

                    {challengeSummary.highlights.length === 0 ? (
                        <View
                            style={[
                                styles.emptyPanel,
                                {
                                    backgroundColor: palette.detailAccent,
                                    borderColor: palette.detailBorder,
                                },
                            ]}
                        >
                            <Text style={styles.emptyTitle}>No active challenges yet</Text>
                            <Text
                                style={[
                                    styles.emptySubtitle,
                                    { color: palette.mutedText },
                                ]}
                            >
                                Join one from the Community tab and your progress will show up
                                here.
                            </Text>
                        </View>
                    ) : (
                        <View style={styles.challengeHighlightList}>
                            {challengeSummary.highlights.map((highlight) => (
                                <View
                                    key={highlight.id}
                                    style={[
                                        styles.challengeHighlightCard,
                                        {
                                            backgroundColor: palette.detailAccent,
                                            borderColor: palette.detailBorder,
                                        },
                                    ]}
                                >
                                    <View style={styles.challengeHighlightCopy}>
                                        <View style={styles.challengeTitleRow}>
                                            <Text style={styles.challengeBadge}>
                                                {highlight.badgeIcon}
                                            </Text>
                                            <Text style={styles.challengeTitle}>
                                                {highlight.title}
                                            </Text>
                                        </View>
                                        <Text
                                            style={[
                                                styles.challengeStatus,
                                                { color: palette.detailHighlight },
                                            ]}
                                        >
                                            {highlight.statusLabel}
                                        </Text>
                                        <Text
                                            style={[
                                                styles.challengeMeta,
                                                { color: palette.mutedText },
                                            ]}
                                        >
                                            {buildChallengeStatusLabel(highlight)} ·{' '}
                                            {highlight.streakLabel}
                                        </Text>
                                    </View>
                                    <View style={styles.challengeProgressBlock}>
                                        <Text style={styles.challengeProgressValue}>
                                            {highlight.progressPercent}%
                                        </Text>
                                        <ProgressBar
                                            color={palette.activeBar}
                                            progress={highlight.progressPercent / 100}
                                            style={[
                                                styles.progressBar,
                                                styles.challengeProgressBar,
                                                { backgroundColor: palette.progressTrack },
                                            ]}
                                        />
                                    </View>
                                </View>
                            ))}
                        </View>
                    )}
                </Card.Content>
            </Card>

            <Card style={[styles.sectionCard, { backgroundColor: palette.surface }]}>
                <Card.Content style={styles.sectionContent}>
                    <View style={styles.sectionHeader}>
                        <Text style={styles.sectionTitle}>Featured Store</Text>
                        <MaterialCommunityIcons
                            color={palette.detailHighlight}
                            name="storefront-outline"
                            size={20}
                        />
                    </View>
                    <View
                        style={[
                            styles.mallAdImageWrap,
                            {
                                backgroundColor: palette.detailAccent,
                                borderColor: palette.detailBorder,
                            },
                        ]}
                    >
                        <Image
                            accessibilityLabel="Featured store promotion"
                            resizeMode="cover"
                            source={{ uri: MOCK_STORE_BANNER_URI }}
                            style={styles.mallAdBanner}
                        />
                    </View>
                </Card.Content>
            </Card>
        </ScrollView>
    )
}
