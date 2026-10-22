# FoodPrint Style Management Guide

This document explains theme and style organization, how to change theme colors, and how screens reuse styles without duplicating configuration across files.

---

## 1. Overview and Principles

- **Single source of truth:** colors, spacing, fonts, and other tokens live in `src/theme/tokens.ts`; one change applies globally.
- **Consistent themes:** React Native Paper (buttons, cards, bottom bars) and React Navigation (navigation bars, headers, tabs) share colors combined through `combinedTheme` and passed to their providers.
- **Avoid hardcoded colors:** each Screen gets styles and colors through `useScreenStyles()` or Paper `useTheme()` rather than defining values such as `backgroundColor: '#fff'`.

---

## 2. Directory Structure

```
src/theme/
├── tokens.ts         # Design tokens: colors, spacing, fonts
├── combinedTheme.ts  # Combined Paper and Navigation themes
├── useScreenStyles.ts# Shared screen styles Hook
└── index.ts          # Unified exports
```

| File | Purpose |
|------|------|
| `tokens.ts` | Define `lightColors` / `darkColors`, `spacing`, and `typography` for themes and styles |
| `combinedTheme.ts` | Generate Paper and Navigation themes from tokens; export `getCombinedTheme` / `getNavigationTheme` |
| `useScreenStyles.ts` | Generate shared screen styles such as `screen` and `title` from the current theme |
| `index.ts` | Export the public theme API |

---

## 3. Design Tokens

### 3.1 Colors

**Light theme `lightColors`**

| Key | Meaning | Typical usage |
|----|------|----------|
| `background` | Screen background | Fullscreen container, Tab Content area |
| `foreground` | Primary text color | Headings and body text |
| `card` | Card background | Card and list item backgrounds |
| `primary` | Primary color | Buttons and selected Tab, Links |
| `primaryForeground` | Text on the primary color | Primary button text |
| `muted` | Muted background | Secondary areas and input backgrounds |
| `mutedForeground` | Muted text | Descriptions and placeholders |
| `border` | Border | Dividers and input borders |
| `destructive` | Destructive actions | Delete actions and error messages |

**Dark theme `darkColors`** uses the same keys as the table above, with different values, and follows system dark mode.

To change theme colors, update the relevant hex or rgba values in `lightColors` / `darkColors` in `tokens.ts`; no other files need changes.

### 3.2 Spacing and Typography

```ts
// tokens.ts
spacing   // xs: 4, sm: 8, md: 16, lg: 24, xl: 32(Unit px)
typography // title: { fontSize: 24, fontWeight: '600' }, body: { fontSize: 16, ... }
```

Use shared tokens such as `spacing.md` and `typography.title` for consistent spacing and font sizes instead of magic numbers.

---

## 4. Combined Theme (Paper + Navigation)

### 4.1 Flow

1. The **App root** (`App.tsx`) selects `'light' | 'dark'` based on the system appearance.
2. Call `getCombinedTheme(scheme)` and pass the Paper theme to `<PaperProvider theme={...}>`.
3. Call `getNavigationTheme(scheme)` and pass the Navigation theme to `<NavigationContainer theme={...}>`.

Both themes use the same `tokens`, keeping tabs, headers, cards, buttons, and other elements visually consistent.

### 4.2 Mapping

In `combinedTheme.ts`, `mapToPaperColors(colors)` maps tokens to Paper MD3 color keys, for example:

- `primary` → Paper `primary` / `onPrimary`
- `background` / `foreground` → `surface` / `onSurface`
- `muted` / `mutedForeground` → `surfaceVariant` / `onSurfaceVariant`
- `destructive` → `error`

The Navigation theme uses keys such as `background`, `card`, `text`, `primary`, and `border`, matching tokens one to one.

When adding or renaming tokens, update the mapping or combined object in `combinedTheme.ts` if they should affect Paper or Navigation.

---

## 5. Screen Styles (useScreenStyles)

### 5.1 Purpose

`useScreenStyles()` generates **shared screen-level styles** from the current Paper theme, avoiding repeated `StyleSheet` and color definitions in each Screen.

### 5.2 Return Value

| Key | Description |
|----|------|
| `screen` | Centered fullscreen container: `flex: 1`, `backgroundColor: theme.colors.background`, centered alignment |
| `title` | Screen heading using `typography.title`, `color: theme.colors.onSurface`, and bottom margin |

### 5.3 Usage

```tsx
// Inside any Screen component
import { useScreenStyles } from '../theme'

export function HomeScreen() {
  const styles = useScreenStyles()
  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Home</Text>
      {/* Other content */}
    </View>
  )
}
```

Screens do not need separate `backgroundColor`, heading font sizes, or colors; these come from the theme.

### 5.4 Extension

For additional shared styles such as `subtitle`, `card`, or `input`, add fields to the `useMemo` return object in `useScreenStyles.ts`, using `theme.colors`, `typography`, and `spacing`.

---

## 6. Using Themes in Components

### 6.1 Use Only Shared Screen Styles

- Use **`useScreenStyles()`** without separate color references (see the previous section).

### 6.2 Individual Colors or the Paper Theme

- Use Paper **`useTheme()`**:

```tsx
import { useTheme } from 'react-native-paper'

const { colors } = useTheme()
// colors.primary, colors.background, colors.onSurface Etc.
```

- Use **tokens** for utilities or non-UI logic without depending on a Provider:

```tsx
import { lightColors, spacing } from '../theme'
// or select based on light or dark mode lightColors / darkColors
```

### 6.3 Navigation Bar / Tab Bar

- `AppNavigator` reads `colors` through Paper `useTheme()` and sets `tabBarStyle`, `tabBarActiveTintColor`, `headerStyle`, and other properties to match the global theme.

---

## 7. Theme Changes and Extensions

| Requirement | Where to change |
|------|----------|
| Change primary, background, text, or other colors | `lightColors` / `darkColors` in `src/theme/tokens.ts` |
| Change shared spacing or font sizes | `spacing` / `typography` in `src/theme/tokens.ts` |
| Add a global color key | Add it to `tokens`, then map it in `mapToPaperColors` or the Navigation theme in `combinedTheme.ts` |
| Add shared screen styles | Add a return field in `src/theme/useScreenStyles.ts` using `theme.colors` and tokens |
| Change only Paper component appearance | Override the relevant `colors` key in `CombinedLightTheme` / `CombinedDarkTheme` in `combinedTheme.ts` |
| Change only Navigation appearance | Override the relevant `colors` key in `NavigationLightTheme` / `NavigationDarkThemeMerged` |

---

## 8. Design Specification / CSS Variable Mapping

If the design specification or Web app uses CSS variables such as `--primary` and `--background`, maintain a mapping table in documentation or comments, for example:

| CSS Variable | tokens Key |
|----------|------------|
| `--background` | `lightColors.background` / `darkColors.background` |
| `--foreground` | `foreground` |
| `--primary` | `primary` |
| `--primary-foreground` | `primaryForeground` |
| `--muted` | `muted` |
| `--muted-foreground` | `mutedForeground` |
| `--border` | `border` |
| `--destructive` | `destructive` |

When the design changes, update the corresponding values in `tokens.ts`.

---

## 9. Frequently Asked Questions

**Q: Can a screen use a background color different from the theme?**  
Yes. Get `colors` with `useTheme()`, keep most theme styles, and override a single container with `style={{ backgroundColor: '...' }}`.

**Q: How can I add a third theme, such as high contrast?**
Add a palette such as `highContrastColors` in `tokens.ts` and a matching theme object in `combinedTheme.ts`. In App, select `scheme` using custom state or Context and pass it to `getCombinedTheme` / `getNavigationTheme`.

**Q: Why not use StyleSheet.create inside useScreenStyles?**
Plain style objects make it easy to use `theme.colors` and update with the theme. For performance optimization, use `StyleSheet.create` for stable structures inside the Hook and inline styles or useMemo for parts depending on `colors`.
