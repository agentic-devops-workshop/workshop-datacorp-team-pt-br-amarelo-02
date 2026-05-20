package br.gov.sifap.payment.domain;

/** REQ-PAY-002: Todo pagamento inicia com status PENDING */
public enum PaymentStatus {
    PENDING, APPROVED, REJECTED, CANCELLED, DELIVERED
}
