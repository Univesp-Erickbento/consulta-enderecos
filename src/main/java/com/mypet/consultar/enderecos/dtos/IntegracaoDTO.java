package com.mypet.consultar.enderecos.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IntegracaoDTO {
    private String cpf;
    private EnderecoDTO enderecoDTO;
}
