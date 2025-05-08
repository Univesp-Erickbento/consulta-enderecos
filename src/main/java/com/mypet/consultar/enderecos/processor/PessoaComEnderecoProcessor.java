package com.mypet.consultar.enderecos.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class PessoaComEnderecoProcessor implements Processor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        // Supondo que esses objetos estão nas propriedades
        JsonNode pessoaJson = exchange.getProperty("pessoaJson", JsonNode.class);
        JsonNode enderecoJson = exchange.getProperty("enderecoJson", JsonNode.class);
        String cep = exchange.getIn().getHeader("cep", String.class);

        Map<String, Object> response = new HashMap<>();
        response.put("pessoaId", pessoaJson.path("pessoaId").asText());
        response.put("rua", enderecoJson.path("logradouro").asText());
        response.put("numero", "123");
        response.put("bairro", enderecoJson.path("bairro").asText());
        response.put("cidade", enderecoJson.path("localidade").asText());
        response.put("estado", enderecoJson.path("uf").asText());
        response.put("cep", cep);
        response.put("pais", "Brasil");
        response.put("tipoDePessoa", pessoaJson.path("perfil").asText());
        response.put("tipoDeEndereco", "Residencial");

        exchange.getIn().setBody(response);
    }
}
