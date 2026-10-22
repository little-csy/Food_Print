/**
 * Combine React Native Paper and React Navigation themes: configure once and apply to both.
 * @see https://react-native-paper.github.io/docs/guides/theming-with-react-navigation
 */
import type { Theme as NavigationTheme } from '@react-navigation/native'
import {
  DarkTheme as NavigationDarkTheme,
  DefaultTheme as NavigationDefaultTheme,
} from '@react-navigation/native'
import { MD3DarkTheme, MD3LightTheme } from 'react-native-paper'

import { darkColors, lightColors } from './tokens'
import type { ColorScheme } from './tokens'

function mapToPaperColors(
  colors: typeof lightColors | typeof darkColors
) {
  return {
    primary: colors.primary,
    onPrimary: colors.primaryForeground,
    surface: colors.background,
    onSurface: colors.foreground,
    surfaceVariant: colors.muted,
    onSurfaceVariant: colors.mutedForeground,
    background: colors.background,
    error: colors.destructive,
    outline: colors.border,
  }
}

/** Light: Paper MD3 and custom colors for PaperProvider */
export const CombinedLightTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    ...mapToPaperColors(lightColors),
  },
}

/** Dark: Paper MD3 and custom colors for PaperProvider */
export const CombinedDarkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    ...mapToPaperColors(darkColors),
  },
}

/** Light: React Navigation theme structure with the same palette for NavigationContainer */
export const NavigationLightTheme: NavigationTheme = {
  ...NavigationDefaultTheme,
  colors: {
    ...NavigationDefaultTheme.colors,
    primary: lightColors.primary,
    background: lightColors.background,
    card: lightColors.card,
    text: lightColors.foreground,
    border: lightColors.border,
  },
}

/** Dark: React Navigation theme structure with the same palette */
export const NavigationDarkThemeMerged: NavigationTheme = {
  ...NavigationDarkTheme,
  colors: {
    ...NavigationDarkTheme.colors,
    primary: darkColors.primary,
    background: darkColors.background,
    card: darkColors.card,
    text: darkColors.foreground,
    border: darkColors.border,
  },
}

export function getCombinedTheme(scheme: ColorScheme) {
  return scheme === 'dark' ? CombinedDarkTheme : CombinedLightTheme
}

export function getNavigationTheme(scheme: ColorScheme): NavigationTheme {
  return scheme === 'dark' ? NavigationDarkThemeMerged : NavigationLightTheme
}
