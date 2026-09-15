package com.soat.vendaveiculos.veiculo.application;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import com.soat.vendaveiculos.veiculo.application.port.in.AtualizarProjecaoVeiculoUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.CriarProjecaoVeiculoUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.DadosSincronizacaoVeiculo;
import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosAVendaUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosVendidosUseCase;
import com.soat.vendaveiculos.veiculo.application.port.out.VeiculoProjecaoRepositoryPort;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VeiculoProjecaoService implements CriarProjecaoVeiculoUseCase, AtualizarProjecaoVeiculoUseCase,
        ListarVeiculosAVendaUseCase, ListarVeiculosVendidosUseCase {

    private final VeiculoProjecaoRepositoryPort repository;
    private final AuditoriaPort auditoriaService;

    public VeiculoProjecaoService(VeiculoProjecaoRepositoryPort repository, AuditoriaPort auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional
    public VeiculoProjecao criar(DadosSincronizacaoVeiculo dados) {
        VeiculoProjecao veiculo = VeiculoProjecao.novaDisponivel(dados.id(), dados.marca(), dados.modelo(),
                dados.ano(), dados.cor(), dados.preco(), dados.estadoConservacao());
        VeiculoProjecao salvo = repository.save(veiculo);
        auditoriaService.registrarSucesso("CRIAR_PROJECAO_VEICULO", salvo.getId(), "Projeção criada como DISPONIVEL");
        return salvo;
    }

    @Override
    @Transactional
    public VeiculoProjecao atualizar(UUID id, DadosSincronizacaoVeiculo dados) {
        VeiculoProjecao veiculo = repository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        veiculo.atualizarDadosCadastrais(dados.marca(), dados.modelo(), dados.ano(), dados.cor(),
                dados.preco(), dados.estadoConservacao());

        VeiculoProjecao salvo = repository.save(veiculo);
        auditoriaService.registrarSucesso("ATUALIZAR_PROJECAO_VEICULO", salvo.getId(), "Dados cadastrais sincronizados");
        return salvo;
    }

    @Override
    public List<VeiculoProjecao> listarAVenda() {
        return repository.findByStatusOrderByPrecoAsc(StatusVeiculo.DISPONIVEL);
    }

    @Override
    public List<VeiculoProjecao> listarVendidos() {
        return repository.findByStatusOrderByPrecoAsc(StatusVeiculo.VENDIDO);
    }
}
