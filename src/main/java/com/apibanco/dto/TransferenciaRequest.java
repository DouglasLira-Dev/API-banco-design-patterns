package com.apibanco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de <b>entrada</b> do endpoint {@code POST /api/transacoes/transferencia}.
 *
 * @param contaOrigemId  id da conta debitada; não pode ser nulo
 * @param contaDestinoId id da conta creditada; não pode ser nulo
 *                       e deve ser diferente da origem
 * @param valor          valor transferido; deve ser positivo
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
public record TransferenciaRequest(
    @NotNull(message = "contaOrigemId é obrigatório")
    Long contaOrigemId,

    @NotNull(message = "contaDestinoId é obrigatório")
    Long contaDestinoId,

    @NotNull(message = "valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "valor deve ser maior que zero")
    BigDecimal valor
) {
}