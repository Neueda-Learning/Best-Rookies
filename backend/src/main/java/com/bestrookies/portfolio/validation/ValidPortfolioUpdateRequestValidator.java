package com.bestrookies.portfolio.validation;

import com.bestrookies.portfolio.dto.PortfolioUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPortfolioUpdateRequestValidator implements ConstraintValidator<ValidPortfolioUpdateRequest, PortfolioUpdateRequest> {

    @Override
    public boolean isValid(PortfolioUpdateRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean hasName = value.name() != null && !value.name().isBlank();
        boolean hasBaseCurrency = value.baseCurrency() != null && !value.baseCurrency().isBlank();
        return hasName || hasBaseCurrency;
    }
}

