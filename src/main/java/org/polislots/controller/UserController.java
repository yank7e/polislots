package org.polislots.controller;

import lombok.RequiredArgsConstructor;
import org.polislots.dto.UserResponse;
import org.polislots.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .map(u -> ResponseEntity.ok(new UserResponse(u.getUsername(), u.getBalance(), u.getAvatarUrl())))
                .orElse(ResponseEntity.notFound().build());
    }
}