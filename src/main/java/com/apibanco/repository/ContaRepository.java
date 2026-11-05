package com.apibanco.repository;

import com.apibanco.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Conta}.
 *
 * <p>Fornece o CRUD padrão herdado de {@link JpaRepository} e adiciona as
 * duas consultas derivadas exigidas pelo domínio bancário:</p>
 * <ul>
 *   <li>{@link #findByNumero(String)} &mdash; número da conta é a chave
 *       natural exposta ao cliente (único no banco);</li>
 *   <li>{@link #findByClienteId(Long)} &mdash; lista as contas de um cliente,
 *       filtrando pela FK {@code cliente_id} sem carregar o cliente.</li>
 * </ul>
 *
 * <p><b>Sobre herança:</b> {@link Conta} é <b>abstrata</b> com
 * {@code @Inheritance(SINGLE_TABLE)} e discriminador {@code tipo}. As queries
 * acima operam sobre <b>todas</b> as subclasses concretas
 * ({@code ContaCorrente}, {@code ContaPoupanca}, {@code ContaEmpresarial}) &mdash;
 * o polimorfismo do JPA devolve a instância correta da subclasse com base
 * na coluna discriminadora.</p>
 *
 * <p><b>Unicidade de {@code numero} &mdash; duas camadas de defesa:</b></p>
 * <ol>
 *   <li><b>Camada de serviço/Factory:</b> verifica antes de inserir
 *       (via {@link #findByNumero(String)}), retornando erro de negócio
 *       amigável em caso de colisão;</li>
 *   <li><b>Camada de banco:</b> a constraint {@code UNIQUE} na coluna
 *       {@code numero} (criada por {@code @Column(unique = true)} em
 *       {@link Conta}) impede duplicidade mesmo sob concorrência.</li>
 * </ol>
 *
 * <p>O {@code @Repository} em interfaces Spring Data é opcional (a interface
 * já é detectada como bean), mas explicitamos por <b>clareza didática</b>.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Conta
 * @see     com.apibanco.model.Cliente
 */
@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    /**
     * Busca uma conta pelo número (chave natural única).
     *
     * <p>O número é atribuído pela Factory no momento da criação e é
     * imutável após a persistência. Como é único no banco, o retorno é
     * {@link Optional} &mdash; {@link Optional#empty()} quando não existir
     * conta com o número informado.</p>
     *
     * <p><b>Query derivada gerada pelo Spring Data:</b></p>
     * <pre>{@code
     *   SELECT c FROM Conta c WHERE c.numero = :numero
     * }</pre>
     *
     * @param numero número da conta (ex.: {@code "0001-12345-6"})
     * @return a conta correspondente, ou {@link Optional#empty()} se não existir
     */
    Optional<Conta> findByNumero(String numero);

    /**
     * Lista todas as contas pertencentes a um cliente.
     *
     * <p>O Spring Data resolve o sufixo {@code ClienteId} navegando a
     * associação {@link Conta#getCliente()} até a propriedade {@code id}
     * de {@link com.apibanco.model.Cliente} &mdash; ou seja, a query usa a
     * FK {@code cliente_id} da tabela {@code conta} sem precisar carregar
     * o cliente.</p>
     *
     * <p>Retorna {@link List} (e não {@link Optional}) porque um cliente
     * pode ter <b>zero, uma ou várias</b> contas. Cliente sem conta
     * devolve lista vazia, nunca {@code null}.</p>
     *
     * <p><b>Query derivada gerada pelo Spring Data:</b></p>
     * <pre>{@code
     *   SELECT c FROM Conta c WHERE c.cliente.id = :clienteId
     * }</pre>
     *
     * @param clienteId id do cliente
     * @return lista (possivelmente vazia, nunca {@code null}) de contas do cliente
     */
    List<Conta> findByClienteId(Long clienteId);

    /**
     * Verifica se já existe uma conta com o número informado.
     *
     * <p>Usado pelo {@code ContaService} durante a geração do número
     * da conta, para garantir unicidade antes do INSERT. A constraint
     * {@code UNIQUE} no banco continua sendo a última defesa.</p>
     *
     * @param numero número a verificar
     * @return {@code true} se já existir conta com esse número
     */
    boolean existsByNumero(String numero);
}