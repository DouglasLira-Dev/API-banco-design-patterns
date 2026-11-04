package com.apibanco.pattern.strategy;

import com.apibanco.model.TipoConta;

/**
 * <b>Design Pattern: Strategy (Comportamental)</b> &mdash; contrato raiz
 * para as operações variáveis por tipo de conta.
 *
 * <p><b>Por que dividir em sub-interfaces:</b> a assinatura genérica
 * {@code BigDecimal executar(BigDecimal, Conta)} é ambígua (retorna a
 * taxa? o novo saldo? o comprovante?). Para evitar {@code instanceof}
 * no {@code TransacaoService} &mdash; o que <b>aniquila</b> o Strategy &mdash;
 * esta interface é apenas <b>marcadora</b> e define o eixo de
 * variabilidade (o {@link TipoConta}); os contratos específicos vivem em
 * {@link SaqueStrategy} e {@link RendimentoStrategy}.</p>
 *
 * <p><b>Uso no {@code TransacaoService}:</b> injetar
 * {@code List<OperacaoStrategy>} (Spring popula automaticamente) e montar
 * um {@code Map<TipoConta, OperacaoStrategy>}. Quando o Service precisar
 * de um saque, filtra as Strategies de saque pelo tipo da conta; quando
 * precisar de rendimento, filtra as de rendimento. Nenhum {@code if/else}
 * sobre tipo &mdash; o polimorfismo resolve.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     SaqueStrategy
 * @see     RendimentoStrategy
 * @see     com.apibanco.model.TipoConta
 */
public interface OperacaoStrategy {

    /**
     * Tipo de conta ao qual esta Strategy se aplica.
     *
     * <p>Usado pelo Service para montar o mapa de roteamento
     * {@code TipoConta -> Strategy} sem precisar de {@code instanceof}.</p>
     *
     * @return o tipo de conta desta Strategy (nunca {@code null})
     */
    TipoConta tipo();
}