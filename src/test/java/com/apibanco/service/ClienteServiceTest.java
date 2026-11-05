package com.apibanco.service;

import com.apibanco.dto.ClienteRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes do {@link ClienteService} &mdash; critério de aceite do Módulo 7:
 * cadastro duplicado lança {@link BusinessException}.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService service;

    private ClienteRequest requestValido() {
        return new ClienteRequest("Ana Silva", "12345678901", "ana@ex.com");
    }

    @Test
    @DisplayName("Cria cliente quando CPF e email são únicos")
    void criaClienteUnico() {
        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(clienteRepository.existsByEmail("ana@ex.com")).thenReturn(false);
        when(clienteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Cliente cliente = service.criar(requestValido());

        assertThat(cliente.getNome()).isEqualTo("Ana Silva");
        assertThat(cliente.getCpf()).isEqualTo("12345678901");
        assertThat(cliente.getEmail()).isEqualTo("ana@ex.com");
        verify(clienteRepository).save(any());
    }

    @Test
    @DisplayName("CPF duplicado lança BusinessException")
    void cpfDuplicadoLancaExcecao() {
        when(clienteRepository.findByCpf("12345678901"))
            .thenReturn(Optional.of(new Cliente("Outro", "12345678901", "x@x.com")));

        assertThatThrownBy(() -> service.criar(requestValido()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CPF");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Email duplicado lança BusinessException")
    void emailDuplicadoLancaExcecao() {
        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(clienteRepository.existsByEmail("ana@ex.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(requestValido()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("e-mail");
        verify(clienteRepository, never()).save(any());
    }
}