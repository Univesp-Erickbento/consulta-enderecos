package com.mypet.consultar.enderecos.controllers;

import com.mypet.consultar.enderecos.dtos.EnderecoDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConsultaCepController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/buscarEndereco")
    public ResponseEntity<?> buscarEndereco(@RequestParam String cep) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cep", cep);

            EnderecoDTO enderecoDTO = producerTemplate.requestBodyAndHeaders("direct:buscarEnderecoPorCep", null, headers, EnderecoDTO.class);
            return ResponseEntity.ok(enderecoDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao buscar endereço: " + e.getMessage());
        }
    }
}
