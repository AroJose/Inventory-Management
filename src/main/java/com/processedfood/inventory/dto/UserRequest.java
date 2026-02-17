package com.processedfood.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "ADMIN|STAFF|VIEWER", message = "Role must be ADMIN, STAFF or VIEWER")
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
