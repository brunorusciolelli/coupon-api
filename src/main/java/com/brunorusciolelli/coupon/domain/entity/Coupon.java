package com.brunorusciolelli.coupon.domain.entity;

import com.brunorusciolelli.coupon.exception.InvalidCouponException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT_VALUE = new BigDecimal("0.5");

    private final UUID id;
    private final String code;
    private final String description;
    private final BigDecimal discountValue;
    private final Instant expirationDate;
    private final boolean published;
    private final boolean redeemed;
    private CouponStatus status;

    private Coupon(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            Instant expirationDate,
            boolean published,
            boolean redeemed,
            CouponStatus status
    ) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.redeemed = redeemed;
        this.status = status;
    }

    public static Coupon create(
            String code,
            String description,
            BigDecimal discountValue,
            Instant expirationDate,
            boolean published
    ) {
        String sanitizedCode = sanitizeCode(code);
        validateCode(sanitizedCode);
        validateDescription(description);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        CouponStatus initialStatus = published ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;
        return new Coupon(
                UUID.randomUUID(),
                sanitizedCode,
                description,
                discountValue,
                expirationDate,
                published,
                false,
                initialStatus
        );
    }

    public static Coupon restore(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            Instant expirationDate,
            boolean published,
            boolean redeemed,
            CouponStatus status
    ) {
        return new Coupon(id, code, description, discountValue, expirationDate, published, redeemed, status);
    }

    public void softDelete() {
        if (status == CouponStatus.DELETED) {
            throw new InvalidCouponException("Coupon is already deleted");
        }
        this.status = CouponStatus.DELETED;
    }

    private static String sanitizeCode(String code) {
        if (code == null) {
            return "";
        }
        return code.replaceAll("[^A-Za-z0-9]", "");
    }

    private static void validateCode(String sanitizedCode) {
        if (sanitizedCode.isBlank()) {
            throw new InvalidCouponException("Code is required");
        }
        if (sanitizedCode.length() != CODE_LENGTH) {
            throw new InvalidCouponException(
                    "Code must have exactly " + CODE_LENGTH + " alphanumeric characters"
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidCouponException("Description is required");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new InvalidCouponException("Discount value is required");
        }
        if (discountValue.compareTo(MIN_DISCOUNT_VALUE) < 0) {
            throw new InvalidCouponException(
                    "Discount value must be at least " + MIN_DISCOUNT_VALUE
            );
        }
    }

    private static void validateExpirationDate(Instant expirationDate) {
        if (expirationDate == null) {
            throw new InvalidCouponException("Expiration date is required");
        }
        if (expirationDate.isBefore(Instant.now())) {
            throw new InvalidCouponException("Expiration date cannot be in the past");
        }
    }
}