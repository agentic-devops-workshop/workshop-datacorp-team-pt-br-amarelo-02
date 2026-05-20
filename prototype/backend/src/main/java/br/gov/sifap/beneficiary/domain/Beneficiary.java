package br.gov.sifap.beneficiary.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(name = "dt_nascimento")
    private LocalDate dtNascimento;

    @Column(length = 2)
    private String uf;

    @Column(name = "cod_regiao")
    private Integer codRegiao;

    @Column(name = "renda_familiar", precision = 15, scale = 2)
    private BigDecimal rendaFamiliar;

    @Column(name = "num_dependentes")
    private Integer numDependentes = 0;

    @Column(name = "cod_programa")
    private Integer codPrograma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BeneficiaryStatus status = BeneficiaryStatus.ACTIVE;

    protected Beneficiary() {}

    public Beneficiary(String cpf, String nome, String uf, BeneficiaryStatus status) {
        this.cpf = cpf;
        this.nome = nome;
        this.uf = uf;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public LocalDate getDtNascimento() { return dtNascimento; }
    public String getUf() { return uf; }
    public Integer getCodRegiao() { return codRegiao; }
    public BigDecimal getRendaFamiliar() { return rendaFamiliar; }
    public Integer getNumDependentes() { return numDependentes; }
    public Integer getCodPrograma() { return codPrograma; }
    public BeneficiaryStatus getStatus() { return status; }

    public void setRendaFamiliar(BigDecimal rendaFamiliar) { this.rendaFamiliar = rendaFamiliar; }
    public void setCodRegiao(Integer codRegiao) { this.codRegiao = codRegiao; }
    public void setNumDependentes(Integer numDependentes) { this.numDependentes = numDependentes; }
    public void setStatus(BeneficiaryStatus status) { this.status = status; }
}
