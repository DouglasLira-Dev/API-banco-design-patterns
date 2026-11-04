package com.apibanco.exception;

/**
 * Exceção de <b>negócio</b> &mdash; representa violação de uma regra de
 * domínio detectada pela aplicação (não pelo banco).
 *
 * <p>É uma {@link RuntimeException} para não poluir assinaturas de métodos
 * com {@code throws}. Deve ser capturada por um {@code @RestControllerAdvice}
 * (módulo de controllers) e convertida em {@code HTTP 400} ou {@code 422}
 * conforme o caso.</p>
 *
 * <p><b>Quando usar:</b></p>
 * <ul>
 *   <li>tipo de conta desconhecido na Factory;</li>
 *   <li>saldo insuficiente em saque/transferência;</li>
 *   <li>CPF já cadastrado;</li>
 *   <li>e-mail já cadastrado;</li>
 *   <li>qualquer violação de invariante de negócio que não seja erro técnico.</li>
 * </ul>
 *
 * <p><b>Não confundir com:</b></p>
 * <ul>
 *   <li>{@link IllegalArgumentException} &mdash; erro de <b>programação</b>
 *       (argumento inválido do ponto de vista do código, não do negócio);</li>
 *   <li>{@link IllegalStateException} &mdash; erro de <b>estado</b> interno
 *       (chamada em ordem errada);</li>
 *   <li>{@link jakarta.persistence.PersistenceException} &mdash; erro de
 *       <b>persistência</b> (banco fora, constraint, etc.).</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
public class BusinessException extends RuntimeException {

    /**
     * Cria uma exceção de negócio com a mensagem indicada.
     *
     * @param mensagem descrição legível do erro (será exibida ao cliente da API)
     */
    public BusinessException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria uma exceção de negócio com a mensagem e a causa original.
     *
     * @param mensagem descrição legível do erro
     * @param causa    exceção que originou o problema (para preservar stack trace)
     */
    public BusinessException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}