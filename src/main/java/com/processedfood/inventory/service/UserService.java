package com.processedfood.inventory.service;

import com.processedfood.inventory.dto.UserRequest;
import com.processedfood.inventory.dto.UserResponse;
import com.processedfood.inventory.exception.BadRequestException;
import com.processedfood.inventory.model.User;
import com.processedfood.inventory.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole()))
                .toList();
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }

    public void delete(Long id, String currentUsername) {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new BadRequestException("You cannot delete the currently logged-in user");
        }
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Default admin cannot be deleted");
        }
        userRepository.delete(user);
    }
}
