package com.apibanco.repository;

import com.apibanco.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para a entidade {@link Transacao}.
 *
 * <p>Fornece o CRUD padrão herdado de {@link JpaRepository} e a consulta
 * derivada que alimenta o <b>extrato</b> de uma conta: todas as transações
 * em que a conta aparece <b>como origem ou como destino</b>, ordenadas da
 * mais recente para a mais antiga.</p>
 *
 * <p><b>Por que "origem OR destino"?</b> Uma conta participa de movimentações
 * dos dois lados:</p>
 * <ul>
 *   <li><b>origem</b> &mdash; saques, transferências enviadas;</li>
 *   <li><b>destino</b> &mdash; depósitos, rendimentos, transferências recebidas.</li>
 * </ul>
 * <p>O extrato precisa das duas pontas. O mesmo {@code id} é passado nos
 * dois parâmetros do método.</p>
 *
 * <p><b>Ordenação:</b> {@code OrderByDataHoraDesc} garante que o extrato
 * venha do lançamento mais recente para o mais antigo &mdash; convenção
 * esperada em aplicativos bancários.</p>
 *
 * <p>O {@code @Repository} em interfaces Spring Data é opcional (a interface
 * já é detectada como bean), mas explicitamos por <b>clareza didática</b>.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     Transacao
 * @see     com.apibanco.model.Conta
 */
@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    /**
     * Retorna o extrato de uma conta: todas as transações em que ela é
     * origem <b>ou</b> destino, ordenadas por {@code dataHora} decrescente.
     *
     * <p><b>Como usar:</b> para o extrato da conta de id {@code 42}, chame
     * {@code findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(42L, 42L)}.
     * Passar o <b>mesmo id</b> nos dois parâmetros é o caso de uso normal;
     * a assinatura com dois parâmetros é o que o Spring Data gera para o
     * nome derivado e permite, se necessário, consultar transações entre
     * duas contas distintas.</p>
     *
     * <p><b>Query derivada gerada pelo Spring Data:</b></p>
     * <pre>{@code
     *   SELECT t FROM Transacao t
     *    WHERE t.contaOrigem.id  = :origem
     *       OR t.contaDestino.id = :destino
     *    ORDER BY t.dataHora DESC
     * }</pre>
     *
     * <p><b>Observação sobre {@code NULL}:</b> transações de saque têm
     * {@code contaDestino} nula e transações de depósito/rendimento têm
     * {@code contaOrigem} nula. O {@code OR} no {@code WHERE} do SQL trata
     * isso naturalmente &mdash; {@code NULL = valor} avalia como falso, e
     * a linha ainda casa pela outra ponta. Nenhuma transação é perdida do
     * extrato por causa disso.</p>
     *
     * <p><b>Paginação:</b> para contas com histórico grande, prefira a
     * variante com {@link org.springframework.data.domain.Pageable} &mdash;
     * não está nesta spec, mas pode ser adicionada sem quebrar este método:</p>
     * <pre>{@code
     *   Page<Transacao> findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(
     *       Long origem, Long destino, Pageable pageable);
     * }</pre>
     *
     * @param origem  id da conta considerada como origem
     * @param destino id da conta considerada como destino
     * @return lista (possivelmente vazia, nunca {@code null}) de transações
     *         ordenadas por {@code dataHora} decrescente
     */
    List<Transacao> findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(
            Long origem, Long destino);
}