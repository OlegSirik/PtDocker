package ru.pt.product.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.pt.product.llm.configuration.LlmModuleConfiguration;

@Configuration
@ComponentScan("ru.pt.product")
@Import(LlmModuleConfiguration.class)
public class ProductModuleConfiguration {
}
