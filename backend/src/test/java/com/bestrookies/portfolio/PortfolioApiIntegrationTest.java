package com.bestrookies.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreatePortfolioAndQuerySummary() throws Exception {
        // 1) create portfolio
        mockMvc.perform(post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"name\": \"My First Portfolio\",
                      \"baseCurrency\": \"USD\"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber());

        // 2) add position
        mockMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"portfolioId\": 1,
                      \"assetType\": \"STOCK\",
                      \"ticker\": \"AAPL\",
                      \"quantity\": 10,
                      \"avgCost\": 150,
                      \"currency\": \"USD\"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticker").value("AAPL"));

        // 3) verify summary reflects total cost = 10 * 150 = 1500
        mockMvc.perform(get("/api/v1/portfolios/1/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.portfolioId").value(1))
            .andExpect(jsonPath("$.totalPositions").value(1))
            .andExpect(jsonPath("$.totalCost").value(1500.0000));
    }
}

