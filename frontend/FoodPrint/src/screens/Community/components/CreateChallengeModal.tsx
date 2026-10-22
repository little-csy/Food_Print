import {
    Text,
    View,
    ScrollView,
    TouchableOpacity,
    Modal,
    TextInput,
    SafeAreaView,
} from 'react-native'
import React from 'react'
import type { CreateChallengeFormData } from '../types'
import { useCommunityStyles } from '../hooks'
import {
    CHALLENGE_TYPES,
    DURATION_OPTIONS,
    BADGE_DESIGNS,
} from '../constants'

interface CreateChallengeModalProps {
    visible: boolean
    createStep: number
    formData: CreateChallengeFormData
    titleError: string
    descError: string
    onClose: () => void
    onStepChange: (step: number) => void
    onFormChange: (data: Partial<CreateChallengeFormData>) => void
    onTitleChange: (value: string) => void
    onDescChange: (value: string) => void
    onSubmit: (typeData: any) => void
}

export function CreateChallengeModal({
    visible,
    createStep,
    formData,
    titleError,
    descError,
    onClose,
    onStepChange,
    onFormChange,
    onTitleChange,
    onDescChange,
    onSubmit,
}: CreateChallengeModalProps) {
    const styles = useCommunityStyles()

    const handleTypeSelect = (type: any) => {
        onFormChange({
            type: type.id,
            color: type.color,
            gradient: type.gradient,
            selectedTypeData: type,
        })
        onStepChange(2)
    }

    return (
        <Modal
            visible={visible}
            animationType="slide"
            transparent={false}
            onRequestClose={onClose}
        >
            <SafeAreaView style={{ flex: 1, backgroundColor: styles.container.backgroundColor }}>
                <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
                    {/* Header */}
                    <View style={styles.challengeModalHeader}>
                        <TouchableOpacity onPress={onClose}>
                            <Text style={styles.challengeModalCloseText}>✕</Text>
                        </TouchableOpacity>
                        <View style={styles.challengeModalTitleContainer}>
                            <Text style={styles.challengeModalIcon}>✨</Text>
                            <View>
                                <Text style={styles.challengeModalTitle}>Create Challenge</Text>
                                <Text style={styles.challengeModalSubtitle}>Premium Feature</Text>
                            </View>
                        </View>
                    </View>

                    {/* Progress bar */}
                    <View style={styles.progressBarContainer}>
                        {[1, 2, 3, 4].map((step) => (
                            <View
                                key={step}
                                style={[
                                    styles.progressBarSegment,
                                    step <= createStep && styles.progressBarSegmentActive,
                                ]}
                            />
                        ))}
                    </View>

                    <View style={styles.createChallengeContent}>
                        {/* Step 1 */}
                        {createStep === 1 && (
                            <View style={styles.stepContainer}>
                                <Text style={styles.stepTitle}>Choose Challenge Type</Text>
                                <Text style={styles.stepSubtitle}>
                                    Select the category that best fits your goal
                                </Text>
                                <View style={styles.typeCardsContainer}>
                                    {CHALLENGE_TYPES.map((type) => (
                                        <TouchableOpacity
                                            key={type.id}
                                            onPress={() => handleTypeSelect(type)}
                                            style={styles.typeCard}
                                        >
                                            <View
                                                style={[
                                                    styles.typeCardIcon,
                                                    { backgroundColor: type.color },
                                                ]}
                                            >
                                                <Text style={styles.typeCardIconText}>
                                                    {type.icon}
                                                </Text>
                                            </View>
                                            <View style={styles.typeCardContent}>
                                                <Text style={styles.typeCardName}>
                                                    {type.name}
                                                </Text>
                                                <Text style={styles.typeCardDesc}>
                                                    {type.description}
                                                </Text>
                                            </View>
                                        </TouchableOpacity>
                                    ))}
                                </View>
                            </View>
                        )}

                        {/* Step 2 */}
                        {createStep === 2 && (
                            <View style={styles.stepContainer}>
                                <TouchableOpacity
                                    onPress={() => onStepChange(1)}
                                    style={styles.backButton}
                                >
                                    <Text style={styles.backButtonText}>← Back to types</Text>
                                </TouchableOpacity>

                                <Text style={styles.stepTitle}>Challenge Details</Text>
                                <Text style={styles.stepSubtitle}>
                                    Give your challenge a catchy name
                                </Text>

                                <View style={styles.formGroup}>
                                    <Text style={styles.formLabelChallenge}>
                                        Challenge Title{' '}
                                        <Text style={styles.requiredMark}>*</Text>
                                    </Text>
                                    <TextInput
                                        value={formData.title}
                                        onChangeText={onTitleChange}
                                        placeholder="e.g., Green Smoothie Week"
                                        placeholderTextColor="#999"
                                        maxLength={50}
                                        style={styles.textInputField}
                                    />
                                    <View style={styles.formHint}>
                                        {titleError ? (
                                            <Text style={styles.errorText}>{titleError}</Text>
                                        ) : (
                                            <Text style={styles.hintText}>
                                                Make it clear and motivating
                                            </Text>
                                        )}
                                        <Text style={styles.charCountChallenge}>
                                            {formData.title.length}/50
                                        </Text>
                                    </View>
                                </View>

                                <View style={styles.formGroup}>
                                    <Text style={styles.formLabelChallenge}>
                                        Description <Text style={styles.requiredMark}>*</Text>
                                    </Text>
                                    <TextInput
                                        value={formData.description}
                                        onChangeText={onDescChange}
                                        placeholder="Describe what participants need to do..."
                                        placeholderTextColor="#999"
                                        maxLength={120}
                                        multiline
                                        numberOfLines={3}
                                        style={[styles.textInputField, { height: 80 }]}
                                    />
                                    <View style={styles.formHint}>
                                        {descError ? (
                                            <Text style={styles.errorText}>{descError}</Text>
                                        ) : (
                                            <Text style={styles.hintText}>
                                                Keep it concise and clear
                                            </Text>
                                        )}
                                        <Text style={styles.charCountChallenge}>
                                            {formData.description.length}/120
                                        </Text>
                                    </View>
                                </View>

                                <TouchableOpacity
                                    onPress={() => onStepChange(3)}
                                    disabled={
                                        !formData.title ||
                                        !formData.description ||
                                        !!titleError ||
                                        !!descError
                                    }
                                    style={[
                                        styles.continueButton,
                                        (!formData.title || !formData.description) &&
                                            styles.continueButtonDisabled,
                                    ]}
                                >
                                    <Text style={styles.continueButtonText}>Continue</Text>
                                </TouchableOpacity>
                            </View>
                        )}

                        {/* Step 3 */}
                        {createStep === 3 && (
                            <View style={styles.stepContainer}>
                                <TouchableOpacity
                                    onPress={() => onStepChange(2)}
                                    style={styles.backButton}
                                >
                                    <Text style={styles.backButtonText}>← Back</Text>
                                </TouchableOpacity>

                                <Text style={styles.stepTitle}>Challenge Duration</Text>
                                <Text style={styles.stepSubtitle}>
                                    How long will this challenge last?
                                </Text>

                                <View style={styles.durationGrid}>
                                    {DURATION_OPTIONS.map((option) => (
                                        <TouchableOpacity
                                            key={option.value}
                                            onPress={() =>
                                                onFormChange({ duration: option.value })
                                            }
                                            style={[
                                                styles.durationCard,
                                                formData.duration === option.value &&
                                                    styles.durationCardSelected,
                                            ]}
                                        >
                                            <Text style={styles.durationLabel}>
                                                {option.label}
                                            </Text>
                                            <Text style={styles.durationDesc}>
                                                {option.description}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>

                                <TouchableOpacity
                                    onPress={() => onStepChange(4)}
                                    disabled={!formData.duration}
                                    style={[
                                        styles.continueButton,
                                        !formData.duration && styles.continueButtonDisabled,
                                    ]}
                                >
                                    <Text style={styles.continueButtonText}>Continue</Text>
                                </TouchableOpacity>
                            </View>
                        )}

                        {/* Step 4 */}
                        {createStep === 4 && (
                            <View style={styles.stepContainer}>
                                <TouchableOpacity
                                    onPress={() => onStepChange(3)}
                                    style={styles.backButton}
                                >
                                    <Text style={styles.backButtonText}>← Back</Text>
                                </TouchableOpacity>

                                <Text style={styles.stepTitle}>Reward Badge</Text>
                                <Text style={styles.stepSubtitle}>
                                    Choose a badge design for participants
                                </Text>

                                <View style={styles.badgeGrid}>
                                    {BADGE_DESIGNS.map((badge, index) => (
                                        <TouchableOpacity
                                            key={index}
                                            onPress={() => onFormChange({ badge: index })}
                                            style={[
                                                styles.badgeOption,
                                                formData.badge === index &&
                                                    styles.badgeOptionSelected,
                                            ]}
                                        >
                                            <View
                                                style={[
                                                    styles.badgeCircle,
                                                    { backgroundColor: badge.gradient },
                                                ]}
                                            >
                                                <Text style={styles.badgeEmoji}>
                                                    {badge.icon}
                                                </Text>
                                            </View>
                                            <Text style={styles.badgeNameChallenge}>
                                                {badge.name}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>

                                <View style={styles.previewContainer}>
                                    <Text style={styles.previewTitle}>🏆 Preview</Text>
                                    <View
                                        style={[
                                            styles.previewCard,
                                            { backgroundColor: formData.color },
                                        ]}
                                    >
                                        <View style={styles.previewHeader}>
                                            <Text style={styles.previewIcon}>
                                                {BADGE_DESIGNS[formData.badge]?.icon ||
                                                    formData.selectedTypeData?.icon}
                                            </Text>
                                            <View>
                                                <Text style={styles.previewTitle2}>
                                                    {formData.title || 'Your Challenge'}
                                                </Text>
                                                <Text style={styles.previewDuration}>
                                                    {formData.duration} Days
                                                </Text>
                                            </View>
                                        </View>
                                        <Text style={styles.previewDesc}>
                                            {formData.description || 'Your description here...'}
                                        </Text>
                                    </View>
                                </View>

                                <TouchableOpacity
                                    onPress={() => onSubmit(formData.selectedTypeData)}
                                    style={styles.createChallengeButton}
                                >
                                    <Text style={styles.createChallengeButtonText}>
                                        Create Challenge
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        )}
                    </View>
                </ScrollView>
            </SafeAreaView>
        </Modal>
    )
}
