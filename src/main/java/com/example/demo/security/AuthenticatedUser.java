package com.example.demo.security;

public record AuthenticatedUser(Long id, String email, String rol) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(rol) || "ROLE_ADMIN".equalsIgnoreCase(rol);
    }
}
