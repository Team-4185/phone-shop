package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper component for extracting authenticated user information from Spring Security context.
 * <p>
 * This utility component provides functionality to retrieve the current authenticated user
 * from the security context using the provided {@link Authentication} object. It handles
 * authentication validation and user lookup from the repository.
 * </p>
 *
 * @see UserRepository
 * @see Authentication
 * @see User
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUserExtractorHelper {
    private final UserRepository userRepository;

    /**
     * Extracts the authenticated user from the provided authentication object.
     * <p>
     * This method validates the authentication state and retrieves the corresponding user
     * from the database using the email address stored in the authentication principal.
     * The email is converted to lowercase before performing the lookup.
     * </p>
     *
     * @param authentication the {@link Authentication} object from the security context,
     *                       containing the authenticated user's credentials and details
     * @return an {@link Optional} containing the {@link User} if authentication is valid
     * and the user exists in the database, or {@link Optional#empty()} if the
     * authentication is null, not authenticated, or the user is not found
     */
    public Optional<User> extractUserFromSecurityContextHolder(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName().toLowerCase());
    }
}