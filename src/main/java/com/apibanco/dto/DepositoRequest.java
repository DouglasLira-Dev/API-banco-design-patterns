package com.apibanco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de <b>entrada</b> do endpoint {@code POST /api/transacoes/deposito}.
 *
 * <p>Campos específicos de depósito: apenas conta de destino e valor
 * &mdash; não aceita {@code contaOrigemId} (não faz sentido em depósito).</p>
 *
 * @param contaDestinoId id da conta que recebe o depósito; não pode ser nulo
 * @param valor          valor do depósito; deve ser positivo
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
public record DepositoRequest(
    @NotNull(message = "contaDestinoId é obrigatório")
    Long contaDestinoId,

    @NotNull(message = "valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "valor deve ser maior que zero")
    BigDecimal valor
) {
}