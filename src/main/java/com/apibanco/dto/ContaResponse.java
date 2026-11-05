package com.apibanco.dto;

import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;

import java.math.BigDecimal;

/**
 * DTO de <b>saída</b> para representar uma {@link Conta} na API.
 *
 * <p><b>Por que existe:</b> isolar o cliente do modelo de persistência
 * &mdash; a entidade {@code Conta} tem listas lazy ({@code transacoesComoOrigem},
 * {@code transacoesComoDestino}) que **não** devem ser serializadas no
 * JSON. Este record expõe apenas o necessário.</p>
 *
 * <p><b>Campos:</b></p>
 * <ul>
 *   <li>{@code id} &mdash; identificador da conta;</li>
 *   <li>{@code numero} &mdash; número único (chave natural);</li>
 *   <li>{@code tipo} &mdash; discriminador;</li>
 *   <li>{@code saldo} &mdash; saldo atual;</li>
 *   <li>{@code clienteId} &mdash; id do dono (útil para chamadas encadeadas);</li>
 *   <li>{@code clienteNome} &mdash; nome do dono (para exibição).</li>
 * </ul>
 *
 * <p><b>LazyInitializationException:</b> o {@link #from(Conta)} acessa
 * {@code conta.getCliente()} &mdash; que é {@code LAZY}. Se o mapeamento
 * for feito dentro da transação (Service com {@code @Transactional}), o
 * cliente já está carregado. Ainda assim, o DTO **não** expõe a relação
 * reversa ({@code transacoes}) &mdash; isso elimina o risco real.</p>
 *
 * @param id          identificador da conta
 * @param numero      número único da conta
 * @param tipo        tipo da conta
 * @param saldo       saldo atual
 * @param clienteId   id do cliente dono
 * @param clienteNome nome do cliente dono
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaRequest
 */
public record ContaResponse(
    Long id,
    String numero,
    TipoConta tipo,
    BigDecimal saldo,
    Long clienteId,
    String clienteNome
) {

    /**
     * Cria um {@code ContaResponse} a partir de uma {@link Conta}.
     *
     * <p><b>Atenção:</b> acessar {@code conta.getCliente().getId()} e
     * {@code .getNome()} só funciona se o cliente estiver carregado
     * (sessão JPA aberta). Chamar de dentro do {@code Service} (com
     * {@code @Transactional}) resolve isso.</p>
     *
     * @param conta entidade conta; não pode ser {@code null}
     * @return o DTO preenchido
     */
    public static ContaResponse from(Conta conta) {
        return new ContaResponse(
            conta.getId(),
            conta.getNumero(),
            conta.getTipo(),
            conta.getSaldo(),
            conta.getCliente().getId(),
            conta.getCliente().getNome()
        );
    }
}