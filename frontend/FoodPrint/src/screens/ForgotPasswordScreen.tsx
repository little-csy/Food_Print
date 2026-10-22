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
import { Button, HelperText, Text, TextInput, useTheme } from 'react-native-paper'

import { isValidEmail } from '../context/AuthContext'
import { useCooldownSeconds } from '../hooks/useCooldownSeconds'
import {
    sendPasswordRecoveryOtp,
    verifyRecoveryOtpOnly,
} from '../lib/passwordRecovery'
import type { AuthStackParamList } from '../navigation/types'
import {
    AUTH_PRIMARY_BUTTON,
    authSharedStyles,
    useAuthScreenTheme,
} from './auth/authScreenShared'

const RESEND_INTERVAL_SEC = 60

type Props = NativeStackScreenProps<AuthStackParamList, 'ForgotPassword'>

export function ForgotPasswordScreen({ navigation }: Props) {
    const theme = useTheme()
    const themed = useAuthScreenTheme(theme)
    const { secondsRemaining, isCoolingDown, startCooldown } =
        useCooldownSeconds()
    const [email, setEmail] = useState('')
    const [otp, setOtp] = useState('')
    const [error, setError] = useState('')
    const [info, setInfo] = useState('')
    const [codeSent, setCodeSent] = useState(false)
    const [sending, setSending] = useState(false)
    const [verifying, setVerifying] = useState(false)

    const emailTouched = email.length > 0
    const emailValid = isValidEmail(email)

    const mapAuthMessage = (message: string) =>
        /network|fetch|failed|connection/i.test(message)
            ? 'Network error. Check your connection and try again.'
            : message

    const onGetCode = async () => {
        if (isCoolingDown || sending) {
            return
        }
        Keyboard.dismiss()
        setError('')
        setInfo('')

        if (!email.trim()) {
            setError('Please enter your email')
            return
        }
        if (!isValidEmail(email)) {
            setError('Please enter a valid email address')
            return
        }

        setSending(true)
        const { error: sendError } = await sendPasswordRecoveryOtp(email)
        setSending(false)

        if (sendError) {
            setError(mapAuthMessage(sendError.message || 'Failed to send email'))
            return
        }

        startCooldown(RESEND_INTERVAL_SEC)
        setCodeSent(true)
        setInfo(
            'If your email is registered, you will receive a verification code. Check spam folder.'
        )
    }

    const onVerifyCode = async () => {
        Keyboard.dismiss()
        setError('')
        setInfo('')

        if (!otp.trim()) {
            setError('Please enter the verification code')
            return
        }

        setVerifying(true)
        const { error: verifyError } = await verifyRecoveryOtpOnly(email, otp)
        setVerifying(false)

        if (verifyError) {
            setError(
                mapAuthMessage(verifyError.message || 'Invalid or expired code')
            )
            return
        }

        navigation.replace('SetNewPassword', {
            email: email.trim().toLowerCase(),
        })
    }

    const getCodeLabel = () => {
        if (sending) {
            return 'Sending...'
        }
        if (isCoolingDown) {
            return `Resend (${secondsRemaining}s)`
        }
        return codeSent ? 'Resend code' : 'Get code'
    }

    const getCodeDisabled = sending || isCoolingDown

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
                            name="email-lock-outline"
                            size={20}
                        />
                        <Text style={[authSharedStyles.statText, themed.statText]}>
                            Reset your password securely
                        </Text>
                    </View>
                </View>

                <View style={[authSharedStyles.formCard, themed.formCard]}>
                    <Text style={authSharedStyles.formTitle}>Forgot password</Text>
                    <Text style={authSharedStyles.subTitle}>
                        {codeSent
                            ? 'Enter the code from your email, then continue to set a new password.'
                            : 'Enter your email, tap Get code to receive a verification code.'}
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

                    {info ? (
                        <View style={[authSharedStyles.infoBox, themed.infoBox]}>
                            <MaterialCommunityIcons
                                color={themed.infoText.color}
                                name="information-outline"
                                size={20}
                            />
                            <Text
                                style={[
                                    authSharedStyles.infoText,
                                    themed.infoText,
                                ]}
                            >
                                {info}
                            </Text>
                        </View>
                    ) : null}

                    <TextInput
                        autoCapitalize="none"
                        autoComplete="email"
                        disabled={codeSent}
                        keyboardType="email-address"
                        label="Email"
                        left={<TextInput.Icon icon="email-outline" />}
                        mode="outlined"
                        onChangeText={setEmail}
                        style={authSharedStyles.input}
                        textContentType="emailAddress"
                        value={email}
                    />
                    {emailTouched && !emailValid && !codeSent ? (
                        <HelperText type="error" visible>
                            <Text>Please enter a valid email</Text>
                        </HelperText>
                    ) : null}

                    <TextInput
                        autoCapitalize="none"
                        autoComplete="one-time-code"
                        keyboardType="number-pad"
                        label="Code"
                        left={<TextInput.Icon icon="numeric" />}
                        mode="outlined"
                        onChangeText={setOtp}
                        style={authSharedStyles.input}
                        textContentType="oneTimeCode"
                        value={otp}
                    />
                    <Button
                        buttonColor={AUTH_PRIMARY_BUTTON}
                        disabled={getCodeDisabled}
                        labelStyle={authSharedStyles.codeActionButtonLabel}
                        loading={sending}
                        mode="contained"
                        onPress={() => void onGetCode()}
                        style={authSharedStyles.codeActionButton}
                        textColor="#FFFFFF"
                    >
                        {getCodeLabel()}
                    </Button>

                    {codeSent ? (
                        <Button
                            buttonColor={AUTH_PRIMARY_BUTTON}
                            disabled={verifying}
                            loading={verifying}
                            mode="contained"
                            onPress={() => void onVerifyCode()}
                            style={authSharedStyles.primaryButton}
                        >
                            <Text>{verifying ? 'Verifying…' : 'Verify code'}</Text>
                        </Button>
                    ) : null}

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
