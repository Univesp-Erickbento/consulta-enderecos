package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnderecoRouteBuilder extends RouteBuilder {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure() throws Exception {
        // Configura o formato de dados do Jackson para usar o ObjectMapper configurado
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Object.class);

        // Tratamento de exceções
        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar endereço pela API externa usando o CEP
        from("direct:buscarEnderecoPorCep")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("http4://api.externa.de.enderecos/cep/${header.cep}")
                .unmarshal(jacksonDataFormat)
                .log(LoggingLevel.INFO, "Endereço obtido: ${body}")
                .to("direct:salvarEndereco");

        // Rota para salvar endereço na API interna
        from("direct:salvarEndereco")
                .log(LoggingLevel.INFO, "Salvando endereço: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal(jacksonDataFormat)
                .to("http://localhost:8080/api/enderecos");
    }
}
