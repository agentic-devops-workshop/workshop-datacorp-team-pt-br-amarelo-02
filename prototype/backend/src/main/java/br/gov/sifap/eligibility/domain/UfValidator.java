package br.gov.sifap.eligibility.domain;

import java.util.Set;

/**
 * REQ-ELG-002: Rejects UF outside official 27 units.
 * source_legacy: VALBENEF.NSN#L76-L102
 */
public final class UfValidator {

    private static final Set<String> VALID_UFS = Set.of(
        "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
        "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"
    );

    private UfValidator() {}

    public static boolean isValid(String uf) {
        return uf != null && VALID_UFS.contains(uf.toUpperCase());
    }
}
