package org.polislots.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.polislots.model.AuthProvider;
import org.polislots.model.User;
import org.polislots.repository.UserRepository;
import org.polislots.security.jwt.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = findOrCreateUser(oAuth2User);
        String token = jwtService.generateToken(user.getUsername());
        getRedirectStrategy().sendRedirect(request, response, "http://localhost/oauth2/callback?token=" + token);
    }

    private User findOrCreateUser(OAuth2User oAuth2User) {
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        return userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(resolveUniqueUsername(name));
                    newUser.setEmail(email);
                    newUser.setProvider(AuthProvider.GOOGLE);
                    newUser.setProviderId(providerId);
                    newUser.setAvatarUrl(avatarUrl);
                    return userRepository.save(newUser);
                });
    }

    private String resolveUniqueUsername(String base) {
        String candidate = base != null ? base.replaceAll("\\s+", "_") : "google_user";
        if (!userRepository.existsByUsername(candidate)) return candidate;
        return candidate + "_" + System.currentTimeMillis() % 10000;
    }
}
