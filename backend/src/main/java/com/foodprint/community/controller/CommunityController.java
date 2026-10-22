package com.foodprint.community.controller;

import java.util.UUID;



// TODO [Engineer C]: Implement CommunityController
//
// Endpoints:
//   POST   /api/community/posts           → create post
//   GET    /api/community/posts           → feed (paginated)
//   POST   /api/community/posts/{id}/comments  → add comment
//   POST   /api/community/posts/{id}/like      → like
//   DELETE /api/community/posts/{id}/like      → unlike

import com.foodprint.community.dto.*;
import com.foodprint.community.service.CommunityService;
import com.foodprint.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserService userService;

    @GetMapping("/impact-metrics")
    public ResponseEntity<ImpactMetricsResponse> getImpactMetrics() {
        long totalUsers = userService.getTotalUsers();
        // Since we don't store initial weight yet, use a realistic base mock + dynamic user factor
        double totalWeightLost = 850000.0 + (totalUsers * 4.5); 
        
        ImpactMetricsResponse response = new ImpactMetricsResponse(totalUsers, totalWeightLost);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request) {
        PostResponse response = communityService.createPost(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponse> feed = communityService.getFeed(page, size);
        return ResponseEntity.ok(feed);
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID id,
            @RequestBody CreateCommentRequest request) {
        CommentResponse response = communityService.addComment(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, Boolean>> likePost(@PathVariable UUID id) {
        communityService.likePost(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, Boolean>> unlikePost(@PathVariable UUID id) {
        communityService.unlikePost(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<Map<String, Boolean>> followUser(@PathVariable UUID userId) {
        communityService.followUser(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<Map<String, Boolean>> unfollowUser(@PathVariable UUID userId) {
        communityService.unfollowUser(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/posts/following")
    public ResponseEntity<List<PostResponse>> getFollowingFeed() {
        List<PostResponse> feed = communityService.getFollowingFeed();
        return ResponseEntity.ok(feed);
    }

    @PostMapping("/topics/{topicName}/join")
    public ResponseEntity<Map<String, Boolean>> joinTopic(@PathVariable String topicName) {
        communityService.joinTopic(topicName);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/my-topics")
    public ResponseEntity<List<String>> getMyTopics() {
        List<String> topics = communityService.getMyTopics();
        return ResponseEntity.ok(topics);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, Boolean>> deletePost(@PathVariable UUID id) {
        communityService.deletePost(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(@PathVariable UUID id) {
        communityService.deleteComment(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
