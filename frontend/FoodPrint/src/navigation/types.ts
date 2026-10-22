export type RootTabParamList = {
    Home: undefined
    AIChat: undefined
    Community: undefined
    Profile: undefined
}

export type AuthStackParamList = {
    Login: undefined
    Register: undefined
    ForgotPassword: undefined
    SetNewPassword: { email: string }
}
