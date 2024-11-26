package com.mypet.consultar.enderecos.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.consultar.enderecos.dtos.PessoaDTO;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PessoaServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(PessoaServiceImpl.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<?> adicionarPessoa(PessoaDTO pessoaDTO) {
        try {
            // Montando o corpo da solicitação com os dados da pessoa
            Map<String, Object> pessoaMap = new HashMap<>();
            pessoaMap.put("nome", pessoaDTO.getNome());
            pessoaMap.put("cpf", pessoaDTO.getCpf());
            pessoaMap.put("sobrenome", pessoaDTO.getSobrenome());
            pessoaMap.put("rg", pessoaDTO.getRg());
            pessoaMap.put("genero", pessoaDTO.getGenero());
            pessoaMap.put("perfis", pessoaDTO.getPerfis());
            pessoaMap.put("email", pessoaDTO.getEmail());
            pessoaMap.put("contato", pessoaDTO.getContato());
            pessoaMap.put("dataNascimento", pessoaDTO.getDataNascimento());
            pessoaMap.put("dataCadastro", pessoaDTO.getDataCadastro());

            // Enviando a solicitação para a rota Camel
            log.info("Enviando solicitação para adicionar pessoa: {}", pessoaMap);
            producerTemplate.sendBody("direct:adicionarPessoa", pessoaMap);
            return new ResponseEntity<>("Pessoa adicionada com sucesso!", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erro ao adicionar pessoa: " + e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PessoaDTO buscarPessoaPorCpf(String cpf) {
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);

            // Chamar a rota Camel para buscar a pessoa por CPF
            log.info("Chamando a rota Camel para buscar pessoa por CPF: {}", cpf);
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Verificação se a resposta não está vazia
            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                log.info("Resposta do serviço de pessoa: {}", pessoaJson);
                // Converter o JSON da resposta para PessoaDTO
                return objectMapper.readValue(pessoaJson, PessoaDTO.class);
            } else {
                return null;
            }

        } catch (Exception e) {
            // Log de erro detalhado
            log.error("Erro ao buscar pessoa por CPF: " + e.getMessage(), e);
            return null;
        }
    }
}
