package com.notifyme.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationQueryTest {

    @Test
    void testIsWithinValidityPeriod_NoConstraints() {
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(null)
            .validTo(null)
            .build();

        assertTrue(query.isWithinValidityPeriod(LocalDateTime.now()));
    }

    @Test
    void testIsWithinValidityPeriod_OnlyValidFrom() {
        LocalDateTime validFrom = LocalDateTime.now().minusHours(1);
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(validFrom)
            .validTo(null)
            .build();

        assertTrue(query.isWithinValidityPeriod(LocalDateTime.now()));
        assertFalse(query.isWithinValidityPeriod(validFrom.minusMinutes(1)));
    }

    @Test
    void testIsWithinValidityPeriod_OnlyValidTo() {
        LocalDateTime validTo = LocalDateTime.now().plusHours(1);
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(null)
            .validTo(validTo)
            .build();

        assertTrue(query.isWithinValidityPeriod(LocalDateTime.now()));
        assertFalse(query.isWithinValidityPeriod(validTo.plusMinutes(1)));
    }

    @Test
    void testIsWithinValidityPeriod_BothConstraints() {
        LocalDateTime validFrom = LocalDateTime.now().minusHours(1);
        LocalDateTime validTo = LocalDateTime.now().plusHours(1);
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(validFrom)
            .validTo(validTo)
            .build();

        assertTrue(query.isWithinValidityPeriod(LocalDateTime.now()));
        assertFalse(query.isWithinValidityPeriod(validFrom.minusMinutes(1)));
        assertFalse(query.isWithinValidityPeriod(validTo.plusMinutes(1)));
    }

    @Test
    void testIsCurrentlyValid() {
        LocalDateTime validFrom = LocalDateTime.now().minusHours(1);
        LocalDateTime validTo = LocalDateTime.now().plusHours(1);
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(validFrom)
            .validTo(validTo)
            .build();

        assertTrue(query.isCurrentlyValid());
    }

    @Test
    void testIsCurrentlyValid_ExpiredQuery() {
        LocalDateTime validFrom = LocalDateTime.now().minusHours(2);
        LocalDateTime validTo = LocalDateTime.now().minusHours(1);
        NotificationQuery query = NotificationQuery.builder()
            .validFrom(validFrom)
            .validTo(validTo)
            .build();

        assertFalse(query.isCurrentlyValid());
    }
}