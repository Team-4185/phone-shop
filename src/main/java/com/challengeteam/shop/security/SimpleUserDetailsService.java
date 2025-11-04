package com.challengeteam.shop.security;

import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleUserDetailsService implements UserDetailsService {

    private final UserService userService;


    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService
                .getByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found user with email: " + username));

        return new SimpleUserDetails(user);
    }

    static class SimpleUserDetails implements UserDetails {
        public static final String SPRING_SECURITY_ROLE_PREFIX = "ROLE_";
        private final User user;

        public SimpleUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String roleName = SPRING_SECURITY_ROLE_PREFIX + user.getRole().getName();

            return List.of(new SimpleGrantedAuthority(roleName));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

    }

}
