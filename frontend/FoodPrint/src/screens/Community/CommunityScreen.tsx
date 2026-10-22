import { StatusBar } from 'expo-status-bar';
import { Text, View, ScrollView, FlatList, TouchableOpacity, SafeAreaView } from 'react-native';
import React, { useEffect, useMemo, useRef, useState } from 'react';

// Import from modularized structure
import { useCommunityState, useCommunityStyles } from './hooks';
import {
  ChallengeCard,
  PostCard,
  CreatePostModal,
  CommentModal,
  ChallengeDetailModal,
  CreateChallengeModal,
} from './components';
import { supabase } from '../../lib/supabase';

export function CommunityScreen() {
  const [isPremium] = useState(true);
  const [currentUserId, setCurrentUserId] = useState<string | undefined>();
  const communityStyles = useCommunityStyles();

  // Main state from hook
  const {
    flatListRef,
    challenges,
    selectedChallenge,
    showChallengeDetail,
    showCreateChallenge,
    posts,
    followedUsers,
    followingFeed,
    feedTab,
    showCreatePost,
    createStep,
    formData,
    titleError,
    descError,
    setSelectedChallenge,
    setShowChallengeDetail,
    setShowCreateChallenge,
    setShowCreatePost,
    setFeedTab,
    setCreateStep,
    setFormData,
    setTitleError,
    setDescError,
    handleScroll,
    handleLoadChallenges,
    handleLoadChallengeDetail,
    handleJoinChallenge,
    handleCheckinChallenge,
    handleAbandonChallenge,
    handleLikePost,
    handleAddComment,
    handleDeletePost,
    handleDeleteComment,
    handleFollowUser,
    handleLoadFeed,
    handleLoadFollowingFeed,
    handleLoadMyTopics,
    handleCreatePost,
    handleCreateChallenge,
    resetChallengeForm,
    scroll,
  } = useCommunityState();

  // Local state for modal interactions
  const [showCommentModal, setShowCommentModal] = useState(false);
  const [activeCommentPostId, setActiveCommentPostId] = useState<string | null>(null);
  const bootstrapLoadedRef = useRef(false);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      if (session?.user?.id) {
        setCurrentUserId(session.user.id);
      }
    };
    void fetchCurrentUser();
  }, []);

  useEffect(() => {
    if (bootstrapLoadedRef.current) return;
    bootstrapLoadedRef.current = true;
    void handleLoadChallenges();
    void handleLoadFeed();
    void handleLoadMyTopics();
    void handleLoadFollowingFeed();
  }, [handleLoadChallenges, handleLoadFeed, handleLoadFollowingFeed, handleLoadMyTopics]);

  const displayPosts = useMemo(
    () => (feedTab === 'following' ? followingFeed : posts),
    [feedTab, followingFeed, posts]
  );

  const activeCommentPost = useMemo(
    () => displayPosts.find(post => post.id === activeCommentPostId) || null,
    [activeCommentPostId, displayPosts]
  );

  const handleTitleChange = (value: string) => {
    if (value.length > 50) {
      setTitleError('Title must be 50 characters or less');
      return;
    }
    setTitleError('');
    setFormData({ ...formData, title: value });
  };

  const handleDescChange = (value: string) => {
    if (value.length > 120) {
      setDescError('Description must be 120 characters or less');
      return;
    }
    setDescError('');
    setFormData({ ...formData, description: value });
  };

  const openCommentModal = (postId: string) => {
    setActiveCommentPostId(postId);
    setShowCommentModal(true);
  };

  const closeCommentModal = () => {
    setShowCommentModal(false);
    setActiveCommentPostId(null);
  };

  const submitComment = async (postId: string, text: string) => {
    await handleAddComment(postId, text);
  };

  const handleCreatePostSubmit = (data: { text: string; image: string }) => {
    handleCreatePost(data);
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: communityStyles.container.backgroundColor }}>
      <ScrollView style={communityStyles.container} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={communityStyles.header}>
          <Text style={communityStyles.title}>FoodPrint</Text>
        </View>

        {/* Community Challenges Section */}
        <View style={communityStyles.section}>
          <View style={communityStyles.sectionHeader}>
            <Text style={communityStyles.sectionTitle}>Challenges</Text>
            <View style={communityStyles.buttonGroup}>
              {isPremium && (
                <TouchableOpacity
                  style={communityStyles.addButton}
                  onPress={() => setShowCreateChallenge(true)}
                >
                  <Text style={communityStyles.addButtonText}>+</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity style={communityStyles.scrollButton} onPress={() => scroll('left')}>
                <Text style={communityStyles.scrollButtonText}>‹</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={communityStyles.scrollButton}
                onPress={() => scroll('right')}
              >
                <Text style={communityStyles.scrollButtonText}>›</Text>
              </TouchableOpacity>
            </View>
          </View>
          <FlatList
            ref={flatListRef}
            data={challenges}
            renderItem={({ item }) => (
              <ChallengeCard
                challenge={item}
                onPress={challenge => {
                  setSelectedChallenge(challenge);
                  setShowChallengeDetail(true);
                  void handleLoadChallengeDetail(challenge);
                }}
                onJoinPress={id => handleJoinChallenge(id)}
              />
            )}
            keyExtractor={item => item.id}
            horizontal
            showsHorizontalScrollIndicator={false}
            scrollEventThrottle={16}
            contentContainerStyle={communityStyles.challengeListContent}
            onScroll={handleScroll}
          />
        </View>

        {/* Community Feed Section */}
        <View style={communityStyles.section}>
          <View style={communityStyles.sectionHeader}>
            <Text style={communityStyles.sectionTitle}>Community Feed</Text>
            <TouchableOpacity
              style={communityStyles.postButton}
              onPress={() => setShowCreatePost(true)}
            >
              <Text style={communityStyles.postButtonIcon}>✎</Text>
              <Text style={communityStyles.postButtonText}>Share</Text>
            </TouchableOpacity>
          </View>

          {/* Feed Tab Toggle */}
          <View style={{ flexDirection: 'row', paddingHorizontal: 16, marginBottom: 12 }}>
            <TouchableOpacity
              style={{
                paddingHorizontal: 12,
                paddingVertical: 8,
                borderRadius: 12,
                backgroundColor: feedTab === 'all' ? '#8BA888' : '#E8F0E7',
                marginRight: 8,
              }}
              onPress={() => setFeedTab('all')}
            >
              <Text
                style={{
                  color: feedTab === 'all' ? '#fff' : '#6D8A6B',
                  fontWeight: '600',
                }}
              >
                All
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={{
                paddingHorizontal: 12,
                paddingVertical: 8,
                borderRadius: 12,
                backgroundColor: feedTab === 'following' ? '#8BA888' : '#E8F0E7',
              }}
              onPress={() => {
                setFeedTab('following');
                void handleLoadFollowingFeed();
              }}
            >
              <Text
                style={{
                  color: feedTab === 'following' ? '#fff' : '#6D8A6B',
                  fontWeight: '600',
                }}
              >
                Following
              </Text>
            </TouchableOpacity>
          </View>

          {/* Posts List */}
          {displayPosts.map(post => (
            <PostCard
              key={post.id}
              post={post}
              onLikePress={id => void handleLikePost(id)}
              onCommentPress={openCommentModal}
              onFollowPress={handleFollowUser}
              isFollowed={followedUsers.has(post.authorId)}
              currentUserId={currentUserId}
              onDeletePress={handleDeletePost}
            />
          ))}
        </View>
      </ScrollView>

      <StatusBar style="auto" />

      {/* Modals */}
      <ChallengeDetailModal
        visible={showChallengeDetail}
        challenge={selectedChallenge}
        onClose={() => setShowChallengeDetail(false)}
        onJoinPress={handleJoinChallenge}
        onCheckinPress={handleCheckinChallenge}
        onAbandonPress={handleAbandonChallenge}
      />

      <CreateChallengeModal
        visible={showCreateChallenge}
        createStep={createStep}
        formData={formData}
        titleError={titleError}
        descError={descError}
        onClose={() => {
          setShowCreateChallenge(false);
          resetChallengeForm();
        }}
        onStepChange={setCreateStep}
        onFormChange={data => setFormData({ ...formData, ...data })}
        onTitleChange={handleTitleChange}
        onDescChange={handleDescChange}
        onSubmit={handleCreateChallenge}
      />

      <CreatePostModal
        visible={showCreatePost}
        onClose={() => setShowCreatePost(false)}
        onSubmit={handleCreatePostSubmit}
        styles={communityStyles}
      />

      <CommentModal
        visible={showCommentModal}
        activeCommentPost={activeCommentPost}
        onClose={closeCommentModal}
        onSubmitComment={submitComment}
        currentUserId={currentUserId}
        onDeleteComment={handleDeleteComment}
      />
    </SafeAreaView>
  );
}
