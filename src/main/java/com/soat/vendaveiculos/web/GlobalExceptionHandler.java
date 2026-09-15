package com.soat.vendaveiculos.web;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import com.soat.vendaveiculos.venda.domain.CpfInvalidoException;
import com.soat.vendaveiculos.venda.application.PagamentoNaoEncontradoException;
import com.soat.vendaveiculos.veiculo.domain.VeiculoIndisponivelException;
import com.soat.vendaveiculos.veiculo.application.VeiculoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditoriaPort auditoriaService;

    public GlobalExceptionHandler(AuditoriaPort auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratar(VeiculoNaoEncontradoException ex) {
        auditoriaService.registrarErro("VEICULO_NAO_ENCONTRADO", ex.getVeiculoId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(VeiculoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratar(VeiculoIndisponivelException ex) {
        auditoriaService.registrarErro("VEICULO_INDISPONIVEL", ex.getVeiculoId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(PagamentoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratar(PagamentoNaoEncontradoException ex) {
        auditoriaService.registrarErro("PAGAMENTO_NAO_ENCONTRADO", null, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ErroResponse> tratar(CpfInvalidoException ex) {
        auditoriaService.registrarErro("CPF_INVALIDO", null, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratar(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse("Requisição inválida"));
    }
}
