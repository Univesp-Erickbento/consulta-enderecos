package com.mypet.consultar.enderecos.controlles;


import com.mypet.consultar.enderecos.services.EnderecoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    @Autowired
    private EnderecoServiceImpl enderecoService;

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarEndereco(@RequestParam String cep) {
        return enderecoService.adicionarEndereco(cep);
    }
}
