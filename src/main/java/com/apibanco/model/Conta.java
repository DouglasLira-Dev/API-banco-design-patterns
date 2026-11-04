package com.apibanco.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade JPA <b>abstrata</b> que representa uma conta bancária.
 *
 * <p>Serve de base para as subclasses concretas criadas pela
 * {@code ContaFactory} (Módulo de Factory) — <b>não deve ser instanciada
 * diretamente</b>. Cada subtipo implementa:</p>
 * <ul>
 *   <li>{@link #calcularTaxaSaque(BigDecimal)} — regra de tarifa de saque;</li>
 *   <li>{@link #aplicarRendimento()} — regra de rendimento mensal.</li>
 * </ul>
 *
 * <p><b>Estratégia de herança:</b> {@link InheritanceType#SINGLE_TABLE} —
 * todas as subclasses vivem em uma única tabela {@code conta}, com uma
 * coluna discriminadora {@code tipo}. Vantagem: consultas sem JOIN e
 * polimorfismo trivial. Desvantagem: colunas específicas de subtipos
 * ficam {@code NULL} quando não se aplicam (aceitável neste domínio,
 * com poucas subclasses).</p>
 *
 * <p><b>Relacionamentos:</b></p>
 * <ul>
 *   <li>{@code N:1} com {@link Cliente} — lado dono da FK {@code cliente_id};</li>
 *   <li>{@code 1:N} com {@link Transacao} — histórico de movimentações.</li>
 * </ul>
 *
 * <p><b>Invariantes de negócio:</b></p>
 * <ul>
 *   <li>{@code numero} é único e imutável após persistência;</li>
 *   <li>{@code saldo} nunca pode ser negativo — protegido em
 *       {@link #debitar(BigDecimal)};</li>
  *  <li>{@code tipo} é coerente com a subclasse concreta (garantido pela
 *       Factory e por {@link #prePersist()} no {@code @PrePersist}).</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Cliente
 * @see     Transacao
 * @see     TipoConta
 */
@Entity
@Table(name = "conta")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Conta {

    /**
     * Identificador técnico gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número da conta (ex.: {@code "0001-12345-6"}). Único no banco.
     *
     * <p><b>Não deve ser setado manualmente pelo código de negócio</b> —
     * é atribuído no {@link #prePersist()} a partir de uma estratégia
     * injetada na Factory, mantendo a {@code Conta} agnóstica ao formato.</p>
     */
    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    /**
     * Saldo atual da conta, em reais ({@link BigDecimal}, escala 2).
     *
     * <p>Nunca é atualizado diretamente — use {@link #creditar(BigDecimal)}
     * e {@link #debitar(BigDecimal)} para preservar a invariante
     * {@code saldo >= 0}.</p>
     */
    @NotNull(message = "saldo é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "saldo não pode ser negativo")
    @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    /**
     * Tipo da conta. Também é o <b>discriminador</b> persistido na coluna
     * {@code tipo} (herança SINGLE_TABLE).
     *
     * <p>É preenchido automaticamente por {@link #preencherTipo()} a partir
     * de {@link #tipoEsperado()} — nenhuma subclasse deve depender de
     * setá-lo manualmente.</p>
     */
    @NotNull(message = "tipo da conta é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, insertable = false, updatable = false, length = 20)
    private TipoConta tipo;

    /**
     * Cliente dono da conta ({@code N:1}).
     *
     * <p>Lado dono da foreign key {@code cliente_id}. {@code LAZY} evita
     * carregar o cliente ao listar contas.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Transações em que esta conta é a <b>origem</b> ({@code 1:N}).
     *
     * <p>{@code mappedBy = "contaOrigem"} — o lado dono está em
     * {@link Transacao#getContaOrigem()}. Lazy para não inflar consultas
     * de conta sem necessidade.</p>
     */
    @OneToMany(mappedBy = "contaOrigem", fetch = FetchType.LAZY)
    private List<Transacao> transacoesComoOrigem = new ArrayList<>();

    /**
     * Transações em que esta conta é o <b>destino</b> ({@code 1:N}).
     *
     * <p>{@code mappedBy = "contaDestino"} — o lado dono está em
     * {@link Transacao#getContaDestino()}.</p>
     */
    @OneToMany(mappedBy = "contaDestino", fetch = FetchType.LAZY)
    private List<Transacao> transacoesComoDestino = new ArrayList<>();

    // Construtores

    /**
     * Construtor vazio exigido pelo JPA.
     * <p>Não deve ser chamado pelo código de negócio — instâncias de
     * {@code Conta} só devem nascer pela {@code ContaFactory}.</p>
     */
    protected Conta() {
        // exigido pelo JPA
    }

    /**
     * Construtor de negócio usado pelas subclasses concretas.
     *
     * @param cliente dono da conta; não pode ser {@code null}
     */
    protected Conta(Cliente cliente) {
        Objects.requireNonNull(cliente, "cliente não pode ser nulo");
        this.cliente = cliente;
        this.saldo = BigDecimal.ZERO;
    }

    // Contrato polimórfico (implementado pelas subclasses concretas)

    /**
     * Calcula a tarifa aplicável a um saque desta conta.
     *
     * <p>Exemplos de regra por subtipo:</p>
     * <ul>
     *   <li>Conta Corrente: tarifa fixa;</li>
     *   <li>Conta Poupança: isenta;</li>
     *   <li>Conta Empresarial: percentual sobre o valor.</li>
     * </ul>
     *
     * @param valor valor do saque, em reais; deve ser positivo
     * @return a tarifa em reais (nunca {@code null}, pode ser {@link BigDecimal#ZERO})
     */
    public abstract BigDecimal calcularTaxaSaque(BigDecimal valor);

    /**
     * Aplica o rendimento mensal ao saldo desta conta.
     *
     * <p>Tipos sem rendimento (ex.: conta corrente) podem implementar
     * como <b>no-op</b> — a assinatura permanece para uniformidade.</p>
     */
    public abstract void aplicarRendimento();

    /**
     * Informa o {@link TipoConta} esperado pela subclasse concreta.
     *
     * <p>Usado por {@link #prePersist()} no {@code @PrePersist} para
     * garantir que a coluna discriminadora esteja coerente com a classe
     * Java, sem depender de o desenvolvedor "lembrar" de setar o campo.</p>
     *
     * @return o tipo correspondente à subclasse
     */
    public abstract TipoConta tipoEsperado();

    // Hooks de ciclo de vida JPA

    /**
     * Preenche o campo {@link #tipo} a partir de {@link #tipoEsperado()}
     * antes do INSERT.
     *
     * <p>Redundante em relação ao {@code @DiscriminatorColumn} (que já
     * grava o discriminador), mas mantém o campo Java coerente em memória
     * — útil para consultas por tipo em código Java antes do flush.</p>
     */
    @PrePersist
    private void prePersist() {
        if (this.tipo == null) {
            this.tipo = tipoEsperado();
        }
    }

    // Operações de saldo (invariantes protegidas aqui)

    /**
     * Credita um valor no saldo.
     *
     * @param valor valor em reais; deve ser positivo
     * @throws IllegalArgumentException se {@code valor} for nulo, zero ou negativo
     */
    public void creditar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor de crédito deve ser positivo");
        }
        this.saldo = this.saldo.add(valor);
    }

    /**
     * Debita um valor do saldo, respeitando a invariante {@code saldo >= 0}.
     *
     * @param valor valor em reais; deve ser positivo
     * @throws IllegalArgumentException se {@code valor} for nulo, zero ou negativo
     * @throws IllegalStateException    se o saldo atual for insuficiente
     */
    public void debitar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor de débito deve ser positivo");
        }
        if (this.saldo.compareTo(valor) < 0) {
            throw new IllegalStateException("saldo insuficiente");
        }
        this.saldo = this.saldo.subtract(valor);
    }

    // Getters

    /** @return o id gerado pelo banco, ou {@code null} antes de persistir */
    public Long getId() {
        return id;
    }

    /** @return o número da conta (único, imutável após persistência) */
    public String getNumero() {
        return numero;
    }

    /** @return o saldo atual (nunca {@code null}; nunca negativo) */
    public BigDecimal getSaldo() {
        return saldo;
    }

    /** @return o tipo da conta, coerente com a subclasse concreta */
    public TipoConta getTipo() {
        return tipo;
    }

    /** @return o cliente dono da conta */
    public Cliente getCliente() {
        return cliente;
    }

    /** @return a lista (lazy) de transações em que esta conta é a origem */
    public List<Transacao> getTransacoesComoOrigem() {
        return transacoesComoOrigem;
    }

    /** @return a lista (lazy) de transações em que esta conta é o destino */
    public List<Transacao> getTransacoesComoDestino() {
        return transacoesComoDestino;
    }

    // Setters controlados

    /**
     * Define o número da conta. Chamado <b>somente</b> pela camada de
     * criação (Factory/Service) — não expor a controllers.
     *
     * @param numero número único da conta
     */
    public void setNumero(String numero) {
        Objects.requireNonNull(numero, "numero não pode ser nulo");
        this.numero = numero;
    }

    /**
     * Define o cliente dono. Usado pelos helpers de {@link Cliente}
     * ({@link Cliente#adicionarConta(Conta)}) para manter os dois lados
     * da relação sincronizados.
     *
     * @param cliente dono da conta
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // setId / setSaldo / setTipo intencionalmente ausentes:
    //  - id        → gerenciado pelo banco
    //  - saldo     → só via creditar()/debitar()
    //  - tipo      → só via tipoEsperado() + @PrePersist

    // equals / hashCode / toString

    /**
     * Igualdade por {@code numero} (chave natural) quando presente,
     * e por {@code id} quando persistida.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta other)) return false;
        // Compara também o subtipo: ContaCorrente != ContaPoupanca mesmo com numero igual
        if (getClass() != other.getClass()) return false;
        if (this.numero != null && this.numero.equals(other.numero)) return true;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), numero != null ? numero : id);
    }

    /**
     * Representação textual segura para logs — inclui número e saldo.
     */
    @Override
    public String toString() {
        return "%s{id=%d, numero='%s', saldo=%s}".formatted(
            getClass().getSimpleName(), id, numero, saldo);
    }
}