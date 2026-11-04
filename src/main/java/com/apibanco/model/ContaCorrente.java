package com.apibanco.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conta <b>Corrente</b> &mdash; subclasse concreta de {@link Conta}.
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li><b>Tarifa de saque:</b> <b>1% do valor sacado</b> (configurável
 *       via {@code banco.taxas.saque-corrente} em {@code application.yml}
 *       &mdash; o percentual é passado por parâmetro para a entidade, que
 *       permanece pura e sem dependência do Spring);</li>
 *   <li><b>Rendimento:</b> <b>não há</b> &mdash; {@link #aplicarRendimento()}
 *       é no-op (conta corrente não rende juros).</li>
 * </ul>
 *
 * <p><b>Sobre o percentual configurável:</b> a entidade recebe o percentual
 * como parâmetro em {@link #calcularTaxaSaque(BigDecimal, BigDecimal)}.
 * A camada de serviço é quem lê {@code banco.taxas.saque-corrente} via
 * {@code @Value} e repassa. Isso mantém a entidade JPA testável sem Spring
 * e sem depender de {@code Environment}.</p>
 *
 * <p><b>Arredondamento:</b> {@link RoundingMode#HALF_EVEN} &mdash; convenção
 * bancária (Banker's Rounding).</p>
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
     * Percentual <b>padrão</b> de tarifa de saque (1%) &mdash; usado quando
     * a camada de serviço não informa um valor configurado.
     *
     * <p>Serve como fallback seguro e como referência legível. O valor
     * efetivo vem de {@code banco.taxas.saque-corrente} no
     * {@code application.yml}.</p>
     *
     * <p><b>Nota:</b> é {@code BigDecimal}, então <b>não</b> pode ser usado
     * com <code>&#123;@value&#125;</code> (que só aceita primitivos/String).</p>
     */
    public static final BigDecimal PERCENTUAL_TARIFA_SAQUE_PADRAO = new BigDecimal("0.01");

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
     * Calcula a tarifa de saque aplicando o percentual <b>padrão</b>
     * ({@link #PERCENTUAL_TARIFA_SAQUE_PADRAO}, ou seja, 1%).
     *
     * <p>Atalho para {@link #calcularTaxaSaque(BigDecimal, BigDecimal)} com
     * o percentual padrão. Útil em testes e em contextos onde não há
     * configuração injetada.</p>
     *
     * @param valor valor do saque; deve ser positivo
     * @return a tarifa calculada (1% do valor, arredondada HALF_EVEN)
     */
    @Override
    public BigDecimal calcularTaxaSaque(BigDecimal valor) {
        return calcularTaxaSaque(valor, PERCENTUAL_TARIFA_SAQUE_PADRAO);
    }

    /**
     * Calcula a tarifa de saque aplicando o percentual <b>informado</b>.
     *
     * <p>O percentual vem da configuração ({@code banco.taxas.saque-corrente})
     * e é repassado pela camada de serviço &mdash; a entidade não conhece
     * Spring nem {@code Environment}.</p>
     *
     * @param valor      valor do saque; deve ser positivo
     * @param percentual percentual da tarifa (ex.: {@code 0.01} para 1%);
     *                   deve ser ≥ 0 e &lt; 1
     * @return a tarifa calculada ({@code valor * percentual}, arredondada HALF_EVEN)
     * @throws IllegalArgumentException se {@code valor} ou {@code percentual}
     *         estiverem fora dos limites
     */
    public BigDecimal calcularTaxaSaque(BigDecimal valor, BigDecimal percentual) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor do saque deve ser positivo");
        }
        if (percentual == null || percentual.signum() < 0 || percentual.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("percentual da tarifa deve estar em [0, 1)");
        }
        return valor.multiply(percentual).setScale(2, RoundingMode.HALF_EVEN);
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