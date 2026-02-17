package com.processedfood.inventory.controller;

import com.processedfood.inventory.dto.UserRequest;
import com.processedfood.inventory.dto.UserResponse;
import com.processedfood.inventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers();
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        userService.delete(id, authentication.getName());
    }
}
