package com.dealershop.dealer_shop_backend.dto;

import com.dealershop.dealer_shop_backend.entity.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role
) {
}