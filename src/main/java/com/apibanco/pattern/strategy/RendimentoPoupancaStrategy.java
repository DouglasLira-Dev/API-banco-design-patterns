package com.apibanco.pattern.strategy;

import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;
import com.apibanco.model.Transacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy de <b>rendimento</b> para {@link com.apibanco.model.ContaPoupanca}.
 *
 * <p><b>Regra:</b></p>
 * <ul>
 *   <li>rendimento = <b>0,5% ao mês</b> sobre o saldo atual
 *       (taxa didática, não reflete o mercado);</li>
 *   <li>credita o rendimento no saldo;</li>
 *   <li>gera uma {@link Transacao} de tipo {@code RENDIMENTO} com o valor
 *       creditado. A Strategy <b>cria</b> a transação; a <b>persistência</b>
 *       é responsabilidade do {@code TransacaoService}.</li>
 * </ul>
 *
 * <p><b>Sobre a transação:</b> só é gerada se o rendimento for &gt; 0.
 * Saldo zero &rarr; rendimento zero &rarr; nenhuma transação (não sujaria
 * o extrato com lançamentos de R$ 0,00).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     RendimentoStrategy
 * @see     com.apibanco.model.ContaPoupanca#aplicarRendimento()
 */
@Component
public class RendimentoPoupancaStrategy implements RendimentoStrategy {

    /**
     * Taxa mensal de rendimento (0,5% &mdash; valor didático).
     */
    private static final BigDecimal TAXA_MENSAL = new BigDecimal("0.005");

    /**
     * Construtor público &mdash; instanciado pelo Spring (via
     * {@code @Component}) e pelos testes.
     */
    public RendimentoPoupancaStrategy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TipoConta tipo() {
        return TipoConta.POUPANCA;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delega ao método da entidade
     * {@link com.apibanco.model.ContaPoupanca#aplicarRendimento()}, que
     * aplica o arredondamento {@code HALF_EVEN} e credita no saldo.</p>
     *
     * <p>Retorna {@code null} se o rendimento for zero (não cria transação).</p>
     */
    @Override
    public Transacao aplicar(Conta conta) {
        BigDecimal rendimento = calcular(conta);
        if (rendimento.signum() == 0) {
            return null;
        }
        conta.creditar(rendimento);
        return Transacao.rendimento(conta, rendimento);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calcular(Conta conta) {
        return conta.getSaldo()
                    .multiply(TAXA_MENSAL)
                    .setScale(2, RoundingMode.HALF_EVEN);
    }
}