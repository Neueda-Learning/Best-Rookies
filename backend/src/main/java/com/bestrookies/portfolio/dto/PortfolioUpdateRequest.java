package com.bestrookies.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 组合更新请求 DTO
 * 用于 PATCH 端点，支持部分更新（可选字段）
 */
public record PortfolioUpdateRequest(
    @NotBlank(message = "组合名称不能为空")
    @Size(max = 100, message = "组合名称长度不能超过 100 个字符")
    String name,

    @NotBlank(message = "基础币种不能为空")
    @Size(min = 3, max = 3, message = "基础币种必须为 3 个字母")
    String baseCurrency
) {
}

