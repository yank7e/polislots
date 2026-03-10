package org.polislots.service;

import lombok.RequiredArgsConstructor;
import org.polislots.dto.AdminUserResponse;
import org.polislots.dto.UpdateBalanceRequest;
import org.polislots.model.User;
import org.polislots.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Value("${app.superadmin-username}")
    private String superAdminUsername;

    public boolean isSuperAdmin(String username) {
        return superAdminUsername.equals(username);
    }

    public List<AdminUserResponse> getUsers(String search, String requester) {
        requireSuperAdmin(requester);
        List<User> users = (search == null || search.isBlank())
                ? userRepository.findAll()
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
        return users.stream().map(this::toAdminResponse).toList();
    }

    public AdminUserResponse updateBalance(Long userId, UpdateBalanceRequest request, String requester) {
        requireSuperAdmin(requester);
        User user = findUser(userId);
        user.setBalance(request.balance());
        userRepository.save(user);
        return toAdminResponse(user);
    }

    public AdminUserResponse banUser(Long userId, String requester) {
        requireSuperAdmin(requester);
        User user = findUser(userId);
        if (superAdminUsername.equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя забанить суперадмина");
        }
        user.setBanned(true);
        userRepository.save(user);
        return toAdminResponse(user);
    }

    public AdminUserResponse unbanUser(Long userId, String requester) {
        requireSuperAdmin(requester);
        User user = findUser(userId);
        user.setBanned(false);
        userRepository.save(user);
        return toAdminResponse(user);
    }

    private void requireSuperAdmin(String username) {
        if (!superAdminUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещён");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    private AdminUserResponse toAdminResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getProvider().name(),
                user.getBalance(),
                user.isBanned(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
