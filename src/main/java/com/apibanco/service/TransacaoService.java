package com.apibanco.service;

import com.apibanco.dto.TransacaoRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.model.Conta;
import com.apibanco.model.TipoConta;
import com.apibanco.model.TipoTransacao;
import com.apibanco.model.Transacao;
import com.apibanco.pattern.strategy.OperacaoStrategy;
import com.apibanco.pattern.strategy.RendimentoStrategy;
import com.apibanco.pattern.strategy.SaqueStrategy;
import com.apibanco.repository.ContaRepository;
import com.apibanco.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço de <b>negócio</b> das transações bancárias.
 *
 * <p><b>Responsabilidades:</b> executar as operações de <b>depósito</b>,
 * <b>saque</b>, <b>transferência</b> e <b>rendimento</b>, <b>delegando
 * as regras variáveis</b> às Strategies ({@link SaqueStrategy},
 * {@link RendimentoStrategy}) por tipo de conta.</p>
 *
 * <p><b>Como as Strategies são selecionadas:</b> no construtor, o Spring
 * injeta {@code List<OperacaoStrategy>} com todas as implementações
 * ({@code @Component}). Filtramos por sub-tipo e montamos dois mapas:</p>
 * <ul>
 *   <li>{@code Map<TipoConta, SaqueStrategy>} &mdash; para saques;</li>
 *   <li>{@code Map<TipoConta, RendimentoStrategy>} &mdash; para rendimento.</li>
 * </ul>
 * <p>Nenhum {@code if/else} sobre {@link TipoConta} em lugar nenhum. O
 * polimorfismo resolve &mdash; é isso que o Strategy entrega.</p>
 *
 * <p><b>Fluxos:</b></p>
 * <ul>
 *   <li><b>Depósito:</b> valida valor &gt; 0 &rarr; credita na conta
 *       &rarr; cria {@link TipoTransacao#DEPOSITO} &rarr; persiste;</li>
 *   <li><b>Saque:</b> busca conta &rarr; seleciona Strategy do tipo &rarr;
 *       {@code strategy.executar(valor, conta)} (que valida saldo e debita
 *       valor + taxa) &rarr; cria {@link TipoTransacao#SAQUE} &rarr;
 *       persiste;</li>
 *   <li><b>Transferência:</b> valida contas distintas &rarr; seleciona
 *       Strategy de saque da origem e debita &rarr; credita no destino
 *       &rarr; cria {@link TipoTransacao#TRANSFERENCIA} &rarr; persiste;</li>
 *   <li><b>Rendimento</b> (extra da spec do Módulo 5): seleciona Strategy
 *       de rendimento &rarr; {@code strategy.aplicar(conta)} (que credita
 *       e cria a Transacao) &rarr; persiste se houve rendimento.</li>
 * </ul>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     SaqueStrategy
 * @see     RendimentoStrategy
 * @see     OperacaoStrategy
 */
@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final Map<TipoConta, SaqueStrategy> saquesPorTipo;
    private final Map<TipoConta, RendimentoStrategy> rendimentosPorTipo;

    /**
     * Construtor com injeção e montagem dos mapas de Strategy.
     *
     * @param transacaoRepository repositório de transações
     * @param contaRepository     repositório de contas
     * @param strategies          todas as Strategies registradas como
     *                            {@code @Component} (Spring popula a lista)
     */
    public TransacaoService(TransacaoRepository transacaoRepository,
                            ContaRepository contaRepository,
                            List<OperacaoStrategy> strategies) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;

        Map<TipoConta, SaqueStrategy> saques = new EnumMap<>(TipoConta.class);
        Map<TipoConta, RendimentoStrategy> rendimentos = new EnumMap<>(TipoConta.class);
        for (OperacaoStrategy s : strategies) {
            if (s instanceof SaqueStrategy saque) {
                saques.put(s.tipo(), saque);
            } else if (s instanceof RendimentoStrategy rendimento) {
                rendimentos.put(s.tipo(), rendimento);
            }
        }
        this.saquesPorTipo = Map.copyOf(saques);
        this.rendimentosPorTipo = Map.copyOf(rendimentos);
    }

    // Depósito

    /**
     * Executa um depósito em uma conta.
     *
     * <p>Depósito é crédito puro &mdash; não varia por tipo de conta, então
     * <b>não</b> usa Strategy. Valida valor &gt; 0, credita, registra
     * {@link TipoTransacao#DEPOSITO}.</p>
     *
     * @param contaId id da conta que recebe o depósito
     * @param valor   valor a depositar; deve ser &gt; 0
     * @return a transação registrada
     * @throws BusinessException se a conta não existir ou o valor for inválido
     */
    @Transactional
    public Transacao depositar(Long contaId, BigDecimal valor) {
        validarValorPositivo(valor);
        Conta conta = buscarConta(contaId);
        conta.creditar(valor);
        contaRepository.save(conta);

        Transacao t = Transacao.deposito(conta, valor);
        return transacaoRepository.save(t);
    }

    // Saque

    /**
     * Executa um saque em uma conta, delegando à {@link SaqueStrategy}
     * correta para o tipo da conta.
     *
     * <p>A Strategy de saque valida saldo suficiente (considerando a taxa
     * do tipo) e debita o total. Depois, registramos a transação
     * {@link TipoTransacao#SAQUE}.</p>
     *
     * @param contaId id da conta a debitar
     * @param valor   valor a sacar; deve ser &gt; 0
     * @return a transação registrada
     * @throws BusinessException se a conta não existir, valor inválido ou
     *         saldo insuficiente
     */
    @Transactional
    public Transacao sacar(Long contaId, BigDecimal valor) {
        validarValorPositivo(valor);
        Conta conta = buscarConta(contaId);

        SaqueStrategy strategy = saquesPorTipo.get(conta.getTipo());
        if (strategy == null) {
            throw new BusinessException(
                "saque não suportado para o tipo de conta " + conta.getTipo());
        }
        strategy.executar(valor, conta);
        contaRepository.save(conta);

        Transacao t = Transacao.saque(conta, valor);
        return transacaoRepository.save(t);
    }

    // Transferência

    /**
     * Executa uma transferência entre contas distintas.
     *
     * <p>O débito na origem usa a Strategy de saque do tipo da origem
     * (respeitando a taxa). O crédito no destino é simples (depósito puro).
     * Registra <b>uma</b> transação {@link TipoTransacao#TRANSFERENCIA}
     * (não duas &mdash; a origem e o destino vivem na mesma linha).</p>
     *
     * @param origemId  conta debitada
     * @param destinoId conta creditada (distinta da origem)
     * @param valor     valor a transferir; deve ser &gt; 0
     * @return a transação registrada
     * @throws BusinessException se as contas não existirem, forem iguais,
     *         valor inválido ou saldo insuficiente
     */
    @Transactional
    public Transacao transferir(Long origemId, Long destinoId, BigDecimal valor) {
        validarValorPositivo(valor);
        if (origemId == null || destinoId == null) {
            throw new BusinessException("contas de origem e destino são obrigatórias");
        }
        if (origemId.equals(destinoId)) {
            throw new BusinessException("conta de origem e destino devem ser diferentes");
        }

        Conta origem = buscarConta(origemId);
        Conta destino = buscarConta(destinoId);

        SaqueStrategy strategy = saquesPorTipo.get(origem.getTipo());
        if (strategy == null) {
            throw new BusinessException(
                "saque não suportado para o tipo de conta " + origem.getTipo());
        }
        strategy.executar(valor, origem);
        destino.creditar(valor);
        contaRepository.save(origem);
        contaRepository.save(destino);

        Transacao t = Transacao.transferencia(origem, destino, valor);
        return transacaoRepository.save(t);
    }

    // Rendimento (complemento do Módulo 5)

    /**
     * Aplica rendimento a uma conta, delegando à {@link RendimentoStrategy}
     * do tipo.
     *
     * <p>Se a Strategy retornar {@code null} (rendimento zero &mdash; ex.:
     * saldo zero), nenhuma transação é registrada.</p>
     *
     * @param contaId id da conta a receber rendimento
     * @return a transação de rendimento registrada, ou {@code null} se
     *         não houve rendimento
     * @throws BusinessException se a conta não existir ou o tipo não
     *         suportar rendimento
     */
    @Transactional
    public Transacao aplicarRendimento(Long contaId) {
        Conta conta = buscarConta(contaId);

        RendimentoStrategy strategy = rendimentosPorTipo.get(conta.getTipo());
        if (strategy == null) {
            throw new BusinessException(
                "rendimento não suportado para o tipo de conta " + conta.getTipo());
        }
        Transacao t = strategy.aplicar(conta);
        contaRepository.save(conta);
        if (t == null) {
            return null;
        }
        return transacaoRepository.save(t);
    }

    // Consulta de extrato

    /**
     * Retorna o extrato de uma conta (transações em que ela é origem ou
     * destino), ordenado por data decrescente.
     *
     * @param contaId id da conta
     * @return lista (possivelmente vazia) de transações
     */
    @Transactional(readOnly = true)
    public List<Transacao> extrato(Long contaId) {
        return transacaoRepository
            .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(contaId, contaId);
    }

    /**
     * Executa um fluxo genérico de transação a partir do DTO.
     *
     * <p><b>Nota:</b> a spec não define esse método &mdash; ele é um
     * <b>atalho de conveniência</b> para o controller (Módulo 8) evitar
     * um {@code switch} sobre {@link TipoTransacao}. Se você preferir
     * endpoints separados (`/deposito`, `/saque`, `/transferencia`),
     * remova este método.</p>
     *
     * @param request dados de entrada
     * @return a transação registrada
     */
    @Transactional
    public Transacao executar(TransacaoRequest request) {
        return switch (request.tipo()) {
            case DEPOSITO -> depositar(request.contaDestinoId(), request.valor());
            case SAQUE -> sacar(request.contaOrigemId(), request.valor());
            case TRANSFERENCIA -> transferir(
                request.contaOrigemId(), request.contaDestinoId(), request.valor());
            case RENDIMENTO -> aplicarRendimento(request.contaDestinoId());
        };
    }

    // Helpers

    /**
     * Valida que o valor é positivo.
     *
     * @param valor valor a validar
     * @throws BusinessException se {@code valor} for nulo, zero ou negativo
     */
    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessException("valor deve ser maior que zero");
        }
    }

    /**
     * Busca uma conta pelo id, lançando {@link BusinessException} se não
     * existir.
     *
     * @param contaId id
     * @return a conta
     * @throws BusinessException se não existir
     */
    private Conta buscarConta(Long contaId) {
        if (contaId == null) {
            throw new BusinessException("id da conta é obrigatório");
        }
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new BusinessException("conta não encontrada: " + contaId));
    }
}