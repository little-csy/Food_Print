package com.foodprint.community;

import com.foodprint.community.entity.Comment;
import com.foodprint.community.entity.Post;
import com.foodprint.community.repository.CommentRepository;
import com.foodprint.community.repository.PostLikeRepository;
import com.foodprint.community.repository.PostRepository;
import com.foodprint.community.service.CommunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunityDeleteTest {

    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private CommunityService communityService;

    private final UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID postId = UUID.randomUUID();
    private final UUID commentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAuth(UUID userId) {
        when(authentication.getName()).thenReturn(userId.toString());
    }

    @Test
    void deletePost_Success() {
        mockAuth(ownerId);
        Post post = new Post(ownerId, "Title", "Content", null);
        post.setId(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> communityService.deletePost(postId));

        verify(postLikeRepository, times(1)).deleteByPostId(postId);
        verify(commentRepository, times(1)).deleteByPostId(postId);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void deletePost_Unauthorized() {
        mockAuth(otherUserId);
        Post post = new Post(ownerId, "Title", "Content", null);
        post.setId(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> communityService.deletePost(postId));
        
        assertTrue(exception.getMessage().contains("Unauthorized"));
        verify(postRepository, never()).delete(any());
        verify(commentRepository, never()).deleteByPostId(any());
    }

    @Test
    void deletePost_NotFound() {
        mockAuth(ownerId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> communityService.deletePost(postId));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteComment_Success() {
        mockAuth(ownerId);
        Comment comment = new Comment(postId, ownerId, "Content");
        comment.setId(commentId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertDoesNotThrow(() -> communityService.deleteComment(commentId));

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteComment_Unauthorized() {
        mockAuth(otherUserId);
        Comment comment = new Comment(postId, ownerId, "Content");
        comment.setId(commentId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> communityService.deleteComment(commentId));
        
        assertTrue(exception.getMessage().contains("Unauthorized"));
        verify(commentRepository, never()).delete(any());
    }
}
