package com.apibanco.service;

import com.apibanco.dto.ContaRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.model.Cliente;
import com.apibanco.model.Conta;
import com.apibanco.pattern.factory.ContaFactory;
import com.apibanco.repository.ClienteRepository;
import com.apibanco.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serviço de <b>negócio</b> da {@link Conta}.
 *
 * <p><b>Responsabilidades:</b></p>
 * <ul>
 *   <li>abrir conta (gera número único, usa {@link ContaFactory} +
 *       {@code ContaBuilder} via Factory);</li>
 *   <li>consultar saldo;</li>
 *   <li>listar contas por cliente.</li>
 * </ul>
 *
 * <p><b>Fluxo de abertura:</b></p>
 * <ol>
 *   <li>valida cliente (existe, via {@link ClienteRepository});</li>
 *   <li>gera número único (checa {@link ContaRepository#existsByNumero});</li>
 *   <li>delega construção ao {@link ContaFactory} &mdash; que usa o
 *       {@code ContaBuilder} correto;</li>
 *   <li>persiste via {@link ContaRepository#save}.</li>
 * </ol>
 *
 * <p><b>Sobre a geração do número:</b> simplificação didática baseada em
 * {@code System.currentTimeMillis()} + sufixo aleatório, com checagem de
 * colisão. Em produção, usar algoritmo do banco (ex.: {@code 0001-CCCCCC-D}
 * com dígito verificador). Documentado em comentário &mdash; sem inflar o
 * escopo do projeto.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ContaFactory
 * @see     ContaRepository
 * @see     ClienteRepository
 */
@Service
public class ContaService {

    private static final int MAX_TENTATIVAS_NUMERO = 5;

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;
    private final ContaFactory contaFactory;

    /**
     * Construtor com injeção das dependências.
     *
     * @param contaRepository   repositório de contas
     * @param clienteRepository repositório de clientes
     * @param contaFactory      factory de contas
     */
    public ContaService(ContaRepository contaRepository,
                        ClienteRepository clienteRepository,
                        ContaFactory contaFactory) {
        this.contaRepository = contaRepository;
        this.clienteRepository = clienteRepository;
        this.contaFactory = contaFactory;
    }

    /**
     * Abre uma nova conta para o cliente informado.
     *
     * @param request dados de entrada; não pode ser nulo
     * @return a conta persistida (com número único)
     * @throws BusinessException se o cliente não existir
     */
    @Transactional
    public Conta abrir(ContaRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
            .orElseThrow(() -> new BusinessException(
                "cliente não encontrado: " + request.clienteId()));

        String numero = gerarNumeroUnico();
        Conta conta = contaFactory.criarConta(
            request.tipo(),
            cliente,
            numero,
            request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO);

        return contaRepository.save(conta);
    }

    /**
     * Consulta o saldo atual de uma conta.
     *
     * @param contaId identificador da conta; não pode ser nulo
     * @return o saldo atual
     * @throws BusinessException se a conta não existir
     */
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(Long contaId) {
        return buscarPorId(contaId).getSaldo();
    }

    /**
     * Lista as contas de um cliente.
     *
     * @param clienteId identificador do cliente; não pode ser nulo
     * @return lista (possivelmente vazia) de contas
     */
    @Transactional(readOnly = true)
    public List<Conta> listarPorCliente(Long clienteId) {
        return contaRepository.findByClienteId(clienteId);
    }

    /**
     * Busca uma conta pelo id.
     *
     * @param contaId identificador da conta; não pode ser nulo
     * @return a conta
     * @throws BusinessException se a conta não existir
     */
    @Transactional(readOnly = true)
    public Conta buscarPorId(Long contaId) {
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new BusinessException("conta não encontrada: " + contaId));
    }

    // Geração de número

    /**
     * Gera um número único para conta, checando colisão no repositório.
     *
     * <p><b>Simplificação:</b> usa {@code System.currentTimeMillis()} +
     * sufixo aleatório de 3 dígitos, com até {@value #MAX_TENTATIVAS_NUMERO}
     * tentativas. Em produção, um gerador real (sequencial por agência,
     * com dígito verificador).</p>
     *
     * @return número único no banco
     * @throws BusinessException se não conseguir gerar após N tentativas
     *         (extremamente improvável no H2 em memória)
     */
    private String gerarNumeroUnico() {
        for (int i = 0; i < MAX_TENTATIVAS_NUMERO; i++) {
            int sufixo = ThreadLocalRandom.current().nextInt(100, 1000);
            String numero = "%d-%03d".formatted(System.currentTimeMillis(), sufixo);
            if (!contaRepository.existsByNumero(numero)) {
                return numero;
            }
        }
        throw new BusinessException(
            "não foi possível gerar número único de conta após "
                + MAX_TENTATIVAS_NUMERO + " tentativas");
    }
}