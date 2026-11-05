package com.apibanco.controller;

import com.apibanco.dto.ContaRequest;
import com.apibanco.dto.ContaResponse;
import com.apibanco.dto.TransacaoResponse;
import com.apibanco.model.Conta;
import com.apibanco.service.ContaService;
import com.apibanco.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST de <b>contas</b>.
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>{@code POST /api/contas} &mdash; abre conta (201);</li>
 *   <li>{@code GET /api/contas/{id}} &mdash; busca conta (200);</li>
 *   <li>{@code GET /api/contas/{id}/extrato} &mdash; extrato da conta (200).</li>
 * </ul>
 *
 * <p><b>Nota:</b> o extrato é delegado ao {@link TransacaoService}, que é
 * o dono do {@code TransacaoRepository}. O {@code ContaController} apenas
 * orquestra &mdash; nenhuma regra.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaService
 * @see     TransacaoService
 */
@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;

    /**
     * Construtor com injeção dos services.
     *
     * @param contaService     serviço de contas
     * @param transacaoService serviço de transações (para o extrato)
     */
    public ContaController(ContaService contaService,
                        TransacaoService transacaoService) {
        this.contaService = contaService;
        this.transacaoService = transacaoService;
    }

    /**
     * Abre uma nova conta.
     *
     * @param request dados da conta; validado por {@code @Valid}
     * @return {@code 201 Created} com a conta criada e header {@code Location}
     */
    @PostMapping
    public ResponseEntity<ContaResponse> abrir(@Valid @RequestBody ContaRequest request) {
        Conta conta = contaService.abrir(request);
        return ResponseEntity
            .created(URI.create("/api/contas/" + conta.getId()))
            .body(ContaResponse.from(conta));
    }

    /**
     * Busca uma conta pelo id.
     *
     * @param id identificador
     * @return {@code 200 OK} com a conta
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        Conta conta = contaService.buscarPorId(id);
        return ResponseEntity.ok(ContaResponse.from(conta));
    }

    /**
     * Retorna o extrato de uma conta (transações em que ela é origem ou
     * destino), ordenado por data decrescente.
     *
     * @param id identificador da conta
     * @return {@code 200 OK} com a lista (possivelmente vazia)
     */
    @GetMapping("/{id}/extrato")
    public ResponseEntity<List<TransacaoResponse>> extrato(@PathVariable Long id) {
        // Garante que a conta existe (senão BusinessException → 400)
        contaService.buscarPorId(id);
        List<TransacaoResponse> extrato = transacaoService.extrato(id).stream()
            .map(TransacaoResponse::from)
            .toList();
        return ResponseEntity.ok(extrato);
    }
}