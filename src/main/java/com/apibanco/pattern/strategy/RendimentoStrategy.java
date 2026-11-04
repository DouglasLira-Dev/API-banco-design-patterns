package com.apibanco.pattern.strategy;

import com.apibanco.model.Conta;
import com.apibanco.model.Transacao;

import java.math.BigDecimal;

/**
 * Contrato das Strategies de <b>rendimento</b>.
 *
 * <p>Aplica o rendimento ao saldo e devolve a {@link Transacao} gerada
 * (tipo {@code RENDIMENTO}) &mdash; o Service é quem <b>persiste</b> a
 * transação no repositório, mantendo a Strategy focada na <b>regra</b>,
 * não na infraestrutura.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     RendimentoPoupancaStrategy
 */
public interface RendimentoStrategy extends OperacaoStrategy {

    /**
     * Aplica o rendimento ao saldo da conta e devolve a transação gerada.
     *
     * <p><b>Efeitos colaterais:</b> o saldo da conta é <b>alterado</b>
     * nesta chamada. Se o rendimento for zero, nada é alterado e a
     * transação não deve ser gerada (retorno {@code null} ou vazio) &mdash;
     * a implementação decide.</p>
     *
     * @param conta conta a receber rendimento; não pode ser {@code null}
     * @return a transação de rendimento gerada, ou {@code null} se o
     *         rendimento foi zero
     */
    Transacao aplicar(Conta conta);

    /**
     * Calcula o valor do rendimento <b>sem</b> aplicar.
     *
     * <p>Útil para exibir "rendimento previsto" na API sem alterar estado.</p>
     *
     * @param conta conta base do cálculo; não pode ser {@code null}
     * @return o valor do rendimento em reais (pode ser {@link BigDecimal#ZERO})
     */
    BigDecimal calcular(Conta conta);
}