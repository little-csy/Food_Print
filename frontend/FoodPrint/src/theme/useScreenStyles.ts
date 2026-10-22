import { useMemo } from 'react'
import { useTheme } from 'react-native-paper'

import { typography } from './tokens'

/**
 * Generate shared screen styles from the theme so each Screen need not repeat StyleSheet and color definitions.
 */
export function useScreenStyles() {
    const theme = useTheme()
    const colors = theme.colors

    return useMemo(
        () => ({
            screen: {
                flex: 1,
                alignItems: 'center' as const,
                justifyContent: 'center' as const,
                backgroundColor: colors.background,
            },
            title: {
                ...typography.title,
                marginBottom: 12,
                color: colors.onSurface,
            },
        }),
        [colors.background, colors.onSurface]
    )
}
