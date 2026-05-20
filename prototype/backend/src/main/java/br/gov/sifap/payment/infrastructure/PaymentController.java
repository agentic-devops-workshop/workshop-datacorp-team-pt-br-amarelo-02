package br.gov.sifap.payment.infrastructure;

import br.gov.sifap.payment.application.PaymentService;
import br.gov.sifap.payment.domain.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment cycle management — Implements REQ-PAY-001 to REQ-PAY-004")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/cycle/{competencia}")
    @Operation(summary = "Generate payment cycle", description = "REQ-PAY-001: Creates payments for all ACTIVE beneficiaries")
    public ResponseEntity<List<Payment>> generateCycle(@PathVariable String competencia) {
        List<Payment> payments = paymentService.generateCycle(competencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(payments);
    }

    @GetMapping
    @Operation(summary = "List all payments")
    public ResponseEntity<List<Payment>> listAll() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}
