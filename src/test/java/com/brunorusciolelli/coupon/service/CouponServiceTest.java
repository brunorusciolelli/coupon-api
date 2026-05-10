package com.brunorusciolelli.coupon.service;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.domain.entity.CouponStatus;
import com.brunorusciolelli.coupon.dto.CreateCouponRequest;
import com.brunorusciolelli.coupon.exception.CouponNotFoundException;
import com.brunorusciolelli.coupon.exception.InvalidCouponException;
import com.brunorusciolelli.coupon.repository.CouponJpaEntity;
import com.brunorusciolelli.coupon.repository.CouponMapper;
import com.brunorusciolelli.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository repository;

    @Mock
    private CouponMapper mapper;

    @InjectMocks
    private CouponService service;

    @Nested
    @DisplayName("when creating a coupon")
    class CreateCoupon {

        @Test
        @DisplayName("should create coupon, sanitize code and persist")
        void shouldCreateAndPersist() {
            CreateCouponRequest request = new CreateCouponRequest(
                    "ABC-123",
                    "Black Friday",
                    new BigDecimal("0.5"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true
            );

            Coupon result = service.createCoupon(request);

            assertThat(result.getCode()).isEqualTo("ABC123");
            assertThat(result.getStatus()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(result.isPublished()).isTrue();
            assertThat(result.isRedeemed()).isFalse();

            verify(mapper).toEntity(any(Coupon.class));
            verify(repository).save(any());
        }

        @Test
        @DisplayName("should default published to false when not provided")
        void shouldDefaultPublishedToFalse() {
            CreateCouponRequest request = new CreateCouponRequest(
                    "ABCDEF",
                    "Promo",
                    new BigDecimal("1.0"),
                    Instant.now().plus(10, ChronoUnit.DAYS),
                    null
            );

            Coupon result = service.createCoupon(request);

            assertThat(result.isPublished()).isFalse();
            assertThat(result.getStatus()).isEqualTo(CouponStatus.INACTIVE);
        }

        @Test
        @DisplayName("should not persist when domain validation fails")
        void shouldNotPersistWhenInvalid() {
            CreateCouponRequest request = new CreateCouponRequest(
                    "ABCDEF",
                    "Promo",
                    new BigDecimal("0.1"),
                    Instant.now().plus(10, ChronoUnit.DAYS),
                    false
            );

            assertThatThrownBy(() -> service.createCoupon(request))
                    .isInstanceOf(InvalidCouponException.class);

            verify(repository, never()).save(any());
            verify(mapper, never()).toEntity(any());
        }
    }

    @Nested
    @DisplayName("when deleting a coupon")
    class DeleteCoupon {

        @Test
        @DisplayName("should soft delete an existing coupon")
        void shouldSoftDelete() {
            UUID id = UUID.randomUUID();
            Coupon active = Coupon.restore(
                    id, "ABCDEF", "desc",
                    new BigDecimal("1.0"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true, false, CouponStatus.ACTIVE
            );
            CouponJpaEntity entity = new CouponJpaEntity();
            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(active);
            when(mapper.toEntity(active)).thenReturn(entity);

            service.deleteCoupon(id);

            assertThat(active.getStatus()).isEqualTo(CouponStatus.DELETED);
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("should throw CouponNotFoundException when coupon does not exist")
        void shouldThrowNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCoupon(id))
                    .isInstanceOf(CouponNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidCouponException when coupon is already deleted")
        void shouldThrowWhenAlreadyDeleted() {
            UUID id = UUID.randomUUID();
            Coupon deleted = Coupon.restore(
                    id, "ABCDEF", "desc",
                    new BigDecimal("1.0"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true, false, CouponStatus.DELETED
            );
            CouponJpaEntity entity = new CouponJpaEntity();
            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(deleted);

            assertThatThrownBy(() -> service.deleteCoupon(id))
                    .isInstanceOf(InvalidCouponException.class)
                    .hasMessageContaining("already deleted");

            verify(repository, never()).save(any());
        }
    }
}