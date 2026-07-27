package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.dto.PortfolioCreateRequest;
import com.bestrookies.portfolio.dto.PortfolioResponse;
import com.bestrookies.portfolio.dto.PortfolioSummaryResponse;
import com.bestrookies.portfolio.dto.PortfolioUpdateRequest;
import com.bestrookies.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public PortfolioResponse createPortfolio(@Valid @RequestBody PortfolioCreateRequest request) {
        return portfolioService.createPortfolio(request);
    }

    @GetMapping
    public List<PortfolioResponse> listPortfolios() {
        return portfolioService.listPortfolios();
    }

    @GetMapping("/{id}")
    public PortfolioResponse getPortfolio(@PathVariable Long id) {
        return portfolioService.getPortfolio(id);
    }

    @GetMapping("/{id}/summary")
    public PortfolioSummaryResponse getSummary(@PathVariable Long id) {
        return portfolioService.getSummary(id);
    }

    @PatchMapping("/{id}")
    public PortfolioResponse updatePortfolio(@PathVariable Long id,
                                             @Valid @RequestBody PortfolioUpdateRequest request) {
        return portfolioService.updatePortfolio(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }
}

