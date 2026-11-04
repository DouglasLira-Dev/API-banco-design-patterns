package com.apibanco.model;

/**
 * Enumeração dos tipos de transação registrados pelo banco.
 *
 * <p>Cada constante determina <b>quais lados</b> da transação são
 * preenchidos ({@link Transacao#getContaOrigem()} e/ou
 * {@link Transacao#getContaDestino()}) e será usada pelas regras de
 * tarifa e pelas Strategies dos próximos módulos.</p>
 *
 * <ul>
 *   <li>{@link #DEPOSITO}      — apenas conta de destino (crédito externo);</li>
 *   <li>{@link #SAQUE}         — apenas conta de origem (débito externo);</li>
 *   <li>{@link #TRANSFERENCIA} — conta de origem e conta de destino;</li>
 *   <li>{@link #RENDIMENTO}    — apenas conta de destino (crédito interno
 *       gerado por {@link Conta#aplicarRendimento()}).</li>
 * </ul>
 *
 * <p><b>Não confundir com {@link TipoConta}:</b> {@code TipoConta} classifica
 * <i>o que a conta é</i> (corrente, poupança, empresarial);
 * {@code TipoTransacao} classifica <i>o que aconteceu</i> com o dinheiro.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Transacao
 * @see     TipoConta
 */
public enum TipoTransacao {

    /**
     * Depósito — entrada de dinheiro na conta. Apenas
     * {@link Transacao#getContaDestino()} é preenchida.
     */
    DEPOSITO,

    /**
     * Saque — saída de dinheiro da conta. Apenas
     * {@link Transacao#getContaOrigem()} é preenchida.
     */
    SAQUE,

    /**
     * Transferência entre contas — débito na origem, crédito no destino.
     * {@link Transacao#getContaOrigem()} e {@link Transacao#getContaDestino()}
     * são obrigatórios.
     */
    TRANSFERENCIA,

    /**
     * Rendimento — crédito interno gerado por
     * {@link Conta#aplicarRendimento()} (ex.: rendimento mensal de poupança).
     * Apenas {@link Transacao#getContaDestino()} é preenchida — a conta
     * que recebeu o rendimento.
     */
    RENDIMENTO
}