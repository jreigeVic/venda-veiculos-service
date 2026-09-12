package com.soat.vendaveiculos.veiculo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoListagemController {

    private final VeiculoProjecaoService service;

    public VeiculoListagemController(VeiculoProjecaoService service) {
        this.service = service;
    }

    @GetMapping("/a-venda")
    public List<VeiculoResponse> listarAVenda() {
        return service.listarAVenda().stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/vendidos")
    public List<VeiculoResponse> listarVendidos() {
        return service.listarVendidos().stream().map(VeiculoResponse::de).toList();
    }
}
