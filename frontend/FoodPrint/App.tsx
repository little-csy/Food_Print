import { NavigationContainer } from '@react-navigation/native'
import { StatusBar } from 'expo-status-bar'
import { useColorScheme } from 'react-native'
import { Provider as PaperProvider } from 'react-native-paper'
import { SafeAreaProvider } from 'react-native-safe-area-context'

import { AuthProvider } from './src/context/AuthContext'
import { AppNavigator } from './src/navigation/AppNavigator'
import { getCombinedTheme, getNavigationTheme } from './src/theme'

export default function App() {
    const colorScheme = useColorScheme()
    const isDark = colorScheme === 'dark'
    const paperTheme = getCombinedTheme(isDark ? 'dark' : 'light')
    const navigationTheme = getNavigationTheme(isDark ? 'dark' : 'light')

    return (
        <SafeAreaProvider style={{ flex: 1 }}>
            <PaperProvider theme={paperTheme}>
                <AuthProvider>
                    <NavigationContainer theme={navigationTheme}>
                        <AppNavigator />
                        <StatusBar style={colorScheme === 'dark' ? 'light' : 'auto'} />
                    </NavigationContainer>
                </AuthProvider>
            </PaperProvider>
        </SafeAreaProvider>
    )
}
