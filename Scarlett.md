# Scarlett（后端工程师 A 职责代码解析）

## 目标说明
本文档聚焦 `README-ing.md` 中“后端工程师 A（组合领域）”的职责，梳理项目内对应代码，并用中文解释这些代码如何完成需求。

后端工程师 A 已完成的范围：
- 组合创建 / 列表 / 详情 / 摘要
- `PATCH /api/v1/portfolios/{id}` 组合更新
- `DELETE /api/v1/portfolios/{id}` 组合删除（含级联删除持仓）
- 更新 DTO 参数校验与业务错误返回
- 集成测试覆盖更新、删除、校验、异常场景

---

## 一、代码定位（后端 A 相关）

- 控制层：`backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- 服务层：`backend/src/main/java/com/bestrookies/portfolio/service/PortfolioService.java`
- 更新请求 DTO：`backend/src/main/java/com/bestrookies/portfolio/dto/PortfolioUpdateRequest.java`
- 级联删除实体关系：`backend/src/main/java/com/bestrookies/portfolio/entity/Portfolio.java`
- 异常与校验返回：`backend/src/main/java/com/bestrookies/portfolio/exception/GlobalExceptionHandler.java`
- 集成测试：`backend/src/test/java/com/bestrookies/portfolio/PortfolioApiIntegrationTest.java`

---

## 二、控制层实现（接口入口）

代码片段（`PortfolioController`）：

```java
@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {
    // ...existing code...

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
    public PortfolioResponse updatePortfolio(@PathVariable Long id, @Valid @RequestBody PortfolioUpdateRequest request) {
        return portfolioService.updatePortfolio(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }
}
```

解析：
- 这里定义了后端 A 负责的全部组合接口。
- `@PatchMapping` 对应“部分更新组合”；`@DeleteMapping` 对应“删除组合”。
- `@Valid` 会触发 DTO 参数校验，非法参数会进入全局异常处理。
- 删除成功返回 `204 No Content`，符合 REST 习惯。

---

## 三、服务层实现（核心业务逻辑）

代码片段（`PortfolioService`）：

```java
@Service
public class PortfolioService {
    // ...existing code...

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioIdOrderByIdAsc(portfolio.getId());

        BigDecimal totalCost = positions.stream()
            .map(p -> p.getQuantity().multiply(p.getAvgCost()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        return new PortfolioSummaryResponse(portfolio.getId(), positions.size(), totalCost);
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioUpdateRequest request) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency().toUpperCase());
        Portfolio updated = portfolioRepository.save(portfolio);
        return toResponse(updated);
    }

    @Transactional
    public void deletePortfolio(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        portfolioRepository.delete(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioEntity(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
    }
}
```

解析：
- `getSummary`：从持仓表汇总“总持仓数 + 总成本”，完成 MVP 的摘要目标。
- `updatePortfolio`：
  - 先查组合，不存在就抛 `ResourceNotFoundException`。
  - 名称 `trim()` 去首尾空格，币种统一 `toUpperCase()`。
  - 保存后返回最新数据。
- `deletePortfolio`：
  - 同样先查存在性，再执行删除。
  - 由于实体关系配置了级联与孤儿删除，关联持仓会一起删除。
- `@Transactional` 确保更新/删除是事务操作，避免部分成功。

---

## 四、更新 DTO 与参数校验

代码片段（`PortfolioUpdateRequest`）：

```java
public record PortfolioUpdateRequest(
    @NotBlank(message = "组合名称不能为空")
    @Size(max = 100, message = "组合名称长度不能超过 100 个字符")
    String name,

    @NotBlank(message = "基础币种不能为空")
    @Size(min = 3, max = 3, message = "基础币种必须为 3 个字母")
    String baseCurrency
) {
}
```

解析：
- 该 DTO 是后端 A 新增，用于 `PATCH /portfolios/{id}`。
- `name` 不能为空且长度不超 100。
- `baseCurrency` 必须是 3 位字符串（如 USD/EUR）。
- 校验失败时，不进入业务层，直接由异常处理统一返回 `400`。

---

## 五、删除策略（级联删除）

代码片段（`Portfolio` 实体关系）：

```java
@OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Position> positions = new ArrayList<>();
```

解析：
- `cascade = CascadeType.ALL`：组合生命周期会传递到持仓。
- `orphanRemoval = true`：组合删除后，其关联持仓作为“孤儿记录”会被移除。
- 这就是后端 A 实现“删除组合并清理关联持仓”的技术基础。

---

## 六、错误与校验信息返回

代码片段（`GlobalExceptionHandler`）：

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
        Instant.now(), HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage()
    ));
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + " " + err.getDefaultMessage())
        .collect(Collectors.joining("; "));

    return ResponseEntity.badRequest().body(new ApiErrorResponse(
        Instant.now(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", message
    ));
}
```

解析：
- 资源不存在返回 `404 NOT_FOUND`，用于更新/删除不存在组合。
- 参数校验失败返回 `400 BAD_REQUEST`，并拼接字段错误信息。
- 这满足了后端 A 的“业务错误消息 + 参数校验返回”的要求。

---

## 七、测试覆盖（后端 A 完成度证据）

代码片段（`PortfolioApiIntegrationTest` 中关键用例）：

```java
@Test
void shouldUpdatePortfolioSuccessfully() throws Exception {
    // 创建组合 -> PATCH 更新 -> GET 校验更新后数据
}

@Test
void shouldReturnNotFoundWhenUpdatingNonExistentPortfolio() throws Exception {
    // PATCH 不存在 ID，断言 404 和 NOT_FOUND
}

@Test
void shouldDeletePortfolioSuccessfully() throws Exception {
    // 创建组合+持仓 -> DELETE 组合 -> 验证组合不存在且持仓清空
}

@Test
void shouldReturnNotFoundWhenDeletingNonExistentPortfolio() throws Exception {
    // DELETE 不存在 ID，断言 404
}

@Test
void shouldValidatePortfolioUpdateRequest() throws Exception {
    // PATCH 空 name / 非法 baseCurrency，断言 400 BAD_REQUEST
}
```

解析：
- 测试不是只测“能调通”，还覆盖“异常与边界”。
- 更新路径：成功 + 不存在 ID。
- 删除路径：成功（含级联） + 不存在 ID。
- 校验路径：空名称、非法币种长度。
- 这些用例直接证明后端 A 的交付“可用且可回归验证”。

---

## 八、后端工程师 A 完成结论

结合当前代码，后端工程师 A 的职责已完整落地：
- 组合领域接口完整（创建、查询、摘要、更新、删除）。
- 更新 DTO + 参数校验 + 统一错误返回已打通。
- 删除策略具备级联清理持仓能力。
- 集成测试覆盖关键成功/失败场景，具备可验证性。

可直接对应 `README-ing.md` 中“后端工程师 A：待完成任务 = 无（已全部完成）”。

