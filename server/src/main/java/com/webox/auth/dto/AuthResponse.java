package com.webox.auth.dto;

import com.webox.common.enums.Role;

public record AuthResponse(String token, UserView user) {

    public record UserView(Long id, String email, Role role) {
    }
}
