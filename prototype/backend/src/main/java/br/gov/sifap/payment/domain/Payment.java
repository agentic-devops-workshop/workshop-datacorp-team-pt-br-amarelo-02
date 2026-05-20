package br.gov.sifap.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_cpf_competencia", columnNames = {"cpf", "competencia"})
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 6)
    private String competencia; // AAAAMM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pgto", nullable = false, columnDefinition = "varchar(1)")
    private PaymentType tipoPgto = PaymentType.N;

    @Column(name = "vlr_bruto", nullable = false, precision = 15, scale = 2)
    private BigDecimal vlrBruto;

    @Column(name = "vlr_desconto", precision = 15, scale = 2)
    private BigDecimal vlrDesconto = BigDecimal.ZERO;

    @Column(name = "vlr_liquido", precision = 15, scale = 2)
    private BigDecimal vlrLiquido;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Payment() {}

    public Payment(String cpf, String competencia, BigDecimal vlrBruto, PaymentType tipoPgto) {
        this.cpf = cpf;
        this.competencia = competencia;
        this.vlrBruto = vlrBruto;
        this.tipoPgto = tipoPgto;
        this.status = PaymentStatus.PENDING;
        this.vlrLiquido = vlrBruto;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public String getCompetencia() { return competencia; }
    public PaymentStatus getStatus() { return status; }
    public PaymentType getTipoPgto() { return tipoPgto; }
    public BigDecimal getVlrBruto() { return vlrBruto; }
    public BigDecimal getVlrDesconto() { return vlrDesconto; }
    public BigDecimal getVlrLiquido() { return vlrLiquido; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void applyDeductions(BigDecimal totalDeductions) {
        this.vlrDesconto = totalDeductions;
        this.vlrLiquido = this.vlrBruto.subtract(totalDeductions);
    }
}
