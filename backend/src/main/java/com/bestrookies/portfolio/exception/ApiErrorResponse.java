package com.bestrookies.portfolio.exception;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(
    @Schema(description = "Error timestamp")
    Instant timestamp,

    @Schema(description = "HTTP status code")
    int status,

    @Schema(description = "Machine-readable error code")
    String error,

    @Schema(description = "Human-readable error message")
    String message
) {
}

