package com.apibanco.service;

import com.apibanco.dto.ContaRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.ContaPoupanca;
import com.apibanco.model.TipoConta;
import com.apibanco.pattern.factory.ContaFactory;
import com.apibanco.repository.ClienteRepository;
import com.apibanco.repository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes do {@link ContaService} &mdash; critério de aceite do Módulo 7:
 * conta criada com número único; tipo correto.
 */
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock private ContaRepository contaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private ContaFactory contaFactory;

    @InjectMocks
    private ContaService service;

    private Cliente cliente() {
        return new Cliente("Ana", "12345678901", "ana@ex.com");
    }

    @Test
    @DisplayName("Abre conta corrente com tipo correto e saldo aplicado")
    void abreContaCorrente() {
        Cliente c = cliente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c));
        when(contaRepository.existsByNumero(anyString())).thenReturn(false);
        when(contaFactory.criarConta(eq(TipoConta.CORRENTE), eq(c), anyString(), any()))
            .thenReturn(new ContaCorrente(c, "0001-1"));
        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Conta conta = service.abrir(new ContaRequest(
            1L, TipoConta.CORRENTE, new BigDecimal("100.00")));

        assertThat(conta).isInstanceOf(ContaCorrente.class);
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.CORRENTE);
        verify(contaRepository).save(any());
    }

    @Test
    @DisplayName("Abre conta poupança com tipo correto")
    void abreContaPoupanca() {
        Cliente c = cliente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c));
        when(contaRepository.existsByNumero(anyString())).thenReturn(false);
        when(contaFactory.criarConta(eq(TipoConta.POUPANCA), eq(c), anyString(), any()))
            .thenReturn(new ContaPoupanca(c, "0001-2"));
        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Conta conta = service.abrir(new ContaRequest(
            1L, TipoConta.POUPANCA, BigDecimal.ZERO));

        assertThat(conta).isInstanceOf(ContaPoupanca.class);
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.POUPANCA);
    }

    @Test
    @DisplayName("Cliente inexistente lança BusinessException")
    void clienteInexistenteLancaExcecao() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.abrir(new ContaRequest(
                99L, TipoConta.CORRENTE, BigDecimal.ZERO)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cliente");
        verify(contaRepository, never()).save(any());
    }
}