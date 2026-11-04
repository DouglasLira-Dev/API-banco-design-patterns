package com.apibanco.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade JPA que registra uma <b>movimentação financeira</b>.
 *
 * <p>Toda operação (depósito, saque, transferência, PIX, TED) gera uma
 * linha nesta tabela — é o <b>livro-razão</b> do sistema. As regras de
 * <i>quando</i> e <i>como</i> criar cada transação ficam nas Strategies
 * do módulo de serviço; aqui guardamos o <b>fato</b> já ocorrido.</p>
 *
 * <p><b>Origem e destino são opcionais individualmente, mas nunca ambos
 * nulos:</b></p>
 * <ul>
 *   <li>{@link TipoTransacao#DEPOSITO} &mdash; apenas {@link #contaDestino};</li>
 *   <li>{@link TipoTransacao#SAQUE} &mdash; apenas {@link #contaOrigem};</li>
 *   <li>{@link TipoTransacao#TRANSFERENCIA} &mdash; ambos ({@link #contaOrigem} e {@link #contaDestino});</li>
 *   <li>{@link TipoTransacao#RENDIMENTO} &mdash; apenas {@link #contaDestino}.</li>
 * </ul>
 *
 * <p>A invariante é verificada em {@link #prePersist()} — se alguém
 * tentar persistir uma transação sem conta de origem <b>e</b> sem conta
 * de destino, uma {@link IllegalStateException} é lançada antes do INSERT.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Conta
 * @see     TipoTransacao
 */
@Entity
@Table(name = "transacao")
public class Transacao {

    /**
     * Identificador técnico gerado pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo da operação. Persistido como texto para legibilidade em logs e SQL.
     */
    @NotNull(message = "tipo da transação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTransacao tipo;

    /**
     * Valor movimentado, em reais (escala 2). Deve ser estritamente positivo.
     */
    @NotNull(message = "valor é obrigatório")
    @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    /**
     * Data e hora em que a transação foi registrada.
     * <p>Preenchida automaticamente em {@link #prePersist()} se não vier
     * do chamador — garante que toda linha tenha timestamp.</p>
     */
    @NotNull(message = "data/hora é obrigatória")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /**
     * Conta de origem ({@code N:1}). Nula em depósitos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id")
    private Conta contaOrigem;

    /**
     * Conta de destino ({@code N:1}). Nula em saques.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id")
    private Conta contaDestino;

    // Construtores

    /**
     * Construtor vazio exigido pelo JPA.
     * <p>Não deve ser usado pelo código de negócio — use um dos
     * construtores estáticos abaixo.</p>
     */
    protected Transacao() {
        // exigido pelo JPA
    }

    /**
     * Construtor completo (para uso interno / testes).
     *
     * @param tipo          tipo da transação
     * @param valor         valor movimentado; deve ser positivo
     * @param contaOrigem   conta de origem (pode ser {@code null} em depósito)
     * @param contaDestino  conta de destino (pode ser {@code null} em saque)
     * @param dataHora      instante da transação (pode ser {@code null} — será preenchido no persist)
     */
    public Transacao(TipoTransacao tipo,
                    BigDecimal valor,
                    Conta contaOrigem,
                    Conta contaDestino,
                    LocalDateTime dataHora) {
        this.tipo = tipo;
        this.valor = valor;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.dataHora = dataHora;
    }

    // Fábricas estáticas (intenção explícita por tipo de operação)

    /**
     * Cria uma transação de <b>depósito</b> (apenas conta de destino).
     *
     * @param contaDestino conta que recebeu o valor; não pode ser {@code null}
     * @param valor        valor depositado; deve ser positivo
     * @return a transação pronta para persistir
     */
    public static Transacao deposito(Conta contaDestino, BigDecimal valor) {
        Objects.requireNonNull(contaDestino, "contaDestino não pode ser nula em depósito");
        return new Transacao(TipoTransacao.DEPOSITO, valor, null, contaDestino, LocalDateTime.now());
    }

    /**
     * Cria uma transação de <b>saque</b> (apenas conta de origem).
     *
     * @param contaOrigem conta que sofreu o débito; não pode ser {@code null}
     * @param valor       valor sacado; deve ser positivo
     * @return a transação pronta para persistir
     */
    public static Transacao saque(Conta contaOrigem, BigDecimal valor) {
        Objects.requireNonNull(contaOrigem, "contaOrigem não pode ser nula em saque");
        return new Transacao(TipoTransacao.SAQUE, valor, contaOrigem, null, LocalDateTime.now());
    }

    /**
     * Cria uma transação de <b>transferência</b> (origem + destino).
     *
     * @param origem  conta debitada; não pode ser {@code null}
     * @param destino conta creditada; não pode ser {@code null}
     * @param valor   valor transferido; deve ser positivo
     * @return a transação pronta para persistir
     */
    public static Transacao transferencia(Conta origem, Conta destino, BigDecimal valor) {
        Objects.requireNonNull(origem,  "contaOrigem não pode ser nula em transferência");
        Objects.requireNonNull(destino, "contaDestino não pode ser nula em transferência");
        return new Transacao(TipoTransacao.TRANSFERENCIA, valor, origem, destino, LocalDateTime.now());
    }

    /**
     * Cria uma transação de <b>rendimento</b> (apenas conta de destino).
     *
     * <p>Usada quando {@link Conta#aplicarRendimento()} credita juros/rendimento
     * em uma conta (ex.: poupança). O valor é o rendimento aplicado, não o
     * saldo resultante.</p>
     *
     * @param contaDestino conta que recebeu o rendimento; não pode ser {@code null}
     * @param valor        valor do rendimento creditado; deve ser positivo
     * @return a transação pronta para persistir
     */
    public static Transacao rendimento(Conta contaDestino, BigDecimal valor) {
        Objects.requireNonNull(contaDestino, "contaDestino não pode ser nula em rendimento");
        return new Transacao(TipoTransacao.RENDIMENTO, valor, null, contaDestino, LocalDateTime.now());
    }

    // Hook JPA

    /**
     * Valida invariantes e preenche {@link #dataHora} antes do INSERT.
     *
     * @throws IllegalStateException se a transação estiver sem conta de
     *         origem <b>e</b> sem conta de destino
     */
    @PrePersist
    private void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
        if (this.contaOrigem == null && this.contaDestino == null) {
            throw new IllegalStateException(
                "transação exige ao menos conta de origem ou conta de destino");
        }
    }

    // Getters (sem setters — transação é imutável após registro)

    /** @return o id gerado pelo banco, ou {@code null} antes de persistir */
    public Long getId() {
        return id;
    }

    /** @return o tipo da transação */
    public TipoTransacao getTipo() {
        return tipo;
    }

    /** @return o valor movimentado (sempre positivo) */
    public BigDecimal getValor() {
        return valor;
    }

    /** @return o instante em que a transação foi registrada */
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /** @return a conta debitada (nula em depósito) */
    public Conta getContaOrigem() {
        return contaOrigem;
    }

    /** @return a conta creditada (nula em saque) */
    public Conta getContaDestino() {
        return contaDestino;
    }

    // equals / hashCode / toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transacao other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representação textual para logs — inclui tipo, valor e IDs das contas
     * (não carrega as contas para evitar lazy leak).
     */
    @Override
    public String toString() {
        Long origemId  = contaOrigem  != null ? contaOrigem.getId()  : null;
        Long destinoId = contaDestino != null ? contaDestino.getId() : null;
        return "Transacao{id=%d, tipo=%s, valor=%s, origem=%s, destino=%s, dataHora=%s}"
            .formatted(id, tipo, valor, origemId, destinoId, dataHora);
    }
}