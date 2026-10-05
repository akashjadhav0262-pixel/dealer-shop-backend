package com.dealershop.dealer_shop_backend.mapper;

import org.springframework.stereotype.Component;

import com.dealershop.dealer_shop_backend.dto.UserResponse;
import com.dealershop.dealer_shop_backend.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}