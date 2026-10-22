import {
    Alert,
    Text,
    View,
    ScrollView,
    TouchableOpacity,
    Image,
    Modal,
    TextInput,
    SafeAreaView,
    ActionSheetIOS,
    Platform,
} from 'react-native'
import React, { useState } from 'react'
import * as ImagePicker from 'expo-image-picker'
import { useCommunityStyles } from '../hooks'
import { EMOJI_CATEGORIES } from '../constants'

interface CreatePostModalProps {
    visible: boolean
    onClose: () => void
    onSubmit: (data: { text: string; image: string }) => void
    styles: ReturnType<typeof useCommunityStyles>
}

export function CreatePostModal({
    visible,
    onClose,
    onSubmit,
    styles,
}: CreatePostModalProps) {
    const [postText, setPostText] = useState('')
    const [selectedImage, setSelectedImage] = useState('')

    const handlePickImageFromLibrary = async () => {
        const permission = await ImagePicker.requestMediaLibraryPermissionsAsync()
        if (!permission.granted) {
            Alert.alert('Permission needed', 'Please allow photo library access.')
            return
        }

        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ['images'],
            allowsEditing: true,
            quality: 0.8,
        })

        if (!result.canceled && result.assets?.[0]?.uri) {
            setSelectedImage(result.assets[0].uri)
        }
    }

    const handleTakePhoto = async () => {
        const permission = await ImagePicker.requestCameraPermissionsAsync()
        if (!permission.granted) {
            Alert.alert('Permission needed', 'Please allow camera access.')
            return
        }

        const result = await ImagePicker.launchCameraAsync({
            allowsEditing: true,
            quality: 0.8,
        })

        if (!result.canceled && result.assets?.[0]?.uri) {
            setSelectedImage(result.assets[0].uri)
        }
    }

    const handleImagePickerOptions = () => {
        if (Platform.OS === 'ios') {
            ActionSheetIOS.showActionSheetWithOptions(
                {
                    options: ['Cancel', 'Take Photo', 'Choose from Library'],
                    cancelButtonIndex: 0,
                    destructiveButtonIndex: undefined,
                },
                (buttonIndex: number) => {
                    if (buttonIndex === 1) {
                        void handleTakePhoto()
                    } else if (buttonIndex === 2) {
                        void handlePickImageFromLibrary()
                    }
                }
            )
        } else {
            Alert.alert('Choose Photo Source', '', [
                { text: 'Cancel', style: 'cancel' },
                { text: 'Take Photo', onPress: () => void handleTakePhoto() },
                { text: 'Choose from Library', onPress: () => void handlePickImageFromLibrary() },
            ])
        }
    }

    const handleEmojiInsert = (emoji: string) => {
        setPostText(postText + emoji)
    }

    const handleClose = () => {
        setPostText('')
        setSelectedImage('')
        onClose()
    }

    const handleCreatePostSubmit = async () => {
        if (!postText.trim()) {
            return
        }

        try {
            await onSubmit({
                text: postText,
                image: selectedImage,
            })
            setPostText('')
            setSelectedImage('')
        } catch (error) {
            console.error('❌ Create post error:', error)
        }
    }

    return (
        <Modal visible={visible} animationType="slide" transparent>
            <SafeAreaView style={{ flex: 1, backgroundColor: styles.container.backgroundColor }}>
                <View style={[styles.container, { paddingTop: 0 }]}>
                    {/* Header */}
                    <View style={styles.createPostHeader}>
                        <Text style={styles.createPostTitle}>Create Post</Text>
                        <TouchableOpacity onPress={handleClose}>
                            <Text style={styles.closeCreatePostButton}>✕</Text>
                        </TouchableOpacity>
                    </View>

                    <ScrollView style={{ flex: 1, paddingHorizontal: 16, paddingTop: 16 }}>
                        {/* Text Input */}
                        <TextInput
                            style={styles.createPostInputField}
                            placeholder="What's on your plate today? Share your meal, achievement, or health tip... 💪"
                            placeholderTextColor="#999"
                            multiline
                            value={postText}
                            onChangeText={setPostText}
                            maxLength={500}
                        />
                        <Text style={styles.charCounterText}>
                            {postText.length}/500
                        </Text>

                        {/* Selected Image Preview */}
                        {selectedImage && (
                            <View style={styles.imagePreviewSection}>
                                <Image source={{ uri: selectedImage }} style={styles.previewImage} />
                                <TouchableOpacity
                                    style={styles.deleteImageButton}
                                    onPress={() => setSelectedImage('')}
                                >
                                    <Text style={styles.deleteImageButtonText}>✕</Text>
                                </TouchableOpacity>
                            </View>
                        )}

                        {/* Image Picker Button */}
                        {!selectedImage && (
                            <View style={{ marginTop: 16 }}>
                                <TouchableOpacity
                                    style={styles.imagePickerButtonContainer}
                                    onPress={handleImagePickerOptions}
                                >
                                    <Text style={styles.imagePickerButtonText}>
                                        📸 Add a photo
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        )}

                        {/* Quick Emojis */}
                        <View style={styles.emojiContainer}>
                            {EMOJI_CATEGORIES.map((emoji) => (
                                <TouchableOpacity
                                    key={emoji}
                                    style={styles.emojiButton}
                                    onPress={() => handleEmojiInsert(emoji)}
                                >
                                    <Text style={styles.emojiText}>{emoji}</Text>
                                </TouchableOpacity>
                            ))}
                        </View>

                        {/* Action Buttons */}
                        <View style={styles.createPostActionsContainer}>
                            <TouchableOpacity
                                style={styles.cancelButtonStyle}
                                onPress={handleClose}
                            >
                                <Text style={styles.cancelButtonTextStyle}>Cancel</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[
                                    styles.postCreateButton,
                                    !postText.trim() && styles.postCreateButtonDisabled,
                                ]}
                                disabled={!postText.trim()}
                                onPress={handleCreatePostSubmit}
                            >
                                <Text style={styles.postCreateButtonText}>Post ✓</Text>
                            </TouchableOpacity>
                        </View>
                    </ScrollView>
                </View>
            </SafeAreaView>
        </Modal>
    )
}
