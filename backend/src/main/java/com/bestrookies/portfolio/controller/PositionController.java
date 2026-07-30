package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.dto.PositionCreateRequest;
import com.bestrookies.portfolio.dto.PositionResponse;
import com.bestrookies.portfolio.dto.PositionUpdateRequest;
import com.bestrookies.portfolio.entity.AssetType;
import com.bestrookies.portfolio.exception.ApiErrorResponse;
import com.bestrookies.portfolio.service.PositionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/positions")
@Tag(name = "Positions", description = "Position CRUD APIs")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Bad request",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Position or portfolio not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
})
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    // 创建新持仓
    @PostMapping
    @Operation(summary = "Create a position")
    public PositionResponse createPosition(@Valid @RequestBody PositionCreateRequest request) {
        return positionService.createPosition(request);
    }

    // 获取资产类型列表（用于前端资产类型下拉框）
    @GetMapping("/asset-types/supported")
    @Operation(summary = "Get supported asset types")
    public ResponseEntity<List<String>> getSupportedAssetTypes() {
        return ResponseEntity.ok(positionService.listAssetTypes());
    }

    // 按资产类型获取可选代码（用于前端ticker下拉框）
    @GetMapping("/tickers/supported")
    @Operation(summary = "Get supported ticker codes by asset type")
    public ResponseEntity<List<String>> getSupportedTickersByAssetType(@RequestParam AssetType assetType) {
        return ResponseEntity.ok(positionService.listSupportedTickers(assetType));
    }

    // 按资产类型 + 关键字搜索可选代码（用于前端可输入搜索下拉）
    @GetMapping("/tickers/search")
    @Operation(summary = "Search ticker codes by asset type and keyword")
    public ResponseEntity<List<String>> searchSupportedTickers(
        @RequestParam AssetType assetType,
        @RequestParam String q,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(positionService.searchSupportedTickers(assetType, q, limit));
    }

    // 获取所有资产类型的可选代码（包含实时结果）
    @GetMapping("/tickers/supported/all")
    @Operation(summary = "Get supported ticker codes for all asset types")
    public ResponseEntity<Map<String, List<String>>> getSupportedTickersForAllAssetTypes() {
        return ResponseEntity.ok(positionService.listSupportedTickersForAllAssetTypes());
    }

    // 列出所有或特定组合的持仓
    @GetMapping
    @Operation(summary = "List positions with optional portfolio filter and pagination")
    public ResponseEntity<List<PositionResponse>> listPositions(
        @RequestParam(required = false) Long portfolioId,
        @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        var page = positionService.listPositions(portfolioId, pageable);
        HttpHeaders headers = PaginationHeaders.from(page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // 部分更新持仓
    @PatchMapping("/{id}")
    @Operation(summary = "Update a position quantity or average cost")
    public PositionResponse updatePosition(@PathVariable Long id, @Valid @RequestBody PositionUpdateRequest request) {
        return positionService.updatePosition(id, request);
    }

    // 删除持仓
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a position")
    public void deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
    }
}

