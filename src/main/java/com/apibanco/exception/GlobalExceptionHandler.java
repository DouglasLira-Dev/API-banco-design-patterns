package com.apibanco.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Handler global de exceções da API.
 *
 * <p>Traduz exceções de domínio ({@link BusinessException},
 * {@link NotFoundException}) e de validação ({@link MethodArgumentNotValidException})
 * em respostas HTTP <b>padronizadas</b>, sem vazar stack trace nem
 * detalhes internos.</p>
 *
 * <p><b>Mapeamento:</b></p>
 * <ul>
 *   <li>{@link NotFoundException} &rarr; <b>404 Not Found</b>
 *       (subtipo de {@code BusinessException} &mdash; mais específico);</li>
 *   <li>{@link BusinessException} &rarr; <b>400 Bad Request</b> (regra de negócio);</li>
 *   <li>{@link MethodArgumentNotValidException} &rarr; <b>400</b> com
 *       mapa de campos inválidos;</li>
 *   <li>{@link EntityNotFoundException} e {@link NoSuchElementException}
 *       &rarr; <b>404</b> (compatibilidade com JPA e {@code Optional.orElseThrow()});</li>
 *   <li>{@link Exception} genérica &rarr; <b>500</b> com mensagem
 *       genérica (sem stack trace exposta ao cliente).</li>
 * </ul>
 *
 * <p><b>Formato do corpo (sempre igual):</b></p>
 * <pre>{@code
 *   {
 *     "erro":      "regra de negócio violada",
 *     "mensagem":  "saldo insuficiente para saque...",
 *     "timestamp": "2026-11-05T14:30:00.123"
 *   }
 * }</pre>
 *
 * <p>Para validação, adiciona o campo {@code "campos"} com o mapa
 * {@code {campo: mensagem}}.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata {@link NotFoundException} &mdash; recurso não encontrado.
     *
     * @param ex a exceção lançada
     * @return {@code 404 Not Found} com corpo padronizado
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo(
            "recurso não encontrado", ex.getMessage()));
    }

    /**
     * Trata {@link BusinessException} &mdash; erro de regra de negócio.
     *
     * <p><b>Nota:</b> este handler só é chamado quando a exceção <b>não</b>
     * for {@link NotFoundException} (que tem handler mais específico e é
     * resolvido primeiro pelo Spring).</p>
     *
     * @param ex a exceção lançada
     * @return {@code 400 Bad Request} com corpo padronizado
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(corpo(
            "regra de negócio violada", ex.getMessage()));
    }

    /**
     * Trata erros de validação de bean ({@code @Valid}) &mdash; devolve
     * a lista de campos inválidos.
     *
     * @param ex a exceção do Spring MVC
     * @return {@code 400 Bad Request} com os erros por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errosPorCampo = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                fe -> fe.getField(),
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido",
                (a, b) -> a,
                LinkedHashMap::new));
        Map<String, Object> body = corpo("validação falhou", "um ou mais campos inválidos");
        body.put("campos", errosPorCampo);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Trata {@link EntityNotFoundException} (JPA) e {@link NoSuchElementException}
     * ({@code Optional.orElseThrow()} padrão) &mdash; compatibilidade.
     *
     * @param ex a exceção lançada
     * @return {@code 404 Not Found} com corpo padronizado
     */
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, Object>> handleNotFoundCompat(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo(
            "recurso não encontrado", ex.getMessage()));
    }

    /**
     * Trata qualquer exceção não mapeada &mdash; devolve 500 genérico
     * (sem stack trace para o cliente).
     *
     * <p><b>Importante:</b> a stack trace <b>completa</b> é registrada
     * no log do servidor; o cliente recebe só a mensagem genérica. Isso
     * evita vazamento de informação.</p>
     *
     * @param ex a exceção não tratada
     * @return {@code 500 Internal Server Error} com corpo padronizado
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo(
            "erro interno",
            "ocorreu um erro inesperado; contate o suporte"));
    }

    // Helper

    /**
     * Monta o corpo padronizado de erro.
     *
     * @param erro     categoria do erro (ex.: "regra de negócio violada")
     * @param mensagem detalhe legível
     * @return mapa mutável com as 3 chaves padrão
     */
    private Map<String, Object> corpo(String erro, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("erro",      erro);
        body.put("mensagem",  mensagem);
        body.put("timestamp", LocalDateTime.now());
        return body;
    }
}