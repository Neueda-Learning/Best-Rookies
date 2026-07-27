package com.bestrookies.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortfolioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreatePortfolioAndQuerySummary() throws Exception {
        long portfolioId = createPortfolio("My First Portfolio", "USD");

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(positionJson(portfolioId, "STOCK", "AAPL", "10", "150", "USD")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticker").value("AAPL"));

        mockMvc.perform(get("/api/v1/portfolios/{id}/summary", portfolioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.portfolioId").value(portfolioId))
            .andExpect(jsonPath("$.totalPositions").value(1))
            .andExpect(jsonPath("$.totalCost").value(1500.0000));
    }

    @Test
    void shouldUpdatePortfolio() throws Exception {
        long portfolioId = createPortfolio("Growth", "usd");

        mockMvc.perform(patch("/api/v1/portfolios/{id}", portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Growth Updated",
                      "baseCurrency": "eur"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(portfolioId))
            .andExpect(jsonPath("$.name").value("Growth Updated"))
            .andExpect(jsonPath("$.baseCurrency").value("EUR"));
    }

    @Test
    void shouldDeletePortfolioAndReturnNotFoundAfterward() throws Exception {
        long portfolioId = createPortfolio("Disposable", "USD");

        mockMvc.perform(delete("/api/v1/portfolios/{id}", portfolioId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolioId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForMissingPortfolioUpdateAndDelete() throws Exception {
        mockMvc.perform(patch("/api/v1/portfolios/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Missing"
                    }
                    """))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/portfolios/{id}", 9999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidPortfolioPatchPayloads() throws Exception {
        long portfolioId = createPortfolio("Validation Target", "USD");

        mockMvc.perform(patch("/api/v1/portfolios/{id}", portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(patch("/api/v1/portfolios/{id}", portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "   ",
                      "baseCurrency": "   "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnPaginatedAndSortedPortfolios() throws Exception {
        createPortfolio("Charlie", "USD");
        createPortfolio("Alpha", "USD");
        createPortfolio("Bravo", "USD");

        mockMvc.perform(get("/api/v1/portfolios")
                .param("size", "2")
                .param("sort", "name,asc"))
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

        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(positionJson(portfolioId, "STOCK", "AAA", "8", "50", "USD")))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/positions")
                .param("portfolioId", String.valueOf(portfolioId))
                .param("size", "1")
                .param("sort", "ticker,asc"))
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
}

