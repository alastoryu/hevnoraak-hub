package br.com.hevnoraak.hub.cnpj;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.com.hevnoraak.hub.consulta.FonteIndisponivelException;
import br.com.hevnoraak.hub.consulta.RespostaConsulta;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Consulta de CNPJ na BrasilAPI (dados públicos da Receita Federal). */
@Service
public class CnpjService {

    static final String FONTE = "BrasilAPI";

    private final RestClient http;
    private final ObjectMapper json;

    public CnpjService(RestClient.Builder builder, ObjectMapper json,
                       @Value("${hub.fontes.brasilapi.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
        this.json = json;
    }

    @Cacheable(cacheNames = "cnpj", key = "#cnpj.valor()", unless = "#result.status().name() != 'ENCONTRADO'")
    public RespostaConsulta<DadosCnpj> consultar(Cnpj cnpj) {
        String corpo;
        try {
            corpo = http.get().uri("/api/cnpj/v1/{cnpj}", cnpj.valor()).retrieve().body(String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return RespostaConsulta.naoEncontrado(FONTE);
            }
            throw new FonteIndisponivelException(FONTE, e);
        } catch (RestClientException e) {
            throw new FonteIndisponivelException(FONTE, e);
        }
        return RespostaConsulta.encontrado(mapear(cnpj, json.readTree(corpo)), FONTE, corpo);
    }

    private static DadosCnpj mapear(Cnpj cnpj, JsonNode n) {
        List<DadosCnpj.Socio> socios = new ArrayList<>();
        n.path("qsa").forEach(s -> socios.add(new DadosCnpj.Socio(txt(s, "nome_socio"), txt(s, "qualificacao_socio"))));
        return new DadosCnpj(
            cnpj.valor(),
            txt(n, "razao_social"),
            txt(n, "nome_fantasia"),
            txt(n, "descricao_situacao_cadastral"),
            txt(n, "data_inicio_atividade"),
            txt(n, "cnae_fiscal"),
            txt(n, "cnae_fiscal_descricao"),
            new DadosCnpj.Endereco(txt(n, "logradouro"), txt(n, "numero"), txt(n, "complemento"),
                txt(n, "bairro"), txt(n, "municipio"), txt(n, "uf"), txt(n, "cep")),
            socios);
    }

    private static String txt(JsonNode n, String campo) {
        JsonNode v = n.path(campo);
        return v.isMissingNode() || v.isNull() ? null : v.asString();
    }
}
