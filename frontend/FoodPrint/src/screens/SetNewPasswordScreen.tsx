import type { NativeStackScreenProps } from '@react-navigation/native-stack'
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
import { Button, Text, TextInput, useTheme } from 'react-native-paper'

import { evaluatePasswordStrength } from '../context/AuthContext'
import { updatePasswordAfterRecovery } from '../lib/passwordRecovery'
import type { AuthStackParamList } from '../navigation/types'
import {
    AUTH_PRIMARY_BUTTON,
    authSharedStyles,
    useAuthScreenTheme,
} from './auth/authScreenShared'

type Props = NativeStackScreenProps<AuthStackParamList, 'SetNewPassword'>

export function SetNewPasswordScreen({ navigation, route }: Props) {
    const theme = useTheme()
    const themed = useAuthScreenTheme(theme)
    const { email } = route.params
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [error, setError] = useState('')
    const [submitting, setSubmitting] = useState(false)

    const passwordStrength = evaluatePasswordStrength(password)

    const mapAuthMessage = (message: string) =>
        /network|fetch|failed|connection/i.test(message)
            ? 'Network error. Check your connection and try again.'
            : message

    const onSubmit = async () => {
        Keyboard.dismiss()
        setError('')

        if (password.length < 8) {
            setError('Password must be at least 8 characters')
            return
        }
        if (!passwordStrength.isStrong) {
            setError(
                'Please meet all password requirements (8+ chars, upper, lower, number, special)'
            )
            return
        }
        if (password !== confirmPassword) {
            setError('Passwords do not match')
            return
        }

        setSubmitting(true)
        const { error: updateError } = await updatePasswordAfterRecovery(password)
        setSubmitting(false)

        if (updateError) {
            setError(
                mapAuthMessage(updateError.message || 'Failed to update password')
            )
            return
        }

        navigation.navigate('Login')
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
                            name="lock-reset"
                            size={20}
                        />
                        <Text style={[authSharedStyles.statText, themed.statText]}>
                            Choose a strong new password
                        </Text>
                    </View>
                </View>

                <View style={[authSharedStyles.formCard, themed.formCard]}>
                    <Text style={authSharedStyles.formTitle}>New password</Text>
                    <Text style={authSharedStyles.subTitle}>
                        This email is resetting its password. Enter and confirm your new
                        password below.
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
                        disabled
                        label="Email"
                        left={<TextInput.Icon icon="email-outline" />}
                        mode="outlined"
                        style={authSharedStyles.input}
                        value={email}
                    />

                    <TextInput
                        autoComplete="password-new"
                        label="New password"
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
                        label="Confirm new password"
                        left={<TextInput.Icon icon="lock-check-outline" />}
                        mode="outlined"
                        onChangeText={setConfirmPassword}
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
                        onPress={() => void onSubmit()}
                        style={authSharedStyles.primaryButton}
                    >
                        <Text>{submitting ? 'Saving…' : 'Save password'}</Text>
                    </Button>

                    <View style={authSharedStyles.linkRow}>
                        <Button
                            compact
                            mode="text"
                            onPress={() => navigation.navigate('Login')}
                        >
                            <Text>Back to Sign In</Text>
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
