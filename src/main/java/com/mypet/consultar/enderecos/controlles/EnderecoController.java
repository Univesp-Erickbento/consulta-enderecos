package com.mypet.consultar.enderecos.controlles;

import com.mypet.consultar.enderecos.dtos.EnderecoCompletoDTO;
import com.mypet.consultar.enderecos.services.EnderecoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    @Autowired
    private EnderecoServiceImpl enderecoService;

    /**
     * Salva um endereço completo no sistema.
     */
    @PostMapping("/salvar-endereco")
    public ResponseEntity<?> salvarEnderecoCompleto(
            @RequestBody EnderecoCompletoDTO enderecoDTO,
            @RequestHeader("Authorization") String token
    ) {
        return enderecoService.salvarEnderecoCompleto(enderecoDTO, token);
    }

    /**
     * Busca todos os endereços de uma pessoa agrupados por tipo de endereço.
     * Retorna também o nome da pessoa e o pessoaId.
     */
    @GetMapping("/por-tipo")
    public ResponseEntity<?> buscarEnderecosPorTipo(
            @RequestParam("cpf") String cpf,
            @RequestHeader("Authorization") String token
    ) {
        return enderecoService.buscarEnderecosPorTipo(cpf, token);
    }
}
