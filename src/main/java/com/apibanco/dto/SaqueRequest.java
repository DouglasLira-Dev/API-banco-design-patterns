package com.apibanco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de <b>entrada</b> do endpoint {@code POST /api/transacoes/saque}.
 *
 * <p>Campos específicos de saque: apenas conta de origem e valor.</p>
 *
 * @param contaOrigemId id da conta debitada; não pode ser nulo
 * @param valor         valor do saque; deve ser positivo
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
public record SaqueRequest(
    @NotNull(message = "contaOrigemId é obrigatório")
    Long contaOrigemId,

    @NotNull(message = "valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "valor deve ser maior que zero")
    BigDecimal valor
) {
}