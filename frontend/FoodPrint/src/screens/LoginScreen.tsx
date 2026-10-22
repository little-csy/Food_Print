import type { StackScreenProps } from '@react-navigation/stack'
import { StatusBar } from 'expo-status-bar'
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import { useEffect, useState } from 'react'
import {
    Keyboard,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    View,
} from 'react-native'
import { Button, Text, TextInput, useTheme } from 'react-native-paper'

import { isValidEmail, useAuth } from '../context/AuthContext'
import type { AuthStackParamList } from '../navigation/types'
import {
    AUTH_PRIMARY_BUTTON,
    authSharedStyles,
    useAuthScreenTheme,
} from './auth/authScreenShared'

type Props = StackScreenProps<AuthStackParamList, 'Login'>

const INSIGHTS = [
    'Small changes in your diet can lead to big improvements in your health.',
    'Eating mindfully helps you enjoy your food and recognize when you\'re full.',
    'Hydration is key—drink water throughout the day for optimal health.',
    'Balanced meals with protein, carbs, and healthy fats fuel your body best.',
    'Quality sleep and nutrition go hand in hand for overall wellness.',
    'Listening to your body\'s hunger cues is the first step to mindful eating.',
    'Consistency, not perfection, is what creates lasting healthy habits.',
    'Colorful plates mean diverse nutrients—eat the rainbow every day.',
    'Your nutrition journey is unique—focus on progress, not comparison.',
    'Whole foods nourish your body better than processed alternatives.',
    'Meal planning saves time and helps you make healthier choices.',
    'Moderation and balance are more sustainable than restriction.',
]

export function LoginScreen({ navigation }: Props) {
    const theme = useTheme()
    const themed = useAuthScreenTheme(theme)
    const { login } = useAuth()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [todayUsers, setTodayUsers] = useState(0)
    const [insight, setInsight] = useState('')
    const [displayedInsight, setDisplayedInsight] = useState('')

    useEffect(() => {
        const randomUsers = Math.floor(Math.random() * 500) + 200
        const randomInsight = INSIGHTS[Math.floor(Math.random() * INSIGHTS.length)]
        setTodayUsers(randomUsers)
        setInsight(randomInsight)
    }, [])

    useEffect(() => {
        if (!insight) {
            return
        }

        let index = 0
        setDisplayedInsight('')
        const timer = setInterval(() => {
            if (index <= insight.length) {
                setDisplayedInsight(insight.slice(0, index))
                index += 1
                return
            }

            clearInterval(timer)
        }, 30)

        return () => clearInterval(timer)
    }, [insight])

    const onLogin = async () => {
        setError('')

        if (!email.trim() || !password) {
            setError('Please enter email and password')
            return
        }

        if (!isValidEmail(email)) {
            setError('Please enter a valid email address')
            return
        }

        setSubmitting(true)
        const result = await login(email, password)
        if (!result.success) {
            setError(result.message ?? 'Login failed')
        }
        setSubmitting(false)
    }

    return (
        <KeyboardAvoidingView
            behavior={Platform.select({ ios: 'padding', android: undefined })}
            style={[authSharedStyles.page, themed.page]}
        >
            <ScrollView
                contentContainerStyle={authSharedStyles.scrollContent}
                horizontal={false}
                keyboardDismissMode={Platform.OS === 'ios' ? 'interactive' : 'on-drag'}
                keyboardShouldPersistTaps="handled"
                onScrollBeginDrag={Keyboard.dismiss}
                scrollEnabled={false}
                showsHorizontalScrollIndicator={false}
                showsVerticalScrollIndicator={false}
            >
                <View style={authSharedStyles.brandSection}>
                    <Text style={authSharedStyles.brandTitle}>FoodPrint</Text>
                    <View style={authSharedStyles.statRow}>
                        <MaterialCommunityIcons
                            color={themed.statText.color}
                            name="account-group-outline"
                            size={20}
                        />
                        <Text style={[authSharedStyles.statText, themed.statText]}>
                            {todayUsers} people started their nutrition journey today
                        </Text>
                    </View>
                    <View style={authSharedStyles.insightCard}>
                        <Text style={authSharedStyles.insightText}>
                            {displayedInsight}
                            <Text style={authSharedStyles.cursor}>|</Text>
                        </Text>
                    </View>
                </View>

                <View style={[authSharedStyles.formCard, themed.formCard]}>
                    <Text style={authSharedStyles.formTitle}>Welcome Back</Text>

                    {error ? (
                        <View style={[authSharedStyles.errorBox, themed.errorBox]}>
                            <MaterialCommunityIcons
                                color={themed.errorText.color}
                                name="alert-circle-outline"
                                size={20}
                            />
                            <Text style={[authSharedStyles.errorText, themed.errorText]}>
                                {error}
                            </Text>
                        </View>
                    ) : null}

                    <TextInput
                        autoCapitalize="none"
                        autoComplete="email"
                        keyboardType="email-address"
                        label="Email"
                        left={<TextInput.Icon icon="email-outline" />}
                        mode="outlined"
                        onChangeText={setEmail}
                        style={authSharedStyles.input}
                        textContentType="emailAddress"
                        value={email}
                    />
                    <TextInput
                        autoComplete="password"
                        label="Password"
                        left={<TextInput.Icon icon="lock-outline" />}
                        mode="outlined"
                        onChangeText={setPassword}
                        onSubmitEditing={Keyboard.dismiss}
                        returnKeyType="done"
                        secureTextEntry
                        style={authSharedStyles.input}
                        textContentType="password"
                        value={password}
                    />

                    <Button
                        compact
                        mode="text"
                        onPress={() => navigation.navigate('ForgotPassword')}
                        style={authSharedStyles.forgotLink}
                    >
                        <Text>Forgot Password?</Text>
                    </Button>

                    <Button
                        buttonColor={AUTH_PRIMARY_BUTTON}
                        disabled={submitting}
                        loading={submitting}
                        mode="contained"
                        onPress={() => void onLogin()}
                        style={authSharedStyles.primaryButton}
                    >
                        <Text>{submitting ? 'Signing In...' : 'Sign In'}</Text>
                    </Button>

                    <View style={authSharedStyles.linkRow}>
                        <Text>Don&apos;t have an account?</Text>
                        <Button
                            compact
                            mode="text"
                            onPress={() => navigation.navigate('Register')}
                        >
                            <Text>Sign Up</Text>
                        </Button>
                    </View>
                </View>
                <View style={authSharedStyles.footer}>
                    <Text style={authSharedStyles.footerText}>
                        By continuing, you agree to our Terms of Service and Privacy Policy
                    </Text>
                </View>
            </ScrollView>
            <StatusBar style="auto" />
        </KeyboardAvoidingView>
    )
}
