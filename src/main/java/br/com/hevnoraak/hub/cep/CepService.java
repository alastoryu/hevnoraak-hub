package br.com.hevnoraak.hub.cep;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.com.hevnoraak.hub.consulta.DocumentoInvalidoException;
import br.com.hevnoraak.hub.consulta.FonteIndisponivelException;
import br.com.hevnoraak.hub.consulta.RespostaConsulta;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Consulta de CEP no ViaCEP. */
@Service
public class CepService {

    static final String FONTE = "ViaCEP";

    private final RestClient http;
    private final ObjectMapper json;

    public CepService(RestClient.Builder builder, ObjectMapper json, @Value("${hub.fontes.viacep.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
        this.json = json;
    }

    public static String normalizar(String entrada) {
        String cep = entrada == null ? "" : entrada.replaceAll("\\D", "");
        if (cep.length() != 8) {
            throw new DocumentoInvalidoException("CEP inválido: " + entrada);
        }
        return cep;
    }

    @Cacheable(cacheNames = "cep", unless = "#result.status().name() != 'ENCONTRADO'")
    public RespostaConsulta<DadosCep> consultar(String cep) {
        String corpo;
        try {
            corpo = http.get().uri("/ws/{cep}/json/", cep).retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new FonteIndisponivelException(FONTE, e);
        }
        JsonNode n = json.readTree(corpo);
        if (n.path("erro").asBoolean(false) || n.path("erro").asString("").equals("true")) {
            return RespostaConsulta.naoEncontrado(FONTE);
        }
        return RespostaConsulta.encontrado(new DadosCep(
            cep, n.path("logradouro").asString(null), n.path("complemento").asString(null), n.path("bairro").asString(null),
            n.path("localidade").asString(null), n.path("uf").asString(null), n.path("ibge").asString(null)), FONTE, corpo);
    }
}
