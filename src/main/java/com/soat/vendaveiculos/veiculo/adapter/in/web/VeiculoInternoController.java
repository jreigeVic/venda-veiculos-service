package com.soat.vendaveiculos.veiculo.adapter.in.web;

import com.soat.vendaveiculos.veiculo.application.port.in.AtualizarProjecaoVeiculoUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.CriarProjecaoVeiculoUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.DadosSincronizacaoVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/interno/veiculos")
public class VeiculoInternoController {

    private final CriarProjecaoVeiculoUseCase criarProjecaoVeiculoUseCase;
    private final AtualizarProjecaoVeiculoUseCase atualizarProjecaoVeiculoUseCase;

    public VeiculoInternoController(CriarProjecaoVeiculoUseCase criarProjecaoVeiculoUseCase,
                                     AtualizarProjecaoVeiculoUseCase atualizarProjecaoVeiculoUseCase) {
        this.criarProjecaoVeiculoUseCase = criarProjecaoVeiculoUseCase;
        this.atualizarProjecaoVeiculoUseCase = atualizarProjecaoVeiculoUseCase;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoSyncRequest request) {
        VeiculoProjecao criado = criarProjecaoVeiculoUseCase.criar(paraDados(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.de(criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody VeiculoSyncRequest request) {
        VeiculoProjecao atualizado = atualizarProjecaoVeiculoUseCase.atualizar(id, paraDados(request));
        return ResponseEntity.ok(VeiculoResponse.de(atualizado));
    }

    private DadosSincronizacaoVeiculo paraDados(VeiculoSyncRequest request) {
        return new DadosSincronizacaoVeiculo(request.id(), request.marca(), request.modelo(), request.ano(),
                request.cor(), request.preco(), request.estadoConservacao());
    }
}
