package com.apibanco.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global de exceções da API.
 *
 * <p><b>Por que existe:</b> traduz exceções de domínio
 * ({@link BusinessException}) e de validação
 * ({@link MethodArgumentNotValidException}) em respostas HTTP padronizadas,
 * sem vazar stack trace nem detalhes internos.</p>
 *
 * <p><b>Mapeamento:</b></p>
 * <ul>
 *   <li>{@link BusinessException} &rarr; {@code 400 Bad Request} com a
 *       mensagem da exceção no corpo;</li>
 *   <li>{@link MethodArgumentNotValidException} &rarr; {@code 400} com a
 *       lista de erros de validação por campo.</li>
 * </ul>
 *
 * <p><b>Evolução futura:</b> separar {@code NotFoundException} para
 * {@code 404} quando o domínio precisar distinguir "não encontrado" de
 * "regra violada". Hoje o contrato é só {@code 400}, coerente com a spec.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata {@link BusinessException} &mdash; erro de regra de negócio.
     *
     * @param ex a exceção lançada
     * @return {@code 400 Bad Request} com um corpo padronizado
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status",    400,
            "erro",      "regra de negócio violada",
            "mensagem",  ex.getMessage()
        ));
    }

    /**
     * Trata erros de validação de bean ({@code @Valid}) &mdash; devolve a
     * lista de campos inválidos.
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
                (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status",    400,
            "erro",      "validação falhou",
            "campos",    errosPorCampo
        ));
    }
}