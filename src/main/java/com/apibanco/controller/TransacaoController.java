package com.apibanco.controller;

import com.apibanco.dto.DepositoRequest;
import com.apibanco.dto.SaqueRequest;
import com.apibanco.dto.TransacaoResponse;
import com.apibanco.dto.TransferenciaRequest;
import com.apibanco.model.Transacao;
import com.apibanco.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller REST de <b>transações</b>.
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>{@code POST /api/transacoes/deposito} &mdash; 201 + TransacaoResponse;</li>
 *   <li>{@code POST /api/transacoes/saque} &mdash; 201 + TransacaoResponse;</li>
 *   <li>{@code POST /api/transacoes/transferencia} &mdash; 201 + TransacaoResponse.</li>
 * </ul>
 *
 * <p>Cada endpoint usa o DTO <b>específico</b> da operação &mdash; não o
 * {@code TransacaoRequest} genérico &mdash; para que o {@code @Valid}
 * pegue exatamente os campos obrigatórios daquela operação.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     TransacaoService
 */
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    /**
     * Construtor com injeção do service.
     *
     * @param transacaoService serviço de transações
     */
    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    /**
     * Executa um depósito.
     *
     * @param request dados do depósito; validado por {@code @Valid}
     * @return {@code 201 Created} com a transação registrada
     */
    @PostMapping("/deposito")
    public ResponseEntity<TransacaoResponse> depositar(@Valid @RequestBody DepositoRequest request) {
        Transacao t = transacaoService.depositar(request.contaDestinoId(), request.valor());
        return criada(t);
    }

    /**
     * Executa um saque.
     *
     * @param request dados do saque; validado por {@code @Valid}
     * @return {@code 201 Created} com a transação registrada
     */
    @PostMapping("/saque")
    public ResponseEntity<TransacaoResponse> sacar(@Valid @RequestBody SaqueRequest request) {
        Transacao t = transacaoService.sacar(request.contaOrigemId(), request.valor());
        return criada(t);
    }

    /**
     * Executa uma transferência.
     *
     * @param request dados da transferência; validado por {@code @Valid}
     * @return {@code 201 Created} com a transação registrada
     */
    @PostMapping("/transferencia")
    public ResponseEntity<TransacaoResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        Transacao t = transacaoService.transferir(
            request.contaOrigemId(), request.contaDestinoId(), request.valor());
        return criada(t);
    }

    /**
     * Helper &mdash; monta o {@code 201 Created} com header {@code Location}
     * apontando para {@code /api/transacoes/{id}} (endpoint de detalhe
     * ainda não existe, mas o header é útil para o cliente).
     *
     * @param t transação criada
     * @return {@code 201 Created}
     */
    private ResponseEntity<TransacaoResponse> criada(Transacao t) {
        return ResponseEntity
            .created(URI.create("/api/transacoes/" + t.getId()))
            .body(TransacaoResponse.from(t));
    }
}