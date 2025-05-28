package com.mypet.consultar.enderecos.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mypet.consultar.enderecos.config.EnderecoProperties;
import com.mypet.consultar.enderecos.dtos.EnderecoCompletoDTO;
import com.mypet.consultar.enderecos.processor.PessoaComEnderecoProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class EnderecoRouteBuilder extends RouteBuilder {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PessoaComEnderecoProcessor pessoaComEnderecoProcessor;

    @Autowired
    private EnderecoProperties enderecoProperties;

    @Override
    public void configure() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Erro ao processar a troca: ${exception.message}")
                .handled(true);

        from("direct:buscarPessoaComEndereco")
                .routeId("rota-buscar-pessoa-com-endereco")
                .log("Recebido CPF=${header.cpf}, CEP=${header.cep}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD(enderecoProperties.getPessoaServiceUrl() + "/cpf/${header.cpf}?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson)
                .setProperty("pessoaJson", simple("${body}"))
                .log("Pessoa encontrada: ${body}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .removeHeader("Authorization")
                .toD(enderecoProperties.getApiBaseUrl() + "/${header.cep}?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson)
                .setProperty("enderecoJson", simple("${body}"))
                .log("Endereço encontrado: ${body}")
                .setBody().simple("""
                {
                  "pessoaId": ${property.pessoaJson[pessoaId]},
                  "rua": "${property.enderecoJson.logradouro}",
                  "numero": "123",
                  "bairro": "${property.enderecoJson.bairro}",
                  "cidade": "${property.enderecoJson.localidade}",
                  "estado": "${property.enderecoJson.uf}",
                  "cep": "${header.cep}",
                  "pais": "Brasil",
                  "tipoDePessoa": "${property.pessoaJson.perfil}",
                  "tipoDeEndereco": "Residencial"
                }
                """)
                .process(pessoaComEnderecoProcessor)
                .log("Payload final após processamento: ${body}");

        from("direct:salvarEnderecoCompleto")
                .routeId("rota-salvar-endereco-completo")
                .log("Recebendo endereço completo para CPF=${header.cpf}")
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Authorization", simple("${header.Authorization}"))
                .toD(enderecoProperties.getPessoaServiceUrl() + "/cpf/${header.cpf}?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson)
                .setProperty("pessoa", simple("${body}"))
                .log("Pessoa encontrada: ${body}")
                .process(exchange -> {
                    EnderecoCompletoDTO enderecoDTO = (EnderecoCompletoDTO) exchange.getIn().getHeader("enderecoDTO");
                    Map<String, Object> pessoaMap = exchange.getProperty("pessoa", Map.class);

                    Object idObj = pessoaMap.get("id");
                    if (idObj == null) {
                        throw new IllegalStateException("ID da pessoa não encontrado na resposta do serviço de pessoas.");
                    }

                    Long pessoaId = Long.parseLong(idObj.toString());

                    Map<String, Object> enderecoFinal = new HashMap<>();
                    enderecoFinal.put("pessoaId", pessoaId);
                    enderecoFinal.put("cep", enderecoDTO.cep());
                    enderecoFinal.put("logradouro", enderecoDTO.logradouro());
                    enderecoFinal.put("numero", enderecoDTO.numero());
                    enderecoFinal.put("complemento", enderecoDTO.complemento());
                    enderecoFinal.put("bairro", enderecoDTO.bairro());
                    enderecoFinal.put("localidade", enderecoDTO.localidade());
                    enderecoFinal.put("estado", enderecoDTO.estado());
                    enderecoFinal.put("pais", enderecoDTO.pais());
                    enderecoFinal.put("perfil", enderecoDTO.perfil());
                    enderecoFinal.put("tipoDeEndereco", enderecoDTO.tipoDeEndereco());

                    exchange.getIn().setBody(enderecoFinal);
                })
                .log("Payload final para salvar: ${body}")
                .setHeader("CamelHttpMethod", constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .marshal().json(JsonLibrary.Jackson)
                .to(enderecoProperties.getApiBaseUrl() + "?bridgeEndpoint=true")
                .convertBodyTo(String.class)
                .log("Resposta do serviço de salvar endereço: ${body}");
    }
}
