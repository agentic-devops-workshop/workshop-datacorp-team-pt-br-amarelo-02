package br.gov.sifap.eligibility.domain;

/**
 * REQ-ELG-001: Validates CPF using modulo 11 algorithm.
 * source_legacy: VALBENEF.NSN#L114-L135
 */
public final class CpfValidator {

    private CpfValidator() {}

    public static boolean isValid(String cpf) {
        if (cpf == null) return false;
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) return false;

        // Reject all-same-digit CPFs (e.g., 000.000.000-00)
        if (digits.chars().distinct().count() == 1) return false;

        // First check digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digits.charAt(i) - '0') * (10 - i);
        }
        int remainder = sum % 11;
        int firstDigit = remainder < 2 ? 0 : 11 - remainder;
        if ((digits.charAt(9) - '0') != firstDigit) return false;

        // Second check digit
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (digits.charAt(i) - '0') * (11 - i);
        }
        remainder = sum % 11;
        int secondDigit = remainder < 2 ? 0 : 11 - remainder;
        return (digits.charAt(10) - '0') == secondDigit;
    }
}
