package br.gov.sifap.payment.application;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BeneficiaryRepository beneficiaryRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository, beneficiaryRepository);
    }

    @Test
    @DisplayName("REQ-PAY-001: should generate payments only for ACTIVE beneficiaries")
    void shouldGeneratePaymentsOnlyForActiveBeneficiaries() {
        var active = new Beneficiary("12345678901", "Maria Silva", "SP", BeneficiaryStatus.ACTIVE);
        active.setCodRegiao(25); // SP
        active.setRendaFamiliar(new BigDecimal("500"));

        when(beneficiaryRepository.findByStatusOrderByCpfAsc(BeneficiaryStatus.ACTIVE))
                .thenReturn(List.of(active));
        when(paymentRepository.existsByCpfAndCompetencia(any(), any())).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Payment> result = service.generateCycle("202601");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("REQ-PAY-003: should not duplicate payment for same CPF+competencia")
    void shouldNotDuplicatePayment() {
        var active = new Beneficiary("12345678901", "Maria Silva", "SP", BeneficiaryStatus.ACTIVE);
        active.setCodRegiao(25);
        active.setRendaFamiliar(new BigDecimal("500"));

        when(beneficiaryRepository.findByStatusOrderByCpfAsc(BeneficiaryStatus.ACTIVE))
                .thenReturn(List.of(active));
        when(paymentRepository.existsByCpfAndCompetencia("12345678901", "202601")).thenReturn(true);

        List<Payment> result = service.generateCycle("202601");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("REQ-PAY-006: should apply regional factor for AC (1.35)")
    void shouldApplyRegionalFactorAC() {
        var beneficiary = new Beneficiary("12345678901", "Jose Santos", "AC", BeneficiaryStatus.ACTIVE);
        beneficiary.setCodRegiao(1); // AC = 1.35
        beneficiary.setRendaFamiliar(new BigDecimal("200")); // < 300 => factor 1.00

        BigDecimal result = service.calculateBenefit(beneficiary);

        // base 1000 * 1.35 (regional) * 1.00 (income) = 1350.00
        assertThat(result).isEqualByComparingTo("1350.00");
    }

    @Test
    @DisplayName("REQ-PAY-007: should apply income factor for renda > R$1500 (0.40)")
    void shouldApplyIncomeFactor() {
        var beneficiary = new Beneficiary("12345678901", "Ana Costa", "SP", BeneficiaryStatus.ACTIVE);
        beneficiary.setCodRegiao(25); // SP = 1.00
        beneficiary.setRendaFamiliar(new BigDecimal("2000")); // > 1500 => 0.40

        BigDecimal result = service.calculateBenefit(beneficiary);

        // base 1000 * 1.00 * 0.40 = 400.00
        assertThat(result).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("REQ-PAY-008: should cap non-judicial deductions at 30%")
    void shouldCapNonJudicialDeductionsAt30Percent() {
        BigDecimal vlrBruto = new BigDecimal("1000.00");
        BigDecimal nonJudicial = new BigDecimal("400.00"); // 40% > 30%
        BigDecimal judicial = BigDecimal.ZERO;

        BigDecimal result = service.calculateTotalDeductions(vlrBruto, nonJudicial, judicial);

        assertThat(result).isEqualByComparingTo("300.00"); // capped at 30%
    }

    @Test
    @DisplayName("REQ-PAY-009: should allow judicial deductions beyond 30% cap")
    void shouldAllowJudicialDeductionsBeyondCap() {
        BigDecimal vlrBruto = new BigDecimal("1000.00");
        BigDecimal nonJudicial = new BigDecimal("200.00"); // 20% within cap
        BigDecimal judicial = new BigDecimal("250.00"); // 25%

        BigDecimal result = service.calculateTotalDeductions(vlrBruto, nonJudicial, judicial);

        // nonJudicial 200 (within 300 cap) + judicial 250 = 450
        assertThat(result).isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("REQ-PAY-010: should calculate social contribution at 5% for R$800")
    void shouldCalculateContributionForBracket2() {
        BigDecimal vlrBruto = new BigDecimal("800.00");

        BigDecimal deductions = service.calculateDeductions(vlrBruto);

        // contribution: 800 * 0.05 = 40.00 + sindical: 800 * 0.01 = 8.00 = 48.00
        assertThat(deductions).isEqualByComparingTo("48.00");
    }

    @Test
    @DisplayName("REQ-PAY-011: should apply 1% sindical discount")
    void shouldApplySindicalDiscount() {
        BigDecimal vlrBruto = new BigDecimal("1000.00");

        BigDecimal deductions = service.calculateDeductions(vlrBruto);

        // contribution: 1000 * 0.05 = 50 + sindical: 1000 * 0.01 = 10 = 60.00
        assertThat(deductions).isEqualByComparingTo("60.00");
    }
}
