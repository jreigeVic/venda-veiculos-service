package com.soat.vendaveiculos.venda.adapter.in.web;

import com.soat.vendaveiculos.venda.application.port.in.DadosVenda;
import com.soat.vendaveiculos.venda.application.port.in.EfetuarVendaUseCase;
import com.soat.vendaveiculos.venda.domain.Venda;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final EfetuarVendaUseCase efetuarVendaUseCase;

    public VendaController(EfetuarVendaUseCase efetuarVendaUseCase) {
        this.efetuarVendaUseCase = efetuarVendaUseCase;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> efetuarVenda(@Valid @RequestBody VendaRequest request) {
        Venda venda = efetuarVendaUseCase.efetuar(new DadosVenda(request.veiculoId(), request.cpfComprador(), request.dataVenda()));
        return ResponseEntity.status(HttpStatus.CREATED).body(VendaResponse.de(venda));
    }
}
