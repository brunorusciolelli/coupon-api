package com.brunorusciolelli.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponJpaEntity, UUID> {
}