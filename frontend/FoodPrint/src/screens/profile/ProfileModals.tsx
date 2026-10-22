import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons'
import * as ImagePicker from 'expo-image-picker'
import { useEffect, useState } from 'react'
import { Alert, Image, ScrollView, View } from 'react-native'
import {
    Button,
    Card,
    Divider,
    HelperText,
    IconButton,
    Modal,
    Portal,
    ProgressBar,
    SegmentedButtons,
    Switch,
    Text,
    TextInput,
} from 'react-native-paper'

import type { BodyDataRequest } from '../../api/types'
import type { UserProfile } from '../../context/AuthContext'
import { uploadAvatarAsset } from '../../lib/avatarUpload'
import { formatActivityLevel, formatDietaryPreference, formatGoal } from './data'
import { styles } from './styles'
import type {
    GoalEntry,
    NotificationPreferences,
    PrivacyPreferences,
    SettingsEntry,
    SettingsTitle,
} from './types'

type ThemeColors = {
    onSurfaceVariant: string
    primary: string
}

type SaveActionResult = {
    message?: string
    success: boolean
}

type DetailContentProps = {
    authUserId: string | null
    bodyMassIndex: number | null
    displayName: string
    goals: GoalEntry[]
    memberSinceLabel: string
    notificationPreferences: NotificationPreferences
    onSaveBodyData: (values: BodyDataRequest) => Promise<SaveActionResult>
    onSavePersonalInfo: (values: {
        avatarUrl: string
        nickname: string
    }) => Promise<SaveActionResult>
    onToggleNotification: (
        key: keyof NotificationPreferences,
        value: boolean
    ) => void
    onTogglePrivacy: (key: keyof PrivacyPreferences, value: boolean) => void
    palette: ReturnType<typeof import('./styles').getProfilePalette>
    privacyPreferences: PrivacyPreferences
    selectedSetting: SettingsTitle | null
    userEmail: string | null
    userProfile: UserProfile | null
}

type ProfileModalsProps = DetailContentProps & {
    onBackToSettings: () => void
    onCloseDetail: () => void
    onCloseSettings: () => void
    onCloseSubscription: () => void
    onOpenSetting: (title: SettingsTitle) => void
    onOpenSubscription: () => void
    selectedSettingEntry: SettingsEntry | null
    settingsEntries: SettingsEntry[]
    showSettings: boolean
    showSubscription: boolean
    signOut: () => void
    themeColors: ThemeColors
}

const AVATAR_PICK_OPTIONS: ImagePicker.ImagePickerOptions = {
    allowsEditing: true,
    aspect: [1, 1],
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    quality: 0.9,
}

function DetailContent({
    authUserId,
    bodyMassIndex,
    displayName,
    goals,
    memberSinceLabel,
    notificationPreferences,
    onSaveBodyData,
    onSavePersonalInfo,
    onToggleNotification,
    onTogglePrivacy,
    palette,
    privacyPreferences,
    selectedSetting,
    userEmail,
    userProfile,
}: DetailContentProps) {
    const [nickname, setNickname] = useState(userProfile?.nickname ?? '')
    const [avatarUrl, setAvatarUrl] = useState(userProfile?.avatarUrl ?? '')
    const [height, setHeight] = useState(
        userProfile?.height ? String(userProfile.height) : ''
    )
    const [weight, setWeight] = useState(
        userProfile?.weight ? String(userProfile.weight) : ''
    )
    const [age, setAge] = useState(userProfile?.age ? String(userProfile.age) : '')
    const [gender, setGender] = useState<UserProfile['gender']>(
        userProfile?.gender ?? 'other'
    )
    const [goal, setGoal] = useState<UserProfile['goal']>(
        userProfile?.goal ?? 'maintain'
    )
    const [dailyCalories, setDailyCalories] = useState(
        userProfile?.dailyCalories ? String(userProfile.dailyCalories) : ''
    )
    const [dietPreference, setDietPreference] = useState(
        userProfile?.dietPreference ?? ''
    )
    const [formError, setFormError] = useState('')
    const [savingSection, setSavingSection] = useState<
        'body' | 'goals' | 'personal' | null
    >(null)

    useEffect(() => {
        setNickname(userProfile?.nickname ?? '')
        setAvatarUrl(userProfile?.avatarUrl ?? '')
        setHeight(userProfile?.height ? String(userProfile.height) : '')
        setWeight(userProfile?.weight ? String(userProfile.weight) : '')
        setAge(userProfile?.age ? String(userProfile.age) : '')
        setGender(userProfile?.gender ?? 'other')
        setGoal(userProfile?.goal ?? 'maintain')
        setDailyCalories(
            userProfile?.dailyCalories ? String(userProfile.dailyCalories) : ''
        )
        setDietPreference(userProfile?.dietPreference ?? '')
        setFormError('')
        setSavingSection(null)
    }, [selectedSetting, userProfile])

    if (!selectedSetting) {
        return null
    }

    const persistPersonalInfo = async (
        nextNickname: string,
        nextAvatarUrl: string,
        successMessage = 'Personal information updated.'
    ) => {
        setFormError('')
        setSavingSection('personal')
        const result = await onSavePersonalInfo({
            avatarUrl: nextAvatarUrl,
            nickname: nextNickname,
        })
        setSavingSection(null)

        if (!result.success) {
            setFormError(result.message ?? 'Failed to update personal information.')
            return
        }

        Alert.alert('Profile', result.message ?? successMessage)
    }

    const handleSavePersonalInfo = async () => {
        await persistPersonalInfo(nickname.trim(), avatarUrl.trim())
    }

    const savePickedAvatar = async (asset: ImagePicker.ImagePickerAsset) => {
        if (!authUserId) {
            setFormError('Please sign in before uploading an avatar.')
            return
        }

        setFormError('')
        setSavingSection('personal')

        try {
            const uploadedAvatarUrl = await uploadAvatarAsset(authUserId, asset)
            setAvatarUrl(uploadedAvatarUrl)
            const result = await onSavePersonalInfo({
                avatarUrl: uploadedAvatarUrl,
                nickname: nickname.trim(),
            })

            if (!result.success) {
                setFormError(result.message ?? 'Failed to update avatar.')
                return
            }

            Alert.alert('Profile', result.message ?? 'Avatar updated.')
        } catch (error) {
            setFormError(
                error instanceof Error ? error.message : 'Failed to upload avatar.'
            )
        } finally {
            setSavingSection(null)
        }
    }

    const handlePickAvatarFromLibrary = async () => {
        const permission = await ImagePicker.requestMediaLibraryPermissionsAsync()

        if (!permission.granted) {
            setFormError('Photo library permission is required to choose an avatar.')
            return
        }

        const result = await ImagePicker.launchImageLibraryAsync(AVATAR_PICK_OPTIONS)

        if (!result.canceled && result.assets?.[0]) {
            await savePickedAvatar(result.assets[0])
        }
    }

    const handleTakeAvatarPhoto = async () => {
        const permission = await ImagePicker.requestCameraPermissionsAsync()

        if (!permission.granted) {
            setFormError('Camera permission is required to take an avatar photo.')
            return
        }

        const result = await ImagePicker.launchCameraAsync(AVATAR_PICK_OPTIONS)

        if (!result.canceled && result.assets?.[0]) {
            await savePickedAvatar(result.assets[0])
        }
    }

    const handleSaveBodyMetrics = async () => {
        const nextHeight = Number(height)
        const nextWeight = Number(weight)
        const nextAge = Number(age)

        if (
            !Number.isFinite(nextHeight) ||
            !Number.isFinite(nextWeight) ||
            !Number.isFinite(nextAge) ||
            nextHeight <= 0 ||
            nextWeight <= 0 ||
            nextAge <= 0
        ) {
            setFormError('Enter valid positive numbers for height, weight, and age.')
            return
        }

        setFormError('')
        setSavingSection('body')
        const result = await onSaveBodyData({
            age: nextAge,
            gender,
            heightCm: nextHeight,
            weightKg: nextWeight,
        })
        setSavingSection(null)

        if (!result.success) {
            setFormError(result.message ?? 'Failed to update body metrics.')
            return
        }

        Alert.alert('Profile', result.message ?? 'Body metrics updated.')
    }

    const handleSaveHealthGoals = async () => {
        const nextDailyCalories = Number(dailyCalories)
        const nextDietPreference = dietPreference.trim()

        if (!Number.isFinite(nextDailyCalories) || nextDailyCalories <= 0) {
            setFormError('Enter a valid positive calorie target.')
            return
        }

        setFormError('')
        setSavingSection('goals')
        const result = await onSaveBodyData({
            dailyCalorieTarget: nextDailyCalories,
            dietaryPreference: nextDietPreference || 'none',
            goal,
        })
        setSavingSection(null)

        if (!result.success) {
            setFormError(result.message ?? 'Failed to update health goals.')
            return
        }

        Alert.alert('Profile', result.message ?? 'Health goals updated.')
    }

    if (selectedSetting === 'Personal Information') {
        return (
            <View style={styles.detailBody}>
                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailAccent,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Profile Identity</Text>
                        {[
                            { label: 'Display name', value: displayName },
                            { label: 'Email', value: userEmail ?? 'Not added yet' },
                            { label: 'Member since', value: memberSinceLabel },
                            {
                                label: 'Diet preference',
                                value: formatDietaryPreference(
                                    userProfile?.dietPreference
                                ),
                            },
                        ].map((item) => (
                            <View key={item.label} style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: palette.mutedText }]}>
                                    {item.label}
                                </Text>
                                <Text style={styles.detailValue}>{item.value}</Text>
                            </View>
                        ))}
                    </Card.Content>
                </Card>

                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailStrongSurface,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Edit personal information</Text>
                        {!!avatarUrl && (
                            <View style={styles.avatarEditorRow}>
                                <Image
                                    source={{ uri: avatarUrl }}
                                    style={styles.avatarEditorImage}
                                />
                                <View style={styles.avatarActionList}>
                                    <Button
                                        disabled={savingSection === 'personal'}
                                        icon="image-outline"
                                        mode="outlined"
                                        onPress={() => {
                                            void handlePickAvatarFromLibrary()
                                        }}
                                    >
                                        <Text>Choose photo</Text>
                                    </Button>
                                    <Button
                                        disabled={savingSection === 'personal'}
                                        icon="camera-outline"
                                        mode="outlined"
                                        onPress={() => {
                                            void handleTakeAvatarPhoto()
                                        }}
                                    >
                                        <Text>Take photo</Text>
                                    </Button>
                                </View>
                            </View>
                        )}
                        {!avatarUrl && (
                            <View style={styles.avatarActionList}>
                                <Button
                                    disabled={savingSection === 'personal'}
                                    icon="image-outline"
                                    mode="outlined"
                                    onPress={() => {
                                        void handlePickAvatarFromLibrary()
                                    }}
                                >
                                    <Text>Choose avatar</Text>
                                </Button>
                                <Button
                                    disabled={savingSection === 'personal'}
                                    icon="camera-outline"
                                    mode="outlined"
                                    onPress={() => {
                                        void handleTakeAvatarPhoto()
                                    }}
                                >
                                    <Text>Take photo</Text>
                                </Button>
                            </View>
                        )}
                        <TextInput
                            autoCapitalize="words"
                            label="Nickname"
                            mode="outlined"
                            onChangeText={setNickname}
                            value={nickname}
                        />
                        <HelperText type="error" visible={!!formError}>
                            {formError || ' '}
                        </HelperText>
                        <Button
                            loading={savingSection === 'personal'}
                            mode="contained"
                            onPress={() => {
                                void handleSavePersonalInfo()
                            }}
                        >
                            <Text>Save personal information</Text>
                        </Button>
                    </Card.Content>
                </Card>
            </View>
        )
    }

    if (selectedSetting === 'Body Metrics') {
        return (
            <View style={styles.detailBody}>
                <View style={styles.metricGrid}>
                    {[
                        {
                            label: 'Weight',
                            value:
                                userProfile && userProfile.weight > 0
                                    ? `${userProfile.weight} kg`
                                    : 'Not set',
                        },
                        {
                            label: 'Height',
                            value:
                                userProfile && userProfile.height > 0
                                    ? `${userProfile.height} cm`
                                    : 'Not set',
                        },
                        {
                            label: 'Age',
                            value:
                                userProfile && userProfile.age > 0
                                    ? `${userProfile.age}`
                                    : 'Not set',
                        },
                        {
                            label: 'BMI',
                            value: bodyMassIndex ? bodyMassIndex.toFixed(1) : 'Not set',
                        },
                    ].map((item) => (
                        <Card
                            key={item.label}
                            style={[
                                styles.metricCard,
                                styles.metricCardFrame,
                                {
                                    backgroundColor: palette.detailStrongSurface,
                                    borderColor: palette.detailBorder,
                                },
                            ]}
                        >
                            <Card.Content>
                                <Text style={[styles.metricLabel, { color: palette.mutedText }]}>
                                    {item.label}
                                </Text>
                                <Text
                                    style={[
                                        styles.metricValue,
                                        { color: palette.detailHighlight },
                                    ]}
                                >
                                    {item.value}
                                </Text>
                            </Card.Content>
                        </Card>
                    ))}
                </View>

                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailAccent,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Daily Snapshot</Text>
                        <View style={styles.detailRow}>
                            <Text style={[styles.detailLabel, { color: palette.mutedText }]}>
                                Activity level
                            </Text>
                            <Text style={styles.detailValue}>
                                {formatActivityLevel(userProfile?.activityLevel)}
                            </Text>
                        </View>
                        <View style={styles.detailRow}>
                            <Text style={[styles.detailLabel, { color: palette.mutedText }]}>
                                Gender
                            </Text>
                            <Text style={styles.detailValue}>
                                {gender[0]?.toUpperCase() + gender.slice(1)}
                            </Text>
                        </View>
                    </Card.Content>
                </Card>

                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailStrongSurface,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Update body metrics</Text>
                        <TextInput
                            keyboardType="numeric"
                            label="Height (cm)"
                            mode="outlined"
                            onChangeText={setHeight}
                            value={height}
                        />
                        <TextInput
                            keyboardType="numeric"
                            label="Weight (kg)"
                            mode="outlined"
                            onChangeText={setWeight}
                            value={weight}
                        />
                        <TextInput
                            keyboardType="numeric"
                            label="Age"
                            mode="outlined"
                            onChangeText={setAge}
                            value={age}
                        />
                        <Text style={styles.settingsTitle}>Gender</Text>
                        <SegmentedButtons
                            buttons={[
                                { label: 'Male', value: 'male' },
                                { label: 'Female', value: 'female' },
                                { label: 'Other', value: 'other' },
                            ]}
                            onValueChange={(value) =>
                                setGender(value as UserProfile['gender'])
                            }
                            value={gender}
                        />
                        <HelperText type="error" visible={!!formError}>
                            {formError || ' '}
                        </HelperText>
                        <Button
                            loading={savingSection === 'body'}
                            mode="contained"
                            onPress={() => {
                                void handleSaveBodyMetrics()
                            }}
                        >
                            <Text>Save body metrics</Text>
                        </Button>
                    </Card.Content>
                </Card>
            </View>
        )
    }

    if (selectedSetting === 'Health Goals') {
        return (
            <View style={styles.detailBody}>
                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailAccent,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Goal Summary</Text>
                        {[
                            { label: 'Primary goal', value: formatGoal(userProfile?.goal) },
                            {
                                label: 'Daily calories',
                                value: `${userProfile?.dailyCalories ?? 2000} kcal`,
                            },
                            {
                                label: 'Protein target',
                                value: `${Math.max(
                                    90,
                                    Math.round((userProfile?.weight ?? 75) * 2)
                                )} g`,
                            },
                            {
                                label: 'Diet preference',
                                value: formatDietaryPreference(userProfile?.dietPreference),
                            },
                        ].map((item) => (
                            <View key={item.label} style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: palette.mutedText }]}>
                                    {item.label}
                                </Text>
                                <Text style={styles.detailValue}>{item.value}</Text>
                            </View>
                        ))}
                    </Card.Content>
                </Card>

                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailStrongSurface,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.goalList}>
                        {goals.map((goalItem) => (
                            <View key={goalItem.label} style={styles.goalBlock}>
                                <View style={styles.goalHeader}>
                                    <Text style={styles.goalLabel}>{goalItem.label}</Text>
                                    <Text
                                        style={[
                                            styles.goalCurrent,
                                            { color: palette.detailHighlight },
                                        ]}
                                    >
                                        {goalItem.current}
                                    </Text>
                                </View>
                                <ProgressBar
                                    color={palette.activeBar}
                                    progress={goalItem.progress}
                                    style={[
                                        styles.progressBar,
                                        { backgroundColor: palette.progressTrack },
                                    ]}
                                />
                            </View>
                        ))}
                    </Card.Content>
                </Card>

                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailStrongSurface,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        <Text style={styles.sectionTitle}>Update health goals</Text>
                        <Text style={styles.settingsTitle}>Goal</Text>
                        <SegmentedButtons
                            buttons={[
                                { label: 'Lose', value: 'lose' },
                                { label: 'Maintain', value: 'maintain' },
                                { label: 'Gain', value: 'gain' },
                            ]}
                            onValueChange={(value) =>
                                setGoal(value as UserProfile['goal'])
                            }
                            value={goal}
                        />
                        <TextInput
                            keyboardType="numeric"
                            label="Daily calorie target"
                            mode="outlined"
                            onChangeText={setDailyCalories}
                            value={dailyCalories}
                        />
                        <TextInput
                            autoCapitalize="words"
                            label="Dietary preference"
                            mode="outlined"
                            onChangeText={setDietPreference}
                            placeholder="e.g. Vegetarian"
                            value={dietPreference}
                        />
                        <HelperText type="error" visible={!!formError}>
                            {formError || ' '}
                        </HelperText>
                        <Button
                            loading={savingSection === 'goals'}
                            mode="contained"
                            onPress={() => {
                                void handleSaveHealthGoals()
                            }}
                        >
                            <Text>Save health goals</Text>
                        </Button>
                    </Card.Content>
                </Card>
            </View>
        )
    }

    if (selectedSetting === 'Notifications') {
        return (
            <View style={styles.detailBody}>
                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailAccent,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        {[ 
                            {
                                description:
                                    'Get reminded before your preferred meal logging time.',
                                key: 'mealReminders' as const,
                                title: 'Meal reminders',
                            },
                            {
                                description:
                                    'Receive motivation when you keep your daily streak alive.',
                                key: 'streakAlerts' as const,
                                title: 'Streak alerts',
                            },
                            {
                                description:
                                    'See a recap of calories, activity, and achievements every week.',
                                key: 'weeklySummary' as const,
                                title: 'Weekly summary',
                            },
                        ].map((item) => (
                            <View
                                key={item.key}
                                style={[
                                    styles.preferencePanel,
                                    styles.preferenceRow,
                                    {
                                        backgroundColor: palette.detailStrongSurface,
                                        borderColor: palette.detailBorder,
                                    },
                                ]}
                            >
                                <View style={styles.preferenceText}>
                                    <Text style={styles.settingsTitle}>{item.title}</Text>
                                    <Text
                                        style={[
                                            styles.preferenceDescription,
                                            { color: palette.mutedText },
                                        ]}
                                    >
                                        {item.description}
                                    </Text>
                                </View>
                                <Switch
                                    onValueChange={(value) =>
                                        onToggleNotification(item.key, value)
                                    }
                                    value={notificationPreferences[item.key]}
                                />
                            </View>
                        ))}
                    </Card.Content>
                </Card>

            </View>
        )
    }

    if (selectedSetting === 'Privacy & Security') {
        return (
            <View style={styles.detailBody}>
                <Card
                    style={[
                        styles.detailInfoCard,
                        styles.detailCardFrame,
                        {
                            backgroundColor: palette.detailAccent,
                            borderColor: palette.detailBorder,
                        },
                    ]}
                >
                    <Card.Content style={styles.sectionContent}>
                        {[ 
                            {
                                description:
                                    'Allow other community members to see your public progress.',
                                key: 'profileVisibility' as const,
                                title: 'Profile visibility',
                            },
                            {
                                description:
                                    'Share anonymous usage signals to improve recommendations.',
                                key: 'analyticsSharing' as const,
                                title: 'Analytics sharing',
                            },
                        ].map((item) => (
                            <View
                                key={item.key}
                                style={[
                                    styles.preferencePanel,
                                    styles.preferenceRow,
                                    {
                                        backgroundColor: palette.detailStrongSurface,
                                        borderColor: palette.detailBorder,
                                    },
                                ]}
                            >
                                <View style={styles.preferenceText}>
                                    <Text style={styles.settingsTitle}>{item.title}</Text>
                                    <Text
                                        style={[
                                            styles.preferenceDescription,
                                            { color: palette.mutedText },
                                        ]}
                                    >
                                        {item.description}
                                    </Text>
                                </View>
                                <Switch
                                    onValueChange={(value) =>
                                        onTogglePrivacy(item.key, value)
                                    }
                                    value={privacyPreferences[item.key]}
                                />
                            </View>
                        ))}
                    </Card.Content>
                </Card>

            </View>
        )
    }

    return (
        <View style={styles.detailBody}>
            <Card
                style={[
                    styles.detailInfoCard,
                    styles.detailCardFrame,
                    {
                        backgroundColor: palette.detailAccent,
                        borderColor: palette.detailBorder,
                    },
                ]}
            >
                <Card.Content style={styles.sectionContent}>
                    <Text style={styles.sectionTitle}>Password Protection</Text>
                    {[ 
                        { label: 'Current status', value: 'Managed by Supabase Auth' },
                        { label: 'Recommended flow', value: 'Use reset password screen' },
                        { label: 'Availability', value: 'Change password not wired here yet' },
                    ].map((item) => (
                        <View key={item.label} style={styles.detailRow}>
                            <Text style={[styles.detailLabel, { color: palette.mutedText }]}>
                                {item.label}
                            </Text>
                            <Text style={styles.detailValue}>{item.value}</Text>
                        </View>
                    ))}
                </Card.Content>
            </Card>

            <Button
                mode="contained"
                onPress={() =>
                    Alert.alert(
                        'Password',
                        'Use the existing Supabase password recovery flow for now.'
                    )
                }
            >
                <Text>Change password</Text>
            </Button>
        </View>
    )
}

export function ProfileModals({
    authUserId,
    bodyMassIndex,
    displayName,
    goals,
    memberSinceLabel,
    notificationPreferences,
    onBackToSettings,
    onCloseDetail,
    onCloseSettings,
    onCloseSubscription,
    onOpenSetting,
    onOpenSubscription,
    onSaveBodyData,
    onSavePersonalInfo,
    onToggleNotification,
    onTogglePrivacy,
    palette,
    privacyPreferences,
    selectedSetting,
    selectedSettingEntry,
    settingsEntries,
    showSettings,
    showSubscription,
    signOut,
    themeColors,
    userEmail,
    userProfile,
}: ProfileModalsProps) {
    return (
        <Portal>
            <Modal
                contentContainerStyle={[
                    styles.modalCard,
                    { backgroundColor: palette.surface },
                ]}
                dismissable
                onDismiss={onCloseSettings}
                visible={showSettings}
            >
                <View style={styles.modalInner}>
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalTitle}>Account & Settings</Text>
                        <IconButton
                            icon={() => (
                                <MaterialCommunityIcons
                                    color={themeColors.onSurfaceVariant}
                                    name="close"
                                    size={22}
                                />
                            )}
                            onPress={onCloseSettings}
                            style={styles.modalCloseButton}
                        />
                    </View>

                    <View style={styles.settingsList}>
                        <View
                            style={[
                                styles.premiumRow,
                                styles.settingsRow,
                                {
                                    backgroundColor: palette.premiumSurface,
                                },
                            ]}
                        >
                            <View
                                style={[
                                    styles.settingsIcon,
                                    { backgroundColor: palette.premiumBadge },
                                ]}
                            >
                                <MaterialCommunityIcons color="#FFFFFF" name="crown-outline" size={20} />
                            </View>
                            <View style={styles.settingsCopy}>
                                <Text style={styles.settingsTitle}>Premium Subscription</Text>
                                <Text
                                    style={[
                                        styles.settingsSubtitle,
                                        { color: palette.mutedText },
                                    ]}
                                >
                                    Unlock advanced features
                                </Text>
                            </View>
                            <IconButton
                                icon={() => (
                                    <MaterialCommunityIcons
                                        color={themeColors.onSurfaceVariant}
                                        name="chevron-right"
                                        size={20}
                                    />
                                )}
                                onPress={() => {
                                    onCloseSettings()
                                    onOpenSubscription()
                                }}
                                style={styles.modalCloseButton}
                            />
                        </View>

                        {settingsEntries.map((item) => (
                            <View
                                key={item.title}
                                style={[
                                    styles.settingsPanel,
                                    styles.settingsRow,
                                    {
                                        backgroundColor: palette.detailAccent,
                                        borderColor: palette.detailBorder,
                                    },
                                ]}
                            >
                                <View
                                    style={[
                                        styles.settingsIcon,
                                        { backgroundColor: palette.detailStrongSurface },
                                    ]}
                                >
                                    <MaterialCommunityIcons
                                        color={themeColors.primary}
                                        name={item.icon}
                                        size={20}
                                    />
                                </View>
                                <View style={styles.settingsCopy}>
                                    <Text style={styles.settingsTitle}>{item.title}</Text>
                                    <Text
                                        style={[
                                            styles.settingsSubtitle,
                                            { color: palette.mutedText },
                                        ]}
                                    >
                                        {item.description}
                                    </Text>
                                </View>
                                <IconButton
                                    icon={() => (
                                        <MaterialCommunityIcons
                                            color={themeColors.onSurfaceVariant}
                                            name="chevron-right"
                                            size={20}
                                        />
                                    )}
                                    onPress={() => onOpenSetting(item.title)}
                                    style={styles.modalCloseButton}
                                />
                            </View>
                        ))}
                    </View>

                    <View style={styles.modalFooter}>
                        <Divider style={styles.divider} />
                        <Button mode="text" onPress={signOut} style={styles.signOutButton}>
                            <Text
                                style={[
                                    styles.signOutText,
                                    { color: palette.dangerText },
                                ]}
                            >
                                Log Out
                            </Text>
                        </Button>
                    </View>
                </View>
            </Modal>

            <Modal
                contentContainerStyle={[
                    styles.modalCard,
                    { backgroundColor: palette.surface },
                ]}
                dismissable
                onDismiss={onCloseSubscription}
                visible={showSubscription}
            >
                <View style={styles.modalInner}>
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalTitle}>Premium Subscription</Text>
                        <IconButton
                            icon={() => (
                                <MaterialCommunityIcons
                                    color={themeColors.onSurfaceVariant}
                                    name="close"
                                    size={22}
                                />
                            )}
                            onPress={onCloseSubscription}
                            style={styles.modalCloseButton}
                        />
                    </View>

                    <ScrollView
                        contentContainerStyle={styles.modalScrollContent}
                        showsVerticalScrollIndicator={false}
                        style={styles.modalScroll}
                    >
                        <View
                            style={[
                                styles.subscriptionHero,
                                { backgroundColor: palette.premiumSurface },
                            ]}
                        >
                            <View
                                style={[
                                    styles.subscriptionIcon,
                                    { backgroundColor: palette.premiumBadge },
                                ]}
                            >
                                <MaterialCommunityIcons color="#FFFFFF" name="crown-outline" size={24} />
                            </View>
                            <Text style={styles.subscriptionTitle}>Upgrade your FoodPrint plan</Text>
                            <Text
                                style={[
                                    styles.subscriptionBody,
                                    { color: palette.mutedText },
                                ]}
                            >
                                Access smarter nutrition insights, progress summaries, and
                                advanced profile analytics.
                            </Text>
                        </View>

                        <View style={styles.subscriptionList}>
                            {[
                                'Advanced calorie and trend insights',
                                'Personalized weekly progress recaps',
                                'Priority access to new features',
                            ].map((item) => (
                                <View key={item} style={styles.subscriptionRow}>
                                    <MaterialCommunityIcons
                                        color={themeColors.primary}
                                        name="check-circle-outline"
                                        size={20}
                                    />
                                    <Text
                                        style={[
                                            styles.subscriptionItemText,
                                            { color: palette.mutedText },
                                        ]}
                                    >
                                        {item}
                                    </Text>
                                </View>
                            ))}
                        </View>
                    </ScrollView>

                    <View style={styles.modalFooter}>
                        <View style={styles.subscriptionActions}>
                            <Button
                                mode="contained"
                                onPress={() => {
                                    onCloseSubscription()
                                    Alert.alert(
                                        'Premium Subscription',
                                        'This action is ready for future payment flow integration.'
                                    )
                                }}
                            >
                                <Text>Start Premium</Text>
                            </Button>
                            <Button mode="text" onPress={onCloseSubscription}>
                                <Text>Maybe later</Text>
                            </Button>
                        </View>
                    </View>
                </View>
            </Modal>

            <Modal
                contentContainerStyle={[
                    styles.modalCard,
                    { backgroundColor: palette.surface },
                ]}
                dismissable
                onDismiss={onCloseDetail}
                visible={!!selectedSetting}
            >
                <View style={styles.modalInner}>
                    <View style={styles.detailHeader}>
                        <IconButton
                            icon={() => (
                                <MaterialCommunityIcons
                                    color={themeColors.onSurfaceVariant}
                                    name="arrow-left"
                                    size={22}
                                />
                            )}
                            onPress={onBackToSettings}
                            style={styles.modalCloseButton}
                        />
                        <View style={styles.detailHeaderCopy}>
                            <Text style={styles.detailHeaderTitle}>{selectedSetting}</Text>
                            <Text
                                style={[
                                    styles.detailHeaderSubtitle,
                                    { color: palette.mutedText },
                                ]}
                            >
                                {selectedSettingEntry?.description ??
                                    'Manage this section of your account.'}
                            </Text>
                        </View>
                        <IconButton
                            icon={() => (
                                <MaterialCommunityIcons
                                    color={themeColors.onSurfaceVariant}
                                    name="close"
                                    size={22}
                                />
                            )}
                            onPress={onCloseDetail}
                            style={styles.modalCloseButton}
                        />
                    </View>

                    <ScrollView
                        contentContainerStyle={styles.modalScrollContent}
                        showsVerticalScrollIndicator={false}
                        style={styles.modalScroll}
                    >
                        <DetailContent
                            authUserId={authUserId}
                            bodyMassIndex={bodyMassIndex}
                            displayName={displayName}
                            goals={goals}
                            memberSinceLabel={memberSinceLabel}
                            notificationPreferences={notificationPreferences}
                            onSaveBodyData={onSaveBodyData}
                            onSavePersonalInfo={onSavePersonalInfo}
                            onToggleNotification={onToggleNotification}
                            onTogglePrivacy={onTogglePrivacy}
                            palette={palette}
                            privacyPreferences={privacyPreferences}
                            selectedSetting={selectedSetting}
                            userEmail={userEmail}
                            userProfile={userProfile}
                        />
                    </ScrollView>
                </View>
            </Modal>
        </Portal>
    )
}
