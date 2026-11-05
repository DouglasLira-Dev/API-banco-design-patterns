package com.apibanco.controller;

import com.apibanco.dto.ClienteRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de aceite do {@link ClienteController} via {@code @WebMvcTest}.
 *
 * <p><b>Estratégia:</b> o {@link ClienteService} é mockado &mdash; o teste
 * valida apenas a camada HTTP (rotas, status, serialização, validação de
 * entrada). Regra de negócio é testada em {@code ClienteServiceTest}.</p>
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private ClienteService clienteService;

    private Cliente novoCliente() {
        Cliente c = new Cliente("Ana Silva", "12345678901", "ana@ex.com");
        // Forçamos um id via setter? Não há — usamos reflexão simples ou
        // confiamos no mock devolver o objeto com id null. Para Location
        // header, só o id importa. O service mockado controla.
        return c;
    }

    @Test
    @DisplayName("POST /api/clientes cria cliente e devolve 201 + Location")
    void criarClienteRetorna201() throws Exception {
        Cliente salvo = novoCliente();
        when(clienteService.criar(any())).thenReturn(salvo);

        ClienteRequest req = new ClienteRequest("Ana Silva", "12345678901", "ana@ex.com");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/clientes/")))
            .andExpect(jsonPath("$.nome").value("Ana Silva"))
            .andExpect(jsonPath("$.cpf").value("12345678901"))
            .andExpect(jsonPath("$.email").value("ana@ex.com"));
    }

    @Test
    @DisplayName("POST /api/clientes com CPF inválido devolve 400 (validação)")
    void criarClienteCpfInvalidoRetorna400() throws Exception {
        ClienteRequest req = new ClienteRequest("Ana", "123", "ana@ex.com");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/{id} devolve 200 + ClienteResponse")
    void buscarPorIdRetorna200() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(novoCliente());

        mockMvc.perform(get("/api/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Ana Silva"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} inexistente devolve 400 (BusinessException)")
    void buscarInexistenteRetorna400() throws Exception {
        when(clienteService.buscarPorId(99L))
            .thenThrow(new BusinessException("cliente não encontrado: 99"));

        mockMvc.perform(get("/api/clientes/99"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").value("cliente não encontrado: 99"));
    }

    @Test
    @DisplayName("GET /api/clientes devolve 200 + lista")
    void listarRetorna200() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(novoCliente()));

        mockMvc.perform(get("/api/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}