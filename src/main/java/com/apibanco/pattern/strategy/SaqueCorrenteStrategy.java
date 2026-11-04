package com.apibanco.pattern.strategy;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.TipoConta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy de <b>saque</b> para {@link ContaCorrente}.
 *
 * <p><b>Regra:</b></p>
 * <ul>
 *   <li>tarifa = <b>1% do valor</b> (configurável via
 *       {@code banco.taxas.saque-corrente});</li>
 *   <li>validação: {@code saldo >= valor + taxa};</li>
 *   <li>execução: debita {@code valor + taxa}.</li>
 * </ul>
 *
 * <p><b>Configuração:</b> o percentual é lido do {@code application.yml}
 * via {@code @Value} &mdash; aqui <b>é o lugar certo</b> para ler config,
 * pois Strategy é um bean Spring (diferente da entidade, que permanece
 * pura).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     SaqueStrategy
 * @see     ContaCorrente#calcularTaxaSaque(BigDecimal, BigDecimal)
 */
@Component
public class SaqueCorrenteStrategy implements SaqueStrategy {

    /**
     * Percentual da tarifa de saque (padrão 1%), lido de
     * {@code banco.taxas.saque-corrente}.
     */
    private final BigDecimal percentualTarifa;

    /**
     * Construtor com injeção do percentual configurado.
     *
     * @param percentualTarifa percentual em {@code [0, 1)}; padrão
     *                         {@code 0.01} se não configurado
     */
    public SaqueCorrenteStrategy(
            @Value("${banco.taxas.saque-corrente:0.01}") BigDecimal percentualTarifa) {
        this.percentualTarifa = percentualTarifa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TipoConta tipo() {
        return TipoConta.CORRENTE;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delega ao método da entidade
     * {@link ContaCorrente#calcularTaxaSaque(BigDecimal, BigDecimal)}, que
     * aplica o arredondamento {@code HALF_EVEN}.</p>
     */
    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        // Delegamos à regra da própria entidade (dono da regra de negócio).
        // Aqui a entidade é instanciada só para acessar o método — se a conta
        // for realmente uma ContaCorrente, esse método é polimórfico.
        return valor.multiply(percentualTarifa)
                    .setScale(2, java.math.RoundingMode.HALF_EVEN);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Regra: {@code saldo >= valor + taxa}. Lança
     * {@link BusinessException} se o saldo for insuficiente.</p>
     */
    @Override
    public void validarSaldoSuficiente(BigDecimal valor, Conta conta) {
        BigDecimal taxa = calcularTaxa(valor);
        BigDecimal totalDebito = valor.add(taxa);
        if (conta.getSaldo().compareTo(totalDebito) < 0) {
            throw new BusinessException(
                "saldo insuficiente para saque: precisa de %s (valor %s + tarifa %s), mas há %s"
                    .formatted(totalDebito, valor, taxa, conta.getSaldo()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal executar(BigDecimal valor, Conta conta) {
        BigDecimal taxa = calcularTaxa(valor);
        BigDecimal totalDebito = valor.add(taxa);
        validarSaldoSuficiente(valor, conta);
        conta.debitar(totalDebito);
        return totalDebito;
    }
}