package com.apibanco.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade JPA que representa um <b>cliente</b> do banco.
 *
 * <p>Um cliente pode possuir várias contas ({@link Conta}) — relação
 * {@code 1:N} mapeada por {@link #contas}. As invariantes de unicidade
 * de {@code cpf} e {@code email} são garantidas <b>no banco</b> via
 * {@code UNIQUE constraints} (geradas por {@code @Column(unique = true)})
 * e reforçadas em memória por {@link #equals(Object)}/{@link #hashCode()}.</p>
 *
 * <p><b>Regras de negócio refletidas aqui:</b></p>
 * <ul>
 *   <li>{@code nome} — obrigatório, não em branco, até 120 caracteres;</li>
 *   <li>{@code cpf}  — obrigatório, exatamente 11 dígitos numéricos, único.
 *       A validação do dígito verificador (algoritmo do CPF) fica no
 *       {@code ClienteService}, não na entidade;</li>
 *   <li>{@code email} — obrigatório, formato de e-mail válido, único.</li>
 * </ul>
 *
 * <p><b>Tabela gerada:</b> {@code cliente} (via {@code @Table}).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Conta
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    /**
     * Identificador técnico gerado pelo banco ({@code IDENTITY}).
     * Não representa o cliente para o negócio — para isso existe {@link #cpf}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do cliente.
     * <p>Obrigatório, não pode ser vazio/só espaços. Limite de 120
     * caracteres para caber confortavelmente no {@code VARCHAR} da tabela.</p>
     */
    @NotBlank(message = "nome é obrigatório")
    @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    /**
     * CPF do cliente, armazenado <b>apenas com os 11 dígitos</b>
     * (sem pontuação). Único no banco.
     * <p>A validação do dígito verificador é responsabilidade do
     * {@code ClienteService} — aqui garantimos apenas o formato.</p>
     */
    @NotBlank(message = "cpf é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "cpf deve conter exatamente 11 dígitos numéricos")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    /**
     * E-mail do cliente. Único no banco.
     * <p>Usado como canal de contato e (futuramente) como login.</p>
     */
    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    @Size(max = 180, message = "email deve ter no máximo 180 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    /**
     * Contas pertencentes a este cliente.
     *
     * <p>{@code mappedBy = "cliente"} indica que o lado "dono" da relação
     * é o campo {@code cliente} na entidade {@link Conta} — ou seja, a
     * foreign key {@code cliente_id} vive na tabela {@code conta}, não aqui.</p>
     *
     * <p>{@code cascade = CascadeType.ALL} + {@code orphanRemoval = true}:
     * ao persistir/remover o cliente, as contas o acompanham. Cuidado em
     * produção: numa conta bancária normalmente você <b>não</b> quer apagar
     * histórico — aqui mantemos o cascade por simplicidade didática e
     * porque a spec pediu {@code @OneToMany} simples.</p>
     *
     * <p>{@code fetch = FetchType.LAZY}: contas só são carregadas se
     * realmente acessadas — evita N+1 acidental em listagens de clientes.</p>
     */
    @OneToMany(
        mappedBy = "cliente",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Conta> contas = new ArrayList<>();

    /**
     * Construtor vazio exigido pelo JPA.
     * <p>Não deve ser usado pelo código de negócio — use
     * {@link #Cliente(String, String, String)}.</p>
     */
    protected Cliente() {
        // exigido pelo JPA
    }

    /**
     * Construtor de negócio para criação de um novo cliente.
     *
     * @param nome  nome completo; não pode ser {@code null} nem vazio
     * @param cpf   CPF com 11 dígitos (somente números)
     * @param email e-mail válido do cliente
     */
    public Cliente(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    // Getters

    /**
     * Retorna o identificador técnico do cliente.
     *
     * @return o id gerado pelo banco, ou {@code null} antes de persistir
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o nome completo do cliente.
     *
     * @return o nome completo do cliente
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o CPF (11 dígitos, sem pontuação).
     *
     * @return o CPF (11 dígitos, sem pontuação)
     */
    public String getCpf() {
        return cpf;
    }

    /**
     * Retorna o e-mail do cliente.
     *
     * @return o e-mail do cliente
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retorna a lista de contas do cliente.
     *
     * @return a lista mutável de contas do cliente (nunca {@code null})
     */
    public List<Conta> getContas() {
        return contas;
    }

    // Setters — apenas para campos mutáveis pelo negócio

    /**
     * Atualiza o nome do cliente.
     *
     * @param nome novo nome; não pode ser {@code null} nem vazio
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Atualiza o e-mail do cliente.
     *
     * @param email novo e-mail; deve ser válido e único
     */
    public void setEmail(String email) {
        this.email = email;
    }

    // Obs.: não há setCpf() — CPF é imutável após a criação (regra de negócio).

    // Helpers de relacionamento

    /**
     * Adiciona uma conta ao cliente mantendo os <b>dois lados</b> da relação
     * sincronizados (evita bugs clássicos de JPA em que só um lado é atualizado).
     *
     * @param conta conta a ser vinculada; não pode ser {@code null}
     */
    public void adicionarConta(Conta conta) {
        Objects.requireNonNull(conta, "conta não pode ser nula");
        contas.add(conta);
        conta.setCliente(this);
    }

    /**
     * Remove uma conta do cliente, mantendo os dois lados sincronizados.
     *
     * @param conta conta a ser desvinculada; não pode ser {@code null}
     */
    public void removerConta(Conta conta) {
        Objects.requireNonNull(conta, "conta não pode ser nula");
        contas.remove(conta);
        conta.setCliente(null);
    }

    // equals / hashCode / toString

    /**
     * Igualdade baseada em {@code cpf} (chave natural) quando presente,
     * e em {@code id} quando ambos são persistentes.
     * <p>CPF é imutável e único no banco — bom candidato a chave de igualdade
     * antes mesmo do {@code id} existir.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente other)) return false;
        if (this.cpf != null && this.cpf.equals(other.cpf)) return true;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf != null ? cpf : id);
    }

    /**
     * Representação textual segura — <b>omite o CPF</b> para não vazar PII em logs.
     */
    @Override
    public String toString() {
        return "Cliente{id=%d, nome='%s', email='%s'}".formatted(id, nome, email);
    }
}