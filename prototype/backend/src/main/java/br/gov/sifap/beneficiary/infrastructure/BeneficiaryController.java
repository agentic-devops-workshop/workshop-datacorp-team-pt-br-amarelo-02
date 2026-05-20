package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.eligibility.domain.CpfValidator;
import br.gov.sifap.eligibility.domain.UfValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiaries", description = "Beneficiary CRUD — Implements REQ-ELG-001 to REQ-ELG-004")
public class BeneficiaryController {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryController(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @GetMapping
    @Operation(summary = "List all beneficiaries")
    public ResponseEntity<List<Beneficiary>> listAll() {
        return ResponseEntity.ok(beneficiaryRepository.findAll());
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Find beneficiary by CPF")
    public ResponseEntity<Beneficiary> findByCpf(@PathVariable String cpf) {
        return beneficiaryRepository.findByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
    }

    @PostMapping
    @Operation(summary = "Register new beneficiary", description = "REQ-ELG-001: validates CPF, REQ-ELG-002: validates UF")
    public ResponseEntity<Beneficiary> create(@RequestBody CreateBeneficiaryRequest request) {
        if (!CpfValidator.isValid(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CPF");
        }
        if (!UfValidator.isValid(request.uf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UF");
        }
        if (request.nome() == null || !request.nome().contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must contain first and last name");
        }

        var beneficiary = new Beneficiary(request.cpf(), request.nome(), request.uf(), BeneficiaryStatus.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryRepository.save(beneficiary));
    }

    public record CreateBeneficiaryRequest(String cpf, String nome, String uf) {}
}
