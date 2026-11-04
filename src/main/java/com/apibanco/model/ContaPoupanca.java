package com.apibanco.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conta <b>Poupança</b> &mdash; subclasse concreta de {@link Conta}.
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li><b>Tarifa de saque:</b> <b>isenta</b> &mdash; retorna
 *       {@link BigDecimal#ZERO};</li>
 *   <li><b>Rendimento:</b> <b>0,5% ao mês</b> sobre o saldo atual
 *       (valor meramente didático &mdash; não reflete a taxa real do
 *       mercado nem regras do Bacen).</li>
 * </ul>
 *
 * <p>Persistida na tabela única {@code conta} com discriminador
 * {@link TipoConta#POUPANCA} (herança SINGLE_TABLE).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Conta
 * @see     TipoConta
 */
public class ContaPoupanca extends Conta {

    /**
     * Taxa mensal de rendimento (0,5% &mdash; valor didático).
     */
    private static final BigDecimal TAXA_RENDIMENTO_MENSAL = new BigDecimal("0.005");

    /**
     * Construtor vazio exigido pelo JPA.
     */
    protected ContaPoupanca() {
        super();
    }

    /**
     * Construtor de negócio chamado pelo {@code ContaPoupancaBuilder}.
     *
     * @param cliente dono da conta; não pode ser {@code null}
     * @param numero  número único da conta; não pode ser vazio
     */
    public ContaPoupanca(Cliente cliente, String numero) {
        super(cliente, numero);
    }

    /**
     * Poupança é <b>isenta</b> de tarifa de saque.
     *
     * @param valor valor do saque (ignorado)
     * @return {@link BigDecimal#ZERO}
     */
    @Override
    public BigDecimal calcularTaxaSaque(BigDecimal valor) {
        return BigDecimal.ZERO;
    }

    /**
     * Aplica rendimento mensal de 0,5% ao saldo atual, com
     * arredondamento {@link RoundingMode#HALF_EVEN} (regra bancária).
     */
    @Override
    public void aplicarRendimento() {
        BigDecimal rendimento = getSaldo()
            .multiply(TAXA_RENDIMENTO_MENSAL)
            .setScale(2, RoundingMode.HALF_EVEN);
        if (rendimento.signum() > 0) {
            creditar(rendimento);
        }
    }

    /**
     * @return {@link TipoConta#POUPANCA}
     */
    @Override
    public TipoConta tipoEsperado() {
        return TipoConta.POUPANCA;
    }
}