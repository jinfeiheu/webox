package com.webox.auth;

import com.webox.auth.dto.AuthResponse;
import com.webox.auth.dto.LoginRequest;
import com.webox.auth.dto.RegisterRequest;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Auth rules (PRD §3.1): duplicate email, wrong password, plaintext-never-stored. Mockito, no DB. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String RAW_PASSWORD = "pass1234";

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;

    @InjectMocks AuthService service;

    @Test
    void register_duplicateEmail_rejected() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Alice@Example.com", RAW_PASSWORD)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_TAKEN);
    }

    @Test
    void register_newEmail_storesHashedPasswordAndReturnsToken() {
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any())).thenReturn("tok-123");

        AuthResponse res = service.register(new RegisterRequest("bob@example.com", RAW_PASSWORD));

        assertThat(res.token()).isEqualTo("tok-123");
        assertThat(res.user().role()).isEqualTo(Role.EMPLOYEE);
        // The saved password must be a BCrypt hash, never the plaintext.
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(u -> {
            String hash = u.getPasswordHash();
            return hash != null && hash.startsWith("$2a$") && !hash.equals(RAW_PASSWORD);
        }));
    }

    @Test
    void login_unknownEmail_rejectedWithGenericCredentialsError() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ghost@example.com", RAW_PASSWORD)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_wrongPassword_rejectedWithSameGenericError() {
        User user = userWithPassword("other-password");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("alice@example.com", RAW_PASSWORD)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_correctPassword_returnsToken() {
        User user = userWithPassword(RAW_PASSWORD);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(any())).thenReturn("tok-456");

        AuthResponse res = service.login(new LoginRequest("alice@example.com", RAW_PASSWORD));

        assertThat(res.token()).isEqualTo("tok-456");
    }

    private User userWithPassword(String rawPassword) {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPasswordHash(new BCryptPasswordEncoder().encode(rawPassword));
        user.setRole(Role.EMPLOYEE);
        return user;
    }
}
