package com.apibanco.model;

import java.math.BigDecimal;

/**
 * Conta <b>Corrente</b> &mdash; subclasse concreta de {@link Conta}.
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li><b>Tarifa de saque:</b> valor <b>fixo</b> de {@code R$ 2,50}
 *       por saque, independentemente do valor movimentado;</li>
 *   <li><b>Rendimento:</b> <b>não há</b> &mdash; {@link #aplicarRendimento()}
 *       é no-op (conta corrente não rende juros).</li>
 * </ul>
 *
 * <p>Persistida na tabela única {@code conta} com discriminador
 * {@link TipoConta#CORRENTE} (herança SINGLE_TABLE).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Conta
 * @see     TipoConta
 */
public class ContaCorrente extends Conta {

    /**
     * Tarifa fixa aplicada a cada saque, em reais.
     */
    private static final BigDecimal TARIFA_SAQUE_FIXA = new BigDecimal("2.50");

    /**
     * Construtor vazio exigido pelo JPA.
     */
    protected ContaCorrente() {
        super();
    }

    /**
     * Construtor de negócio chamado pelo {@code ContaCorrenteBuilder}.
     *
     * @param cliente dono da conta; não pode ser {@code null}
     * @param numero  número único da conta; não pode ser vazio
     */
    public ContaCorrente(Cliente cliente, String numero) {
        super(cliente, numero);
    }

    /**
     * Calcula a tarifa de saque &mdash; valor <b>fixo</b> para conta corrente.
     *
     * @param valor valor do saque (não afeta o cálculo nesta subclasse)
     * @return a tarifa fixa de {@code R$ 2,50}
     */
    @Override
    public BigDecimal calcularTaxaSaque(BigDecimal valor) {
        return TARIFA_SAQUE_FIXA;
    }

    /**
     * Conta corrente não rende juros &mdash; no-op.
     */
    @Override
    public void aplicarRendimento() {
        // no-op: conta corrente não rende
    }

    /**
     * @return {@link TipoConta#CORRENTE}
     */
    @Override
    public TipoConta tipoEsperado() {
        return TipoConta.CORRENTE;
    }
}