package com.apibanco.controller;

import com.apibanco.dto.DepositoRequest;
import com.apibanco.dto.SaqueRequest;
import com.apibanco.dto.TransferenciaRequest;
import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.TipoTransacao;
import com.apibanco.model.Transacao;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de aceite do {@link TransacaoController} via {@code @WebMvcTest}.
 */
@WebMvcTest(TransacaoController.class)
class TransacaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TransacaoService transacaoService;

    private Transacao novaTransacao(TipoTransacao tipo) {
        Cliente c = new Cliente("Ana", "12345678901", "ana@ex.com");
        Conta conta = new ContaCorrente(c, "0001-1");
        return switch (tipo) {
            case DEPOSITO      -> Transacao.deposito(conta, new BigDecimal("50.00"));
            case SAQUE         -> Transacao.saque(conta, new BigDecimal("50.00"));
            case TRANSFERENCIA -> Transacao.transferencia(conta, conta, new BigDecimal("50.00"));
            case RENDIMENTO    -> Transacao.rendimento(conta, new BigDecimal("5.00"));
        };
    }

    @Test
    @DisplayName("POST /api/transacoes/deposito devolve 201 + TransacaoResponse")
    void depositarRetorna201() throws Exception {
        when(transacaoService.depositar(any(), any())).thenReturn(novaTransacao(TipoTransacao.DEPOSITO));

        DepositoRequest req = new DepositoRequest(1L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/transacoes/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
            .andExpect(jsonPath("$.valor").value(50.00));
    }

    @Test
    @DisplayName("POST /api/transacoes/saque devolve 201 + TransacaoResponse")
    void sacarRetorna201() throws Exception {
        when(transacaoService.sacar(any(), any())).thenReturn(novaTransacao(TipoTransacao.SAQUE));

        SaqueRequest req = new SaqueRequest(1L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/transacoes/saque")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("SAQUE"));
    }

    @Test
    @DisplayName("POST /api/transacoes/transferencia devolve 201 + TransacaoResponse")
    void transferirRetorna201() throws Exception {
        when(transacaoService.transferir(any(), any(), any()))
            .thenReturn(novaTransacao(TipoTransacao.TRANSFERENCIA));

        TransferenciaRequest req = new TransferenciaRequest(1L, 2L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/transacoes/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"));
    }

    @Test
    @DisplayName("POST /api/transacoes/saque com valor zero devolve 400")
    void sacarValorZeroRetorna400() throws Exception {
        SaqueRequest req = new SaqueRequest(1L, BigDecimal.ZERO);

        mockMvc.perform(post("/api/transacoes/saque")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    @DisplayName("POST /api/transacoes/deposito com conta inexistente devolve 404")
    void depositarContaInexistenteRetorna404() throws Exception {
        when(transacaoService.depositar(any(), any()))
            .thenThrow(new com.apibanco.exception.NotFoundException("conta não encontrada: 99"));

        DepositoRequest req = new DepositoRequest(99L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/transacoes/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").value("conta não encontrada: 99"))
            .andExpect(jsonPath("$.erro").value("recurso não encontrado"));
    }
}