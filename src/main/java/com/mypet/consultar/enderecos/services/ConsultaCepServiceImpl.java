package com.mypet.consultar.enderecos.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.consultar.enderecos.dtos.EnderecoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ConsultaCepServiceImpl {

    @Autowired
    private ObjectMapper objectMapper;

    public EnderecoDTO buscaEndereco(String cep) {
        URI endereco = URI.create("https://viacep.com.br/ws/" + cep + "/json/");

        // Log de debug para a URL da API
        System.out.println("Consultando API de CEP com URL: " + endereco);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(endereco)
                .build();

        try {
            HttpResponse<String> response = HttpClient
                    .newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Log de debug para a resposta da API
            System.out.println("Resposta da API de CEP: " + response.body());

            return objectMapper.readValue(response.body(), EnderecoDTO.class);
        } catch (Exception e) {
            // Log de erro
            System.err.println("Erro ao consultar API de CEP: " + e.getMessage());
            throw new RuntimeException("Não consegui obter o endereço a partir desse CEP.", e);
        }
    }
}
