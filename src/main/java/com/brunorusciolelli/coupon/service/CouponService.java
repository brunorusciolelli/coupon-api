package com.brunorusciolelli.coupon.service;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.dto.CreateCouponRequest;
import com.brunorusciolelli.coupon.exception.CouponNotFoundException;
import com.brunorusciolelli.coupon.repository.CouponJpaEntity;
import com.brunorusciolelli.coupon.repository.CouponMapper;
import com.brunorusciolelli.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository repository;
    private final CouponMapper mapper;

    public CouponService(CouponRepository repository, CouponMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Coupon createCoupon(CreateCouponRequest request) {
        boolean published = Boolean.TRUE.equals(request.published());

        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                published
        );

        CouponJpaEntity entity = mapper.toEntity(coupon);
        repository.save(entity);

        return coupon;
    }

    public void deleteCoupon(UUID id) {
        CouponJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon with id " + id + " not found"));

        Coupon coupon = mapper.toDomain(entity);
        coupon.softDelete();

        CouponJpaEntity updated = mapper.toEntity(coupon);
        repository.save(updated);
    }
}