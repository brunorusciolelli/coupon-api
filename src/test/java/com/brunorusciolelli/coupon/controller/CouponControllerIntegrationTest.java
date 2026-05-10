package com.brunorusciolelli.coupon.controller;

import com.brunorusciolelli.coupon.domain.entity.Coupon;
import com.brunorusciolelli.coupon.domain.entity.CouponStatus;
import com.brunorusciolelli.coupon.repository.CouponJpaEntity;
import com.brunorusciolelli.coupon.repository.CouponMapper;
import com.brunorusciolelli.coupon.repository.CouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository repository;

    @Autowired
    private CouponMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("POST /coupon")
    class CreateCoupon {

        @Test
        @DisplayName("should create coupon and return 201")
        void shouldCreateCoupon() throws Exception {
            Map<String, Object> request = Map.of(
                    "code", "ABC-123",
                    "description", "Black Friday discount",
                    "discountValue", new BigDecimal("0.8"),
                    "expirationDate", Instant.now().plus(30, ChronoUnit.DAYS).toString(),
                    "published", true
            );

            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.description").value("Black Friday discount"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.published").value(true))
                    .andExpect(jsonPath("$.redeemed").value(false));

            assertThat(repository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return INACTIVE status when not published")
        void shouldCreateInactiveWhenNotPublished() throws Exception {
            Map<String, Object> request = Map.of(
                    "code", "PROMO1",
                    "description", "Cyber Monday",
                    "discountValue", new BigDecimal("1.5"),
                    "expirationDate", Instant.now().plus(15, ChronoUnit.DAYS).toString(),
                    "published", false
            );

            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void shouldReturn400OnMissingFields() throws Exception {
            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("should return 400 when discount is below 0.5")
        void shouldReturn400OnLowDiscount() throws Exception {
            Map<String, Object> request = Map.of(
                    "code", "ABCDEF",
                    "description", "Sale",
                    "discountValue", new BigDecimal("0.1"),
                    "expirationDate", Instant.now().plus(30, ChronoUnit.DAYS).toString(),
                    "published", false
            );

            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("0.5")));
        }

        @Test
        @DisplayName("should return 400 when expiration date is in the past")
        void shouldReturn400OnPastExpiration() throws Exception {
            Map<String, Object> request = Map.of(
                    "code", "ABCDEF",
                    "description", "Sale",
                    "discountValue", new BigDecimal("1.0"),
                    "expirationDate", Instant.now().minus(1, ChronoUnit.DAYS).toString(),
                    "published", false
            );

            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("past")));
        }

        @Test
        @DisplayName("should return 400 when sanitized code length is invalid")
        void shouldReturn400OnInvalidCodeLength() throws Exception {
            Map<String, Object> request = Map.of(
                    "code", "ABC",
                    "description", "Sale",
                    "discountValue", new BigDecimal("1.0"),
                    "expirationDate", Instant.now().plus(30, ChronoUnit.DAYS).toString(),
                    "published", false
            );

            mockMvc.perform(post("/coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("6")));
        }
    }

    @Nested
    @DisplayName("GET /coupon/{id}")
    class FindById {

        @Test
        @DisplayName("should return coupon and 200 when it exists")
        void shouldReturnCoupon() throws Exception {
            Coupon coupon = Coupon.create(
                    "FIND01",
                    "Coupon to find",
                    new BigDecimal("2.0"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true
            );
            repository.save(mapper.toEntity(coupon));

            mockMvc.perform(get("/coupon/" + coupon.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(coupon.getId().toString()))
                    .andExpect(jsonPath("$.code").value("FIND01"))
                    .andExpect(jsonPath("$.description").value("Coupon to find"))
                    .andExpect(jsonPath("$.discountValue").value(2.0))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.published").value(true))
                    .andExpect(jsonPath("$.redeemed").value(false));
        }

        @Test
        @DisplayName("should return 404 when coupon does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/coupon/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString(nonExistentId.toString())));
        }
    }

    @Nested
    @DisplayName("DELETE /coupon/{id}")
    class DeleteCoupon {

        @Test
        @DisplayName("should soft delete coupon and return 204")
        void shouldSoftDelete() throws Exception {
            Coupon coupon = Coupon.create(
                    "ABCDEF",
                    "To be deleted",
                    new BigDecimal("1.0"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true
            );
            CouponJpaEntity entity = mapper.toEntity(coupon);
            repository.save(entity);

            mockMvc.perform(delete("/coupon/" + coupon.getId()))
                    .andExpect(status().isNoContent());

            CouponJpaEntity afterDelete = repository.findById(coupon.getId()).orElseThrow();
            assertThat(afterDelete.getStatus()).isEqualTo(CouponStatus.DELETED);
        }

        @Test
        @DisplayName("should return 404 when coupon does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(delete("/coupon/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString(nonExistentId.toString())));
        }

        @Test
        @DisplayName("should return 400 when coupon is already deleted")
        void shouldReturn400WhenAlreadyDeleted() throws Exception {
            Coupon coupon = Coupon.create(
                    "DELETD",
                    "Already deleted",
                    new BigDecimal("1.0"),
                    Instant.now().plus(30, ChronoUnit.DAYS),
                    true
            );
            coupon.softDelete();
            repository.save(mapper.toEntity(coupon));

            mockMvc.perform(delete("/coupon/" + coupon.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("already deleted")));
        }
    }
}