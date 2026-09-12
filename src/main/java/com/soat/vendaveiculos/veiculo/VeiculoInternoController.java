package com.soat.vendaveiculos.veiculo;

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

    private final VeiculoProjecaoService service;

    public VeiculoInternoController(VeiculoProjecaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoSyncRequest request) {
        VeiculoProjecao criado = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.de(criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody VeiculoSyncRequest request) {
        VeiculoProjecao atualizado = service.atualizar(id, request);
        return ResponseEntity.ok(VeiculoResponse.de(atualizado));
    }
}
