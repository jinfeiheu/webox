package com.webox.auth;

import com.webox.common.enums.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller (or method) as requiring a specific role — e.g. Console endpoints
 * are ADMIN-only (PRD §4.3). Enforced by {@link AuthInterceptor} with a 403 response.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    Role value();
}
