package com.dealershop.dealer_shop_backend.security;

import com.dealershop.dealer_shop_backend.entity.Role;

public record AuthenticatedUser(Long id, String email, Role role) {
}