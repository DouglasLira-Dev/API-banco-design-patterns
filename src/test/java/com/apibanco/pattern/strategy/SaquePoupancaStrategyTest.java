package com.apibanco.pattern.strategy;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.ContaPoupanca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de aceite da {@link SaquePoupancaStrategy}.
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class SaquePoupancaStrategyTest {

    private final SaquePoupancaStrategy strategy = new SaquePoupancaStrategy();

    private ContaPoupanca contaComSaldo(String saldo) {
        ContaPoupanca c = new ContaPoupanca(
            new Cliente("Ana", "12345678901", "ana@ex.com"),
            "0001-2");
        c.creditar(new BigDecimal(saldo));
        return c;
    }

    @Test
    @DisplayName("Taxa de saque é zero (poupança isenta)")
    void taxaZero() {
        assertThat(strategy.calcularTaxa(new BigDecimal("100.00")))
            .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Saque debita apenas o valor (sem taxa)")
    void saqueSemTaxa() {
        ContaPoupanca conta = contaComSaldo("100.00");

        BigDecimal totalDebitado = strategy.executar(new BigDecimal("40.00"), conta);

        assertThat(totalDebitado).isEqualByComparingTo("40.00");
        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("Saldo insuficiente lança BusinessException")
    void saldoInsuficienteLancaExcecao() {
        ContaPoupanca conta = contaComSaldo("30.00");

        assertThatThrownBy(() -> strategy.executar(new BigDecimal("31.00"), conta))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("saldo insuficiente");

        assertThat(conta.getSaldo()).isEqualByComparingTo("30.00");
    }
}