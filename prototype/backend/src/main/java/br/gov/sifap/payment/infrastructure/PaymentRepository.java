package br.gov.sifap.payment.infrastructure;

import br.gov.sifap.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCpfAndCompetencia(String cpf, String competencia);
    boolean existsByCpfAndCompetencia(String cpf, String competencia);
}
