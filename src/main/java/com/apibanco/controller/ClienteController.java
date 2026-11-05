package com.apibanco.controller;

import com.apibanco.dto.ClienteRequest;
import com.apibanco.dto.ClienteResponse;
import com.apibanco.model.Cliente;
import com.apibanco.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST de <b>clientes</b>.
 *
 * <p><b>Responsabilidade:</b> orquestrar DTO &harr; Service. Nenhuma regra
 * de negócio vive aqui &mdash; validações de campo ficam nos DTOs
 * ({@code @Valid}), validações de domínio ficam no
 * {@link ClienteService}.</p>
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>{@code POST /api/clientes} &mdash; cria cliente (201 + Location);</li>
 *   <li>{@code GET /api/clientes/{id}} &mdash; busca por id (200);</li>
 *   <li>{@code GET /api/clientes} &mdash; lista todos (200).</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ClienteService
 * @see     ClienteRequest
 * @see     ClienteResponse
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Construtor com injeção do service.
     *
     * @param clienteService serviço de clientes; não pode ser nulo
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Cria um novo cliente.
     *
     * @param request dados do cliente; validado por {@code @Valid}
     * @return {@code 201 Created} com o cliente criado e header {@code Location}
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        Cliente cliente = clienteService.criar(request);
        return ResponseEntity
            .created(URI.create("/api/clientes/" + cliente.getId()))
            .body(ClienteResponse.from(cliente));
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador
     * @return {@code 200 OK} com o cliente
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(ClienteResponse.from(cliente));
    }

    /**
     * Lista todos os clientes.
     *
     * @return {@code 200 OK} com a lista (possivelmente vazia)
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        List<ClienteResponse> response = clienteService.listar().stream()
            .map(ClienteResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}