package com.mypet.consultar.enderecos.routes;

import com.mypet.consultar.enderecos.config.EnderecoProperties;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PessoaEnderecoRouteBuilder extends RouteBuilder {

    @Autowired
    private EnderecoProperties enderecoProperties;

    @Override
    public void configure() throws Exception {

        from("direct:buscarEnderecosPorTipo")
                .routeId("rota-buscar-enderecos-por-tipo")
                .log("Iniciando busca de endereços por CPF=${header.cpf}")

                // 1. Buscar a pessoa pelo CPF
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD(enderecoProperties.getPessoaServiceUrl() + "/cpf/${header.cpf}?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson)
                .process(exchange -> {
                    Map<String, Object> pessoaMap = exchange.getIn().getBody(Map.class);
                    String nome = (String) pessoaMap.get("nome");
                    Object idObj = pessoaMap.get("id");

                    if (idObj == null) {
                        throw new IllegalArgumentException("Pessoa não encontrada ou resposta inválida.");
                    }

                    Long pessoaId = Long.parseLong(idObj.toString());

                    exchange.setProperty("pessoaId", pessoaId);
                    exchange.setProperty("nome", nome);
                })
                .log("Pessoa encontrada: ${exchangeProperty.nome} (ID: ${exchangeProperty.pessoaId})")

                // 2. Buscar endereços da pessoa
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .removeHeader("Authorization") // Remova se o serviço de endereços não exige autenticação
                .toD(enderecoProperties.getApiBaseUrl() + "/pessoa/${exchangeProperty.pessoaId}?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson)
                .log("Endereços encontrados: ${body}")

                // 3. Agrupar endereços por tipo
                .process(exchange -> {
                    List<Map<String, Object>> enderecos = exchange.getIn().getBody(List.class);
                    Map<String, List<Map<String, Object>>> agrupado = new HashMap<>();

                    for (Map<String, Object> endereco : enderecos) {
                        String tipo = (String) endereco.getOrDefault("tipoDeEndereco", "Desconhecido");
                        agrupado.computeIfAbsent(tipo, k -> new ArrayList<>()).add(endereco);
                    }

                    Map<String, Object> respostaFinal = new HashMap<>();
                    respostaFinal.put("nome", exchange.getProperty("nome"));
                    respostaFinal.put("pessoaId", exchange.getProperty("pessoaId"));
                    respostaFinal.put("enderecosPorTipo", agrupado);

                    exchange.getIn().setBody(respostaFinal);
                })
                .log("Resposta agrupada final: ${body}");
    }
}
