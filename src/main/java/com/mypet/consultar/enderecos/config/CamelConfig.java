package com.mypet.consultar.enderecos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContext camelContext(List<RoutesBuilder> routeBuilders) throws Exception {
        CamelContext context = new DefaultCamelContext();

        // Adiciona todas as rotas que forem @Component
        for (RoutesBuilder builder : routeBuilders) {
            context.addRoutes(builder);
        }

        context.start(); // ⚠️ Isso que estava faltando
        return context;
    }

    @Bean
    public ProducerTemplate producerTemplate(CamelContext camelContext) {
        return camelContext.createProducerTemplate(); // Agora ele estará ativo
    }

    @Bean
    public JacksonDataFormat jacksonDataFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new JacksonDataFormat(objectMapper, Object.class);
    }
}
