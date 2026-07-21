package com.webox.auth;

import com.webox.auth.dto.AuthResponse;
import com.webox.auth.dto.LoginRequest;
import com.webox.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Token probe: lets the SPA revalidate a persisted token after refresh. */
    @GetMapping("/me")
    public AuthResponse.UserView me() {
        AuthUser user = AuthContext.require();
        return new AuthResponse.UserView(user.id(), user.email(), user.role());
    }
}
