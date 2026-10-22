package com.foodprint.user.dto;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String nickname;
    private String avatarUrl;
}
