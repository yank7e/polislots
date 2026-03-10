package org.polislots.service;

import lombok.RequiredArgsConstructor;
import org.polislots.dto.*;
import org.polislots.model.User;
import org.polislots.repository.UserRepository;
import org.polislots.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminService adminService;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public UserResponse getProfile(String username) {
        return toResponse(findUser(username));
    }

    public AuthResponse updateUsername(String currentUsername, UpdateUsernameRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Никнейм уже занят");
        }
        User user = findUser(currentUsername);
        user.setUsername(request.username());
        userRepository.save(user);
        String newToken = jwtService.generateToken(user.getUsername());
        return new AuthResponse(newToken, user.getUsername(), user.getBalance());
    }

    public UserResponse updatePassword(String username, UpdatePasswordRequest request) {
        User user = findUser(username);
        if (user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Смена пароля недоступна для аккаунтов через Google");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Текущий пароль неверен");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateEmail(String username, UpdateEmailRequest request) {
        User user = findUser(username);
        user.setEmail(request.email());
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateAvatar(String username, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл пустой");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Разрешены только изображения");
        }

        User user = findUser(username);

        try {
            Path dir = Paths.get(uploadDir, "avatars");
            Files.createDirectories(dir);

            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            file.transferTo(dir.resolve(filename));

            deleteOldAvatar(user.getAvatarUrl());

            user.setAvatarUrl("/uploads/avatars/" + filename);
            userRepository.save(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить аватар");
        }

        return toResponse(user);
    }

    private void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith("/uploads/")) return;
        try {
            String relative = avatarUrl.substring("/uploads/".length());
            Files.deleteIfExists(Paths.get(uploadDir, relative));
        } catch (IOException ignored) {}
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getBalance(),
                user.getAvatarUrl(),
                user.getEmail(),
                user.getProvider().name(),
                adminService.isSuperAdmin(user.getUsername())
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
