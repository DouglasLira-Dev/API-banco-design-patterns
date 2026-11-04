package com.apibanco.pattern.builder;

import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.ContaCorrente;
import com.apibanco.model.ContaPoupanca;
import com.apibanco.model.TipoConta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de aceite do <b>Design Pattern Builder</b> para {@link Conta}.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>construção fluente correta das subclasses {@link ContaCorrente}
 *       e {@link ContaPoupanca};</li>
 *   <li>aplicação do saldo inicial;</li>
 *   <li>validações de criação: cliente obrigatório, número não vazio,
 *       saldo ≥ 0, coerência de tipo.</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class ContaBuilderTest {

    /** Cliente auxiliar para os testes. */
    private Cliente novoCliente() {
        return new Cliente("Ana Silva", "12345678901", "ana@ex.com");
    }

    @Test
    @DisplayName("Constrói ContaCorrente com número, saldo e cliente")
    void constroiContaCorrenteValida() {
        Cliente cliente = novoCliente();

        Conta conta = new ContaCorrenteBuilder()
            .comNumero("0001-12345-6")
            .comSaldoInicial(new BigDecimal("500.00"))
            .comCliente(cliente)
            .build();

        assertThat(conta).isInstanceOf(ContaCorrente.class);
        assertThat(conta.getNumero()).isEqualTo("0001-12345-6");
        assertThat(conta.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(conta.getCliente()).isSameAs(cliente);
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("Constrói ContaPoupanca com número, saldo e cliente")
    void constroiContaPoupancaValida() {
        Cliente cliente = novoCliente();

        Conta conta = new ContaPoupancaBuilder()
            .comNumero("0001-12345-7")
            .comSaldoInicial(new BigDecimal("1000.00"))
            .comCliente(cliente)
            .build();

        assertThat(conta).isInstanceOf(ContaPoupanca.class);
        assertThat(conta.getNumero()).isEqualTo("0001-12345-7");
        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.POUPANCA);
    }

    @Test
    @DisplayName("Aceita saldo inicial zero")
    void aceitaSaldoZero() {
        Conta conta = new ContaCorrenteBuilder()
            .comNumero("0001-12345-6")
            .comSaldoInicial(BigDecimal.ZERO)
            .comCliente(novoCliente())
            .build();

        assertThat(conta.getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("comTipo coerente com o subtipo é aceito")
    void aceitaTipoCoerente() {
        Conta conta = new ContaCorrenteBuilder()
            .comNumero("0001-12345-6")
            .comSaldoInicial(BigDecimal.ZERO)
            .comCliente(novoCliente())
            .comTipo(TipoConta.CORRENTE)
            .build();

        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("Falha quando cliente não é informado")
    void falhaSemCliente() {
        assertThatThrownBy(() -> new ContaCorrenteBuilder()
                .comNumero("0001-12345-6")
                .comSaldoInicial(BigDecimal.TEN)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cliente");
    }

    @Test
    @DisplayName("Falha quando número é nulo")
    void falhaNumeroNulo() {
        assertThatThrownBy(() -> new ContaCorrenteBuilder()
                .comSaldoInicial(BigDecimal.TEN)
                .comCliente(novoCliente())
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("numero");
    }

    @Test
    @DisplayName("Falha quando número é vazio")
    void falhaNumeroVazio() {
        assertThatThrownBy(() -> new ContaCorrenteBuilder()
                .comNumero("   ")
                .comSaldoInicial(BigDecimal.TEN)
                .comCliente(novoCliente())
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("numero");
    }

    @Test
    @DisplayName("Falha quando saldo inicial é negativo")
    void falhaSaldoNegativo() {
        assertThatThrownBy(() -> new ContaCorrenteBuilder()
                .comNumero("0001-12345-6")
                .comSaldoInicial(new BigDecimal("-1.00"))
                .comCliente(novoCliente())
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("negativo");
    }

    @Test
    @DisplayName("Falha quando tipo informado é incompatível com o builder")
    void falhaTipoIncompativel() {
        assertThatThrownBy(() -> new ContaCorrenteBuilder()
                .comNumero("0001-12345-6")
                .comSaldoInicial(BigDecimal.TEN)
                .comCliente(novoCliente())
                .comTipo(TipoConta.POUPANCA)   // errado: builder é de corrente
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("incompatível");
    }
}