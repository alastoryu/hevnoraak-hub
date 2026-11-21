package br.com.hevnoraak.hub.cnpj;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.hevnoraak.hub.consulta.RespostaConsulta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CNPJ")
@RestController
@RequestMapping("/api/v1/cnpj")
class CnpjController {

    private final CnpjService service;

    CnpjController(CnpjService service) {
        this.service = service;
    }

    @Operation(summary = "Consulta dados cadastrais de um CNPJ (numérico ou alfanumérico, com ou sem máscara)")
    @GetMapping("/{cnpj}")
    RespostaConsulta<DadosCnpj> consultar(@PathVariable String cnpj) {
        return service.consultar(Cnpj.de(cnpj));
    }
}
