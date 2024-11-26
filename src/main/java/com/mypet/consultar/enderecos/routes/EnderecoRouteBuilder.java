package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.LoggingLevel;  // Adicione esta importação

@Component
public class EnderecoRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(EnderecoRouteBuilder.class);

    @Override
    public void configure() throws Exception {
        log.info("Iniciando ConsultaCepRouteBuilder");

        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logStackTrace(true)
        );

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar pessoa por CPF
        log.info("Registrando rota: direct:buscarPessoaPorCpf");
        from("direct:buscarPessoaPorCpf")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("http://localhost:9090/api/pessoas/cpf/${header.cpf}")
                .convertBodyTo(String.class);

        // Rota para salvar endereço
        log.info("Registrando rota: direct:salvarEndereco");
        from("direct:salvarEndereco")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .to("http://localhost:7070/api/enderecos")
                .log(LoggingLevel.INFO, "Endereço cadastrado com sucesso: ${body}");
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
}
