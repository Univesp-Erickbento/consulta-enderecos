package com.mypet.consultar.enderecos.services;

import com.mypet.consultar.enderecos.dtos.EnderecoCompletoDTO;
import jakarta.transaction.Transactional;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EnderecoServiceImpl {

    @Autowired
    private ProducerTemplate producerTemplate;

    /**
     * Busca informações de uma pessoa e seu endereço com base no CPF e CEP.
     */
    @Transactional
    public ResponseEntity<?> buscarPessoaComEndereco(String cpf, String cep, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("cep", cep);
            headers.put("Authorization", token);

            Object response = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaComEndereco", null, headers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Erro ao buscar pessoa com endereço: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Salva um endereço completo no sistema.
     */
    @Transactional
    public ResponseEntity<?> salvarEnderecoCompleto(EnderecoCompletoDTO enderecoDTO, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", enderecoDTO.cpf());
            headers.put("Authorization", token);
            headers.put("enderecoDTO", enderecoDTO);

            Object response = producerTemplate.requestBodyAndHeaders("direct:salvarEnderecoCompleto", null, headers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Erro ao salvar endereço completo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Busca os endereços de uma pessoa agrupados por tipo de endereço.
     */
    @Transactional
    public ResponseEntity<?> buscarEnderecosPorTipo(String cpf, String token) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);
            headers.put("Authorization", token);

            Object response = producerTemplate.requestBodyAndHeaders("direct:buscarEnderecosPorTipo", null, headers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Erro ao buscar endereços por tipo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
