package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PessoaRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(PessoaRouteBuilder.class);
    private static final String PESSOA_SERVICE_URL = "http://localhost:9090/api/pessoas";

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure() throws Exception {
        log.info("Iniciando PessoaRouteBuilder");

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar pessoa por CPF
        from("direct:buscarPessoaPorCpf")
                .log(LoggingLevel.INFO, "Buscando pessoa por CPF: ${header.cpf}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Accept", constant("application/json"))
                .toD(PESSOA_SERVICE_URL + "/cpf/${header.cpf}")
                .convertBodyTo(String.class)
                .log(LoggingLevel.INFO, "Resposta do serviço de pessoa: ${body}");

        // Rota para atualizar pessoa
        from("direct:atualizarPessoa")
                .log(LoggingLevel.INFO, "Atualizando pessoa com ID: ${header.id}")
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .toD(PESSOA_SERVICE_URL + "/${header.id}");

        // Rota para salvar endereço
        from("direct:salvarEndereco")
                .log(LoggingLevel.INFO, "Salvando endereço: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json()
                .to("http://localhost:7070/api/enderecos")
                .log(LoggingLevel.INFO, "Endereço cadastrado com sucesso: ${body}");
    }
}
