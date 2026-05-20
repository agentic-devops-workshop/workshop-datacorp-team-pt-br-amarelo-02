package br.gov.sifap.eligibility.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

// REQ-ELG-002: UF validation against 27 official codes
class UfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"SP", "RJ", "MG", "BA", "RS", "PR", "PE", "CE", "PA", "MA",
            "GO", "SC", "PB", "AM", "ES", "RN", "AL", "MT", "PI", "DF",
            "MS", "SE", "RO", "TO", "AC", "AP", "RR"})
    void should_accept_valid_uf(String uf) {
        assertThat(UfValidator.isValid(uf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"XX", "ZZ", "AB", "00", ""})
    void should_reject_invalid_uf(String uf) {
        assertThat(UfValidator.isValid(uf)).isFalse();
    }

    @Test
    void should_reject_null_uf() {
        assertThat(UfValidator.isValid(null)).isFalse();
    }

    @Test
    void should_accept_lowercase_uf() {
        assertThat(UfValidator.isValid("sp")).isTrue();
    }
}
