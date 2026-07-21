package com.webox.auth;

import com.webox.common.enums.Role;

/** The authenticated principal, carried per-request in {@link AuthContext}. */
public record AuthUser(Long id, String email, Role role) {
}
