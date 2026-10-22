import React from 'react'
import { View, Text, Image, TouchableOpacity, StyleSheet } from 'react-native'
import { Post } from '../types'

interface PostCardProps {
    post: Post
    onLikePress: (postId: string) => void
    onCommentPress: (postId: string) => void
    onFollowPress?: (authorId: string) => void
    isFollowed?: boolean
    currentUserId?: string
    onDeletePress?: (postId: string) => void
}

export const PostCard: React.FC<PostCardProps> = ({
    post,
    onLikePress,
    onCommentPress,
    onFollowPress,
    isFollowed = false,
    currentUserId,
    onDeletePress,
}) => {
    return (
        <View style={styles.card}>
            <View style={styles.header}>
                <Image source={{ uri: post.authorImage }} style={styles.avatar} />
                <View style={styles.meta}>
                    <Text style={styles.name}>{post.authorName}</Text>
                    <Text style={styles.timestamp}>{post.timestamp}</Text>
                </View>
                {onFollowPress && post.authorId !== currentUserId && (
                    <TouchableOpacity
                        style={[styles.followButton, isFollowed && styles.followButtonFollowed]}
                        onPress={() => onFollowPress(post.authorId)}
                    >
                        <Text
                            style={[
                                styles.followButtonText,
                                isFollowed && styles.followButtonTextFollowed,
                            ]}
                        >
                            {isFollowed ? 'Following' : '+ Follow'}
                        </Text>
                    </TouchableOpacity>
                )}
                {onDeletePress && post.authorId === currentUserId && (
                    <TouchableOpacity
                        style={styles.deleteButton}
                        onPress={() => onDeletePress(post.id)}
                    >
                        <Text style={styles.deleteButtonText}>Delete</Text>
                    </TouchableOpacity>
                )}
            </View>

            <Text style={styles.text}>{post.text}</Text>

            {post.image && <Image source={{ uri: post.image }} style={styles.image} />}

            <View style={styles.footer}>
                <View style={styles.stats}>
                    <TouchableOpacity style={styles.statItem} onPress={() => onLikePress(post.id)}>
                        <Text style={styles.heartIcon}>{post.userLiked ? '❤️' : '🤍'}</Text>
                        <Text style={styles.statNumber}>{post.likes}</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.statItem} onPress={() => onCommentPress(post.id)}>
                        <Text style={styles.commentIcon}>💬</Text>
                        <Text style={styles.statNumber}>{post.comments}</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </View>
    )
}

const styles = StyleSheet.create({
    card: {
        marginHorizontal: 16,
        marginBottom: 16,
        backgroundColor: '#FFF',
        borderRadius: 12,
        overflow: 'hidden',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
    },
    avatar: {
        width: 48,
        height: 48,
        borderRadius: 24,
        marginRight: 12,
    },
    meta: {
        flex: 1,
    },
    name: {
        fontSize: 16,
        fontWeight: '600',
        color: '#333',
        marginBottom: 2,
    },
    timestamp: {
        fontSize: 13,
        color: '#999',
    },
    text: {
        fontSize: 15,
        color: '#333',
        paddingHorizontal: 16,
        marginBottom: 12,
        lineHeight: 22,
    },
    image: {
        width: '100%',
        height: 240,
        backgroundColor: '#EEE',
    },
    footer: {
        padding: 16,
        borderTopWidth: 1,
        borderTopColor: '#F0F0F0',
    },
    stats: {
        flexDirection: 'row',
        gap: 24,
    },
    statItem: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
    },
    heartIcon: {
        fontSize: 18,
    },
    commentIcon: {
        fontSize: 18,
    },
    statNumber: {
        fontSize: 14,
        color: '#666',
        fontWeight: '500',
    },
    followButton: {
        backgroundColor: '#8BA888',
        paddingVertical: 6,
        paddingHorizontal: 12,
        borderRadius: 6,
        marginLeft: 8,
    },
    followButtonFollowed: {
        backgroundColor: '#E8F0E7',
    },
    followButtonText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#FFF',
    },
    followButtonTextFollowed: {
        color: '#6D8A6B',
    },
    deleteButton: {
        paddingVertical: 4,
        paddingHorizontal: 10,
        marginLeft: 8,
        borderRadius: 4,
        backgroundColor: '#FFE5E5',
    },
    deleteButtonText: {
        fontSize: 12,
        color: '#D32F2F',
        fontWeight: '500',
    },
})
