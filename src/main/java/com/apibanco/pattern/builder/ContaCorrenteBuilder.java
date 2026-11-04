package com.apibanco.pattern.builder;

import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.TipoConta;

/**
 * Builder concreto para {@link ContaCorrente}.
 *
 * <p>Herda toda a fluência e as validações de {@link ContaBuilder}; só
 * implementa o que é específico da conta corrente: instanciar a subclasse
 * correta em {@link #construir()} e informar o {@link #tipoEsperado()}.</p>
 *
 * <p><b>Exemplo:</b></p>
 * <pre>{@code
 *   ContaCorrente cc = (ContaCorrente) new ContaCorrenteBuilder()
 *       .comNumero("0001-12345-6")
 *       .comSaldoInicial(new BigDecimal("500.00"))
 *       .comCliente(cliente)
 *       .build();
 * }</pre>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaBuilder
 */
public class ContaCorrenteBuilder extends ContaBuilder<ContaCorrenteBuilder> {

    /**
     * Construtor público para que o cliente possa instanciar diretamente.
     */
    public ContaCorrenteBuilder() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Conta construir() {
        ContaCorrente conta = new ContaCorrente(cliente, numero);
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
        return TipoConta.CORRENTE;
    }
}