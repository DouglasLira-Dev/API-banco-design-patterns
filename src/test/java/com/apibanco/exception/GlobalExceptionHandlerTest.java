package com.apibanco.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes dedicados do {@link GlobalExceptionHandler} &mdash; cobrem cada
 * handler sem depender de um controller real.
 *
 * <p><b>Estratégia:</b></p>
 * <ol>
 *   <li>{@code @WebMvcTest(controllers = TestController.class)} &mdash;
 *       <b>filtra</b> a varredura de controllers para o {@code TestController}
 *       apenas. Sem este filtro, o Spring tenta instanciar os controllers
 *       reais ({@code ClienteController}, etc.) e falha por falta de
 *       services no contexto de teste.</li>
 *   <li>{@code @Import({GlobalExceptionHandler.class, TestConfig.class})}
 *       &mdash; carrega o {@code @RestControllerAdvice} real e a
 *       configuração que <b>registra</b> o {@code TestController} como bean.</li>
 * </ol>
 *
 * <p><b>Por que {@code @TestConfiguration} + {@code @Bean} e não só
 * {@code @WebMvcTest(controllers = TestController.class)}:</b> o atributo
 * {@code controllers} <b>filtra</b> a varredura, mas não <b>registra</b>
 * classes aninhadas de teste (que não são varridas). O {@code @Bean} é
 * a forma garantida de registrar.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("NotFoundException -> 404 com corpo padronizado")
    void notFoundRetorna404() throws Exception {
        mockMvc.perform(get("/test/notfound"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value("recurso não encontrado"))
            .andExpect(jsonPath("$.mensagem").value("recurso X não existe"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("BusinessException -> 400 com corpo padronizado")
    void businessRetorna400() throws Exception {
        mockMvc.perform(get("/test/business"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("regra de negócio violada"))
            .andExpect(jsonPath("$.mensagem").value("regra X violada"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("EntityNotFoundException -> 404")
    void entityNotFoundRetorna404() throws Exception {
        mockMvc.perform(get("/test/entity-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value("recurso não encontrado"));
    }

    @Test
    @DisplayName("NoSuchElementException -> 404")
    void noSuchElementRetorna404() throws Exception {
        mockMvc.perform(get("/test/no-such-element"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value("recurso não encontrado"));
    }

    @Test
    @DisplayName("Exception genérica -> 500 sem stack trace")
    void genericaRetorna500() throws Exception {
        mockMvc.perform(get("/test/generica"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.erro").value("erro interno"))
            .andExpect(jsonPath("$.mensagem").value("ocorreu um erro inesperado; contate o suporte"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.trace").doesNotExist())
            .andExpect(jsonPath("$.exception").doesNotExist());
    }

    /**
     * Configuração de teste que <b>declara</b> o {@link TestController}
     * como bean MVC. É o que garante que o {@code HandlerMapping} do
     * {@code @WebMvcTest} reconheça o controller de teste.
     */
    @TestConfiguration
    static class TestConfig {

        /**
         * Registra o {@link TestController} como bean.
         *
         * @return a instância do controller de teste
         */
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    /**
     * Controller <b>de teste</b> &mdash; lança exceções em endpoints
     * isolados para o {@link GlobalExceptionHandler} mapear.
     */
    @RestController
    public static class TestController {

        @GetMapping("/test/notfound")
        public void notFound() {
            throw new NotFoundException("recurso X não existe");
        }

        @GetMapping("/test/business")
        public void business() {
            throw new BusinessException("regra X violada");
        }

        @GetMapping("/test/entity-not-found")
        public void entityNotFound() {
            throw new EntityNotFoundException("entidade Y não existe");
        }

        @GetMapping("/test/no-such-element")
        public void noSuchElement() {
            throw new NoSuchElementException("elemento Z não existe");
        }

        @GetMapping("/test/generica")
        public void generica() {
            throw new RuntimeException("mensagem sensível que NÃO deve vazar");
        }
    }
}