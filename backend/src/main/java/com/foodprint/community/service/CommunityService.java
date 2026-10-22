package com.foodprint.community.service;

import java.util.UUID;

import com.foodprint.community.dto.*;
import com.foodprint.community.entity.*;
import com.foodprint.community.repository.*;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private CommunitySubscriptionRepository communitySubscriptionRepository;

    @Autowired
    private UserService userService;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        UUID userId = getCurrentUserId();
        Post post = new Post(userId, request.getTitle(), request.getContent(), request.getImageUrl());
        post = postRepository.save(post);
        return mapToPostResponse(post, false);
    }

    public List<PostResponse> getFeed(int page, int size) {
        UUID currentUserId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        List<Post> posts = postRepository.findAllOrderByCreatedAtDesc();
        // For simplicity, not using pageable in query, but can adjust
        int offset = page * size;
        List<Post> pagedPosts = posts.stream().skip(offset).limit(size).collect(Collectors.toList());

        return pagedPosts.stream()
                .map(post -> {
                    boolean liked = postLikeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent();
                    List<CommentResponse> comments = getCommentsForPost(post.getId());
                    return mapToPostResponse(post, liked, comments);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(UUID postId, CreateCommentRequest request) {
        UUID userId = getCurrentUserId();
        Comment comment = new Comment(postId, userId, request.getContent());
        comment = commentRepository.save(comment);
        return mapToCommentResponse(comment);
    }

    @Transactional
    public void likePost(UUID postId) {
        UUID userId = getCurrentUserId();
        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isEmpty()) {
            PostLike like = new PostLike(postId, userId);
            postLikeRepository.save(like);
            // Update like count
            Post post = postRepository.findById(postId).orElseThrow();
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
        }
    }

    @Transactional
    public void unlikePost(UUID postId) {
        UUID userId = getCurrentUserId();
        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            // Update like count
            Post post = postRepository.findById(postId).orElseThrow();
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
        }
    }

    @Transactional
    public void followUser(UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId.equals(targetUserId)) return;
        Optional<UserFollow> existing = userFollowRepository.findByFollowerIdAndFolloweeId(currentUserId, targetUserId);
        if (existing.isEmpty()) {
            userFollowRepository.save(new UserFollow(currentUserId, targetUserId));
        }
    }

    @Transactional
    public void unfollowUser(UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();
        Optional<UserFollow> existing = userFollowRepository.findByFollowerIdAndFolloweeId(currentUserId, targetUserId);
        existing.ifPresent(userFollow -> userFollowRepository.delete(userFollow));
    }

    public List<PostResponse> getFollowingFeed() {
        UUID currentUserId = getCurrentUserId();
        System.out.println("[DEBUG] getFollowingFeed for user: " + currentUserId);
        
        List<UUID> followingIds = userFollowRepository.findByFollowerId(currentUserId)
                .stream().map(UserFollow::getFolloweeId).collect(Collectors.toList());

        System.out.println("[DEBUG] followingIds found: " + followingIds);

        if (followingIds.isEmpty()) {
            System.out.println("[DEBUG] followingIds is empty, returning empty list.");
            return List.of();
        }

        List<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(followingIds);
        System.out.println("[DEBUG] posts found: " + posts.size());
        
        return posts.stream()
                .map(post -> {
                    boolean liked = postLikeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent();
                    return mapToPostResponse(post, liked);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void joinTopic(String topicName) {
        UUID currentUserId = getCurrentUserId();
        Optional<CommunitySubscription> existing = communitySubscriptionRepository.findByUserIdAndTopicName(currentUserId, topicName);
        if (existing.isEmpty()) {
            communitySubscriptionRepository.save(new CommunitySubscription(currentUserId, topicName));
        }
    }

    public List<String> getMyTopics() {
        UUID currentUserId = getCurrentUserId();
        return communitySubscriptionRepository.findByUserId(currentUserId)
                .stream()
                .map(CommunitySubscription::getTopicName)
                .collect(Collectors.toList());
    }

    /**
     * Delete a post owned by the current user.
     * Manually cleans up related comments and likes due to lack of DB-level cascading.
     */
    @Transactional
    public void deletePost(UUID postId) {
        UUID currentUserId = getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts");
        }

        // Cleanup orphaned data
        postLikeRepository.deleteByPostId(postId);
        commentRepository.deleteByPostId(postId);
        
        // Delete the post
        postRepository.delete(post);
    }

    /**
     * Delete a comment owned by the current user.
     */
    @Transactional
    public void deleteComment(UUID commentId) {
        UUID currentUserId = getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private List<CommentResponse> getCommentsForPost(UUID postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream().map(this::mapToCommentResponse).collect(Collectors.toList());
    }

    private PostResponse mapToPostResponse(Post post, boolean liked) {
        return mapToPostResponse(post, liked, getCommentsForPost(post.getId()));
    }

    private PostResponse mapToPostResponse(Post post, boolean liked, List<CommentResponse> comments) {
        String nickname = "Unknown";
        String avatarUrl = null;
        try {
            User user = userService.getUserById(post.getUserId().toString());
            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                nickname = user.getNickname();
            }
            avatarUrl = user.getAvatarUrl();
        } catch (Exception e) {
            // User not found or UUID format error (esp in mocked tests)
        }
        return new PostResponse(
                post.getId(),
                post.getUserId(),
                nickname,
                avatarUrl,
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments,
                liked
        );
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        String nickname = "Unknown";
        String avatarUrl = null;
        try {
            User user = userService.getUserById(comment.getUserId().toString());
            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                nickname = user.getNickname();
            }
            avatarUrl = user.getAvatarUrl();
        } catch (Exception e) {
            // User not found
        }
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                nickname,
                avatarUrl,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}