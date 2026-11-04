package com.apibanco.model;

/**
 * Enumeração dos tipos de conta suportados pelo banco.
 *
 * <p>Cada constante corresponde a uma subclasse concreta de {@link Conta}
 * criada pela <b>Factory</b> ({@code ContaFactory}) e determina as regras
 * específicas de {@link Conta#calcularTaxaSaque(java.math.BigDecimal)} e
 * {@link Conta#aplicarRendimento()}.</p>
 *
 * <p>É usado também como <b>discriminador</b> de herança no banco
 * ({@code @DiscriminatorColumn(name = "tipo")}), permitindo consultas
 * por tipo sem carregar instâncias.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
public enum TipoConta {

    /**
     * Conta Corrente — tarifa fixa de saque, sem rendimento.
     */
    CORRENTE,

    /**
     * Conta Poupança — sem tarifa de saque, rendimento mensal aplicado.
     */
    POUPANCA,

    /**
     * Conta Empresarial — tarifa de saque diferenciada, sem rendimento.
     */
    EMPRESARIAL
}