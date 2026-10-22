import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { useTheme } from 'react-native-paper'

import { useAuth } from '../context/AuthContext'
import { CommunityScreen } from '../screens/Community'
import { HomeScreen } from '../screens/HomeScreen'
import { ForgotPasswordScreen } from '../screens/ForgotPasswordScreen'
import { LoginScreen } from '../screens/LoginScreen'
import { OnboardingScreen } from '../screens/OnboardingScreen'
import { ProfileScreen } from '../screens/ProfileScreen'
import { AIChatScreen } from '../screens/AIChatScreen'
import { RegisterScreen } from '../screens/RegisterScreen'
import { SetNewPasswordScreen } from '../screens/SetNewPasswordScreen'
import type { AuthStackParamList, RootTabParamList } from './types'

const Tab = createBottomTabNavigator<RootTabParamList>()
const AuthStack = createNativeStackNavigator<AuthStackParamList>()

function MainTabs() {
    const theme = useTheme()
    const { colors } = theme

    return (
        <Tab.Navigator
            screenOptions={{
                headerShown: true,
                headerTitleAlign: 'center',
                headerStyle: { backgroundColor: colors.background },
                headerTintColor: colors.onSurface,
                tabBarStyle: {
                    backgroundColor: colors.surface,
                    borderTopColor: colors.outline,
                },
                tabBarActiveTintColor: colors.primary,
                tabBarInactiveTintColor: colors.onSurfaceVariant,
                tabBarShowLabel: true,
                animation: 'shift',
            }}
        >
            <Tab.Screen
                name="Home"
                component={HomeScreen}
                options={{
                    title: 'Home',
                    tabBarLabel: 'Home',
                    tabBarIcon: ({ color, size }) => (
                        <MaterialCommunityIcons
                            name="home"
                            size={size ?? 24}
                            color={color}
                        />
                    ),
                }}
            />
            <Tab.Screen
                name="AIChat"
                component={AIChatScreen}
                options={{
                    title: 'AI Chat',
                    tabBarLabel: 'AI Chat',
                    tabBarIcon: ({ color, size }) => (
                        <MaterialCommunityIcons
                            name="chat-processing-outline"
                            size={size ?? 24}
                            color={color}
                        />
                    ),
                }}
            />
            <Tab.Screen
                name="Community"
                component={CommunityScreen}
                options={{
                    title: 'Community',
                    tabBarLabel: 'Community',
                    tabBarIcon: ({ color, size }) => (
                        <MaterialCommunityIcons
                            name="account-group"
                            size={size ?? 24}
                            color={color}
                        />
                    ),
                }}
            />
            <Tab.Screen
                name="Profile"
                component={ProfileScreen}
                options={{
                    title: 'Profile',
                    tabBarLabel: 'Profile',
                    tabBarIcon: ({ color, size }) => (
                        <MaterialCommunityIcons
                            name="account"
                            size={size ?? 24}
                            color={color}
                        />
                    ),
                }}
            />
        </Tab.Navigator>
    )
}

function AuthNavigator() {
    const theme = useTheme()
    const { colors } = theme

    return (
        <AuthStack.Navigator
            screenOptions={{
                contentStyle: { backgroundColor: colors.background },
                headerShown: false,
            }}
        >
            <AuthStack.Screen name="Login" component={LoginScreen} />
            <AuthStack.Screen name="Register" component={RegisterScreen} />
            <AuthStack.Screen
                name="ForgotPassword"
                component={ForgotPasswordScreen}
            />
            <AuthStack.Screen
                name="SetNewPassword"
                component={SetNewPasswordScreen}
            />
        </AuthStack.Navigator>
    )
}

export function AppNavigator() {
    const theme = useTheme()
    const {
        hasCompletedOnboarding,
        isAuthenticated,
        isLoading,
        isPasswordRecovery,
    } = useAuth()

    if (isLoading) {
        return (
            <View
                style={[
                    styles.loadingContainer,
                    { backgroundColor: theme.colors.background },
                ]}
            >
                <ActivityIndicator color={theme.colors.primary} size="large" />
            </View>
        )
    }

    if (!isAuthenticated || isPasswordRecovery) {
        return <AuthNavigator />
    }

    if (!hasCompletedOnboarding) {
        return <OnboardingScreen />
    }

    return <MainTabs />
}

const styles = StyleSheet.create({
    loadingContainer: {
        alignItems: 'center',
        flex: 1,
        justifyContent: 'center',
    },
})
