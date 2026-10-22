package com.foodprint.community;

import com.foodprint.community.dto.CreateCommentRequest;
import com.foodprint.community.dto.CreatePostRequest;
import com.foodprint.community.dto.CommentResponse;
import com.foodprint.community.dto.PostResponse;
import com.foodprint.community.entity.Comment;
import com.foodprint.community.entity.Post;
import com.foodprint.community.entity.PostLike;
import com.foodprint.community.repository.CommentRepository;
import com.foodprint.community.repository.PostLikeRepository;
import com.foodprint.community.repository.PostRepository;
import com.foodprint.community.service.CommunityService;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommunityServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CommunityService communityService;

    private final java.util.UUID dummyPostId = java.util.UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("00000000-0000-0000-0000-000000000001");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreatePost() {
        CreatePostRequest request = new CreatePostRequest("Test Title", "Test Content", "image.jpg");
        Post post = new Post(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test Title", "Test Content", "image.jpg");
        post.setId(dummyPostId);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = communityService.createPost(request);

        assertNotNull(response);
        assertEquals("Test Title", response.getTitle());
        assertEquals("Test Content", response.getContent());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testAddComment() {
        CreateCommentRequest request = new CreateCommentRequest("Test Comment");
        Comment comment = new Comment(dummyPostId, java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test Comment");
        comment.setId(dummyPostId);

        when(postRepository.findById(dummyPostId)).thenReturn(Optional.of(new Post()));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = communityService.addComment(dummyPostId, request);

        assertNotNull(response);
        assertEquals("Test Comment", response.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testLikePost() {
        when(postLikeRepository.findByPostIdAndUserId(dummyPostId, java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.empty());
        when(postRepository.findById(dummyPostId)).thenReturn(Optional.of(new Post()));

        communityService.likePost(dummyPostId);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testUnlikePost() {
        PostLike like = new PostLike(dummyPostId, java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(postLikeRepository.findByPostIdAndUserId(dummyPostId, java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.of(like));
        when(postRepository.findById(dummyPostId)).thenReturn(Optional.of(new Post()));

        communityService.unlikePost(dummyPostId);

        verify(postLikeRepository, times(1)).delete(like);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testGetFeed() {
        Post post = new Post(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"), "Title", "Content", null);
        post.setId(dummyPostId);
        List<Post> posts = Arrays.asList(post);

        User mockUser = new User();
        mockUser.setNickname("TestUser");
        when(userService.getUserById("00000000-0000-0000-0000-000000000001")).thenReturn(mockUser);

        when(postRepository.findAllOrderByCreatedAtDesc()).thenReturn(posts);
        when(postLikeRepository.findByPostIdAndUserId(dummyPostId, java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.empty());
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(dummyPostId)).thenReturn(Arrays.asList());

        List<PostResponse> feed = communityService.getFeed(0, 10);

        assertNotNull(feed);
        assertEquals(1, feed.size());
        assertEquals("Title", feed.get(0).getTitle());
    }
}