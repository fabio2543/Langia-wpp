package com.langia.dto;

import java.time.OffsetDateTime;

public record StudentResponse(
        Long id,
        String name,
        String phoneE164,
        String timezone,
        Boolean active,
        OffsetDateTime createdAt
) {}
