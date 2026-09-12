package com.soat.vendaveiculos.venda;

import com.soat.vendaveiculos.auditoria.AuditoriaService;
import com.soat.vendaveiculos.veiculo.EstadoConservacao;
import com.soat.vendaveiculos.veiculo.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.VeiculoNaoEncontradoException;
import com.soat.vendaveiculos.veiculo.VeiculoProjecao;
import com.soat.vendaveiculos.veiculo.VeiculoProjecaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private VeiculoProjecaoRepository veiculoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private VendaService service;

    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        service = new VendaService(vendaRepository, veiculoRepository, auditoriaService);
        veiculoId = UUID.randomUUID();
    }

    private VeiculoProjecao veiculoDisponivel() {
        return new VeiculoProjecao(veiculoId, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.DISPONIVEL, null);
    }

    @Test
    void deveEfetuarVendaQuandoVeiculoDisponivelECpfValido() {
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculoDisponivel()));
        when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VendaRequest request = new VendaRequest(veiculoId, "111.444.777-35", LocalDate.now());
        Venda venda = service.efetuarVenda(request);

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(venda.getCodigoPagamento()).isNotNull();
        assertThat(venda.getVeiculoId()).isEqualTo(veiculoId);

        ArgumentCaptor<VeiculoProjecao> captor = ArgumentCaptor.forClass(VeiculoProjecao.class);
        verify(veiculoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void deveRejeitarCpfInvalido() {
        VendaRequest request = new VendaRequest(veiculoId, "111.111.111-11", LocalDate.now());

        assertThatThrownBy(() -> service.efetuarVenda(request))
                .isInstanceOf(CpfInvalidoException.class);

        verify(vendaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVeiculoInexistente() {
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.empty());
        VendaRequest request = new VendaRequest(veiculoId, "111.444.777-35", LocalDate.now());

        assertThatThrownBy(() -> service.efetuarVenda(request))
                .isInstanceOf(VeiculoNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarVeiculoIndisponivel() {
        VeiculoProjecao reservado = veiculoDisponivel();
        reservado.setStatus(StatusVeiculo.RESERVADO);
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(reservado));

        VendaRequest request = new VendaRequest(veiculoId, "111.444.777-35", LocalDate.now());

        assertThatThrownBy(() -> service.efetuarVenda(request))
                .isInstanceOf(VeiculoIndisponivelException.class);
    }

    @Test
    void deveMarcarVeiculoComoVendidoQuandoWebhookAprovado() {
        Venda venda = vendaPendente();
        VeiculoProjecao veiculo = veiculoDisponivel();
        veiculo.setStatus(StatusVeiculo.RESERVADO);

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        service.processarWebhook(new WebhookRequest(venda.getCodigoPagamento(), WebhookRequest.StatusPagamentoWebhook.APROVADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.VENDIDO);
    }

    @Test
    void deveVoltarVeiculoParaDisponivelQuandoWebhookCancelado() {
        Venda venda = vendaPendente();
        VeiculoProjecao veiculo = veiculoDisponivel();
        veiculo.setStatus(StatusVeiculo.RESERVADO);

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        service.processarWebhook(new WebhookRequest(venda.getCodigoPagamento(), WebhookRequest.StatusPagamentoWebhook.CANCELADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void webhookDeveSerIdempotenteQuandoVendaJaEstaEmEstadoFinal() {
        Venda venda = vendaPendente();
        venda.setStatusPagamento(StatusPagamento.APROVADO);

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));

        service.processarWebhook(new WebhookRequest(venda.getCodigoPagamento(), WebhookRequest.StatusPagamentoWebhook.CANCELADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        verify(veiculoRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarWebhookComCodigoPagamentoInexistente() {
        UUID codigo = UUID.randomUUID();
        when(vendaRepository.findByCodigoPagamento(codigo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processarWebhook(new WebhookRequest(codigo, WebhookRequest.StatusPagamentoWebhook.APROVADO)))
                .isInstanceOf(PagamentoNaoEncontradoException.class);
    }

    private Venda vendaPendente() {
        return new Venda(UUID.randomUUID(), veiculoId, "111.444.777-35", LocalDate.now(),
                UUID.randomUUID(), StatusPagamento.PENDENTE, java.time.Instant.now(), java.time.Instant.now());
    }
}
