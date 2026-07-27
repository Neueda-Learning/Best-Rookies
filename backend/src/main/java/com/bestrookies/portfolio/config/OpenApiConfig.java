package com.bestrookies.portfolio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Best Rookies Portfolio API",
        version = "v1",
        description = "Portfolio and position management APIs for the MVP",
        contact = @Contact(name = "Best Rookies"),
        license = @License(name = "Proprietary")
    )
)
public class OpenApiConfig {
}

