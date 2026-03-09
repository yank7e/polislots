package org.polislots.dto;

public record UserResponse(
        String username,
        Long balance,
        String avatarUrl
) {}