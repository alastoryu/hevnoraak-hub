package br.com.hevnoraak.hub.cep;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.hevnoraak.hub.consulta.RespostaConsulta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CEP")
@RestController
@RequestMapping("/api/v1/cep")
class CepController {

    private final CepService service;

    CepController(CepService service) {
        this.service = service;
    }

    @Operation(summary = "Consulta endereço de um CEP (com ou sem hífen)")
    @GetMapping("/{cep}")
    RespostaConsulta<DadosCep> consultar(@PathVariable String cep) {
        return service.consultar(CepService.normalizar(cep));
    }
}
