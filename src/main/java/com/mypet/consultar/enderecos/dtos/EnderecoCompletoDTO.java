package com.mypet.consultar.enderecos.dtos;

public record EnderecoCompletoDTO(
        String cpf,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String localidade,
        String estado,
        String pais,
        String perfil,
        String tipoDeEndereco
) {}
