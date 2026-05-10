package com.brunorusciolelli.coupon.controller;

import com.brunorusciolelli.coupon.repository.CouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /coupon should create coupon and return 201")
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
    @DisplayName("POST /coupon should return INACTIVE status when not published")
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
    @DisplayName("POST /coupon should return 400 when required fields are missing")
    void shouldReturn400OnMissingFields() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /coupon should return 400 when discount is below 0.5")
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
    @DisplayName("POST /coupon should return 400 when expiration date is in the past")
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
    @DisplayName("POST /coupon should return 400 when sanitized code length is invalid")
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