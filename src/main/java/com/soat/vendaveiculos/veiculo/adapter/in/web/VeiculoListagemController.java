package com.soat.vendaveiculos.veiculo.adapter.in.web;

import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosAVendaUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosVendidosUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoListagemController {

    private final ListarVeiculosAVendaUseCase listarVeiculosAVendaUseCase;
    private final ListarVeiculosVendidosUseCase listarVeiculosVendidosUseCase;

    public VeiculoListagemController(ListarVeiculosAVendaUseCase listarVeiculosAVendaUseCase,
                                      ListarVeiculosVendidosUseCase listarVeiculosVendidosUseCase) {
        this.listarVeiculosAVendaUseCase = listarVeiculosAVendaUseCase;
        this.listarVeiculosVendidosUseCase = listarVeiculosVendidosUseCase;
    }

    @GetMapping("/a-venda")
    public List<VeiculoResponse> listarAVenda() {
        return listarVeiculosAVendaUseCase.listarAVenda().stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/vendidos")
    public List<VeiculoResponse> listarVendidos() {
        return listarVeiculosVendidosUseCase.listarVendidos().stream().map(VeiculoResponse::de).toList();
    }
}
