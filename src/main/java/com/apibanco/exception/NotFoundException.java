package com.apibanco.exception;

/**
 * Exceção de <b>recurso não encontrado</b> &mdash; subclasse de
 * {@link BusinessException} para diferenciar <b>404</b> de <b>400</b>
 * no {@code GlobalExceptionHandler}.
 *
 * <p><b>Por que estende {@link BusinessException}:</b></p>
 * <ul>
 *   <li>Um "não encontrado" <b>é</b> uma regra de negócio (o cliente pediu
 *       algo que não existe) &mdash; herda {@code RuntimeException} e a
 *       semântica de "domínio";</li>
 *   <li>Quem capturar {@code BusinessException} continua pegando 404 &mdash;
 *       compatível com o código existente;</li>
 *   <li>O {@code @RestControllerAdvice} diferencia pela subclasse:
 *       {@code NotFoundException} → 404, demais {@code BusinessException} → 400.</li>
 * </ul>
 *
 * <p><b>Quando usar:</b> busca por ID/CPF/número que não retornou nada
 * (cliente inexistente, conta inexistente, etc.). <b>Não</b> usar para
 * "regra violada" &mdash; para isso existe {@link BusinessException} pura.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     BusinessException
 * @see     GlobalExceptionHandler
 */
public class NotFoundException extends BusinessException {

    /**
     * Cria a exceção com a mensagem indicada.
     *
     * @param mensagem descrição legível (ex.: "cliente não encontrado: 42")
     */
    public NotFoundException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção com a mensagem e a causa original.
     *
     * @param mensagem descrição legível
     * @param causa    exceção que originou o problema
     */
    public NotFoundException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}