package com.example.account.modules.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientDeserializers() {
        return builder -> {
            builder.deserializerByType(UUID.class, new LenientUUIDDeserializer());
            builder.deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
            builder.featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    /**
     * Deserializes UUID fields leniently:
     * - valid UUID string  → parsed normally
     * - null / ""          → null
     * - any other string   → null (avoids 400 on non-UUID IDs like "c001")
     */
    public static class LenientUUIDDeserializer extends StdDeserializer<UUID> {

        public LenientUUIDDeserializer() {
            super(UUID.class);
        }

        @Override
        public UUID deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getText();
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    /**
     * Deserializes LocalDateTime leniently:
     * - "2026-06-19T08:53:45"        → parsed as-is
     * - "2026-06-19T08:53:45.938Z"   → strips timezone, uses local part
     * - "2026-06-19T08:53:45+01:00"  → converts to LocalDateTime
     * - null / ""                    → null
     */
    public static class LenientLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

        public LenientLocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getText();
            if (value == null || value.isBlank()) {
                return null;
            }
            // Try plain LocalDateTime first
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ignored) {
            }
            // Fall back to offset/zoned (handles trailing Z or +01:00)
            try {
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toLocalDateTime();
            } catch (DateTimeParseException ignored) {
            }
            // Fall back to date-only "2026-06-19" → midnight
            try {
                return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
            return null;
        }
    }
}
