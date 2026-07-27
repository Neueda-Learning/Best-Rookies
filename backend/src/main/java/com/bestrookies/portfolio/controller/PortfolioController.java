package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.dto.PortfolioCreateRequest;
import com.bestrookies.portfolio.dto.PortfolioResponse;
import com.bestrookies.portfolio.dto.PortfolioSummaryResponse;
import com.bestrookies.portfolio.dto.PortfolioUpdateRequest;
import com.bestrookies.portfolio.exception.ApiErrorResponse;
import com.bestrookies.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import java.util.List;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Portfolios", description = "Portfolio CRUD and summary APIs")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Bad request",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Portfolio not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
})
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // 创建新组合
    @PostMapping
    @Operation(summary = "Create a portfolio")
    public PortfolioResponse createPortfolio(@Valid @RequestBody PortfolioCreateRequest request) {
        return portfolioService.createPortfolio(request);
    }

    // 列出所有组合
    @GetMapping
    @Operation(summary = "List portfolios with pagination and sorting")
    public ResponseEntity<List<PortfolioResponse>> listPortfolios(
        @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        var page = portfolioService.listPortfolios(pageable);
        HttpHeaders headers = PaginationHeaders.from(page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // 获取组合详情
    @GetMapping("/{id}")
    @Operation(summary = "Get a portfolio by id")
    public PortfolioResponse getPortfolio(@PathVariable Long id) {
        return portfolioService.getPortfolio(id);
    }

    // 获取组合摘要（总持仓数和总成本）
    @GetMapping("/{id}/summary")
    @Operation(summary = "Get portfolio summary")
    public PortfolioSummaryResponse getSummary(@PathVariable Long id) {
        return portfolioService.getSummary(id);
    }

    // 更新组合（支持部分更新）
    @PatchMapping("/{id}")
    public PortfolioResponse updatePortfolio(@PathVariable Long id, @Valid @RequestBody PortfolioUpdateRequest request) {
        return portfolioService.updatePortfolio(id, request);
    }

    // 删除组合及其所有关联的持仓
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a portfolio and its positions")
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }
}

