package com.apibanco.repository;

import com.apibanco.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Cliente}.
 *
 * <p>Fornece o CRUD padrão herdado de {@link JpaRepository} e adiciona as
 * duas consultas derivadas exigidas pelo domínio bancário:</p>
 * <ul>
 *   <li>{@link #findByCpf(String)} &mdash; CPF é a chave natural de busca
 *       do cliente (usada em cadastro, atendimento e login futuro);</li>
 *   <li>{@link #existsByEmail(String)} &mdash; validação de e-mail duplicado
 *       antes do INSERT, <b>sem carregar a entidade</b> (mais leve que
 *       buscar e comparar).</li>
 * </ul>
 *
 * <p><b>Unicidade &mdash; duas camadas de defesa:</b></p>
 * <ol>
 *   <li><b>Camada de serviço:</b> chama {@link #existsByEmail(String)} e
 *       {@link #findByCpf(String)} antes de persistir, retornando um erro
 *       de negócio amigável;</li>
 *   <li><b>Camada de banco:</b> as constraints {@code UNIQUE} nas colunas
 *       {@code cpf} e {@code email} (criadas por {@code @Column(unique = true)}
 *       na entidade {@link Cliente}) impedem duplicidade mesmo sob concorrência.</li>
 * </ol>
 *
 * <p>O {@code @Repository} em interfaces Spring Data é opcional (a interface
 * já é detectada como bean), mas explicitamos aqui por <b>clareza didática</b>
 * &mdash; o projeto demonstra boas práticas e o leitor vê a intenção.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Cliente
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca um cliente pelo CPF.
     *
     * <p>O CPF é armazenado <b>normalizado</b> na entidade {@link Cliente}
     * &mdash; apenas os 11 dígitos, sem pontuação ({@code "12345678901"}).
     * O valor passado a este método deve estar no mesmo formato;
     * normalização (remover {@code .} e {@code -}) é responsabilidade da
     * camada de serviço.</p>
     *
     * <p><b>Query derivada gerada pelo Spring Data:</b></p>
     * <pre>{@code
     *   SELECT c FROM Cliente c WHERE c.cpf = :cpf
     * }</pre>
     *
     * @param cpf CPF com exatamente 11 dígitos numéricos
     * @return o cliente correspondente, ou {@link Optional#empty()} se não existir
     */
    Optional<Cliente> findByCpf(String cpf);

    /**
     * Verifica se já existe um cliente com o e-mail informado.
     *
     * <p>Retorna {@code boolean} em vez de {@code Optional<Cliente>} porque
     * a camada de serviço só precisa saber <i>se</i> existe &mdash; não
     * precisa do cliente. Isso evita carregar uma entidade inteira apenas
     * para descartá-la.</p>
     *
     * <p><b>Query derivada gerada pelo Spring Data:</b></p>
     * <pre>{@code
     *   SELECT COUNT(c) > 0 FROM Cliente c WHERE c.email = :email
     * }</pre>
     *
     * @param email e-mail a verificar; deve estar normalizado (trim + lowercase)
     *              pela camada de serviço
     * @return {@code true} se já existir cliente com esse e-mail
     */
    boolean existsByEmail(String email);
}