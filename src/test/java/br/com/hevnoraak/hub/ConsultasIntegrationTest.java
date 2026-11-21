package br.com.hevnoraak.hub;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.sun.net.httpserver.HttpServer;

/** Sobe uma fonte externa falsa (BrasilAPI e ViaCEP) e exercita o contrato do Hub. */
@SpringBootTest
@AutoConfigureMockMvc
class ConsultasIntegrationTest {

    static final AtomicInteger chamadasCnpj = new AtomicInteger();
    static final HttpServer fonte = iniciarFonteFalsa();

    @Autowired MockMvc mvc;
    @MockitoBean JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void fontes(DynamicPropertyRegistry r) {
        String url = "http://localhost:" + fonte.getAddress().getPort();
        r.add("hub.fontes.brasilapi.url", () -> url);
        r.add("hub.fontes.viacep.url", () -> url);
        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }

    @AfterAll
    static void parar() {
        fonte.stop(0);
    }

    static RequestPostProcessor instalacao() {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_hub.consulta"));
    }

    @Test
    void cnpjEncontradoVemNormalizadoComEvidenciaECache() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/api/v1/cnpj/11.222.333000181").with(instalacao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCONTRADO"))
                .andExpect(jsonPath("$.fonte").value("BrasilAPI"))
                .andExpect(jsonPath("$.dados.razaoSocial").value("PADARIA X LTDA"))
                .andExpect(jsonPath("$.dados.endereco.uf").value("PR"))
                .andExpect(jsonPath("$.dados.socios[0].nome").value("FULANO DE TAL"))
                .andExpect(jsonPath("$.evidencia").isNotEmpty());
        }
        org.assertj.core.api.Assertions.assertThat(chamadasCnpj.get()).isEqualTo(1);
    }

    @Test
    void cnpjInexistenteENaoEncontrado() throws Exception {
        mvc.perform(get("/api/v1/cnpj/12ABC34501DE35").with(instalacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("NAO_ENCONTRADO"));
    }

    @Test
    void cnpjComDigitoErradoEBadRequest() throws Exception {
        mvc.perform(get("/api/v1/cnpj/11222333000182").with(instalacao())).andExpect(status().isBadRequest());
    }

    @Test
    void cepEncontradoENaoEncontrado() throws Exception {
        mvc.perform(get("/api/v1/cep/87013-000").with(instalacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ENCONTRADO"))
            .andExpect(jsonPath("$.dados.municipio").value("Maringá"));
        mvc.perform(get("/api/v1/cep/99999999").with(instalacao()))
            .andExpect(jsonPath("$.status").value("NAO_ENCONTRADO"));
    }

    @Test
    void fonteForaDoArEServiceUnavailable() throws Exception {
        mvc.perform(get("/api/v1/cep/50000000").with(instalacao()))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.tentarNovamente").value(true));
    }

    @Test
    void exigeEscopoDoHub() throws Exception {
        mvc.perform(get("/api/v1/cep/87013000")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/cep/87013000").with(jwt())).andExpect(status().isForbidden());
    }

    private static HttpServer iniciarFonteFalsa() {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            s.createContext("/api/cnpj/v1/", ex -> {
                String cnpj = ex.getRequestURI().getPath().substring("/api/cnpj/v1/".length());
                if (cnpj.equals("11222333000181")) {
                    chamadasCnpj.incrementAndGet();
                    responder(ex, 200, """
                        {"cnpj":"11222333000181","razao_social":"PADARIA X LTDA","nome_fantasia":"PADARIA X",
                         "descricao_situacao_cadastral":"ATIVA","data_inicio_atividade":"2020-01-15",
                         "cnae_fiscal":1091102,"cnae_fiscal_descricao":"Fabricação de produtos de padaria",
                         "logradouro":"AV BRASIL","numero":"100","complemento":null,"bairro":"CENTRO",
                         "municipio":"MARINGA","uf":"PR","cep":"87013000",
                         "qsa":[{"nome_socio":"FULANO DE TAL","qualificacao_socio":"Sócio-Administrador"}]}""");
                } else {
                    responder(ex, 404, "{\"message\":\"CNPJ não encontrado\"}");
                }
            });
            s.createContext("/ws/", ex -> {
                String path = ex.getRequestURI().getPath();
                if (path.contains("87013000")) {
                    responder(ex, 200, """
                        {"cep":"87013-000","logradouro":"Avenida Brasil","complemento":"","bairro":"Zona 01",
                         "localidade":"Maringá","uf":"PR","ibge":"4115200"}""");
                } else if (path.contains("50000000")) {
                    responder(ex, 502, "Bad Gateway");
                } else {
                    responder(ex, 200, "{\"erro\": \"true\"}");
                }
            });
            s.start();
            return s;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void responder(com.sun.net.httpserver.HttpExchange ex, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
