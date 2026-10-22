import { StyleSheet } from 'react-native'
import type { MD3Theme as Theme } from 'react-native-paper'

export function getProfilePalette(theme: Theme) {
    return {
        accentSurface: '#E8F0E7',
        activeBar: '#8BA888',
        bannerSurface: '#8BA888',
        dangerText: '#D14343',
        detailAccent: '#F1F6F0',
        detailBorder: 'rgba(139, 168, 136, 0.18)',
        detailHighlight: '#7C9A79',
        detailStrongSurface: '#E1ECDF',
        mutedText: theme.colors.onSurfaceVariant,
        page: '#E8F0E7',
        premiumBadge: '#D1A52E',
        premiumSurface: '#FBF5DE',
        progressTrack: theme.colors.outlineVariant,
        surface: theme.colors.surface,
    }
}

export const styles = StyleSheet.create({
    achievementCard: {
        alignItems: 'center',
        borderRadius: 18,
        flexDirection: 'row',
        gap: 12,
        padding: 12,
    },
    achievementCopy: {
        flex: 1,
        gap: 2,
    },
    achievementIcon: {
        alignItems: 'center',
        backgroundColor: '#8BA888',
        borderRadius: 24,
        height: 48,
        justifyContent: 'center',
        width: 48,
    },
    achievementList: {
        gap: 12,
    },
    achievementSubtitle: {
        fontSize: 12,
        lineHeight: 18,
    },
    achievementTime: {
        fontSize: 12,
    },
    achievementTitle: {
        fontSize: 15,
        fontWeight: '600',
    },
    avatarActionList: {
        flex: 1,
        gap: 10,
    },
    avatarEditorImage: {
        borderRadius: 44,
        height: 88,
        width: 88,
    },
    avatarEditorRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 14,
    },
    averageText: {
        fontSize: 14,
        textAlign: 'center',
    },
    averageValue: {
        fontWeight: '700',
    },
    banner: {
        borderRadius: 24,
        gap: 18,
        padding: 20,
    },
    bannerMeta: {
        color: 'rgba(255,255,255,0.88)',
        fontSize: 12,
        lineHeight: 18,
    },
    bannerStatBlock: {
        flex: 1,
        gap: 4,
    },
    bannerStatLabel: {
        color: 'rgba(255,255,255,0.88)',
        fontSize: 12,
        lineHeight: 18,
    },
    bannerStatValue: {
        color: '#FFFFFF',
        fontSize: 26,
        fontWeight: '700',
    },
    bannerStats: {
        flexDirection: 'row',
        gap: 16,
    },
    bannerTitle: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: '600',
    },
    bannerTitleRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 8,
    },
    challengeBadge: {
        fontSize: 18,
    },
    challengeHighlightCard: {
        alignItems: 'center',
        borderRadius: 18,
        borderWidth: 1,
        flexDirection: 'row',
        gap: 12,
        padding: 14,
    },
    challengeHighlightCopy: {
        flex: 1,
        gap: 4,
    },
    challengeHighlightList: {
        gap: 12,
    },
    challengeMeta: {
        fontSize: 12,
        lineHeight: 18,
    },
    challengeProgressBar: {
        width: 76,
    },
    challengeProgressBlock: {
        alignItems: 'flex-end',
        gap: 8,
    },
    challengeProgressValue: {
        fontSize: 16,
        fontWeight: '700',
    },
    challengeStatCard: {
        borderRadius: 16,
        borderWidth: 1,
        flex: 1,
        gap: 4,
        padding: 12,
    },
    challengeStatLabel: {
        fontSize: 12,
    },
    challengeStatValue: {
        fontSize: 18,
        fontWeight: '700',
    },
    challengeStatsRow: {
        flexDirection: 'row',
        gap: 10,
    },
    challengeStatus: {
        fontSize: 13,
        fontWeight: '600',
    },
    challengeTitle: {
        flex: 1,
        fontSize: 15,
        fontWeight: '700',
    },
    challengeTitleRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 8,
    },
    chartBar: {
        borderRadius: 999,
        width: '100%',
    },
    chartColumn: {
        alignItems: 'center',
        flex: 1,
        gap: 6,
    },
    chartDay: {
        fontSize: 12,
    },
    chartRow: {
        alignItems: 'flex-end',
        flexDirection: 'row',
        gap: 6,
        justifyContent: 'space-between',
        minHeight: 182,
    },
    chartSummary: {
        borderRadius: 16,
        padding: 12,
    },
    chartSummaryTitle: {
        fontSize: 13,
        fontWeight: '600',
    },
    chartSummaryValue: {
        fontSize: 16,
        fontWeight: '700',
        marginTop: 2,
    },
    chartTrack: {
        borderRadius: 999,
        height: 132,
        justifyContent: 'flex-end',
        padding: 4,
        width: '100%',
    },
    chartValue: {
        fontSize: 10,
    },
    content: {
        gap: 16,
        padding: 20,
        paddingBottom: 32,
    },
    detailBody: {
        gap: 16,
        paddingBottom: 8,
    },
    detailCardFrame: {
        borderWidth: 1,
    },
    detailHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 4,
        marginBottom: 16,
    },
    detailHeaderCopy: {
        flex: 1,
        gap: 2,
        justifyContent: 'center',
    },
    detailHeaderSubtitle: {
        fontSize: 13,
        lineHeight: 18,
    },
    detailHeaderTitle: {
        fontSize: 18,
        fontWeight: '700',
        lineHeight: 24,
    },
    detailInfoCard: {
        borderRadius: 20,
    },
    detailLabel: {
        fontSize: 13,
    },
    detailRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 16,
        justifyContent: 'space-between',
    },
    detailValue: {
        flexShrink: 1,
        fontSize: 15,
        fontWeight: '600',
        textAlign: 'right',
    },
    divider: {
        marginVertical: 12,
    },
    emptyPanel: {
        borderRadius: 18,
        borderWidth: 1,
        gap: 6,
        padding: 14,
    },
    emptySubtitle: {
        fontSize: 13,
        lineHeight: 19,
    },
    emptyTitle: {
        fontSize: 15,
        fontWeight: '700',
    },
    goalBlock: {
        gap: 8,
    },
    goalCurrent: {
        fontSize: 13,
        fontWeight: '600',
    },
    goalHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    goalLabel: {
        fontSize: 14,
    },
    goalList: {
        gap: 16,
    },
    mallAdBanner: {
        aspectRatio: 2.25,
        width: '100%',
    },
    mallAdImageWrap: {
        borderRadius: 16,
        borderWidth: 1,
        overflow: 'hidden',
    },
    metricCard: {
        borderRadius: 20,
        width: '48%',
    },
    metricCardFrame: {
        borderWidth: 1,
    },
    metricGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 12,
        justifyContent: 'space-between',
    },
    metricLabel: {
        fontSize: 13,
    },
    metricValue: {
        fontSize: 22,
        fontWeight: '700',
        marginTop: 4,
    },
    modalCard: {
        alignSelf: 'center',
        borderRadius: 24,
        marginHorizontal: 20,
        marginVertical: 32,
        width: '100%',
    },
    modalCloseButton: {
        margin: 0,
    },
    modalFooter: {
        paddingBottom: 2,
        paddingTop: 0,
    },
    modalHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    modalInner: {
        borderRadius: 24,
        overflow: 'hidden',
        paddingBottom: 10,
        paddingHorizontal: 20,
        paddingTop: 24,
    },
    modalScroll: {
        maxHeight: 440,
    },
    modalScrollContent: {
        gap: 16,
        paddingBottom: 8,
    },
    modalTitle: {
        flex: 1,
        fontSize: 22,
        fontWeight: '700',
    },
    page: {
        flex: 1,
    },
    preferenceDescription: {
        fontSize: 12,
        lineHeight: 18,
    },
    preferencePanel: {
        borderRadius: 18,
        borderWidth: 1,
        padding: 12,
    },
    preferenceRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 16,
        justifyContent: 'space-between',
    },
    preferenceText: {
        flex: 1,
        gap: 4,
    },
    premiumRow: {
        borderRadius: 18,
    },
    profileCopy: {
        flex: 1,
        gap: 4,
    },
    profileHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 12,
        marginBottom: 12,
    },
    profileImage: {
        borderRadius: 40,
        height: 80,
        width: 80,
    },
    profileMeta: {
        fontSize: 13,
        lineHeight: 18,
    },
    profileName: {
        fontSize: 22,
        fontWeight: '700',
    },
    progressBar: {
        borderRadius: 999,
        height: 8,
    },
    sectionCaption: {
        fontSize: 13,
        lineHeight: 19,
    },
    sectionCard: {
        borderRadius: 24,
    },
    sectionContent: {
        gap: 16,
    },
    sectionHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: '700',
    },
    securityActions: {
        gap: 10,
    },
    settingsCopy: {
        flex: 1,
        gap: 2,
    },
    settingsIcon: {
        alignItems: 'center',
        borderRadius: 20,
        height: 40,
        justifyContent: 'center',
        width: 40,
    },
    settingsList: {
        gap: 8,
    },
    settingsPanel: {
        borderRadius: 18,
        borderWidth: 1,
    },
    settingsRow: {
        alignItems: 'center',
        borderRadius: 18,
        flexDirection: 'row',
        gap: 12,
        padding: 12,
    },
    settingsSubtitle: {
        fontSize: 12,
        lineHeight: 18,
    },
    settingsTitle: {
        fontSize: 15,
        fontWeight: '600',
    },
    signOutButton: {
        marginBottom: 0,
        marginTop: -4,
    },
    signOutText: {
        fontWeight: '600',
    },
    statIcon: {
        alignItems: 'center',
        borderRadius: 16,
        height: 48,
        justifyContent: 'center',
        marginBottom: 8,
        width: 48,
    },
    statItem: {
        alignItems: 'center',
        flex: 1,
    },
    statLabel: {
        fontSize: 12,
        textAlign: 'center',
    },
    statValue: {
        fontSize: 20,
        fontWeight: '700',
        marginBottom: 2,
    },
    statsGrid: {
        flexDirection: 'row',
        gap: 12,
    },
    subscriptionActions: {
        gap: 8,
        marginTop: 8,
    },
    subscriptionBody: {
        fontSize: 14,
        lineHeight: 20,
        textAlign: 'center',
    },
    subscriptionHero: {
        alignItems: 'center',
        borderRadius: 20,
        gap: 10,
        marginTop: 6,
        padding: 18,
    },
    subscriptionIcon: {
        alignItems: 'center',
        borderRadius: 22,
        height: 44,
        justifyContent: 'center',
        width: 44,
    },
    subscriptionItemText: {
        flex: 1,
        fontSize: 14,
        lineHeight: 20,
    },
    subscriptionList: {
        gap: 12,
        marginVertical: 18,
    },
    subscriptionRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 10,
    },
    subscriptionTitle: {
        fontSize: 20,
        fontWeight: '700',
        textAlign: 'center',
    },
    topicChip: {
        borderRadius: 999,
        borderWidth: 1,
        paddingHorizontal: 12,
        paddingVertical: 8,
    },
    topicChipList: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 10,
    },
    topicChipText: {
        fontSize: 13,
        fontWeight: '600',
    },
    wellnessCard: {
        borderRadius: 18,
        borderWidth: 1,
        flex: 1,
        gap: 12,
        minWidth: '48%',
        padding: 14,
    },
    wellnessGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 12,
    },
    wellnessHeader: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 10,
    },
    wellnessIcon: {
        alignItems: 'center',
        borderRadius: 16,
        height: 40,
        justifyContent: 'center',
        width: 40,
    },
    wellnessLabel: {
        fontSize: 15,
        fontWeight: '600',
    },
    wellnessMeta: {
        fontSize: 12,
        fontWeight: '600',
    },
    wellnessSubtitle: {
        fontSize: 12,
        lineHeight: 18,
        minHeight: 36,
    },
    wellnessValue: {
        fontSize: 24,
        fontWeight: '700',
    },
})
