package br.gov.sifap.eligibility.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    @Test
    @DisplayName("REQ-ELG-001: should accept valid CPF")
    void shouldAcceptValidCpf() {
        assertThat(CpfValidator.isValid("52998224725")).isTrue();
    }

    @Test
    @DisplayName("REQ-ELG-001: should reject CPF 000.000.000-00")
    void shouldRejectAllZeros() {
        assertThat(CpfValidator.isValid("00000000000")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "22222222222", "99999999999"})
    @DisplayName("REQ-ELG-001: should reject all-same-digit CPFs")
    void shouldRejectAllSameDigit(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isFalse();
    }

    @Test
    @DisplayName("REQ-ELG-001: should reject CPF with invalid check digit")
    void shouldRejectInvalidCheckDigit() {
        assertThat(CpfValidator.isValid("52998224720")).isFalse();
    }

    @Test
    @DisplayName("REQ-ELG-001: should reject null CPF")
    void shouldRejectNull() {
        assertThat(CpfValidator.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("REQ-ELG-001: should reject CPF with wrong length")
    void shouldRejectWrongLength() {
        assertThat(CpfValidator.isValid("1234")).isFalse();
    }
}
