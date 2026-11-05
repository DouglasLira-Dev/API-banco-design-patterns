package com.apibanco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de <b>entrada</b> para criação/atualização de {@link com.apibanco.model.Cliente}.
 *
 * <p><b>Por que existe:</b> nunca receber a entidade JPA diretamente no
 * controller &mdash; a entidade expõe campos que o cliente não deve controlar
 * (como {@code id}) e acopla a API ao modelo de persistência. Este record
 * é o <b>contrato público</b> da API, validado na borda.</p>
 *
 * <p><b>Validações (Bean Validation, executadas pelo {@code @Valid} no
 * controller):</b></p>
 * <ul>
 *   <li>{@code nome} &mdash; não em branco, até 120 caracteres;</li>
 *   <li>{@code cpf} &mdash; exatamente 11 dígitos numéricos (sem pontuação);</li>
 *   <li>{@code email} &mdash; formato de e-mail válido, até 180 caracteres.</li>
 * </ul>
 *
 * <p><b>Normalização:</b> este DTO <b>não</b> normaliza (trim, lowercase).
 * Isso fica no {@code ClienteService}, que recebe o DTO e ajusta antes de
 * persistir &mdash; separação de responsabilidades.</p>
 *
 * @param nome  nome completo; não pode ser vazio
 * @param cpf   CPF com 11 dígitos numéricos
 * @param email e-mail do cliente
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ClienteResponse
 */
public record ClienteRequest(

    @NotBlank(message = "nome é obrigatório")
    @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
    String nome,

    @NotBlank(message = "cpf é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "cpf deve conter exatamente 11 dígitos numéricos")
    String cpf,

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    @Size(max = 180, message = "email deve ter no máximo 180 caracteres")
    String email
) {
}