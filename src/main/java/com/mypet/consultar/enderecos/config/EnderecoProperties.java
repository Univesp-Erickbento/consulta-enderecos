package com.mypet.consultar.enderecos.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "enderecos")
public class EnderecoProperties {

    private String apiBaseUrl;
    private String pessoaServiceUrl;

    // Getters e Setters
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getPessoaServiceUrl() {
        return pessoaServiceUrl;
    }

    public void setPessoaServiceUrl(String pessoaServiceUrl) {
        this.pessoaServiceUrl = pessoaServiceUrl;
    }
}
