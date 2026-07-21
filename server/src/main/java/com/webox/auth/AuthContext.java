package com.webox.auth;

import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;

/** Per-request holder of the authenticated user; populated by {@link AuthInterceptor}. */
public final class AuthContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    /** Returns the current user or throws 401 — use in endpoints that require login. */
    public static AuthUser require() {
        AuthUser user = HOLDER.get();
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
