package org.polislots.dto;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        String provider,
        Long balance,
        boolean banned,
        String createdAt
) {}
