package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.consultar.enderecos.dtos.EnderecoDTO;
import com.mypet.consultar.enderecos.services.ConsultaCepServiceImpl;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsultaCepRouteBuilder extends RouteBuilder {

    @Autowired
    private ConsultaCepServiceImpl consultaCep;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure() throws Exception {
        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logStackTrace(true)
        );

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar endereço pelo CEP
        from("direct:buscarEnderecoPorCep")
                .process(exchange -> {
                    String cep = exchange.getIn().getHeader("cep", String.class);

                    // Log de debug para o CEP recebido
                    System.out.println("Rota Camel - CEP: " + cep);

                    // Busca o endereço pelo CEP
                    EnderecoDTO enderecoDTO = consultaCep.buscaEndereco(cep);
                    exchange.getIn().setBody(enderecoDTO);
                })
                .log(LoggingLevel.INFO, "Endereço obtido: ${body}");
    }
}
