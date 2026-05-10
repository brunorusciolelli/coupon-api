package com.brunorusciolelli.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(
        @NotBlank(message = "Code is required")
        String code,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Discount value is required")
        BigDecimal discountValue,

        @NotNull(message = "Expiration date is required")
        Instant expirationDate,

        Boolean published
) {
}