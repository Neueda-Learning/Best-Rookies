package com.bestrookies.portfolio.dto;

import com.bestrookies.portfolio.validation.ValidPortfolioUpdateRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@ValidPortfolioUpdateRequest
public record PortfolioUpdateRequest(
    @Size(max = 100, message = "name length must be <= 100")
    @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
    String name,

    @Size(min = 3, max = 3, message = "baseCurrency must be 3 letters")
    @Pattern(regexp = ".*\\S.*", message = "baseCurrency must not be blank")
    String baseCurrency
) {
}

