package com.soat.vendaveiculos.venda.application;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import com.soat.vendaveiculos.veiculo.application.VeiculoNaoEncontradoException;
import com.soat.vendaveiculos.veiculo.application.port.out.VeiculoProjecaoRepositoryPort;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import com.soat.vendaveiculos.venda.application.port.in.DadosVenda;
import com.soat.vendaveiculos.venda.application.port.in.EfetuarVendaUseCase;
import com.soat.vendaveiculos.venda.application.port.in.NotificacaoPagamento;
import com.soat.vendaveiculos.venda.application.port.in.ProcessarWebhookPagamentoUseCase;
import com.soat.vendaveiculos.venda.application.port.out.VendaRepositoryPort;
import com.soat.vendaveiculos.venda.domain.CpfInvalidoException;
import com.soat.vendaveiculos.venda.domain.CpfValidador;
import com.soat.vendaveiculos.venda.domain.Venda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendaService implements EfetuarVendaUseCase, ProcessarWebhookPagamentoUseCase {

    private final VendaRepositoryPort vendaRepository;
    private final VeiculoProjecaoRepositoryPort veiculoRepository;
    private final AuditoriaPort auditoriaService;

    public VendaService(VendaRepositoryPort vendaRepository, VeiculoProjecaoRepositoryPort veiculoRepository,
                         AuditoriaPort auditoriaService) {
        this.vendaRepository = vendaRepository;
        this.veiculoRepository = veiculoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional
    public Venda efetuar(DadosVenda dados) {
        if (!CpfValidador.isValido(dados.cpfComprador())) {
            throw new CpfInvalidoException(dados.cpfComprador());
        }

        VeiculoProjecao veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(dados.veiculoId()));

        veiculo.reservar();
        veiculoRepository.save(veiculo);

        Venda venda = Venda.criar(veiculo.getId(), dados.cpfComprador(), dados.dataVenda());
        Venda salva = vendaRepository.save(venda);

        auditoriaService.registrarSucesso("EFETUAR_VENDA", salva.getId(), "Venda criada, veículo reservado, aguardando pagamento");
        return salva;
    }

    @Override
    @Transactional
    public void processar(NotificacaoPagamento notificacao) {
        Venda venda = vendaRepository.findByCodigoPagamento(notificacao.codigoPagamento())
                .orElseThrow(() -> new PagamentoNaoEncontradoException(notificacao.codigoPagamento()));

        if (venda.estaEmEstadoFinal()) {
            auditoriaService.registrarSucesso("WEBHOOK_PAGAMENTO", venda.getId(),
                    "Notificação ignorada — venda já estava em estado final (" + venda.getStatusPagamento() + ")");
            return;
        }

        VeiculoProjecao veiculo = veiculoRepository.findById(venda.getVeiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException(venda.getVeiculoId()));

        if (notificacao.status() == NotificacaoPagamento.Status.APROVADO) {
            venda.aprovar();
            veiculo.marcarVendido();
        } else {
            venda.cancelar();
            veiculo.liberar();
        }

        vendaRepository.save(venda);
        veiculoRepository.save(veiculo);

        auditoriaService.registrarSucesso("WEBHOOK_PAGAMENTO", venda.getId(), "Pagamento " + notificacao.status());
    }
}
