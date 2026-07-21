package com.webox.auth;

import com.webox.auth.dto.AuthResponse;
import com.webox.auth.dto.LoginRequest;
import com.webox.auth.dto.RegisterRequest;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BizException(ErrorCode.EMAIL_TAKEN);
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.EMPLOYEE);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        User user = userRepository.findByEmail(email).orElse(null);
        // Same error whether the email is unknown or the password wrong — no account enumeration.
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.INVALID_CREDENTIALS);
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        AuthUser principal = new AuthUser(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(
                jwtService.generate(principal),
                new AuthResponse.UserView(user.getId(), user.getEmail(), user.getRole()));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
