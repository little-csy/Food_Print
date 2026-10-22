import type { AuthError } from '@supabase/supabase-js'

import { supabase } from './supabase'

/** Send a password reset email. To show a 6-digit code, add {{ .Token }} to Supabase → Auth → Email Templates → Reset password. */
export async function sendPasswordRecoveryOtp(
    email: string
): Promise<{ error: AuthError | null }> {
    const normalized = email.trim().toLowerCase()
    const { error } = await supabase.auth.resetPasswordForEmail(normalized)
    return { error }
}

/** Validate only the recovery code; success creates a local recovery session for a later updateUser password change. */
export async function verifyRecoveryOtpOnly(
    email: string,
    otp: string
): Promise<{ error: AuthError | null }> {
    const normalizedEmail = email.trim().toLowerCase()
    const token = otp.trim().replace(/\s/g, '')

    const { error } = await supabase.auth.verifyOtp({
        email: normalizedEmail,
        token,
        type: 'recovery',
    })
    return { error }
}

/** Update the password in a verified recovery session, then log out. */
export async function updatePasswordAfterRecovery(
    newPassword: string
): Promise<{ error: AuthError | null }> {
    const { error: updateError } = await supabase.auth.updateUser({
        password: newPassword,
    })
    if (updateError) {
        return { error: updateError }
    }
    await supabase.auth.signOut()
    return { error: null }
}
