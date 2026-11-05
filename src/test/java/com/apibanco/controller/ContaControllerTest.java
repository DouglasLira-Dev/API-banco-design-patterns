package com.apibanco.controller;

import com.apibanco.dto.ContaRequest;
import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.TipoConta;
import com.apibanco.service.ContaService;
import com.apibanco.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de aceite do {@link ContaController} via {@code @WebMvcTest}.
 */
@WebMvcTest(ContaController.class)
class ContaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private ContaService contaService;
    @MockBean  private TransacaoService transacaoService;

    private Conta novaConta() {
        Cliente c = new Cliente("Ana", "12345678901", "ana@ex.com");
        return new ContaCorrente(c, "0001-1");
    }

    @Test
    @DisplayName("POST /api/contas abre conta e devolve 201 + Location")
    void abrirContaRetorna201() throws Exception {
        when(contaService.abrir(any())).thenReturn(novaConta());

        ContaRequest req = new ContaRequest(1L, TipoConta.CORRENTE, BigDecimal.ZERO);

        mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/contas/")))
            .andExpect(jsonPath("$.numero").value("0001-1"))
            .andExpect(jsonPath("$.tipo").value("CORRENTE"));
    }

    @Test
    @DisplayName("POST /api/contas com saldo negativo devolve 400 (validação)")
    void abrirContaSaldoNegativoRetorna400() throws Exception {
        ContaRequest req = new ContaRequest(1L, TipoConta.CORRENTE, new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.saldoInicial").exists());
    }

    @Test
    @DisplayName("GET /api/contas/{id} devolve 200 + ContaResponse")
    void buscarContaRetorna200() throws Exception {
        when(contaService.buscarPorId(1L)).thenReturn(novaConta());

        mockMvc.perform(get("/api/contas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numero").value("0001-1"));
    }

    @Test
    @DisplayName("GET /api/contas/{id}/extrato devolve 200 + lista")
    void extratoRetorna200() throws Exception {
        when(contaService.buscarPorId(1L)).thenReturn(novaConta());
        when(transacaoService.extrato(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/contas/1/extrato"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/contas/{id}/extrato de conta inexistente devolve 404")
    void extratoContaInexistenteRetorna404() throws Exception {
        when(contaService.buscarPorId(99L))
            .thenThrow(new com.apibanco.exception.NotFoundException("conta não encontrada: 99"));

        mockMvc.perform(get("/api/contas/99/extrato"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").value("conta não encontrada: 99"))
            .andExpect(jsonPath("$.erro").value("recurso não encontrado"));
    }
}