import Ionicons from '@expo/vector-icons/Ionicons'
import { useHeaderHeight } from '@react-navigation/elements'
import React, { useEffect, useRef, useState } from 'react'
import {
    ActivityIndicator,
    KeyboardAvoidingView,
    Platform,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'

import { analyze } from '../api/ai'
import type { UserNutritionContext } from '../api/types'
import { StreamingAssistantMarkdown } from '../components/common/StreamingAssistantMarkdown'
import { useAuth } from '../context/AuthContext'

const COLORS = {
    bg: '#F4F7F2',
    card: '#FFFFFF',
    primary: '#97B08A',
    dark: '#1D3557',
    sub: '#7D8A97',
    iconBg: '#E8F0E4',
}

interface ChatMessage {
    id: string
    text: string
    sender: 'user' | 'assistant'
    timestamp: Date
}

export function AIChatScreen() {
    const scrollRef = useRef<ScrollView>(null)
    const headerHeight = useHeaderHeight()
    const { userProfile } = useAuth()

    const userContext: UserNutritionContext | undefined = userProfile
        ? {
              heightCm: userProfile.height,
              weightKg: userProfile.weight,
              age: userProfile.age,
              gender: userProfile.gender,
              goal: userProfile.goal,
              dailyCalorieTarget: userProfile.dailyCalories,
          }
        : undefined

    const [messages, setMessages] = useState<ChatMessage[]>([])
    const [input, setInput] = useState('')
    const [sending, setSending] = useState(false)

    useEffect(() => {
        const t = setTimeout(() => {
            scrollRef.current?.scrollToEnd({ animated: true })
        }, 80)
        return () => clearTimeout(t)
    }, [messages, sending])

    const handleSend = async () => {
        const messageText = input.trim()
        if (!messageText) return

        const userMessage: ChatMessage = {
            id: Date.now().toString(),
            text: messageText,
            sender: 'user',
            timestamp: new Date(),
        }
        setMessages((prev) => [...prev, userMessage])
        setInput('')
        setSending(true)

        try {
            const res = await analyze({ text: messageText, userContext })
            const data = res.data
            let reply: string

            if (!data) {
                reply = 'No response from AI'
            } else if (data.type === 'PROFILE_NEEDED' && data.profilePrompt) {
                reply = data.profilePrompt
            } else if (data.adviceText) {
                reply = data.adviceText
            } else if (data.type === 'FOOD_ANALYSIS' && data.foodAnalysis) {
                const s = data.foodAnalysis.summary
                const foods =
                    data.foodAnalysis.foods?.map((f) => f.nameEn || f.nameZh).join(', ') || ''
                reply = `Detected: ${foods}\nCalories: ${s?.totalCalories ?? '-'} kcal | Protein: ${s?.totalProteinG ?? '-'}g | Fat: ${s?.totalFatG ?? '-'}g | Carbs: ${s?.totalCarbsG ?? '-'}g\n\nTip: use **Home → Log meal** to save food logs with editable AI nutrition.`
            } else {
                reply = res.message || 'No response from AI'
            }
            const assistantMessage: ChatMessage = {
                id: (Date.now() + 1).toString(),
                text: reply,
                sender: 'assistant',
                timestamp: new Date(),
            }
            setMessages((prev) => [...prev, assistantMessage])
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Unknown error'
            setMessages((prev) => [
                ...prev,
                {
                    id: (Date.now() + 1).toString(),
                    text: `Error: ${msg}`,
                    sender: 'assistant',
                    timestamp: new Date(),
                },
            ])
        } finally {
            setSending(false)
        }
    }

    return (
        <SafeAreaView style={styles.safe} edges={['left', 'right']}>
            <KeyboardAvoidingView
                style={styles.flex}
                behavior={Platform.OS === 'ios' ? 'padding' : undefined}
                keyboardVerticalOffset={Platform.OS === 'ios' ? headerHeight : 0}
            >
                <ScrollView
                    ref={scrollRef}
                    style={styles.messages}
                    contentContainerStyle={styles.messagesContent}
                    contentInsetAdjustmentBehavior="never"
                    keyboardShouldPersistTaps="handled"
                    showsVerticalScrollIndicator={false}
                >
                    {messages.length === 0 ? (
                        <View style={styles.empty}>
                            <Text style={styles.emptyTitle}>Start a conversation</Text>
                            <Text style={styles.emptyBody}>
                                Food logging with AI nutrition estimates lives on the Home tab under
                                &quot;Log meal&quot;.
                            </Text>
                        </View>
                    ) : null}
                    {messages.map((message) => (
                        <View
                            key={message.id}
                            style={[
                                styles.messageRow,
                                message.sender === 'user' ? styles.userRow : styles.assistantRow,
                            ]}
                        >
                            <View
                                style={[
                                    styles.bubble,
                                    message.sender === 'user' ? styles.userBubble : styles.assistantBubble,
                                ]}
                            >
                                {message.sender === 'user' ? (
                                    <Text style={[styles.messageText, styles.userText]}>{message.text}</Text>
                                ) : (
                                    <StreamingAssistantMarkdown
                                        markdown={message.text}
                                        textColor={COLORS.dark}
                                        linkColor={COLORS.primary}
                                    />
                                )}
                            </View>
                        </View>
                    ))}
                    {sending ? (
                        <View style={[styles.messageRow, styles.assistantRow]}>
                            <View style={[styles.bubble, styles.assistantBubble, styles.thinkingBubble]}>
                                <ActivityIndicator size="small" color={COLORS.primary} />
                                <Text style={[styles.messageText, styles.thinkingLabel]}>Thinking…</Text>
                            </View>
                        </View>
                    ) : null}
                </ScrollView>

                <View style={styles.inputArea}>
                    <TextInput
                        style={styles.input}
                        value={input}
                        onChangeText={setInput}
                        placeholder="Message…"
                        placeholderTextColor={COLORS.sub}
                        multiline
                        maxLength={800}
                    />
                    <Pressable
                        style={[styles.sendButton, (!input.trim() || sending) && styles.sendButtonDisabled]}
                        onPress={() => void handleSend()}
                        disabled={!input.trim() || sending}
                    >
                        {sending ? (
                            <ActivityIndicator size="small" color="#fff" />
                        ) : (
                            <Ionicons
                                name="send"
                                size={18}
                                color={input.trim() && !sending ? '#fff' : COLORS.sub}
                            />
                        )}
                    </Pressable>
                </View>
            </KeyboardAvoidingView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    assistantBubble: {
        backgroundColor: COLORS.iconBg,
        borderBottomLeftRadius: 4,
    },
    assistantRow: {
        alignItems: 'flex-start',
    },
    bubble: {
        borderRadius: 18,
        maxWidth: '88%',
        paddingHorizontal: 14,
        paddingVertical: 10,
    },
    empty: {
        paddingHorizontal: 8,
        paddingVertical: 40,
    },
    emptyBody: {
        color: COLORS.sub,
        fontSize: 14,
        lineHeight: 20,
    },
    emptyTitle: {
        color: COLORS.dark,
        fontSize: 17,
        fontWeight: '700',
        marginBottom: 8,
    },
    flex: {
        flex: 1,
    },
    input: {
        backgroundColor: COLORS.bg,
        borderRadius: 20,
        color: COLORS.dark,
        flex: 1,
        fontSize: 15,
        marginRight: 8,
        maxHeight: 120,
        minHeight: 44,
        paddingHorizontal: 16,
        paddingVertical: 10,
        textAlignVertical: 'center',
    },
    inputArea: {
        alignItems: 'center',
        backgroundColor: COLORS.card,
        borderTopColor: '#F0F0F0',
        borderTopWidth: 1,
        flexDirection: 'row',
        paddingHorizontal: 12,
        paddingVertical: 10,
    },
    messageRow: {
        marginBottom: 12,
    },
    messageText: {
        fontSize: 15,
        lineHeight: 21,
    },
    messages: {
        flex: 1,
    },
    messagesContent: {
        paddingBottom: 12,
        paddingHorizontal: 16,
        paddingTop: 8,
    },
    safe: {
        backgroundColor: COLORS.bg,
        flex: 1,
    },
    sendButton: {
        alignItems: 'center',
        backgroundColor: COLORS.primary,
        borderRadius: 22,
        height: 44,
        justifyContent: 'center',
        width: 44,
    },
    sendButtonDisabled: {
        backgroundColor: COLORS.iconBg,
    },
    thinkingBubble: {
        alignItems: 'center',
        flexDirection: 'row',
        gap: 10,
    },
    thinkingLabel: {
        color: COLORS.dark,
        opacity: 0.85,
    },
    userBubble: {
        backgroundColor: COLORS.primary,
        borderBottomRightRadius: 4,
    },
    userRow: {
        alignItems: 'flex-end',
    },
    userText: {
        color: '#fff',
    },
})
