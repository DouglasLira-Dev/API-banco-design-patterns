package com.apibanco.pattern.builder;

import com.apibanco.model.Conta;
import com.apibanco.model.ContaPoupanca;
import com.apibanco.model.TipoConta;

/**
 * Builder concreto para {@link ContaPoupanca}.
 *
 * <p>Herda toda a fluência e as validações de {@link ContaBuilder}; só
 * implementa o que é específico da poupança.</p>
 *
 * <p><b>Exemplo:</b></p>
 * <pre>{@code
 *   ContaPoupanca cp = (ContaPoupanca) new ContaPoupancaBuilder()
 *       .comNumero("0001-12345-7")
 *       .comSaldoInicial(new BigDecimal("100.00"))
 *       .comCliente(cliente)
 *       .build();
 * }</pre>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaBuilder
 */
public class ContaPoupancaBuilder extends ContaBuilder<ContaPoupancaBuilder> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Conta construir() {
        ContaPoupanca conta = new ContaPoupanca(cliente, numero);
        if (saldoInicial.signum() > 0) {
            conta.creditar(saldoInicial);
        }
        return conta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TipoConta tipoEsperado() {
        return TipoConta.POUPANCA;
    }
}