package com.webox.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registration input. Limits per PRD §3.1: email <= 200 chars; password >= 8 with letters+digits. */
public record RegisterRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 200, message = "Email must be at most 200 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain both letters and numbers.")
        String password) {
}
