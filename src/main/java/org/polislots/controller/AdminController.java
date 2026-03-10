package org.polislots.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.polislots.dto.AdminUserResponse;
import org.polislots.dto.UpdateBalanceRequest;
import org.polislots.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            Authentication authentication,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(search, authentication.getName()));
    }

    @PutMapping("/users/{id}/balance")
    public ResponseEntity<AdminUserResponse> updateBalance(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(adminService.updateBalance(id, request, authentication.getName()));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<AdminUserResponse> ban(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.banUser(id, authentication.getName()));
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<AdminUserResponse> unban(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.unbanUser(id, authentication.getName()));
    }
}
