package com.bestrookies.portfolio.dto;

import jakarta.validation.constraints.Size;

public record PortfolioUpdateRequest(
    @Size(max = 100, message = "name length must be <= 100")
    String name,

    @Size(min = 3, max = 3, message = "baseCurrency must be 3 letters")
    String baseCurrency
) {
}

