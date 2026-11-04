package com.apibanco.pattern.factory;

import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;
import com.apibanco.pattern.builder.ContaCorrenteBuilder;
import com.apibanco.pattern.builder.ContaPoupancaBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * <b>Design Pattern: Factory Method (Criacional)</b> &mdash; centraliza a
 * criação de contas conforme o {@link TipoConta} solicitado.
 *
 * <p><b>Por que usar aqui:</b></p>
 * <ul>
 *   <li>O código cliente não deve conhecer as subclasses concretas
 *       ({@code ContaCorrente}, {@code ContaPoupanca}) nem saber
 *       <i>qual</i> instanciar em cada caso &mdash; pede ao Factory;</li>
 *   <li>Se amanhã surgir {@code ContaEmpresarial}, a única mudança fica
 *       no {@code switch} desta classe (e o compilador avisa com o
 *       {@code switch} expression exaustivo);</li>
 *   <li>Combina com o <b>Builder</b> (Módulo 3): o Factory <b>orquestra</b>
 *       a decisão e delega a construção validada ao sub-builder correto.</li>
 * </ul>
 *
 * <p><b>Exemplo de uso (produção):</b></p>
 * <pre>{@code
 *   Conta conta = contaFactory.criarConta(
 *       TipoConta.CORRENTE,
 *       cliente,
 *       "0001-12345-6",
 *       new BigDecimal("500.00"));
 * }</pre>
 *
 * <p><b>Regra de negócio:</b> tipo nulo ou desconhecido resulta em
 * {@link BusinessException} &mdash; o método nunca devolve {@code null}.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     com.apibanco.pattern.builder.ContaBuilder
 * @see     com.apibanco.model.TipoConta
 * @see     BusinessException
 */
@Component
public class ContaFactory {

    /**
     * Cria uma conta a partir do {@link TipoConta} apenas &mdash; sem dados
     * de cliente/número/saldo reais.
     *
     * <p><b>Uso:</b> exercita a decisão de subtipo (critério de aceite),
     * prototipagem e testes. <b>Não persistir</b> o resultado &mdash;
     * usa número placeholder {@code "TEMP"} e um cliente dummy.</p>
     *
     * <p><b>Preferir</b> a sobrecarga
     * {@link #criarConta(TipoConta, Cliente, String, BigDecimal)} no fluxo
     * de negócio real.</p>
     *
     * @param tipo tipo de conta desejado; não pode ser {@code null}
     * @return a subclasse concreta correspondente (nunca {@code null})
     * @throws BusinessException se {@code tipo} for {@code null} ou desconhecido
     */
    public Conta criarConta(TipoConta tipo) {
        // Cliente dummy apenas para satisfazer a validação do Builder.
        // NÃO PERSISTIR contas criadas por esta sobrecarga.
        Cliente dummy = new Cliente("__placeholder__", "00000000000", "placeholder@invalid.local");
        return criarConta(tipo, dummy, "TEMP", BigDecimal.ZERO);
    }

    /**
     * Cria uma conta com os dados reais informados, delegando ao sub-builder
     * correspondente.
     *
     * <p><b>Fluxo:</b></p>
     * <ol>
     *   <li>Valida {@code tipo} (nulo/desconhecido &rarr; {@link BusinessException});</li>
     *   <li>Seleciona o sub-builder correto;</li>
     *   <li>Preenche número, saldo e cliente;</li>
     *   <li>Chama {@code build()} &mdash; que valida as invariantes do Builder.</li>
     * </ol>
     *
     * @param tipo        tipo de conta desejado; não pode ser {@code null}
     * @param cliente     dono da conta; não pode ser {@code null}
     * @param numero      número único da conta; não pode ser vazio
     * @param saldoInicial saldo inicial; deve ser ≥ 0
     * @return a subclasse concreta correspondente (nunca {@code null})
     * @throws BusinessException se {@code tipo} for {@code null} ou desconhecido
     */
    public Conta criarConta(TipoConta tipo,
                            Cliente cliente,
                            String numero,
                            BigDecimal saldoInicial) {

        if (tipo == null) {
            throw new BusinessException("tipo de conta não pode ser nulo");
        }

        return switch (tipo) {
            case CORRENTE -> new ContaCorrenteBuilder()
                .comNumero(numero)
                .comSaldoInicial(saldoInicial)
                .comCliente(cliente)
                .comTipo(tipo)
                .build();

            case POUPANCA -> new ContaPoupancaBuilder()
                .comNumero(numero)
                .comSaldoInicial(saldoInicial)
                .comCliente(cliente)
                .comTipo(tipo)
                .build();

            // case null seria redundante (já validado acima), e
            // o switch expression é exaustivo — se TipoConta ganhar um
            // novo valor no futuro, o compilador falha aqui e nos força
            // a decidir como tratá-lo. Rede de segurança intencional.

            case EMPRESARIAL -> throw new BusinessException(
                "tipo de conta ainda não suportado: " + tipo);

            default -> throw new BusinessException(
                "tipo de conta desconhecido: " + tipo);
        };
    }
}