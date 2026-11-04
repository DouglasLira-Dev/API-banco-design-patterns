package com.apibanco.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de aceite da regra de <b>rendimento</b> da {@link ContaPoupanca}.
 *
 * <p><b>Regras validadas:</b></p>
 * <ul>
 *   <li>{@code calcularTaxaSaque} retorna {@link BigDecimal#ZERO}
 *       (poupança é isenta de tarifa de saque);</li>
 *   <li>{@code aplicarRendimento} credita <b>0,5% ao mês</b> sobre o
 *       saldo (taxa didática, HALF_EVEN);</li>
 *   <li>rendimento zero quando o saldo é zero (sem efeito);</li>
 *   <li>rendimento sobre saldo vindo de créditos anteriores.</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class ContaPoupancaTest {

    private Cliente novoCliente() {
        return new Cliente("Ana Silva", "12345678901", "ana@ex.com");
    }

    private ContaPoupanca novaContaPoupanca() {
        return new ContaPoupanca(novoCliente(), "0001-12345-7");
    }

    // Isenção de tarifa de saque (regra da spec)

    @Test
    @DisplayName("Tarifa de saque é zero (poupança isenta)")
    void tarifaDeSaqueIsenta() {
        ContaPoupanca conta = novaContaPoupanca();

        assertThat(conta.calcularTaxaSaque(new BigDecimal("100.00")))
            .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Tarifa de saque é zero para qualquer valor")
    void tarifaDeSaqueIsentaParaQualquerValor() {
        ContaPoupanca conta = novaContaPoupanca();

        assertThat(conta.calcularTaxaSaque(new BigDecimal("1.00")))
            .isEqualByComparingTo("0.00");
        assertThat(conta.calcularTaxaSaque(new BigDecimal("10000.00")))
            .isEqualByComparingTo("0.00");
    }

    // Critério de aceite: rendimento de 0,5%/mês

    @Test
    @DisplayName("Rendimento de 0,5% sobre R$ 1000,00 = R$ 5,00")
    void rendimentoSobreMilReais() {
        ContaPoupanca conta = novaContaPoupanca();
        conta.creditar(new BigDecimal("1000.00"));

        conta.aplicarRendimento();

        assertThat(conta.getSaldo()).isEqualByComparingTo("1005.00");
    }

    @Test
    @DisplayName("Rendimento de 0,5% sobre R$ 100,00 = R$ 0,50")
    void rendimentoSobreCemReais() {
        ContaPoupanca conta = novaContaPoupanca();
        conta.creditar(new BigDecimal("100.00"));

        conta.aplicarRendimento();

        assertThat(conta.getSaldo()).isEqualByComparingTo("100.50");
    }

    @Test
    @DisplayName("Rendimento é calculado sobre saldo atual, não sobre saldo inicial")
    void rendimentoConsideraSaldoAtual() {
        ContaPoupanca conta = novaContaPoupanca();
        conta.creditar(new BigDecimal("1000.00"));
        conta.creditar(new BigDecimal("500.00"));  // saldo = 1500

        conta.aplicarRendimento();                  // +0,5% = +7,50

        assertThat(conta.getSaldo()).isEqualByComparingTo("1507.50");
    }

    @Test
    @DisplayName("Rendimento sobre saldo zero é zero (sem efeito)")
    void rendimentoSobreSaldoZeroNaoAltera() {
        ContaPoupanca conta = novaContaPoupanca();

        conta.aplicarRendimento();

        assertThat(conta.getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Rendimento composto: dois meses sobre R$ 1000,00")
    void rendimentoCompostoDoisMeses() {
        ContaPoupanca conta = novaContaPoupanca();
        conta.creditar(new BigDecimal("1000.00"));

        conta.aplicarRendimento(); // 1000,00 -> +0,5% = 5,00 -> saldo 1005,00
        conta.aplicarRendimento(); // 1005,00 -> +0,5% = 5,025 -> HALF_EVEN -> 5,02 (par) -> saldo 1010,02

        assertThat(conta.getSaldo()).isEqualByComparingTo("1010.02");
    }
}