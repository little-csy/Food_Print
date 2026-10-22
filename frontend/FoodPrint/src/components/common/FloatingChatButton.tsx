import Ionicons from '@expo/vector-icons/Ionicons'
import React from 'react'
import { Pressable, StyleSheet } from 'react-native'

// ─── Types ───────────────────────────────────────────────────
interface FloatingChatButtonProps {
    onPress: () => void
    bottom?: number
    right?: number
}

// ─── Colors ──────────────────────────────────────────────────
const COLORS = {
    primary: '#97B08A',
    primaryDark: '#7A9A6D',
    shadow: '#000',
}

// ─── Component ───────────────────────────────────────────────
export function FloatingChatButton({
    onPress,
    bottom = 32,
    right = 20,
}: FloatingChatButtonProps) {
    return (
        <Pressable
            style={({ pressed }) => [
                styles.fab,
                { bottom, right },
                pressed && styles.fabPressed,
            ]}
            onPress={onPress}
        >
            <Ionicons
                name="chatbubble-ellipses-outline"
                size={22}
                color="#fff"
            />
        </Pressable>
    )
}

// ─── Styles ──────────────────────────────────────────────────
const styles = StyleSheet.create({
    fab: {
        alignItems: 'center',
        backgroundColor: COLORS.primary,
        borderRadius: 26,
        elevation: 4,
        height: 52,
        justifyContent: 'center',
        position: 'absolute',
        shadowColor: COLORS.shadow,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.12,
        shadowRadius: 5,
        width: 52,
    },
    fabPressed: {
        backgroundColor: COLORS.primaryDark,
    },
})