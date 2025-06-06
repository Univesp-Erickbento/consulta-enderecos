package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mypet.consultar.enderecos.config.EnderecoProperties;
import com.mypet.consultar.enderecos.services.AuthService;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PessoaRouteBuilder extends RouteBuilder {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private EnderecoProperties enderecoProperties;

    @Override
    public void configure() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(objectMapper, Object.class);

        // Tratamento de exceções
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    return exception.getStatusCode() == 401;
                })
                .log(LoggingLevel.ERROR, "Erro de autenticação: Token inválido ou expirado. Status: ${exception.statusCode} - ${exception.message}")
                .handled(true);

        onException(JsonProcessingException.class)
                .log(LoggingLevel.ERROR, "Erro ao processar o corpo JSON: ${exception.message}")
                .handled(true);

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        // Rota para buscar pessoa por CPF
        from("direct:buscarPessoaPorCpf")
                .log(LoggingLevel.INFO, "Cabeçalhos recebidos: ${headers}")
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    log.info("Cabeçalho Authorization: " + authorizationHeader);
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        throw new IllegalArgumentException("Token extraído está vazio ou inválido.");
                    }
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .log(LoggingLevel.INFO, "Token de autorização após processamento: ${header.Authorization}")
                .log(LoggingLevel.INFO, "URL da Requisição: " + enderecoProperties.getPessoaServiceUrl() + "/cpf/${header.cpf}")
                .toD(enderecoProperties.getPessoaServiceUrl() + "/cpf/${header.cpf}")
                .log(LoggingLevel.INFO, "Resposta da API para o CPF ${header.cpf}: ${body}")
                .convertBodyTo(String.class);

        // Rota para atualizar pessoa
        from("direct:atualizarPessoa")
                .log(LoggingLevel.INFO, "Cabeçalhos Iniciais: ${headers}")
                .process(exchange -> {
                    String authorizationHeader = exchange.getIn().getHeader("Authorization", String.class);
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Token de autorização inválido ou não fornecido.");
                    }
                    String token = authService.extractToken(exchange);
                    if (token == null || token.trim().isEmpty()) {
                        throw new IllegalArgumentException("Token extraído está vazio ou inválido.");
                    }
                    exchange.getIn().setHeader("Authorization", "Bearer " + token);
                })
                .log(LoggingLevel.INFO, "Token de autorização após processamento: ${header.Authorization}")
                .setHeader("CamelHttpMethod", constant("PUT"))
                .setHeader("Accept", constant("application/json"))
                .setHeader("Content-Type", constant("application/json"))
                .process(exchange -> {
                    try {
                        Object body = exchange.getIn().getBody();
                        String jsonBody = objectMapper.writeValueAsString(body);
                        exchange.getIn().setBody(jsonBody, String.class);
                    } catch (JsonProcessingException e) {
                        exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
                        throw new IllegalArgumentException("Erro ao converter o corpo da mensagem para JSON", e);
                    }
                })
                .log(LoggingLevel.INFO, "URL da Requisição de Atualização: " + enderecoProperties.getPessoaServiceUrl() + "/${header.id}")
                .toD(enderecoProperties.getPessoaServiceUrl() + "/${header.id}")
                .convertBodyTo(String.class);

        // Rota para transformar dados da pessoa
        from("direct:buscarPessoa")
                .routeId("rota-buscar-pessoa-com-endereco")
                .log(LoggingLevel.INFO, "Transformando dados da pessoa recebida")
                .process(exchange -> {
                    Map<String, Object> pessoaJson = exchange.getMessage().getBody(Map.class);
                    Map<String, Object> enderecoJson = (Map<String, Object>) pessoaJson.get("endereco");

                    exchange.setProperty("pessoaId", pessoaJson.get("pessoaId"));
                    exchange.setProperty("perfil", pessoaJson.get("perfil"));

                    if (enderecoJson != null) {
                        exchange.setProperty("logradouro", enderecoJson.get("logradouro"));
                        exchange.setProperty("bairro", enderecoJson.get("bairro"));
                        exchange.setProperty("cidade", enderecoJson.get("localidade"));
                        exchange.setProperty("estado", enderecoJson.get("uf"));
                    }
                })
                .setBody(simple("""
                            {
                              "pessoaId": "${exchangeProperty.pessoaId}",
                              "rua": "${exchangeProperty.logradouro}",
                              "numero": "123",
                              "bairro": "${exchangeProperty.bairro}",
                              "cidade": "${exchangeProperty.cidade}",
                              "estado": "${exchangeProperty.estado}",
                              "cep": "${header.cep}",
                              "pais": "Brasil",
                              "tipoDePessoa": "${exchangeProperty.perfil}",
                              "tipoDeEndereco": "Residencial"
                            }
                        """))


                .log(LoggingLevel.INFO, "JSON transformado: ${body}");
    }
    }
