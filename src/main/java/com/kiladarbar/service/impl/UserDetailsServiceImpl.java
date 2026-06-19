package com.kiladarbar.service.impl;

import com.kiladarbar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return loadUserById(UUID.fromString(userId));
    }

    public UserDetails loadUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    String roleName = user.getRole() != null ? user.getRole().getName() : "CUSTOMER";
                    return User.builder()
                            .username(user.getId().toString())
                            .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}
