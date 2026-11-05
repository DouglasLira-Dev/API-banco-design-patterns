package com.apibanco.dto;

import com.apibanco.model.Conta;
import com.apibanco.model.TipoTransacao;
import com.apibanco.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de <b>saída</b> para representar uma {@link Transacao} na API.
 *
 * <p><b>Por que existe:</b> a entidade {@code Transacao} tem
 * {@code @ManyToOne Conta contaOrigem} e {@code contaDestino} &mdash;
 * serializar as contas inteiras geraria payload enorme (e, com
 * {@code LAZY}, {@code LazyInitializationException}). O DTO extrai
 * apenas os IDs.</p>
 *
 * <p><b>Campos:</b></p>
 * <ul>
 *   <li>{@code id}, {@code tipo}, {@code valor}, {@code dataHora};</li>
 *   <li>{@code contaOrigemId} &mdash; nulo em depósito/rendimento;</li>
 *   <li>{@code contaDestinoId} &mdash; nulo em saque.</li>
 * </ul>
 *
 * @param id             identificador da transação
 * @param tipo           tipo da transação
 * @param valor          valor movimentado
 * @param dataHora       instante do registro
 * @param contaOrigemId  id da conta debitada (pode ser {@code null})
 * @param contaDestinoId id da conta creditada (pode ser {@code null})
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     TransacaoRequest
 */
public record TransacaoResponse(
    Long id,
    TipoTransacao tipo,
    BigDecimal valor,
    LocalDateTime dataHora,
    Long contaOrigemId,
    Long contaDestinoId
) {

    /**
     * Cria um {@code TransacaoResponse} a partir de uma {@link Transacao}.
     *
     * <p><b>Sobre {@code getId()} em proxy LAZY:</b> acessar
     * {@code conta.getId()} em um proxy do Hibernate <b>não</b> dispara
     * query (o ID já está no proxy). Por isso este método é seguro mesmo
     * com a sessão fechada &mdash; o único acesso é ao ID.</p>
     *
     * @param transacao entidade transação; não pode ser {@code null}
     * @return o DTO preenchido
     */
    public static TransacaoResponse from(Transacao transacao) {
        return new TransacaoResponse(
            transacao.getId(),
            transacao.getTipo(),
            transacao.getValor(),
            transacao.getDataHora(),
            idDe(transacao.getContaOrigem()),
            idDe(transacao.getContaDestino())
        );
    }

    /**
     * Extrai o id de uma conta, tolerando {@code null}.
     *
     * @param conta conta (possivelmente {@code null})
     * @return o id, ou {@code null} se a conta for {@code null}
     */
    private static Long idDe(Conta conta) {
        return conta != null ? conta.getId() : null;
    }
}