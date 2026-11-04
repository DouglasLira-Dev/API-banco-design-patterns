package com.apibanco.pattern.builder;

import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <b>Design Pattern: Builder (Criacional)</b> &mdash; base abstrata para
 * construção fluente e validada de {@link Conta} e suas subclasses.
 *
 * <p><b>Justificativa:</b> {@code Conta} possui múltiplos atributos
 * (número, saldo inicial, cliente, tipo) e regras de criação. O Builder:</p>
 * <ul>
 *   <li>evita <b>construtores telescópicos</b> com N parâmetros posicionais
 *       difíceis de ler e fáceis de errar;</li>
 *   <li><b>centraliza as validações</b> de criação em um único ponto
 *       (número não vazio, saldo ≥ 0, cliente obrigatório, coerência de tipo);</li>
 *   <li>torna o código cliente <b>autoexplicativo</b>:
 *       {@code new ContaCorrenteBuilder().comNumero("...").comSaldoInicial(...).comCliente(c).build()}.</li>
 * </ul>
 *
 * <p><b>Como usar (self-type):</b> a subclasse concreta declara seu próprio
 * tipo genérico ({@code class ContaCorrenteBuilder extends ContaBuilder<ContaCorrenteBuilder>}),
 * permitindo que a cadeia {@code comX(...).comY(...).build()} preserve o
 * tipo concreto em todas as chamadas.</p>
 *
 * <p><b>Fluxo típico:</b></p>
 * <pre>{@code
 *   Conta conta = new ContaCorrenteBuilder()
 *       .comNumero("0001-12345-6")
 *       .comSaldoInicial(new BigDecimal("500.00"))
 *       .comCliente(cliente)
 *       .build();
 * }</pre>
 *
 * <p><b>Estado interno:</b> o builder guarda os valores até {@code build()}.
 * O {@link #build()} é a barreira final &mdash; instâncias de {@link Conta}
 * só devem nascer ali (ou pela Factory do próximo módulo).</p>
 *
 * @param <T> tipo concreto do builder (self-type) &mdash; garante que os
 *            métodos fluentes retornem o subtipo certo
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaCorrenteBuilder
 * @see     ContaPoupancaBuilder
 */
public abstract class ContaBuilder<T extends ContaBuilder<T>> {

    /**
     * Número da conta. Validado em {@link #validar()} &mdash; não pode ser nulo/vazio.
     */
    protected String numero;

    /**
     * Saldo inicial da conta. Validado em {@link #validar()} &mdash; deve ser ≥ 0.
     * Padrão: {@link BigDecimal#ZERO} caso não seja informado.
     */
    protected BigDecimal saldoInicial = BigDecimal.ZERO;

    /**
     * Dono da conta. Validado em {@link #validar()} &mdash; não pode ser nulo.
     */
    protected Cliente cliente;

    /**
     * Tipo informado via {@link #comTipo(TipoConta)}. Opcional &mdash; se
     * ausente, o subtipo concreto do builder determina o tipo esperado.
     */
    protected TipoConta tipo;

    /**
     * Retorna {@code this} como o tipo concreto do builder &mdash; truque
     * padrão do self-type.
     *
     * @return a instância tipada como {@code T}
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Define o número da conta.
     *
     * @param numero número único; não pode ser nulo nem vazio
     * @return o próprio builder (para encadear chamadas)
     */
    public T comNumero(String numero) {
        this.numero = numero;
        return self();
    }

    /**
     * Define o saldo inicial da conta.
     *
     * @param saldoInicial valor inicial; deve ser ≥ 0
     * @return o próprio builder (para encadear chamadas)
     */
    public T comSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
        return self();
    }

    /**
     * Define o tipo da conta.
     *
     * <p>Opcional. Se informado, deve ser <b>coerente</b> com o subtipo do
     * builder &mdash; o {@link #validar()} verifica. Se não informado, o
     * subtipo determina o tipo naturalmente.</p>
     *
     * @param tipo tipo de conta desejado
     * @return o próprio builder (para encadear chamadas)
     */
    public T comTipo(TipoConta tipo) {
        this.tipo = tipo;
        return self();
    }

    /**
     * Define o dono da conta.
     *
     * @param cliente cliente dono; não pode ser nulo
     * @return o próprio builder (para encadear chamadas)
     */
    public T comCliente(Cliente cliente) {
        this.cliente = cliente;
        return self();
    }

    /**
     * Constrói a conta após validar as regras de criação.
     *
     * <p><b>Regras validadas:</b></p>
     * <ol>
     *   <li>cliente obrigatório (não nulo);</li>
     *   <li>número não nulo nem vazio;</li>
     *   <li>saldo inicial ≥ 0;</li>
     *   <li>tipo (se informado) coerente com o subtipo do builder.</li>
     * </ol>
     *
     * @return a conta construída, pronta para persistir
     * @throws IllegalStateException se alguma regra de criação for violada
     */
    public Conta build() {
        validar();
        return construir();
    }

    /**
     * Fábrica interna chamada após {@link #validar()}.
     *
     * @return a instância concreta de {@link Conta}
     */
    protected abstract Conta construir();

    /**
     * Retorna o {@link TipoConta} esperado pelo subtipo concreto.
     *
     * @return tipo esperado
     */
    protected abstract TipoConta tipoEsperado();

    /**
     * Valida as invariantes de criação. Chamado automaticamente por
     * {@link #build()} &mdash; não deve ser chamado manualmente.
     *
     * @throws IllegalStateException se alguma regra for violada
     */
    protected void validar() {
        if (cliente == null) {
            throw new IllegalStateException("cliente é obrigatório para construir uma conta");
        }
        if (numero == null || numero.isBlank()) {
            throw new IllegalStateException("numero da conta é obrigatório e não pode ser vazio");
        }
        if (saldoInicial == null) {
            throw new IllegalStateException("saldo inicial é obrigatório (use 0 para conta zerada)");
        }
        if (saldoInicial.signum() < 0) {
            throw new IllegalStateException("saldo inicial não pode ser negativo");
        }
        if (tipo != null && tipo != tipoEsperado()) {
            throw new IllegalStateException(
                "tipo informado (" + tipo + ") é incompatível com o builder " +
                "que constrói " + tipoEsperado());
        }
        // Nota: Objects.requireNonNull garante que Cliente/Conta não são nulos.
        Objects.requireNonNull(cliente, "cliente não pode ser nulo");
    }
}