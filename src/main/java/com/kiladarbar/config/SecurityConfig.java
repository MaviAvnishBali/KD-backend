package com.kiladarbar.config;

import com.kiladarbar.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_PATHS = {
            "/v1/auth/**",
            "/v1/products/**", "/v1/categories/**",
            "/v1/banners/**", "/v1/offers/**",
            "/v1/branches/*/info",
            "/v1/reservations/availability",
            "/v1/payments/webhook/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] CUSTOMER_PATHS = {
            "/v1/orders/**",
            "/v1/cart/**",
            "/v1/users/me/**",
            "/v1/reservations/**",
            "/v1/reviews/**",
            "/v1/loyalty/**",
            "/v1/notifications/me/**",
            "/v1/addresses/**"
    };

    private static final String[] ADMIN_PATHS = {
            "/v1/admin/**",
            "/v1/inventory/**",
            "/v1/employees/**",
            "/v1/marketing/**",
            "/v1/reports/**",
            "/v1/kds/**",
            "/v1/pos/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/**", "/v1/categories/**").permitAll()
                        .requestMatchers(CUSTOMER_PATHS).hasAnyRole("CUSTOMER", "MANAGER", "OWNER", "SUPER_ADMIN")
                        .requestMatchers(ADMIN_PATHS).hasAnyRole("MANAGER", "OWNER", "SUPER_ADMIN")
                        .requestMatchers("/v1/super-admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/v1/kds/**").hasAnyRole("CHEF", "KITCHEN_STAFF", "MANAGER", "OWNER")
                        .requestMatchers("/v1/pos/**").hasAnyRole("CASHIER", "MANAGER", "OWNER")
                        .requestMatchers("/v1/delivery/**").hasAnyRole("DELIVERY_PARTNER", "MANAGER", "OWNER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
