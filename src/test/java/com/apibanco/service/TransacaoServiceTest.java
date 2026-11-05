package com.apibanco.service;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.ContaPoupanca;
import com.apibanco.model.TipoTransacao;
import com.apibanco.model.Transacao;
import com.apibanco.pattern.strategy.OperacaoStrategy;
import com.apibanco.pattern.strategy.RendimentoPoupancaStrategy;
import com.apibanco.pattern.strategy.SaqueCorrenteStrategy;
import com.apibanco.pattern.strategy.SaquePoupancaStrategy;
import com.apibanco.repository.ContaRepository;
import com.apibanco.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do {@link TransacaoService} &mdash; critério de aceite do Módulo 7:
 * todos os fluxos cobertos; saldo consistente.
 *
 * <p><b>Nota sobre os stubs de {@code save}:</b> cada teste declara apenas
 * os stubs que <b>usa</b> &mdash; evita {@code UnnecessaryStubbingException}
 * do {@code MockitoExtension} (que é strict por padrão).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock private TransacaoRepository transacaoRepository;
    @Mock private ContaRepository contaRepository;

    private TransacaoService service;

    @BeforeEach
    void setUp() {
        List<OperacaoStrategy> strategies = List.of(
            new SaqueCorrenteStrategy(new BigDecimal("0.01")),
            new SaquePoupancaStrategy(),
            new RendimentoPoupancaStrategy()
        );
        service = new TransacaoService(transacaoRepository, contaRepository, strategies);
    }

    private Cliente cliente() {
        return new Cliente("Ana", "12345678901", "ana@ex.com");
    }

    private ContaCorrente contaCorrente(String saldo) {
        ContaCorrente c = new ContaCorrente(cliente(), "0001-1");
        if (new BigDecimal(saldo).signum() > 0) c.creditar(new BigDecimal(saldo));
        return c;
    }

    private ContaPoupanca contaPoupanca(String saldo) {
        ContaPoupanca c = new ContaPoupanca(cliente(), "0001-2");
        if (new BigDecimal(saldo).signum() > 0) c.creditar(new BigDecimal(saldo));
        return c;
    }

    /**
     * Stub padrão dos {@code save} de repositório — usado apenas pelos
     * testes que chegam a chamar {@code save}.
     */
    private void stubSaves() {
        when(transacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ---------------------- Depósito ----------------------

    @Test
    @DisplayName("Depósito credita e cria Transacao(DEPOSITO)")
    void depositoCreditaECriaTransacao() {
        ContaCorrente conta = contaCorrente("100.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        stubSaves();

        Transacao t = service.depositar(1L, new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.DEPOSITO);
        assertThat(t.getValor()).isEqualByComparingTo("50.00");
        assertThat(t.getContaDestino()).isSameAs(conta);
        assertThat(t.getContaOrigem()).isNull();
    }

    @Test
    @DisplayName("Depósito com valor zero lança BusinessException")
    void depositoValorZeroLancaExcecao() {
        // NÃO stubba save — a exceção acontece antes
        assertThatThrownBy(() -> service.depositar(1L, BigDecimal.ZERO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("maior que zero");
    }

    // ---------------------- Saque ----------------------

    @Test
    @DisplayName("Saque em conta corrente debita valor + 1% e cria Transacao(SAQUE)")
    void saqueCorrenteAplicaTaxa() {
        ContaCorrente conta = contaCorrente("100.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        stubSaves();

        Transacao t = service.sacar(1L, new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("49.50"); // 100 - (50 + 0,50)
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(t.getValor()).isEqualByComparingTo("50.00");
        assertThat(t.getContaOrigem()).isSameAs(conta);
    }

    @Test
    @DisplayName("Saque em poupança não aplica taxa")
    void saquePoupancaSemTaxa() {
        ContaPoupanca conta = contaPoupanca("100.00");
        when(contaRepository.findById(2L)).thenReturn(Optional.of(conta));
        stubSaves();

        service.sacar(2L, new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("Saque delega à Strategy correta do tipo da conta")
    void saqueDelegaParaStrategyCorreta() {
        // Usa spies para provar a chamada sem perder o comportamento real
        SaqueCorrenteStrategy strategyReal = spy(new SaqueCorrenteStrategy(new BigDecimal("0.01")));
        SaquePoupancaStrategy strategyPoupanca = spy(new SaquePoupancaStrategy());
        RendimentoPoupancaStrategy rendimento = new RendimentoPoupancaStrategy();

        List<OperacaoStrategy> strategies = List.of(strategyReal, strategyPoupanca, rendimento);
        TransacaoService service = new TransacaoService(transacaoRepository, contaRepository, strategies);

        ContaCorrente conta = contaCorrente("100.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.sacar(1L, new BigDecimal("50.00"));

        // Verifica que a Strategy de conta corrente foi chamada,
        // e a de poupança NÃO foi
        verify(strategyReal, times(1)).executar(eq(new BigDecimal("50.00")), same(conta));
        verify(strategyPoupanca, never()).executar(any(), any());
    }

    @Test
    @DisplayName("Saque acima do saldo lança BusinessException")
    void saqueAcimaDoSaldoLancaExcecao() {
        ContaCorrente conta = contaCorrente("30.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        // NÃO stubba save — a exceção acontece antes do save
        assertThatThrownBy(() -> service.sacar(1L, new BigDecimal("30.00")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("saldo insuficiente");
        assertThat(conta.getSaldo()).isEqualByComparingTo("30.00");
    }

    // ---------------------- Transferência ----------------------

    @Test
    @DisplayName("Transferência debita origem, credita destino e cria Transacao(TRANSFERENCIA)")
    void transferenciaEntreContas() {
        ContaCorrente origem = contaCorrente("100.00");
        ContaPoupanca destino = contaPoupanca("20.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findById(2L)).thenReturn(Optional.of(destino));
        stubSaves();

        Transacao t = service.transferir(1L, 2L, new BigDecimal("50.00"));

        // origem: 100 - (50 + 0,50 taxa) = 49,50
        assertThat(origem.getSaldo()).isEqualByComparingTo("49.50");
        // destino: 20 + 50 = 70
        assertThat(destino.getSaldo()).isEqualByComparingTo("70.00");
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.TRANSFERENCIA);
        assertThat(t.getContaOrigem()).isSameAs(origem);
        assertThat(t.getContaDestino()).isSameAs(destino);
    }

    @Test
    @DisplayName("Transferência para mesma conta lança BusinessException")
    void transferenciaMesmaContaLancaExcecao() {
        // NÃO stubba save — a exceção acontece antes
        assertThatThrownBy(() -> service.transferir(1L, 1L, BigDecimal.TEN))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("diferentes");
    }

    // ---------------------- Rendimento ----------------------

    @Test
    @DisplayName("Rendimento em poupança credita 0,5% e cria Transacao(RENDIMENTO)")
    void rendimentoPoupanca() {
        ContaPoupanca conta = contaPoupanca("1000.00");
        when(contaRepository.findById(2L)).thenReturn(Optional.of(conta));
        stubSaves();

        Transacao t = service.aplicarRendimento(2L);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1005.00");
        assertThat(t).isNotNull();
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.RENDIMENTO);
        assertThat(t.getValor()).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("Rendimento em conta corrente não é suportado")
    void rendimentoCorrenteNaoSuportado() {
        ContaCorrente conta = contaCorrente("100.00");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        // NÃO stubba save — a exceção acontece antes do save
        assertThatThrownBy(() -> service.aplicarRendimento(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("rendimento não suportado");
    }
}