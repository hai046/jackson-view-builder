package com.hai046.builder.configuration;

import com.hai046.builder.ViewBuilder;
import com.hai046.builder.ViewBuilderObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonObjectMapperConfig {

    /**
     * 强制指定使用我们自定义处理的 ObjectMapper
     *
     * @return
     */
    @Bean
    @Primary
    public ViewBuilderObjectMapper objectMapper(ViewBuilder viewBuilder) {
        return new ViewBuilderObjectMapper(viewBuilder);
    }
}
