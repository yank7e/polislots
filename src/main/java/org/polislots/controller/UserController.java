package org.polislots.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.polislots.dto.*;
import org.polislots.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }

    @PutMapping("/me/username")
    public ResponseEntity<AuthResponse> updateUsername(
            Authentication authentication,
            @Valid @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(userService.updateUsername(authentication.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request) {
        return ResponseEntity.ok(userService.updatePassword(authentication.getName(), request));
    }

    @PutMapping("/me/email")
    public ResponseEntity<UserResponse> updateEmail(
            Authentication authentication,
            @Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(userService.updateEmail(authentication.getName(), request));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<UserResponse> updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.updateAvatar(authentication.getName(), file));
    }
}
