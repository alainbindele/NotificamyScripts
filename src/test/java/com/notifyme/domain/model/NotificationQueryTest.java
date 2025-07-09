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

    @Test
    void testGetQueryZoneId_ValidTimezone() {
        NotificationQuery query = NotificationQuery.builder()
            .timezone("Europe/Rome")
            .build();

        assertEquals(java.time.ZoneId.of("Europe/Rome"), query.getQueryZoneId());
    }

    @Test
    void testGetQueryZoneId_InvalidTimezone() {
        NotificationQuery query = NotificationQuery.builder()
            .timezone("Invalid/Timezone")
            .build();

        assertEquals(java.time.ZoneId.of("UTC"), query.getQueryZoneId()); // Should fallback to UTC
    }

    @Test
    void testGetQueryZoneId_NullTimezone() {
        NotificationQuery query = NotificationQuery.builder()
            .timezone(null)
            .build();

        assertEquals(java.time.ZoneId.of("UTC"), query.getQueryZoneId()); // Should default to UTC
    }

    @Test
    void testHasTimezone() {
        NotificationQuery queryWithTimezone = NotificationQuery.builder()
            .timezone("Europe/Rome")
            .build();
        assertTrue(queryWithTimezone.hasTimezone());

        NotificationQuery queryWithoutTimezone = NotificationQuery.builder()
            .timezone(null)
            .build();
        assertFalse(queryWithoutTimezone.hasTimezone());

        NotificationQuery queryWithEmptyTimezone = NotificationQuery.builder()
            .timezone("")
            .build();
        assertFalse(queryWithEmptyTimezone.hasTimezone());
    }
}