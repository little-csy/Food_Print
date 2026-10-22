import { useMemo } from 'react'
import { StyleSheet } from 'react-native'
import type { MD3Theme } from 'react-native-paper'

/** Shared authentication colors, independent of the theme */
export const AUTH_PAGE_BG = '#E8F0E7'
export const AUTH_PRIMARY_BUTTON = '#8BA888'
export const AUTH_BRAND_TITLE = '#2F3E2D'
export const AUTH_FORM_TITLE = '#324032'
export const AUTH_SUBTITLE = '#5A6B58'
export const AUTH_STAT_MUTED = '#7C9A79'
export const AUTH_FOOTER_TEXT = '#66756A'
export const AUTH_INSIGHT_CARD_BG = '#F5F8F5'
export const AUTH_INSIGHT_TEXT = '#4E5D4D'
export const AUTH_RULE_CARD_BG = '#F5F8F5'

/** Shared static screen layouts: spacing, corner radius, shadows, etc. */
export const authSharedStyles = StyleSheet.create({
    brandSection: {
        marginBottom: 20,
    },
    brandTitle: {
        color: AUTH_BRAND_TITLE,
        fontSize: 38,
        fontWeight: '700',
        marginBottom: 14,
        textAlign: 'center',
    },
    codeActionButton: {
        borderRadius: 14,
        marginTop: -2,
    },
    codeActionButtonLabel: {
        fontSize: 14,
        fontWeight: '600',
    },
    cursor: {
        color: AUTH_PRIMARY_BUTTON,
    },
    errorBox: {
        alignItems: 'flex-start',
        borderRadius: 12,
        flexDirection: 'row',
        gap: 8,
        marginBottom: 14,
        padding: 12,
    },
    errorText: {
        flex: 1,
        fontSize: 13,
        lineHeight: 18,
    },
    footer: {
        marginTop: 16,
    },
    footerText: {
        color: AUTH_FOOTER_TEXT,
        fontSize: 12,
        lineHeight: 17,
        textAlign: 'center',
    },
    forgotLink: {
        alignSelf: 'flex-end',
        marginBottom: 8,
    },
    formCard: {
        borderRadius: 24,
        elevation: 3,
        padding: 22,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.08,
        shadowRadius: 10,
    },
    formTitle: {
        color: AUTH_FORM_TITLE,
        fontSize: 28,
        fontWeight: '600',
        marginBottom: 6,
    },
    infoBox: {
        alignItems: 'flex-start',
        borderRadius: 12,
        flexDirection: 'row',
        gap: 8,
        marginBottom: 14,
        padding: 12,
    },
    infoText: {
        flex: 1,
        fontSize: 13,
        lineHeight: 18,
    },
    input: {
        marginBottom: 12,
    },
    insightCard: {
        alignItems: 'center',
        backgroundColor: AUTH_INSIGHT_CARD_BG,
        borderRadius: 18,
        justifyContent: 'center',
        minHeight: 86,
        padding: 14,
    },
    insightText: {
        color: AUTH_INSIGHT_TEXT,
        fontSize: 13,
        fontStyle: 'italic',
        lineHeight: 19,
        textAlign: 'center',
    },
    linkRow: {
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'center',
        marginTop: 12,
    },
    page: {
        flex: 1,
    },
    primaryButton: {
        borderRadius: 14,
        marginTop: 8,
        paddingVertical: 2,
    },
    ruleCard: {
        backgroundColor: AUTH_RULE_CARD_BG,
        borderRadius: 14,
        marginBottom: 12,
        marginTop: 4,
        padding: 12,
    },
    ruleTitle: {
        color: AUTH_FORM_TITLE,
        fontSize: 13,
        fontWeight: '600',
        marginBottom: 6,
    },
    scrollContent: {
        flexGrow: 1,
        justifyContent: 'center',
        paddingHorizontal: 18,
        paddingVertical: 24,
    },
    statRow: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 6,
        justifyContent: 'center',
        marginBottom: 10,
    },
    statText: {
        fontSize: 13,
        fontWeight: '500',
        textAlign: 'center',
    },
    subTitle: {
        color: AUTH_SUBTITLE,
        fontSize: 14,
        lineHeight: 20,
        marginBottom: 16,
    },
})

export function useAuthScreenTheme(theme: MD3Theme) {
    return useMemo(
        () => ({
            errorBox: { backgroundColor: theme.colors.errorContainer },
            errorText: { color: theme.colors.onErrorContainer },
            formCard: { backgroundColor: theme.colors.surface },
            infoBox: { backgroundColor: theme.colors.secondaryContainer },
            infoText: { color: theme.colors.onSecondaryContainer },
            page: { backgroundColor: AUTH_PAGE_BG },
            passRuleDone: { color: theme.colors.primary },
            passRulePending: { color: theme.colors.onSurfaceVariant },
            statText: { color: AUTH_STAT_MUTED },
        }),
        [
            theme.colors.errorContainer,
            theme.colors.onErrorContainer,
            theme.colors.onSecondaryContainer,
            theme.colors.onSurfaceVariant,
            theme.colors.primary,
            theme.colors.secondaryContainer,
            theme.colors.surface,
        ]
    )
}
