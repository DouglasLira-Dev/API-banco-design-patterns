package com.apibanco.dto;

import com.apibanco.model.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de <b>entrada</b> para criação de transação.
 *
 * <p><b>Campos:</b></p>
 * <ul>
 *   <li>{@code contaOrigemId} &mdash; <b>nullable</b> em depósito e rendimento;</li>
 *   <li>{@code contaDestinoId} &mdash; <b>nullable</b> em saque;</li>
 *   <li>{@code valor} &mdash; estritamente positivo;</li>
 *   <li>{@code tipo} &mdash; obrigatório.</li>
 * </ul>
 *
 * <p><b>Invariante "ao menos uma conta":</b> este DTO <b>não</b> valida
 * isso &mdash; a regra é aplicada no {@code TransacaoService} (mensagem
 * amigável) e reforçada no {@code @PrePersist} da entidade. O DTO
 * permanece <b>declarativo</b>: só formato, não semântica.</p>
 *
 * @param contaOrigemId  id da conta de origem (nulo em depósito/rendimento)
 * @param contaDestinoId id da conta de destino (nulo em saque)
 * @param valor          valor movimentado; deve ser &gt; 0
 * @param tipo           tipo da transação; não pode ser nulo
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     TransacaoResponse
 */
public record TransacaoRequest(

    Long contaOrigemId,

    Long contaDestinoId,

    @NotNull(message = "valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "valor deve ser maior que zero")
    BigDecimal valor,

    @NotNull(message = "tipo é obrigatório")
    TipoTransacao tipo
) {
}