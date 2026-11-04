package com.apibanco.pattern.strategy;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy de <b>saque</b> para {@link com.apibanco.model.ContaPoupanca}.
 *
 * <p><b>Regra:</b></p>
 * <ul>
 *   <li>tarifa = <b>zero</b> (poupança é isenta);</li>
 *   <li>validação: {@code saldo >= valor};</li>
 *   <li>execução: debita apenas {@code valor}.</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     SaqueStrategy
 */
@Component
public class SaquePoupancaStrategy implements SaqueStrategy {

    /**
     * Construtor público &mdash; instanciado pelo Spring (via
     * {@code @Component}) e pelos testes.
     */
    public SaquePoupancaStrategy() {
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
     * <p>Poupança é isenta &mdash; retorna {@link BigDecimal#ZERO}.</p>
     */
    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        return BigDecimal.ZERO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Regra: {@code saldo >= valor}. Lança {@link BusinessException}
     * se o saldo for insuficiente.</p>
     */
    @Override
    public void validarSaldoSuficiente(BigDecimal valor, Conta conta) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new BusinessException(
                "saldo insuficiente para saque: precisa de %s, mas há %s"
                    .formatted(valor, conta.getSaldo()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal executar(BigDecimal valor, Conta conta) {
        validarSaldoSuficiente(valor, conta);
        conta.debitar(valor);
        return valor;
    }
}