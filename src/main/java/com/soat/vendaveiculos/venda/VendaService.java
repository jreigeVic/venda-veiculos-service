package com.soat.vendaveiculos.venda;

import com.soat.vendaveiculos.auditoria.AuditoriaService;
import com.soat.vendaveiculos.veiculo.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.VeiculoNaoEncontradoException;
import com.soat.vendaveiculos.veiculo.VeiculoProjecao;
import com.soat.vendaveiculos.veiculo.VeiculoProjecaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final VeiculoProjecaoRepository veiculoRepository;
    private final AuditoriaService auditoriaService;

    public VendaService(VendaRepository vendaRepository, VeiculoProjecaoRepository veiculoRepository, AuditoriaService auditoriaService) {
        this.vendaRepository = vendaRepository;
        this.veiculoRepository = veiculoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Venda efetuarVenda(VendaRequest request) {
        if (!CpfValidador.isValido(request.cpfComprador())) {
            throw new CpfInvalidoException(request.cpfComprador());
        }

        VeiculoProjecao veiculo = veiculoRepository.findById(request.veiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(request.veiculoId()));

        if (!veiculo.estaDisponivel()) {
            throw new VeiculoIndisponivelException(veiculo.getId());
        }

        veiculo.setStatus(StatusVeiculo.RESERVADO);
        veiculoRepository.save(veiculo);

        Instant agora = Instant.now();
        Venda venda = new Venda(
                null,
                veiculo.getId(),
                request.cpfComprador(),
                request.dataVenda(),
                UUID.randomUUID(),
                StatusPagamento.PENDENTE,
                agora,
                agora
        );
        Venda salva = vendaRepository.save(venda);

        auditoriaService.registrarSucesso("EFETUAR_VENDA", salva.getId(), "Venda criada, veículo reservado, aguardando pagamento");
        return salva;
    }

    @Transactional
    public void processarWebhook(WebhookRequest request) {
        Venda venda = vendaRepository.findByCodigoPagamento(request.codigoPagamento())
                .orElseThrow(() -> new PagamentoNaoEncontradoException(request.codigoPagamento()));

        if (venda.estaEmEstadoFinal()) {
            auditoriaService.registrarSucesso("WEBHOOK_PAGAMENTO", venda.getId(),
                    "Notificação ignorada — venda já estava em estado final (" + venda.getStatusPagamento() + ")");
            return;
        }

        VeiculoProjecao veiculo = veiculoRepository.findById(venda.getVeiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(venda.getVeiculoId()));

        if (request.status() == WebhookRequest.StatusPagamentoWebhook.APROVADO) {
            venda.setStatusPagamento(StatusPagamento.APROVADO);
            veiculo.setStatus(StatusVeiculo.VENDIDO);
        } else {
            venda.setStatusPagamento(StatusPagamento.CANCELADO);
            veiculo.setStatus(StatusVeiculo.DISPONIVEL);
        }
        venda.setAtualizadoEm(Instant.now());

        vendaRepository.save(venda);
        veiculoRepository.save(veiculo);

        auditoriaService.registrarSucesso("WEBHOOK_PAGAMENTO", venda.getId(), "Pagamento " + request.status());
    }
}
