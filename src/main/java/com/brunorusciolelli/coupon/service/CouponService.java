package com.brunorusciolelli.coupon.service;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.dto.CreateCouponRequest;
import com.brunorusciolelli.coupon.exception.CouponNotFoundException;
import com.brunorusciolelli.coupon.repository.CouponJpaEntity;
import com.brunorusciolelli.coupon.repository.CouponMapper;
import com.brunorusciolelli.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository repository;
    private final CouponMapper mapper;

    public CouponService(CouponRepository repository, CouponMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public Coupon findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new CouponNotFoundException("Coupon with id " + id + " not found"));
    }

    @Transactional
    public void deleteCoupon(UUID id) {
        CouponJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon with id " + id + " not found"));

        Coupon coupon = mapper.toDomain(entity);
        coupon.softDelete();

        repository.save(mapper.toEntity(coupon));
    }

    @Transactional(readOnly = true)
    public List<Coupon> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}