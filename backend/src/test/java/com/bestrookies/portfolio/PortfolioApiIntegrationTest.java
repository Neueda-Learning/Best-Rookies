package com.bestrookies.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortfolioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreatePortfolioAndQuerySummary() throws Exception {
        // 1) 创建组合
        mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"My First Portfolio\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(portfolioId))
            .andExpect(jsonPath("$.name").value("Growth Updated"))
            .andExpect(jsonPath("$.baseCurrency").value("EUR"));
    }

        // 2) 添加持仓
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"portfolioId\": 1, \"assetType\": \"STOCK\", \"ticker\": \"AAPL\", \"quantity\": 10, \"avgCost\": 150, \"currency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Elements", "3"))
            .andExpect(header().string("X-Total-Pages", "2"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Alpha"))
            .andExpect(jsonPath("$[1].name").value("Bravo"));
    }

    @Test
    void shouldReturnPaginatedSortedPositionsForPortfolio() throws Exception {
        long portfolioId = createPortfolio("Positions", "USD");

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(positionJson(portfolioId, "STOCK", "ZZZ", "5", "100", "USD")))
            .andExpect(status().isOk());

        // 3) 验证摘要反映总成本 = 10 * 150 = 1500
        mockMvc.perform(get("/api/v1/portfolios/1/summary"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Elements", "2"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].ticker").value("AAA"));
    }

    private long createPortfolio(String name, String baseCurrency) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(portfolioJson(name, baseCurrency)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();

        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String portfolioJson(String name, String baseCurrency) {
        return """
            {
              "name": "%s",
              "baseCurrency": "%s"
            }
            """.formatted(name, baseCurrency);
    }

    private String positionJson(Long portfolioId, String assetType, String ticker, String quantity, String avgCost, String currency) {
        return """
            {
              "portfolioId": %d,
              "assetType": "%s",
              "ticker": "%s",
              "quantity": %s,
              "avgCost": %s,
              "currency": "%s"
            }
            """.formatted(portfolioId, assetType, ticker, quantity, avgCost, currency);
    }

    @Test
    void shouldUpdatePortfolioSuccessfully() throws Exception {
        // 1) 创建组合
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Portfolio For Update\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        // 提取 ID
        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        // 2) 更新组合
        mockMvc.perform(patch("/api/v1/portfolios/" + portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Portfolio Name\", \"baseCurrency\": \"EUR\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Portfolio Name"))
            .andExpect(jsonPath("$.baseCurrency").value("EUR"));

        // 3) 验证更新已持久化
        mockMvc.perform(get("/api/v1/portfolios/" + portfolioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Portfolio Name"))
            .andExpect(jsonPath("$.baseCurrency").value("EUR"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPortfolio() throws Exception {
        mockMvc.perform(patch("/api/v1/portfolios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Non-existent\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldDeletePortfolioSuccessfully() throws Exception {
        // 1) 创建组合
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Portfolio For Deletion\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        // 2) 添加持仓
        String positionJson = String.format("{\"portfolioId\": %d, \"assetType\": \"BOND\", \"ticker\": \"BOND1\", \"quantity\": 5, \"avgCost\": 100, \"currency\": \"USD\"}", portfolioId);
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(positionJson))
            .andExpect(status().isOk());

        // 3) 验证持仓存在
        mockMvc.perform(get("/api/v1/positions?portfolioId=" + portfolioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticker").value("BOND1"));

        // 4) 删除组合
        mockMvc.perform(delete("/api/v1/portfolios/" + portfolioId))
            .andExpect(status().isNoContent());

        // 5) 验证组合已删除
        mockMvc.perform(get("/api/v1/portfolios/" + portfolioId))
            .andExpect(status().isNotFound());

        // 6) 验证关联持仓也被删除
        mockMvc.perform(get("/api/v1/positions?portfolioId=" + portfolioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPortfolio() throws Exception {
        mockMvc.perform(delete("/api/v1/portfolios/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidatePortfolioUpdateRequest() throws Exception {
        // 1) 创建组合
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Portfolio For Validation\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        // 2) 尝试用空名称更新
        mockMvc.perform(patch("/api/v1/portfolios/" + portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

        // 3) 尝试用无效币种更新
        mockMvc.perform(patch("/api/v1/portfolios/" + portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Valid Name\", \"baseCurrency\": \"INVALID\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
}


