package com.apibanco.pattern.strategy;

import com.apibanco.model.Conta;

import java.math.BigDecimal;

/**
 * Contrato das Strategies de <b>saque</b>.
 *
 * <p>Um saque sempre tem 3 passos, na ordem:</p>
 * <ol>
 *   <li>{@link #calcularTaxa(BigDecimal)} &mdash; obter a tarifa;</li>
 *   <li>{@link #validarSaldoSuficiente(BigDecimal, Conta)} &mdash; garantir
 *       {@code saldo >= valor + taxa};</li>
 *   <li>{@link #executar(BigDecimal, Conta)} &mdash; debitar
 *       {@code valor + taxa} do saldo.</li>
 * </ol>
 *
 * <p><b>Nota sobre a interface:</b> o Service pode chamar
 * {@code executar} direto (que internamente valida) ou chamar
 * {@code validarSaldoSuficiente} antes (fail-fast, feedback cedo).
 * As implementações <b>nunca</b> deixam o saldo negativo.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     SaqueCorrenteStrategy
 * @see     SaquePoupancaStrategy
 */
public interface SaqueStrategy extends OperacaoStrategy {

    /**
     * Calcula a tarifa aplicável a um saque.
     *
     * @param valor valor do saque (deve ser positivo)
     * @return a tarifa em reais (nunca {@code null}; pode ser {@link BigDecimal#ZERO})
     */
    BigDecimal calcularTaxa(BigDecimal valor);

    /**
     * Valida se a conta tem saldo suficiente para o saque <b>incluindo a tarifa</b>.
     *
     * <p>Fórmula da regra: {@code saldo >= valor + calcularTaxa(valor)}.</p>
     *
     * @param valor valor do saque
     * @param conta conta debitada
     * @throws com.apibanco.exception.BusinessException se o saldo for insuficiente
     */
    void validarSaldoSuficiente(BigDecimal valor, Conta conta);

    /**
     * Executa o saque: valida o saldo e debita {@code valor + taxa}.
     *
     * @param valor valor do saque; deve ser positivo
     * @param conta conta debitada; deve existir e ter saldo suficiente
     * @return o valor efetivamente debitado ({@code valor + taxa})
     * @throws com.apibanco.exception.BusinessException se o saldo for insuficiente
     */
    BigDecimal executar(BigDecimal valor, Conta conta);
}