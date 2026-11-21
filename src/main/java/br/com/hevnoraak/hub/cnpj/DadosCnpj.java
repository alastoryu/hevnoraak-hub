package br.com.hevnoraak.hub.cnpj;

import java.util.List;

public record DadosCnpj(
    String cnpj,
    String razaoSocial,
    String nomeFantasia,
    String situacaoCadastral,
    String dataInicioAtividade,
    String cnaePrincipal,
    String cnaePrincipalDescricao,
    Endereco endereco,
    List<Socio> socios) {

    public record Endereco(String logradouro, String numero, String complemento, String bairro, String municipio, String uf, String cep) {}

    public record Socio(String nome, String qualificacao) {}
}
