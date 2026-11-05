package com.apibanco.service;

import com.apibanco.dto.ClienteRequest;
import com.apibanco.exception.BusinessException;
import com.apibanco.exception.NotFoundException;
import com.apibanco.model.Cliente;
import com.apibanco.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de <b>negócio</b> do {@link Cliente}.
 *
 * <p><b>Responsabilidades:</b></p>
 * <ul>
 *   <li>CRUD de clientes;</li>
 *   <li>validação de CPF e e-mail únicos antes de persistir (mensagem
 *       amigável, sem estourar constraint do banco);</li>
 *   <li>normalização de entrada (trim em {@code nome} e {@code email};
 *       {@code cpf} só dígitos).</li>
 * </ul>
 *
 * <p><b>Por que o Service não retorna DTO:</b> manter a camada agnóstica
 * ao HTTP. A conversão entidade &rarr; DTO fica no controller (Módulo 8).</p>
 *
 * <p><b>Unicidade &mdash; duas camadas:</b> este serviço valida antes
 * (feedback amigável); as constraints {@code UNIQUE} do banco são a
 * última defesa. Capturamos {@link org.springframework.dao.DataIntegrityViolationException}
 * e traduzimos para {@link BusinessException} &mdash; o cliente HTTP
 * nunca vê stack trace de banco.</p>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     ClienteRepository
 * @see     BusinessException
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Construtor com injeção do repositório.
     *
     * @param clienteRepository repositório de clientes; não pode ser nulo
     */
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Cria um novo cliente, validando CPF e e-mail únicos.
     *
     * @param request dados de entrada; não pode ser nulo
     * @return o cliente persistido (com {@code id} preenchido)
     * @throws BusinessException se já existir cliente com o CPF ou e-mail
     */
    @Transactional
    public Cliente criar(ClienteRequest request) {
        String cpf = request.cpf().replaceAll("\\D", "");
        String email = request.email().trim().toLowerCase();

        if (clienteRepository.findByCpf(cpf).isPresent()) {
            throw new BusinessException("já existe cliente cadastrado com o CPF informado");
        }
        if (clienteRepository.existsByEmail(email)) {
            throw new BusinessException("já existe cliente cadastrado com o e-mail informado");
        }

        Cliente cliente = new Cliente(request.nome().trim(), cpf, email);
        try {
            return clienteRepository.save(cliente);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Corrida: alguém inseriu entre a validação e o save.
            throw new BusinessException(
                "cliente já cadastrado (CPF ou e-mail em uso)", e);
        }
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador; não pode ser nulo
     * @return o cliente
     * @throws NotFoundException se não existir
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("cliente não encontrado: " + id));
    }

    /**
     * Busca um cliente pelo CPF.
     *
     * @param cpf CPF (11 dígitos, sem pontuação)
     * @return o cliente
     * @throws NotFoundException se não existir
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        String normalizado = cpf.replaceAll("\\D", "");
        return clienteRepository.findByCpf(normalizado)
            .orElseThrow(() -> new NotFoundException("cliente não encontrado para o CPF informado"));
    }

    /**
     * Lista todos os clientes.
     *
     * @return lista (possivelmente vazia) de clientes
     */
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    /**
     * Remove um cliente pelo id.
     *
     * @param id identificador; não pode ser nulo
     * @throws BusinessException se não existir
     */
    @Transactional
    public void remover(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }
}