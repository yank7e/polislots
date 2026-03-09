package org.polislots.repository;

import org.polislots.model.AuthProvider;
import org.polislots.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByUsername(String username);
}