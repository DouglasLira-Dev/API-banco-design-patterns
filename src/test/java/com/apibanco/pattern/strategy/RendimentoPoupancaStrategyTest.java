package com.apibanco.pattern.strategy;

import com.apibanco.model.Cliente;
import com.apibanco.model.ContaPoupanca;
import com.apibanco.model.TipoTransacao;
import com.apibanco.model.Transacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de aceite da {@link RendimentoPoupancaStrategy}.
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class RendimentoPoupancaStrategyTest {

    private final RendimentoPoupancaStrategy strategy = new RendimentoPoupancaStrategy();

    private ContaPoupanca contaComSaldo(String saldo) {
        ContaPoupanca c = new ContaPoupanca(
            new Cliente("Ana", "12345678901", "ana@ex.com"),
            "0001-3");
        BigDecimal valor = new BigDecimal(saldo);
        // Conta.creditar() rejeita valores <= 0 (invariante do Módulo 1).
        // Para "saldo zero", deixamos a conta como foi criada.
        if (valor.signum() > 0) {
            c.creditar(valor);
        }
        return c;
    }

    @Test
    @DisplayName("Rendimento de 0,5% sobre R$ 1000,00 = R$ 5,00")
    void rendimentoSobreMil() {
        ContaPoupanca conta = contaComSaldo("1000.00");

        Transacao t = strategy.aplicar(conta);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1005.00");
        assertThat(t).isNotNull();
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.RENDIMENTO);
        assertThat(t.getValor()).isEqualByComparingTo("5.00");
        assertThat(t.getContaDestino()).isSameAs(conta);
        assertThat(t.getContaOrigem()).isNull();
    }

    @Test
    @DisplayName("Saldo zero não gera rendimento nem transação")
    void saldoZeroNaoGeraTransacao() {
        ContaPoupanca conta = contaComSaldo("0.00");

        Transacao t = strategy.aplicar(conta);

        assertThat(t).isNull();
        assertThat(conta.getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Rendimento composto: 2 meses sobre R$ 1000,00")
    void rendimentoComposto() {
        ContaPoupanca conta = contaComSaldo("1000.00");

        // 1000,00 -> +5,00 = 1005,00
        strategy.aplicar(conta);
        // 1005,00 -> +5,025 -> HALF_EVEN -> +5,02 = 1010,02
        strategy.aplicar(conta);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1010.02");
    }
}