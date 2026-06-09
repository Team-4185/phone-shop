package com.challengeteam.shop.config.security;

import com.challengeteam.shop.properties.CorsProperties;
import com.challengeteam.shop.security.filter.AuthRateLimitFilter;
import com.challengeteam.shop.security.filter.JwtTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
        CorsConfiguration commonCorsConfig = new CorsConfiguration();
        commonCorsConfig.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        commonCorsConfig.setAllowedMethods(corsProperties.getAllowedMethods());
        commonCorsConfig.setAllowedHeaders(corsProperties.getAllowedHeaders());
        commonCorsConfig.setAllowCredentials(corsProperties.isAllowCredentials());
        commonCorsConfig.setMaxAge(corsProperties.getMaxCacheAge());

        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", commonCorsConfig);

        return corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenFilter jwtTokenFilter,
                                                   AuthRateLimitFilter authRateLimitFilter,
                                                   Environment environment,
                                                   @Qualifier("mainCorsConfig") CorsConfigurationSource corsConfig) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfig))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                            .requestMatchers("/actuator/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/v1/logout").authenticated()
                            .requestMatchers("/api/auth/**").permitAll();

                    if (!environment.acceptsProfiles(Profiles.of("prod"))) {
                        auth
                                .requestMatchers("/docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/v1/test-data/**").hasRole("ADMIN");
                    }

                    auth
                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/filter/**").permitAll()
                            .requestMatchers("/api/v1/images/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/phones/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/phones").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/v1/phones/*").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/phones/*").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/v1/phones/*/add-image").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/phones/*/images/*").hasRole("ADMIN")
                            .requestMatchers("/api/v1/delivery/**").permitAll()
                            .requestMatchers("/api/v1/payments/webhooks/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("ADMIN")
                            .requestMatchers("/api/v1/orders/**").authenticated()
                            .requestMatchers("/api/v1/users/me").authenticated()
                            .requestMatchers("/api/v1/users/me/**").authenticated()
                            .requestMatchers("/api/v1/users/sensitive").authenticated()
                            .requestMatchers("/api/v1/me/**").authenticated()
                            .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/users").hasRole("ADMIN")
                            .anyRequest().authenticated();
                }
                )
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                                }
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    response.getWriter().write("{\"error\": \"Forbidden\"}");
                                }
                        ))
                .build();
    }
}
