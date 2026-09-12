package com.soat.vendaveiculos.venda;

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

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> efetuarVenda(@Valid @RequestBody VendaRequest request) {
        Venda venda = service.efetuarVenda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VendaResponse.de(venda));
    }
}
