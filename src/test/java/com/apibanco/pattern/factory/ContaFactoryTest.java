package com.apibanco.pattern.factory;

import com.apibanco.exception.BusinessException;
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
 * Testes de aceite do <b>Design Pattern Factory Method</b>.
 *
 * <p>Verifica:</p>
 * <ul>
 *   <li>cada {@link TipoConta} retorna a subclasse concreta correta;</li>
 *   <li>tipo nulo lança {@link BusinessException};</li>
 *   <li>tipo conhecido mas ainda não suportado ({@code EMPRESARIAL})
 *       lança {@link BusinessException};</li>
 *   <li>a sobrecarga com dados reais propaga corretamente para o Builder.</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 */
class ContaFactoryTest {

    /** Instância sob teste. Não depende de Spring — testamos a classe pura. */
    private final ContaFactory factory = new ContaFactory();

    /** Cliente auxiliar. */
    private Cliente novoCliente() {
        return new Cliente("Ana Silva", "12345678901", "ana@ex.com");
    }

    // Critério de aceite: cada TipoConta retorna a instância correta

    @Test
    @DisplayName("TipoConta.CORRENTE retorna ContaCorrente")
    void correnteRetornaContaCorrente() {
        Conta conta = factory.criarConta(TipoConta.CORRENTE);

        assertThat(conta)
            .isInstanceOf(ContaCorrente.class)
            .isNotInstanceOf(ContaPoupanca.class);
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("TipoConta.POUPANCA retorna ContaPoupanca")
    void poupancaRetornaContaPoupanca() {
        Conta conta = factory.criarConta(TipoConta.POUPANCA);

        assertThat(conta)
            .isInstanceOf(ContaPoupanca.class)
            .isNotInstanceOf(ContaCorrente.class);
        assertThat(conta.tipoEsperado()).isEqualTo(TipoConta.POUPANCA);
    }

    // Critério de aceite: exceção em casos inválidos

    @Test
    @DisplayName("Tipo nulo lança BusinessException")
    void tipoNuloLancaBusinessException() {
        assertThatThrownBy(() -> factory.criarConta(null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("Tipo EMPRESARIAL (ainda não suportado) lança BusinessException")
    void tipoEmpresarialNaoSuportadoLancaBusinessException() {
        assertThatThrownBy(() -> factory.criarConta(TipoConta.EMPRESARIAL))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("EMPRESARIAL");
    }

    // Sobrecarga com dados reais: propaga ao Builder

    @Test
    @DisplayName("Sobrecarga completa aplica numero, saldo e cliente")
    void sobrecargaCompletaAplicaDados() {
        Cliente cliente = novoCliente();

        Conta conta = factory.criarConta(
            TipoConta.CORRENTE,
            cliente,
            "0001-12345-6",
            new BigDecimal("500.00"));

        assertThat(conta).isInstanceOf(ContaCorrente.class);
        assertThat(conta.getNumero()).isEqualTo("0001-12345-6");
        assertThat(conta.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(conta.getCliente()).isSameAs(cliente);
    }

    @Test
    @DisplayName("Sobrecarga completa propaga erro de validação do Builder")
    void sobrecargaCompletaPropagaErroDoBuilder() {
        Cliente cliente = novoCliente();

        assertThatThrownBy(() -> factory.criarConta(
                TipoConta.CORRENTE,
                cliente,
                null,                       // número inválido
                BigDecimal.ZERO))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("numero");
    }
}