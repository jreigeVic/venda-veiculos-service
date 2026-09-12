package com.soat.vendaveiculos.veiculo;

import com.soat.vendaveiculos.auditoria.AuditoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoProjecaoService {

    private final VeiculoProjecaoRepository repository;
    private final AuditoriaService auditoriaService;

    public VeiculoProjecaoService(VeiculoProjecaoRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public VeiculoProjecao criar(VeiculoSyncRequest request) {
        VeiculoProjecao veiculo = new VeiculoProjecao(
                request.id(),
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor(),
                request.preco(),
                request.estadoConservacao(),
                StatusVeiculo.DISPONIVEL,
                null
        );
        VeiculoProjecao salvo = repository.save(veiculo);
        auditoriaService.registrarSucesso("CRIAR_PROJECAO_VEICULO", salvo.getId(), "Projeção criada como DISPONIVEL");
        return salvo;
    }

    @Transactional
    public VeiculoProjecao atualizar(java.util.UUID id, VeiculoSyncRequest request) {
        VeiculoProjecao veiculo = repository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        veiculo.setCor(request.cor());
        veiculo.setPreco(request.preco());
        veiculo.setEstadoConservacao(request.estadoConservacao());

        VeiculoProjecao salvo = repository.save(veiculo);
        auditoriaService.registrarSucesso("ATUALIZAR_PROJECAO_VEICULO", salvo.getId(), "Dados cadastrais sincronizados");
        return salvo;
    }

    public List<VeiculoProjecao> listarAVenda() {
        return repository.findByStatusOrderByPrecoAsc(StatusVeiculo.DISPONIVEL);
    }

    public List<VeiculoProjecao> listarVendidos() {
        return repository.findByStatusOrderByPrecoAsc(StatusVeiculo.VENDIDO);
    }
}
