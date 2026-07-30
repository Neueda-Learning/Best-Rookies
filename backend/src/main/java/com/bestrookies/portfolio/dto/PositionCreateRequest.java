package com.bestrookies.portfolio.dto;

import com.bestrookies.portfolio.entity.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record PositionCreateRequest(
    @NotNull(message = "portfolioId is required")
    Long portfolioId,

    @NotNull(message = "assetType is required")
    AssetType assetType,

    @NotBlank(message = "ticker is required")
    @Size(max = 12, message = "ticker length must be <= 12")
    String ticker,

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.0001", message = "quantity must be > 0")
    BigDecimal quantity,

    @NotNull(message = "avgCost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "avgCost must be >= 0")
    BigDecimal avgCost,

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be 3 letters")
    String currency,

    Instant updatedAt
) {
}

