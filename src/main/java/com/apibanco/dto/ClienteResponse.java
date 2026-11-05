package com.apibanco.dto;

import com.apibanco.model.Cliente;

/**
 * DTO de <b>saída</b> para representar um {@link Cliente} na API.
 *
 * <p><b>Por que existe:</b> nunca expor a entidade JPA crua &mdash; ela
 * carrega anotações de persistência, listas lazy e potencialmente campos
 * que não devem sair (ex.: em versões futuras, senha ou token). Este
 * record é o <b>contrato público</b> da API.</p>
 *
 * <p><b>Campos expostos (spec):</b> {@code id}, {@code nome}, {@code cpf},
 * {@code email}. Note que <b>não</b> expomos {@code contas} &mdash; a lista
 * de contas é consultada via endpoint dedicado, evitando payloads
 * gigantes e {@code LazyInitializationException}.</p>
 *
 * <p><b>Conversão:</b> use {@link #from(Cliente)} &mdash; mantém o mapeamento
 * no DTO, sem depender de {@code Mapper} externo.</p>
 *
 * @param id    identificador do cliente
 * @param nome  nome completo
 * @param cpf   CPF (11 dígitos, sem pontuação)
 * @param email e-mail do cliente
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ClienteRequest
 */
public record ClienteResponse(
    Long id,
    String nome,
    String cpf,
    String email
) {

    /**
     * Cria um {@code ClienteResponse} a partir de um {@link Cliente}.
     *
     * @param cliente entidade cliente; não pode ser {@code null}
     * @return o DTO preenchido
     */
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpf(),
            cliente.getEmail()
        );
    }
}