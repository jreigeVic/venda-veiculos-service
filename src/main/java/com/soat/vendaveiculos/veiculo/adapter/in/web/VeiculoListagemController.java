package com.soat.vendaveiculos.veiculo.adapter.in.web;

import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosAVendaUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosPorStatusUseCase;
import com.soat.vendaveiculos.veiculo.application.port.in.ListarVeiculosVendidosUseCase;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoListagemController {

    private final ListarVeiculosAVendaUseCase listarVeiculosAVendaUseCase;
    private final ListarVeiculosVendidosUseCase listarVeiculosVendidosUseCase;
    private final ListarVeiculosPorStatusUseCase listarVeiculosPorStatusUseCase;

    public VeiculoListagemController(ListarVeiculosAVendaUseCase listarVeiculosAVendaUseCase,
                                      ListarVeiculosVendidosUseCase listarVeiculosVendidosUseCase,
                                      ListarVeiculosPorStatusUseCase listarVeiculosPorStatusUseCase) {
        this.listarVeiculosAVendaUseCase = listarVeiculosAVendaUseCase;
        this.listarVeiculosVendidosUseCase = listarVeiculosVendidosUseCase;
        this.listarVeiculosPorStatusUseCase = listarVeiculosPorStatusUseCase;
    }

    @GetMapping("/a-venda")
    public List<VeiculoResponse> listarAVenda() {
        return listarVeiculosAVendaUseCase.listarAVenda().stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/vendidos")
    public List<VeiculoResponse> listarVendidos() {
        return listarVeiculosVendidosUseCase.listarVendidos().stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping(params = "status")
    public List<VeiculoResponse> listarPorStatus(@RequestParam StatusVeiculo status) {
        return listarVeiculosPorStatusUseCase.listarPorStatus(status).stream().map(VeiculoResponse::de).toList();
    }
}
