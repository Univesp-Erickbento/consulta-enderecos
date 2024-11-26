package com.mypet.consultar.enderecos.config;

import com.mypet.consultar.enderecos.routes.PessoaRouteBuilder;
import com.mypet.consultar.enderecos.routes.ConsultaCepRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContext camelContext(ConsultaCepRouteBuilder enderecoRouteBuilder, PessoaRouteBuilder pessoaRouteBuilder) {
        CamelContext context = new DefaultCamelContext();
        try {
            context.addRoutes(enderecoRouteBuilder);
            context.addRoutes(pessoaRouteBuilder); // Adicione todas as rotas necessárias aqui
            context.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
    }
}
