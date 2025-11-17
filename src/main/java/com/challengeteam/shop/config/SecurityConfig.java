package com.challengeteam.shop.config;

import com.challengeteam.shop.properties.CorsProperties;
import com.challengeteam.shop.security.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SneakyThrows
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean("mainCorsConfig")
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        // create common cors configuration
        CorsConfiguration commonCorsConfig = new CorsConfiguration();
        commonCorsConfig.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        commonCorsConfig.setAllowedMethods(corsProperties.getAllowedMethods());
        commonCorsConfig.setAllowedHeaders(corsProperties.getAllowedHeaders());
        commonCorsConfig.setAllowCredentials(corsProperties.isAllowCredentials());
        commonCorsConfig.setMaxAge(corsProperties.getMaxCacheAge());

        // Set common cors configuration for any request pattern
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", commonCorsConfig);

        return corsConfigurationSource;
    }

    // SECURITY CONFIG
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenFilter jwtTokenFilter,
                                                   @Qualifier("mainCorsConfig") CorsConfigurationSource corsConfig) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfig))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()             // critical for CORS
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
