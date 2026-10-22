package com.foodprint.community;

import com.foodprint.community.controller.CommunityController;
import com.foodprint.community.dto.CreatePostRequest;
import com.foodprint.community.dto.PostResponse;
import com.foodprint.community.service.CommunityService;
import com.foodprint.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(CommunityController.class)
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final java.util.UUID dummyPostId = java.util.UUID.randomUUID();

    @MockBean
    private CommunityService communityService;

    @MockBean
    private UserService userService;

    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void testCreatePost() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Test Title", "Test Content", null);
        PostResponse response = new PostResponse();
        response.setId(dummyPostId);
        response.setTitle("Test Title");
        response.setContent("Test Content");

        when(communityService.createPost(any(CreatePostRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/community/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Title\",\"content\":\"Test Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dummyPostId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }
}