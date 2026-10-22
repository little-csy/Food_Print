/**
 * Design tokens: primary color #8ba888 for the combined Paper and Navigation themes.
 */
export const lightColors = {
  background: '#f5f7f4',
  foreground: '#2d3b2d',
  card: '#ffffff',
  primary: '#8ba888',
  primaryForeground: '#ffffff',
  muted: '#d4e0d3',
  mutedForeground: '#5a6b5a',
  border: 'rgba(139, 168, 136, 0.25)',
  destructive: '#c62828',
} as const

export const darkColors = {
  background: '#2d3b2d',
  foreground: '#e8ebe7',
  card: '#3d4d3d',
  primary: '#a8c4a5',
  primaryForeground: '#1e261e',
  muted: '#4a5c4a',
  mutedForeground: '#b8c9b6',
  border: 'rgba(232, 235, 231, 0.2)',
  destructive: '#ef5350',
} as const

export type ColorScheme = 'light' | 'dark'

export const spacing = { xs: 4, sm: 8, md: 16, lg: 24, xl: 32 } as const
export const typography = {
  title: { fontSize: 24, fontWeight: '600' as const },
  body: { fontSize: 16, fontWeight: '400' as const },
} as const
