import type { StackScreenProps } from '@react-navigation/stack'
import { StatusBar } from 'expo-status-bar'
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import { useState } from 'react'
import {
    Keyboard,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    View,
} from 'react-native'
import {
    Button,
    HelperText,
    Text,
    TextInput,
    useTheme,
} from 'react-native-paper'

import {
    evaluatePasswordStrength,
    isValidEmail,
    useAuth,
} from '../context/AuthContext'
import type { AuthStackParamList } from '../navigation/types'
import {
    AUTH_PRIMARY_BUTTON,
    authSharedStyles,
    useAuthScreenTheme,
} from './auth/authScreenShared'

type Props = StackScreenProps<AuthStackParamList, 'Register'>

export function RegisterScreen({ navigation }: Props) {
    const theme = useTheme()
    const themed = useAuthScreenTheme(theme)
    const { register } = useAuth()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [error, setError] = useState('')
    const [submitting, setSubmitting] = useState(false)

    const emailTouched = email.length > 0
    const emailValid = isValidEmail(email)
    const passwordStrength = evaluatePasswordStrength(password)

    const onRegister = async () => {
        Keyboard.dismiss()
        setError('')

        if (!email.trim()) {
            setError('Please enter your email')
            return
        }
        if (!isValidEmail(email)) {
            setError('Please enter a valid email address')
            return
        }
        if (password.length < 8) {
            setError('Password must be at least 8 characters with upper, lower, number and special character')
            return
        }
        if (!passwordStrength.isStrong) {
            setError('Please meet all password requirements (8+ chars, upper, lower, number, special character)')
            return
        }
        if (password !== confirmPassword) {
            setError('Passwords do not match')
            return
        }

        setSubmitting(true)
        const result = await register(email, password)
        setSubmitting(false)

        if (!result.success) {
            setError(result.message ?? 'Registration failed')
            return
        }
        if (result.message && result.message.includes('verify your email')) {
            setError(result.message)
            return
        }
    }

    return (
        <KeyboardAvoidingView
            behavior={Platform.select({ ios: 'padding', android: undefined })}
            style={[authSharedStyles.page, themed.page]}
        >
            <ScrollView
                contentContainerStyle={authSharedStyles.scrollContent}
                horizontal={false}
                keyboardDismissMode={
                    Platform.OS === 'ios' ? 'interactive' : 'on-drag'
                }
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
                            name="shield-check-outline"
                            size={20}
                        />
                        <Text style={[authSharedStyles.statText, themed.statText]}>
                            Start your healthy eating journey
                        </Text>
                    </View>
                </View>

                <View style={[authSharedStyles.formCard, themed.formCard]}>
                    <Text style={authSharedStyles.formTitle}>Create Account</Text>
                    <Text style={authSharedStyles.subTitle}>
                        Save your diet records and get personalized suggestions
                    </Text>

                    {error ? (
                        <View style={[authSharedStyles.errorBox, themed.errorBox]}>
                            <MaterialCommunityIcons
                                color={themed.errorText.color}
                                name="alert-circle-outline"
                                size={20}
                            />
                            <Text
                                style={[
                                    authSharedStyles.errorText,
                                    themed.errorText,
                                ]}
                            >
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
                    <HelperText
                        type="error"
                        visible={emailTouched && !emailValid}
                    >
                        <Text>Please enter a valid email (e.g. name@example.com)</Text>
                    </HelperText>

                    <TextInput
                        autoComplete="password-new"
                        label="Password"
                        left={<TextInput.Icon icon="lock-outline" />}
                        mode="outlined"
                        onChangeText={setPassword}
                        passwordRules="required: lower; required: upper; required: digit; required: special; minlength: 8;"
                        secureTextEntry
                        style={authSharedStyles.input}
                        textContentType="newPassword"
                        value={password}
                    />

                    <View style={authSharedStyles.ruleCard}>
                        <Text style={authSharedStyles.ruleTitle}>Password requirements</Text>
                        <Text
                            variant="bodySmall"
                            style={
                                passwordStrength.minLength
                                    ? themed.passRuleDone
                                    : themed.passRulePending
                            }
                        >
                            - At least 8 characters
                        </Text>
                        <Text
                            variant="bodySmall"
                            style={
                                passwordStrength.hasUppercase
                                    ? themed.passRuleDone
                                    : themed.passRulePending
                            }
                        >
                            - One uppercase letter
                        </Text>
                        <Text
                            variant="bodySmall"
                            style={
                                passwordStrength.hasLowercase
                                    ? themed.passRuleDone
                                    : themed.passRulePending
                            }
                        >
                            - One lowercase letter
                        </Text>
                        <Text
                            variant="bodySmall"
                            style={
                                passwordStrength.hasNumber
                                    ? themed.passRuleDone
                                    : themed.passRulePending
                            }
                        >
                            - One number
                        </Text>
                        <Text
                            variant="bodySmall"
                            style={
                                passwordStrength.hasSpecial
                                    ? themed.passRuleDone
                                    : themed.passRulePending
                            }
                        >
                            - One special character
                        </Text>
                    </View>

                    <TextInput
                        autoComplete="password-new"
                        label="Confirm Password"
                        left={<TextInput.Icon icon="lock-check-outline" />}
                        mode="outlined"
                        onChangeText={setConfirmPassword}
                        passwordRules="required: lower; required: upper; required: digit; required: special; minlength: 8;"
                        secureTextEntry
                        style={authSharedStyles.input}
                        textContentType="newPassword"
                        value={confirmPassword}
                    />

                    <Button
                        buttonColor={AUTH_PRIMARY_BUTTON}
                        disabled={submitting}
                        loading={submitting}
                        mode="contained"
                        onPress={() => void onRegister()}
                        style={authSharedStyles.primaryButton}
                    >
                        <Text>{submitting ? 'Signing Up...' : 'Sign Up'}</Text>
                    </Button>

                    <View style={authSharedStyles.linkRow}>
                        <Text>Already have an account?</Text>
                        <Button
                            compact
                            mode="text"
                            onPress={() => navigation.navigate('Login')}
                        >
                            <Text>Sign In</Text>
                        </Button>
                    </View>
                </View>

                <View style={authSharedStyles.footer}>
                    <Text style={authSharedStyles.footerText}>
                        By creating an account, you agree to our Terms of Service and Privacy Policy
                    </Text>
                </View>
            </ScrollView>
            <StatusBar style="auto" />
        </KeyboardAvoidingView>
    )
}
