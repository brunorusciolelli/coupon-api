package com.brunorusciolelli.coupon.repository;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.domain.entity.CouponStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private CouponMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponMapper();
    }

    @Test
    @DisplayName("toEntity should preserve all fields from domain")
    void toEntityShouldPreserveAllFields() {
        Coupon coupon = Coupon.create(
                "ABC123",
                "Black Friday",
                new BigDecimal("0.5"),
                Instant.now().plus(30, ChronoUnit.DAYS),
                true
        );

        CouponJpaEntity entity = mapper.toEntity(coupon);

        assertThat(entity.getId()).isEqualTo(coupon.getId());
        assertThat(entity.getCode()).isEqualTo(coupon.getCode());
        assertThat(entity.getDescription()).isEqualTo(coupon.getDescription());
        assertThat(entity.getDiscountValue()).isEqualByComparingTo(coupon.getDiscountValue());
        assertThat(entity.getExpirationDate()).isEqualTo(coupon.getExpirationDate());
        assertThat(entity.getStatus()).isEqualTo(coupon.getStatus());
        assertThat(entity.isPublished()).isEqualTo(coupon.isPublished());
        assertThat(entity.isRedeemed()).isEqualTo(coupon.isRedeemed());
    }

    @Test
    @DisplayName("toDomain should preserve all fields from JPA entity")
    void toDomainShouldPreserveAllFields() {
        UUID id = UUID.randomUUID();
        Instant expiration = Instant.now().plus(30, ChronoUnit.DAYS);

        CouponJpaEntity entity = new CouponJpaEntity(
                id,
                "ABC123",
                "Black Friday",
                new BigDecimal("0.5"),
                expiration,
                CouponStatus.ACTIVE,
                true,
                false
        );

        Coupon coupon = mapper.toDomain(entity);

        assertThat(coupon.getId()).isEqualTo(id);
        assertThat(coupon.getCode()).isEqualTo("ABC123");
        assertThat(coupon.getDescription()).isEqualTo("Black Friday");
        assertThat(coupon.getDiscountValue()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(coupon.getExpirationDate()).isEqualTo(expiration);
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(coupon.isPublished()).isTrue();
        assertThat(coupon.isRedeemed()).isFalse();
    }

    @Test
    @DisplayName("round trip should preserve all data")
    void roundTripShouldPreserveAllData() {
        Coupon original = Coupon.create(
                "XYZ789",
                "Cyber Monday discount",
                new BigDecimal("10.5"),
                Instant.now().plus(60, ChronoUnit.DAYS),
                false
        );

        Coupon roundTrip = mapper.toDomain(mapper.toEntity(original));

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getCode()).isEqualTo(original.getCode());
        assertThat(roundTrip.getDescription()).isEqualTo(original.getDescription());
        assertThat(roundTrip.getDiscountValue()).isEqualByComparingTo(original.getDiscountValue());
        assertThat(roundTrip.getExpirationDate()).isEqualTo(original.getExpirationDate());
        assertThat(roundTrip.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTrip.isPublished()).isEqualTo(original.isPublished());
        assertThat(roundTrip.isRedeemed()).isEqualTo(original.isRedeemed());
    }

    @Test
    @DisplayName("toDomain should preserve DELETED status")
    void toDomainShouldPreserveDeletedStatus() {
        CouponJpaEntity entity = new CouponJpaEntity(
                UUID.randomUUID(),
                "DEL123",
                "Deleted coupon",
                new BigDecimal("1.0"),
                Instant.now().plus(30, ChronoUnit.DAYS),
                CouponStatus.DELETED,
                true,
                false
        );

        Coupon coupon = mapper.toDomain(entity);

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
    }
}