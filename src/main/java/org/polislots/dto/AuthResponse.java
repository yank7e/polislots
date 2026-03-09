package org.polislots.dto;

public record AuthResponse(
        String token,
        String username,
        Long balance
) {}