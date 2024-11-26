package com.mypet.consultar.enderecos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypet.consultar.enderecos.dtos.OriginalEnderecoDTO;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private static final Logger log = LoggerFactory.getLogger(EnderecoController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarEndereco(@RequestBody OriginalEnderecoDTO enderecoDTO) {
        try {
            // Obter o CPF do OriginalEnderecoDTO
            String cpf = enderecoDTO.getCpf();
            Map<String, Object> headers = new HashMap<>();
            headers.put("cpf", cpf);

            // Chamar a rota Camel para buscar a pessoa por CPF
            String pessoaJson = producerTemplate.requestBodyAndHeaders("direct:buscarPessoaPorCpf", null, headers, String.class);

            // Verificação se a resposta não está vazia
            if (pessoaJson != null && !pessoaJson.isEmpty()) {
                // Converter o JSON da resposta para um Map
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> pessoaMap = objectMapper.readValue(pessoaJson, HashMap.class);

                // Atualizar a pessoa existente com o tipo de pessoa do EnderecoDTO
                pessoaMap.put("perfis", enderecoDTO.getTipoDePessoa());

                // Adicionar o ID da pessoa aos cabeçalhos para atualização
                headers.put("id", pessoaMap.get("id"));

                // Enviar a pessoa atualizada para a rota de atualização
                producerTemplate.sendBodyAndHeaders("direct:atualizarPessoa", pessoaMap, headers);

                // Criar e preparar o objeto endereço para salvar, sem enviar o CPF
                Map<String, Object> enderecoMap = new HashMap<>();
                enderecoMap.put("pessoaId", pessoaMap.get("id"));
                enderecoMap.put("tipoDeEndereco", enderecoDTO.getTipoDeEndereco());
                enderecoMap.put("tipoDePessoa", enderecoDTO.getTipoDePessoa());
                enderecoMap.put("localidade", enderecoDTO.getLocalidade());
                enderecoMap.put("cep", enderecoDTO.getCep());
                enderecoMap.put("uf", enderecoDTO.getUf());
                enderecoMap.put("bairro", enderecoDTO.getBairro());
                enderecoMap.put("complemento", enderecoDTO.getComplemento());
                enderecoMap.put("logradouro", enderecoDTO.getLogradouro());
                enderecoMap.put("numero", enderecoDTO.getNumero());
                enderecoMap.put("pais", enderecoDTO.getPais());

                // Enviar o objeto endereço para a rota de salvar endereço
                producerTemplate.sendBody("direct:salvarEndereco", enderecoMap);

                return new ResponseEntity<>("Endereço adicionado com sucesso!", HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Pessoa não encontrada.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Log de erro detalhado
            log.error("Erro ao adicionar endereço: " + e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
