package com.brunorusciolelli.coupon.dto;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.domain.entity.CouponStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        Instant expirationDate,
        CouponStatus status,
        boolean published,
        boolean redeemed
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.getStatus(),
                coupon.isPublished(),
                coupon.isRedeemed()
        );
    }

    public static List<CouponResponse> from(List<Coupon> coupons) {
        List<CouponResponse> listCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            listCoupons.add(from(coupon));
        }
        return listCoupons;
    }
}