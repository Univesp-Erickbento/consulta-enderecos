package com.mypet.consultar.enderecos.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.consultar.enderecos.dtos.PessoaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ConsultaPessoaService {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PESSOA_SERVICE_URL = "http://localhost:9090/api/pessoas";

    public PessoaDTO buscaPessoa(String cpf) {
        URI uri = URI.create(PESSOA_SERVICE_URL + "/cpf/" + cpf);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();

        try {
            HttpResponse<String> response = HttpClient
                    .newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), PessoaDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar pessoa com CPF: " + cpf, e);
        }
    }
}
