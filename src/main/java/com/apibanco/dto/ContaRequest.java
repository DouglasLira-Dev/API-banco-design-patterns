package com.apibanco.dto;

import com.apibanco.model.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de <b>entrada</b> para criação de conta.
 *
 * <p><b>Campos:</b></p>
 * <ul>
 *   <li>{@code clienteId} &mdash; id do cliente dono; obrigatório;</li>
 *   <li>{@code tipo} &mdash; {@link TipoConta} desejado; obrigatório;</li>
 *   <li>{@code saldoInicial} &mdash; saldo inicial; deve ser ≥ 0.</li>
 * </ul>
 *
 * <p><b>Por que {@code tipo} é um {@code TipoConta} e não {@code String}:</b>
 * o Jackson (usado pelo Spring) desserializa {@code "CORRENTE"} diretamente
 * no enum. Tipo inválido &rarr; erro 400 automático do Jackson antes mesmo
 * do {@code @Valid}.</p>
 *
 * @param clienteId    id do cliente dono; não pode ser nulo
 * @param tipo         tipo da conta; não pode ser nulo
 * @param saldoInicial saldo inicial em reais; ≥ 0
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaResponse
 */
public record ContaRequest(

    @NotNull(message = "clienteId é obrigatório")
    Long clienteId,

    @NotNull(message = "tipo é obrigatório")
    TipoConta tipo,

    @NotNull(message = "saldoInicial é obrigatório (use 0 para conta zerada)")
    @DecimalMin(value = "0", inclusive = true, message = "saldoInicial não pode ser negativo")
    BigDecimal saldoInicial
) {
}