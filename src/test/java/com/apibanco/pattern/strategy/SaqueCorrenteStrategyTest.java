package com.apibanco.pattern.strategy;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.ContaCorrente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de aceite da {@link SaqueCorrenteStrategy}.
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class SaqueCorrenteStrategyTest {

    private final SaqueCorrenteStrategy strategy =
        new SaqueCorrenteStrategy(new BigDecimal("0.01"));

    private ContaCorrente contaComSaldo(String saldo) {
        ContaCorrente c = new ContaCorrente(
            new Cliente("Ana", "12345678901", "ana@ex.com"),
            "0001-1");
        c.creditar(new BigDecimal(saldo));
        return c;
    }

    @Test
    @DisplayName("Taxa de saque = 1% do valor")
    void taxaDeUmPorCento() {
        assertThat(strategy.calcularTaxa(new BigDecimal("100.00")))
            .isEqualByComparingTo("1.00");
        assertThat(strategy.calcularTaxa(new BigDecimal("250.00")))
            .isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Saque com saldo suficiente debita valor + taxa")
    void saqueDebitaValorMaisTaxa() {
        ContaCorrente conta = contaComSaldo("100.00");

        BigDecimal totalDebitado = strategy.executar(new BigDecimal("50.00"), conta);

        assertThat(totalDebitado).isEqualByComparingTo("50.50");
        assertThat(conta.getSaldo()).isEqualByComparingTo("49.50");
    }

    @Test
    @DisplayName("Saque acima do saldo (com taxa) lança BusinessException")
    void saqueAcimaDoSaldoLancaExcecao() {
        // saldo 50, saque 50 -> total 50,50 > 50 -> falha
        ContaCorrente conta = contaComSaldo("50.00");

        assertThatThrownBy(() -> strategy.executar(new BigDecimal("50.00"), conta))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("saldo insuficiente");

        assertThat(conta.getSaldo()).isEqualByComparingTo("50.00"); // não debitou
    }

    @Test
    @DisplayName("Saldo exatamente suficiente (valor + taxa) é aceito")
    void saldoExatoAceito() {
        // saldo 50,50; saque 50 + taxa 0,50 -> total 50,50 -> ok
        ContaCorrente conta = contaComSaldo("50.50");

        strategy.executar(new BigDecimal("50.00"), conta);

        assertThat(conta.getSaldo()).isEqualByComparingTo("0.00");
    }
}