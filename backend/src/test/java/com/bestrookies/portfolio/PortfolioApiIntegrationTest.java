package com.bestrookies.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class PortfolioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreatePortfolioAndQuerySummary() throws Exception {
        mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"My Portfolio\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"portfolioId\": 1, \"assetType\": \"STOCK\", \"ticker\": \"AAPL\", \"quantity\": 10, \"avgCost\": 150, \"currency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticker").value("AAPL"));

        mockMvc.perform(get("/api/v1/portfolios/1/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.portfolioId").value(1))
            .andExpect(jsonPath("$.totalPositions").value(1))
            .andExpect(jsonPath("$.totalCost").value(1500.0000))
            // 测试环境禁用行情（app.market-price.enabled=false），市值回退均价 => 市值=成本
            .andExpect(jsonPath("$.marketValue").value(1500.0000))
            .andExpect(jsonPath("$.unrealizedPnL").value(0.0000))
            // 收益率：无行情时成本=市值 => returnRate = 0.00
            .andExpect(jsonPath("$.returnRate").value(0.00))
            // 数据质量：0 个实时行情，1 个回退
            .andExpect(jsonPath("$.positionsWithLivePrice").value(0))
            .andExpect(jsonPath("$.positionsWithFallback").value(1))
            // 基础币种应为 USD
            .andExpect(jsonPath("$.baseCurrency").value("USD"));
    }

    @Test
    void shouldUpdatePortfolioSuccessfully() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Portfolio\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        mockMvc.perform(patch("/api/v1/portfolios/" + portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\", \"baseCurrency\": \"EUR\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"))
            .andExpect(jsonPath("$.baseCurrency").value("EUR"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistent() throws Exception {
        mockMvc.perform(patch("/api/v1/portfolios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePortfolioSuccessfully() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"To Delete\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        mockMvc.perform(delete("/api/v1/portfolios/" + portfolioId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/portfolios/" + portfolioId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistent() throws Exception {
        mockMvc.perform(delete("/api/v1/portfolios/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidatePortfolioUpdateRequest() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        mockMvc.perform(patch("/api/v1/portfolios/" + portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldConvertEurCostToUsdInSummary() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"FX Portfolio\", \"baseCurrency\": \"USD\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Long portfolioId = ((Number) responseBody.get("id")).longValue();

        // 固定汇率：1 USD = 0.88 EUR，因此 1 EUR = 1 / 0.88 USD。
        // 1 股 * 88 EUR => 100 USD（四舍五入后 100.0000）。
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"portfolioId\": " + portfolioId + ", \"assetType\": \"STOCK\", \"ticker\": \"SAP\", \"quantity\": 1, \"avgCost\": 88, \"currency\": \"EUR\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/portfolios/" + portfolioId + "/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.baseCurrency").value("USD"))
            .andExpect(jsonPath("$.totalCost").value(100.0000))
            .andExpect(jsonPath("$.marketValue").value(100.0000));
    }
}

