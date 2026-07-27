package com.bestrookies.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record PositionUpdateRequest(
    @DecimalMin(value = "0.0001", message = "quantity must be > 0")
    BigDecimal quantity,

    @DecimalMin(value = "0.0", inclusive = true, message = "avgCost must be >= 0")
    BigDecimal avgCost
) {
}

