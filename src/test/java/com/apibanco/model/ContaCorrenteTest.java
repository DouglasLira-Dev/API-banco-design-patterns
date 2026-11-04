package com.apibanco.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de aceite da regra de tarifa de saque da {@link ContaCorrente}.
 *
 * <p><b>Regra:</b> tarifa = <b>1% do valor sacado</b> (percentual
 * configurável via {@code banco.taxas.saque-corrente}). O arredondamento
 * é {@link java.math.RoundingMode#HALF_EVEN} (convenção bancária).</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class ContaCorrenteTest {

    private Cliente novoCliente() {
        return new Cliente("Ana Silva", "12345678901", "ana@ex.com");
    }

    private ContaCorrente novaContaCorrente() {
        return new ContaCorrente(novoCliente(), "0001-12345-6");
    }

    // Critério de aceite: taxa de 1% aplicada corretamente

    @Test
    @DisplayName("Saque de R$ 100,00 gera tarifa de R$ 1,00 (1%)")
    void saque100GeraTarifa1Real() {
        ContaCorrente conta = novaContaCorrente();

        BigDecimal tarifa = conta.calcularTaxaSaque(new BigDecimal("100.00"));

        assertThat(tarifa).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("Saque de R$ 250,00 gera tarifa de R$ 2,50 (1%)")
    void saque250GeraTarifa2e50() {
        ContaCorrente conta = novaContaCorrente();

        BigDecimal tarifa = conta.calcularTaxaSaque(new BigDecimal("250.00"));

        assertThat(tarifa).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Saque de R$ 1,00 gera tarifa de R$ 0,01 (1%, HALF_EVEN)")
    void saque1RealGeraTarifa0e01() {
        ContaCorrente conta = novaContaCorrente();

        BigDecimal tarifa = conta.calcularTaxaSaque(new BigDecimal("1.00"));

        assertThat(tarifa).isEqualByComparingTo("0.01");
    }

    @Test
    @DisplayName("Saque de R$ 10,55 aplica HALF_EVEN: 0,1055 -> 0,11")
    void saqueComCentavosAplicaHalfEven() {
        ContaCorrente conta = novaContaCorrente();

        // 10.55 * 0.01 = 0.1055 -> arredonda para 0.11 (HALF_EVEN)
        BigDecimal tarifa = conta.calcularTaxaSaque(new BigDecimal("10.55"));

        assertThat(tarifa).isEqualByComparingTo("0.11");
    }

    @Test
    @DisplayName("Saque com percentual configurado (2%) usa o percentual informado")
    void saqueComPercentualCustomizado() {
        ContaCorrente conta = novaContaCorrente();

        BigDecimal tarifa = conta.calcularTaxaSaque(
            new BigDecimal("100.00"),
            new BigDecimal("0.02"));

        assertThat(tarifa).isEqualByComparingTo("2.00");
    }

    @Test
    @DisplayName("Percentual zero é válido (isenta na hora)")
    void percentualZeroIsenta() {
        ContaCorrente conta = novaContaCorrente();

        BigDecimal tarifa = conta.calcularTaxaSaque(
            new BigDecimal("100.00"),
            BigDecimal.ZERO);

        assertThat(tarifa).isEqualByComparingTo("0.00");
    }

    // Validações de entrada

    @Test
    @DisplayName("Valor nulo lança IllegalArgumentException")
    void valorNuloLancaExcecao() {
        ContaCorrente conta = novaContaCorrente();

        assertThatThrownBy(() -> conta.calcularTaxaSaque(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("positivo");
    }

    @Test
    @DisplayName("Valor zero lança IllegalArgumentException")
    void valorZeroLancaExcecao() {
        ContaCorrente conta = novaContaCorrente();

        assertThatThrownBy(() -> conta.calcularTaxaSaque(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Percentual >= 1 lança IllegalArgumentException")
    void percentualInvalidoLancaExcecao() {
        ContaCorrente conta = novaContaCorrente();

        assertThatThrownBy(() -> conta.calcularTaxaSaque(
                new BigDecimal("100.00"),
                BigDecimal.ONE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("percentual");
    }

    // Sem rendimento (regra do Módulo 3 mantida)

    @Test
    @DisplayName("Conta corrente não aplica rendimento (saldo inalterado)")
    void semRendimento() {
        ContaCorrente conta = novaContaCorrente();
        conta.creditar(new BigDecimal("1000.00"));

        conta.aplicarRendimento();

        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }
}