package br.com.hevnoraak.hub.cnpj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.hevnoraak.hub.consulta.DocumentoInvalidoException;

class CnpjTest {

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "11222333000181", "12.ABC.345/01DE-35", "12abc34501de35"})
    void aceitaNumericoEAlfanumerico(String entrada) {
        assertThat(Cnpj.de(entrada).valor()).hasSize(14).isUpperCase();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000182", "12ABC34501DE36", "00000000000000", "123", "", "12ABC34501DEAB"})
    void recusaInvalidos(String entrada) {
        assertThatThrownBy(() -> Cnpj.de(entrada)).isInstanceOf(DocumentoInvalidoException.class);
    }
}
