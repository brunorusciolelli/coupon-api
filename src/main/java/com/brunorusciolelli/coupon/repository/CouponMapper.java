package com.brunorusciolelli.coupon.repository;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponJpaEntity toEntity(Coupon coupon) {
        return new CouponJpaEntity(
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

    public Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.restore(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDiscountValue(),
                entity.getExpirationDate(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.getStatus()
        );
    }
}