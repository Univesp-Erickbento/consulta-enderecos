package com.mypet.consultar.enderecos.dtos;

import lombok.*;

import java.time.LocalDate;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoDTO {


    private long id;
    private Long pessoaId;  // Use apenas o identificador de Pessoa
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais = "Brasil";
    private String tipoDePessoa;

    private String tipoDeEndereco;
}