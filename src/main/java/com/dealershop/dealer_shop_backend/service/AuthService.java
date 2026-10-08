package com.dealershop.dealer_shop_backend.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealershop.dealer_shop_backend.dto.LoginRequest;
import com.dealershop.dealer_shop_backend.dto.LoginResponse;
import com.dealershop.dealer_shop_backend.dto.RegisterRequest;
import com.dealershop.dealer_shop_backend.dto.UserResponse;
import com.dealershop.dealer_shop_backend.entity.Role;
import com.dealershop.dealer_shop_backend.entity.User;
import com.dealershop.dealer_shop_backend.exception.ConflictException;
import com.dealershop.dealer_shop_backend.exception.UnauthorizedException;
import com.dealershop.dealer_shop_backend.mapper.UserMapper;
import com.dealershop.dealer_shop_backend.repository.UserRepository;
import com.dealershop.dealer_shop_backend.security.JwtService;

@Service
public class AuthService {

    private static final String INVALID_LOGIN_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    // Used when the email is unknown, so the response takes the same time as a real check
    private final String dummyHash;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.DEALER);

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Optional<User> found = userRepository.findByEmail(email);
        String hashToCheck = found.map(User::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (found.isEmpty() || !passwordMatches || !found.get().isActive()) {
            throw new UnauthorizedException(INVALID_LOGIN_MESSAGE);
        }

        User user = found.get();
        String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getAccessTokenSeconds(),
                userMapper.toResponse(user)
        );
    }
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return userMapper.toResponse(user);
    }
}