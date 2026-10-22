import {
    Alert,
    Text,
    View,
    ScrollView,
    TouchableOpacity,
    Modal,
    TextInput,
    SafeAreaView,
    Platform,
    KeyboardAvoidingView,
} from 'react-native'
import React, { useState } from 'react'
import type { Post } from '../types'

interface CommentModalProps {
    visible: boolean
    activeCommentPost: Post | null
    onClose: () => void
    onSubmitComment: (postId: string, text: string) => Promise<void>
    currentUserId?: string
    onDeleteComment?: (postId: string, commentId: string) => void
}

export function CommentModal({
    visible,
    activeCommentPost,
    onClose,
    onSubmitComment,
    currentUserId,
    onDeleteComment,
}: CommentModalProps) {
    const [commentText, setCommentText] = useState('')

    const handleClose = () => {
        setCommentText('')
        onClose()
    }

    const submitComment = async () => {
        if (!activeCommentPost?.id || !commentText.trim()) {
            return
        }

        try {
            await onSubmitComment(activeCommentPost.id, commentText)
            handleClose()
        } catch (error) {
            Alert.alert('Failed', 'Comment could not be posted.')
            console.error('❌ add comment failed:', error)
        }
    }

    return (
        <Modal visible={visible} animationType="fade" transparent>
            <View
                style={{
                    flex: 1,
                    backgroundColor: 'rgba(0,0,0,0.35)',
                    justifyContent: 'flex-end',
                }}
            >
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    keyboardVerticalOffset={0}
                    style={{ flex: 1 }}
                >
                    <View
                        style={{
                            flex: 1,
                            backgroundColor: '#fff',
                            borderTopLeftRadius: 16,
                            borderTopRightRadius: 16,
                            display: 'flex',
                            flexDirection: 'column',
                        }}
                    >
                        <SafeAreaView style={{ backgroundColor: '#fff' }}>
                            <View
                                style={{
                                    paddingHorizontal: 16,
                                    paddingVertical: 14,
                                    borderBottomWidth: 1,
                                    borderBottomColor: '#EEF2ED',
                                    flexDirection: 'row',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                }}
                            >
                                <Text style={{ fontSize: 18, fontWeight: '700', color: '#1B3F1C' }}>
                                    Comments
                                </Text>
                                <TouchableOpacity onPress={handleClose}>
                                    <Text style={{ color: '#7A8A78', fontWeight: '600', fontSize: 15 }}>
                                        Close
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        </SafeAreaView>

                        <ScrollView
                            style={{ flexGrow: 1, paddingHorizontal: 16, paddingTop: 8 }}
                            contentContainerStyle={{ flexGrow: 1, paddingBottom: 4 }}
                            scrollEventThrottle={16}
                        >
                            {!activeCommentPost ||
                            (activeCommentPost?.commentList || []).length === 0 ? (
                                <View
                                    style={{
                                        flex: 1,
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                    }}
                                >
                                    <Text
                                        style={{
                                            color: '#8D9B8B',
                                            textAlign: 'center',
                                            fontSize: 15,
                                        }}
                                    >
                                        No comments yet. Be the first to comment.
                                    </Text>
                                </View>
                            ) : (
                                (activeCommentPost?.commentList || []).map((comment) => (
                                    <View
                                        key={comment.id}
                                        style={{
                                            marginBottom: 16,
                                            backgroundColor: '#F7FAF6',
                                            borderRadius: 12,
                                            padding: 14,
                                        }}
                                    >
                                        <Text
                                            style={{
                                                fontWeight: '700',
                                                color: '#334234',
                                                marginBottom: 6,
                                                fontSize: 15,
                                            }}
                                        >
                                            {comment.authorName}
                                        </Text>
                                        <Text
                                            style={{
                                                color: '#334234',
                                                lineHeight: 22,
                                                fontSize: 14,
                                            }}
                                        >
                                            {comment.text}
                                        </Text>
                                        <View
                                            style={{
                                                flexDirection: 'row',
                                                justifyContent: 'space-between',
                                                alignItems: 'center',
                                                marginTop: 8,
                                            }}
                                        >
                                            <Text
                                                style={{
                                                    color: '#8D9B8B',
                                                    fontSize: 13,
                                                }}
                                            >
                                                {comment.timestamp}
                                            </Text>
                                            {onDeleteComment &&
                                                comment.authorId === currentUserId && (
                                                    <TouchableOpacity
                                                        onPress={() => {
                                                            if (
                                                                activeCommentPost?.id &&
                                                                onDeleteComment
                                                            ) {
                                                                onDeleteComment(
                                                                    activeCommentPost.id,
                                                                    comment.id
                                                                );
                                                            }
                                                        }}
                                                        style={{
                                                            paddingVertical: 4,
                                                            paddingHorizontal: 8,
                                                            borderRadius: 4,
                                                            backgroundColor: '#FFE5E5',
                                                        }}
                                                    >
                                                        <Text
                                                            style={{
                                                                fontSize: 12,
                                                                color: '#D32F2F',
                                                                fontWeight: '500',
                                                            }}
                                                        >
                                                            Delete
                                                        </Text>
                                                    </TouchableOpacity>
                                                )}
                                        </View>
                                    </View>
                                ))
                            )}
                        </ScrollView>

                        <View
                            style={{
                                borderTopWidth: 1,
                                borderTopColor: '#EEF2ED',
                                paddingHorizontal: 16,
                                paddingTop: 10,
                                paddingBottom: 0,
                                backgroundColor: '#fff',
                            }}
                        >
                            <TextInput
                                style={{
                                    minHeight: 40,
                                    maxHeight: 80,
                                    borderColor: '#D8E3D6',
                                    borderWidth: 1,
                                    borderRadius: 12,
                                    paddingHorizontal: 14,
                                    paddingVertical: 10,
                                    textAlignVertical: 'top',
                                    backgroundColor: '#fff',
                                    fontSize: 15,
                                }}
                                value={commentText}
                                onChangeText={setCommentText}
                                multiline
                                placeholder="Write your comment..."
                                placeholderTextColor="#A0B19D"
                            />
                            <View
                                style={{
                                    flexDirection: 'row',
                                    justifyContent: 'flex-end',
                                    marginTop: 8,
                                    gap: 12,
                                    paddingBottom: 12,
                                }}
                            >
                                <TouchableOpacity onPress={handleClose}>
                                    <Text style={{ color: '#7A8A78', fontWeight: '600', fontSize: 15 }}>
                                        Cancel
                                    </Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    onPress={() => void submitComment()}
                                    disabled={!commentText.trim()}
                                >
                                    <Text
                                        style={{
                                            color: commentText.trim() ? '#8BA888' : '#C8DCC6',
                                            fontWeight: '700',
                                            fontSize: 15,
                                        }}
                                    >
                                        Post
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </View>
        </Modal>
    )
}
