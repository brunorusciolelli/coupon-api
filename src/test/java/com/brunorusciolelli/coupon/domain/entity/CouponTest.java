package com.brunorusciolelli.coupon.domain.entity;

import com.brunorusciolelli.coupon.exception.InvalidCouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private static final String VALID_CODE = "ABC123";
    private static final String VALID_DESCRIPTION = "Black Friday discount";
    private static final BigDecimal VALID_DISCOUNT = new BigDecimal("0.5");
    private static final Instant FUTURE_DATE = Instant.now().plus(30, ChronoUnit.DAYS);

    @Nested
    @DisplayName("when creating a coupon")
    class Create {

        @Test
        @DisplayName("should create with valid data")
        void shouldCreateWithValidData() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false);

            assertThat(coupon.getId()).isNotNull();
            assertThat(coupon.getCode()).isEqualTo(VALID_CODE);
            assertThat(coupon.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo(VALID_DISCOUNT);
            assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE_DATE);
            assertThat(coupon.isRedeemed()).isFalse();
        }

        @Test
        @DisplayName("should sanitize code by removing special characters")
        void shouldSanitizeCode() {
            Coupon coupon = Coupon.create("ABC-123", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false);

            assertThat(coupon.getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should set status as ACTIVE when published is true")
        void shouldSetStatusActiveWhenPublished() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, true);

            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(coupon.isPublished()).isTrue();
        }

        @Test
        @DisplayName("should set status as INACTIVE when published is false")
        void shouldSetStatusInactiveWhenNotPublished() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false);

            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.INACTIVE);
            assertThat(coupon.isPublished()).isFalse();
        }

        @Test
        @DisplayName("should always start with redeemed=false")
        void shouldAlwaysStartNotRedeemed() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, true);

            assertThat(coupon.isRedeemed()).isFalse();
        }
    }

    @Nested
    @DisplayName("when validating code")
    class CodeValidation {

        @Test
        @DisplayName("should throw when code is null")
        void shouldThrowWhenCodeIsNull() {
            assertThatThrownBy(() -> Coupon.create(null, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("Code");
        }

        @Test
        @DisplayName("should throw when code is blank")
        void shouldThrowWhenCodeIsBlank() {
            assertThatThrownBy(() -> Coupon.create("   ", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class);
        }

        @Test
        @DisplayName("should throw when sanitized code is shorter than 6 characters")
        void shouldThrowWhenCodeTooShort() {
            assertThatThrownBy(() -> Coupon.create("ABC12", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("6");
        }

        @Test
        @DisplayName("should throw when sanitized code is longer than 6 characters")
        void shouldThrowWhenCodeTooLong() {
            assertThatThrownBy(() -> Coupon.create("ABC1234", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("6");
        }

        @Test
        @DisplayName("should throw when code has only special characters")
        void shouldThrowWhenCodeOnlySpecialChars() {
            assertThatThrownBy(() -> Coupon.create("---@@@", VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class);
        }
    }

    @Nested
    @DisplayName("when validating description")
    class DescriptionValidation {

        @Test
        @DisplayName("should throw when description is null")
        void shouldThrowWhenDescriptionIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, null, VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("Description");
        }

        @Test
        @DisplayName("should throw when description is blank")
        void shouldThrowWhenDescriptionIsBlank() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, "   ", VALID_DISCOUNT, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class);
        }
    }

    @Nested
    @DisplayName("when validating discount value")
    class DiscountValidation {

        @Test
        @DisplayName("should throw when discount value is null")
        void shouldThrowWhenDiscountIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, null, FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("Discount");
        }

        @Test
        @DisplayName("should throw when discount value is below 0.5")
        void shouldThrowWhenDiscountBelowMin() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, new BigDecimal("0.49"), FUTURE_DATE, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("0.5");
        }

        @Test
        @DisplayName("should accept discount value at exact minimum 0.5")
        void shouldAcceptDiscountAtMin() {
            assertThatCode(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, new BigDecimal("0.5"), FUTURE_DATE, false))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept very large discount values")
        void shouldAcceptLargeDiscount() {
            assertThatCode(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, new BigDecimal("999999"), FUTURE_DATE, false))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("when validating expiration date")
    class ExpirationValidation {

        @Test
        @DisplayName("should throw when expiration date is null")
        void shouldThrowWhenExpirationIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, null, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("Expiration");
        }

        @Test
        @DisplayName("should throw when expiration date is in the past")
        void shouldThrowWhenExpirationInPast() {
            Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

            assertThatThrownBy(() -> Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, pastDate, false))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("past");
        }
    }

    @Nested
    @DisplayName("when soft deleting")
    class SoftDelete {

        @Test
        @DisplayName("should change status to DELETED")
        void shouldChangeStatusToDeleted() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, true);

            coupon.softDelete();

            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
        }

        @Test
        @DisplayName("should throw when trying to delete already deleted coupon")
        void shouldThrowWhenAlreadyDeleted() {
            Coupon coupon = Coupon.create(VALID_CODE, VALID_DESCRIPTION, VALID_DISCOUNT, FUTURE_DATE, true);
            coupon.softDelete();

            assertThatThrownBy(coupon::softDelete)
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("already deleted");
        }
    }

    @Nested
    @DisplayName("when restoring from persistence")
    class Restore {

        @Test
        @DisplayName("should not revalidate expiration date")
        void shouldRestoreEvenIfExpired() {
            Instant pastDate = Instant.now().minus(30, ChronoUnit.DAYS);

            assertThatCode(() -> Coupon.restore(
                    UUID.randomUUID(),
                    VALID_CODE,
                    VALID_DESCRIPTION,
                    VALID_DISCOUNT,
                    pastDate,
                    true,
                    false,
                    CouponStatus.ACTIVE
            )).doesNotThrowAnyException();
        }
    }
}