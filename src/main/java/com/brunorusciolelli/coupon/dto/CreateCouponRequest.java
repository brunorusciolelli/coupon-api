package com.brunorusciolelli.coupon.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(
        String code,
        String description,
        BigDecimal discountValue,
        Instant expirationDate,
        Boolean published
) {
}