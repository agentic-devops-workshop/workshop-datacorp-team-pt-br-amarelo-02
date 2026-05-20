package br.gov.sifap.payment.application;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.domain.PaymentType;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements REQ-PAY-001 through REQ-PAY-011.
 * Core payment cycle generation with regional factors, income brackets, and deduction rules.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    // REQ-PAY-006: Regional factors (27 UFs) — source: BATCHPGT.NSN#L138-L165
    private static final BigDecimal[] REGIONAL_FACTORS = {
        new BigDecimal("1.35"), // 1-AC
        new BigDecimal("1.25"), // 2-AL
        new BigDecimal("1.10"), // 3-AP
        new BigDecimal("1.20"), // 4-AM
        new BigDecimal("1.15"), // 5-BA
        new BigDecimal("1.10"), // 6-CE
        new BigDecimal("1.00"), // 7-DF
        new BigDecimal("1.05"), // 8-ES
        new BigDecimal("1.10"), // 9-GO
        new BigDecimal("1.30"), // 10-MA
        new BigDecimal("1.15"), // 11-MT
        new BigDecimal("1.10"), // 12-MS
        new BigDecimal("1.00"), // 13-MG
        new BigDecimal("1.25"), // 14-PA
        new BigDecimal("1.20"), // 15-PB
        new BigDecimal("1.05"), // 16-PR
        new BigDecimal("1.20"), // 17-PE
        new BigDecimal("1.30"), // 18-PI
        new BigDecimal("1.00"), // 19-RJ
        new BigDecimal("1.15"), // 20-RN
        new BigDecimal("1.00"), // 21-RS
        new BigDecimal("1.00"), // 22-RO
        new BigDecimal("1.00"), // 23-RR
        new BigDecimal("1.05"), // 24-SC
        new BigDecimal("1.00"), // 25-SP
        new BigDecimal("1.20"), // 26-SE
        new BigDecimal("1.40"), // 27-TO
    };

    // REQ-PAY-007: Income brackets — source: BATCHPGT.NSN#L167-L177
    private static final BigDecimal[][] INCOME_BRACKETS = {
        { new BigDecimal("300"), new BigDecimal("1.00") },
        { new BigDecimal("600"), new BigDecimal("0.85") },
        { new BigDecimal("1000"), new BigDecimal("0.70") },
        { new BigDecimal("1500"), new BigDecimal("0.55") },
        { new BigDecimal("999999"), new BigDecimal("0.40") },
    };

    // REQ-PAY-010: Social contribution brackets — source: CALCDSCT.NSN#L45-L60
    private static final BigDecimal[][] CONTRIB_BRACKETS = {
        { new BigDecimal("500"), new BigDecimal("0.03") },
        { new BigDecimal("1000"), new BigDecimal("0.05") },
        { new BigDecimal("2000"), new BigDecimal("0.07") },
        { new BigDecimal("999999"), new BigDecimal("0.09") },
    };

    private static final BigDecimal DEDUCTION_CAP = new BigDecimal("0.30");
    private static final BigDecimal SINDICAL_RATE = new BigDecimal("0.01");

    public PaymentService(PaymentRepository paymentRepository,
                          BeneficiaryRepository beneficiaryRepository) {
        this.paymentRepository = paymentRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    /**
     * REQ-PAY-001: Generate payment cycle for ACTIVE beneficiaries only.
     * REQ-PAY-003: No duplicate payment for same CPF+competencia.
     * REQ-PAY-004: Process in CPF ascending order.
     */
    @Transactional
    public List<Payment> generateCycle(String competencia) {
        List<Beneficiary> actives = beneficiaryRepository
                .findByStatusOrderByCpfAsc(BeneficiaryStatus.ACTIVE);

        List<Payment> generated = new ArrayList<>();
        for (Beneficiary b : actives) {
            // REQ-PAY-003: skip if already exists
            if (paymentRepository.existsByCpfAndCompetencia(b.getCpf(), competencia)) {
                continue;
            }

            BigDecimal vlrBruto = calculateBenefit(b);
            PaymentType tipo = competencia.endsWith("12") ? PaymentType.D : PaymentType.N;
            Payment payment = new Payment(b.getCpf(), competencia, vlrBruto, tipo);

            BigDecimal deductions = calculateDeductions(vlrBruto);
            payment.applyDeductions(deductions);

            generated.add(paymentRepository.save(payment));
        }
        return generated;
    }

    /**
     * REQ-PAY-006 + REQ-PAY-007: Calculate benefit with regional and income factors.
     * Uses RoundingMode.DOWN to preserve legacy truncation behavior (MYS-004/ADR-002).
     */
    public BigDecimal calculateBenefit(Beneficiary b) {
        BigDecimal base = new BigDecimal("1000.00"); // base from social program

        // REQ-PAY-006: regional factor
        BigDecimal regionalFactor = getRegionalFactor(b.getCodRegiao());
        // REQ-PAY-007: income factor
        BigDecimal incomeFactor = getIncomeFactor(b.getRendaFamiliar());

        return base.multiply(regionalFactor)
                   .multiply(incomeFactor)
                   .setScale(2, RoundingMode.DOWN);
    }

    /**
     * REQ-PAY-008: Non-judicial deductions capped at 30%.
     * REQ-PAY-010: Social contribution by bracket.
     * REQ-PAY-011: Sindical at 1%.
     */
    public BigDecimal calculateDeductions(BigDecimal vlrBruto) {
        // Social contribution (REQ-PAY-010)
        BigDecimal contrib = calculateContribution(vlrBruto);
        // Sindical (REQ-PAY-011)
        BigDecimal sindical = vlrBruto.multiply(SINDICAL_RATE).setScale(2, RoundingMode.DOWN);

        BigDecimal total = contrib.add(sindical);

        // REQ-PAY-008: cap at 30%
        BigDecimal maxDeduction = vlrBruto.multiply(DEDUCTION_CAP).setScale(2, RoundingMode.DOWN);
        if (total.compareTo(maxDeduction) > 0) {
            total = maxDeduction;
        }

        return total;
    }

    /**
     * REQ-PAY-009: Judicial deductions bypass the 30% cap.
     */
    public BigDecimal calculateTotalDeductions(BigDecimal vlrBruto,
                                               BigDecimal nonJudicialTotal,
                                               BigDecimal judicialTotal) {
        BigDecimal maxNonJudicial = vlrBruto.multiply(DEDUCTION_CAP).setScale(2, RoundingMode.DOWN);
        BigDecimal cappedNonJudicial = nonJudicialTotal.min(maxNonJudicial);
        return cappedNonJudicial.add(judicialTotal);
    }

    private BigDecimal calculateContribution(BigDecimal vlrBruto) {
        for (BigDecimal[] bracket : CONTRIB_BRACKETS) {
            if (vlrBruto.compareTo(bracket[0]) <= 0) {
                return vlrBruto.multiply(bracket[1]).setScale(2, RoundingMode.DOWN);
            }
        }
        return vlrBruto.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal getRegionalFactor(Integer codRegiao) {
        if (codRegiao == null || codRegiao < 1 || codRegiao > 27) {
            return BigDecimal.ONE;
        }
        return REGIONAL_FACTORS[codRegiao - 1];
    }

    private BigDecimal getIncomeFactor(BigDecimal rendaFamiliar) {
        if (rendaFamiliar == null) return BigDecimal.ONE;
        for (BigDecimal[] bracket : INCOME_BRACKETS) {
            if (rendaFamiliar.compareTo(bracket[0]) <= 0) {
                return bracket[1];
            }
        }
        return new BigDecimal("0.40");
    }
}
