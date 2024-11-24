package com.mypet.consultar.enderecos.services;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EnderecoServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Transactional
    public ResponseEntity<?> adicionarEndereco(String cep) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cep", cep);

            // Chama a rota Camel para buscar o endereço por CEP
            producerTemplate.sendBodyAndHeaders("direct:buscarEnderecoPorCep", null, headers);
            return new ResponseEntity<>("Endereço processado com sucesso!", HttpStatus.CREATED);
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("Erro ao adicionar endereço: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
