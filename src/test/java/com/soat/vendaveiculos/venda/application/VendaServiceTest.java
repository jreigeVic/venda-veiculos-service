package com.soat.vendaveiculos.venda.application;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import com.soat.vendaveiculos.veiculo.application.VeiculoNaoEncontradoException;
import com.soat.vendaveiculos.veiculo.application.port.out.VeiculoProjecaoRepositoryPort;
import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoIndisponivelException;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import com.soat.vendaveiculos.venda.application.port.in.DadosVenda;
import com.soat.vendaveiculos.venda.application.port.in.NotificacaoPagamento;
import com.soat.vendaveiculos.venda.application.port.out.VendaRepositoryPort;
import com.soat.vendaveiculos.venda.domain.CpfInvalidoException;
import com.soat.vendaveiculos.venda.domain.StatusPagamento;
import com.soat.vendaveiculos.venda.domain.Venda;
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
    private VendaRepositoryPort vendaRepository;

    @Mock
    private VeiculoProjecaoRepositoryPort veiculoRepository;

    @Mock
    private AuditoriaPort auditoriaService;

    private VendaService service;

    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        service = new VendaService(vendaRepository, veiculoRepository, auditoriaService);
        veiculoId = UUID.randomUUID();
    }

    private VeiculoProjecao veiculoDisponivel() {
        return VeiculoProjecao.novaDisponivel(veiculoId, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
    }

    @Test
    void deveEfetuarVendaQuandoVeiculoDisponivelECpfValido() {
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculoDisponivel()));
        when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DadosVenda dados = new DadosVenda(veiculoId, "111.444.777-35", LocalDate.now());
        Venda venda = service.efetuar(dados);

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(venda.getCodigoPagamento()).isNotNull();
        assertThat(venda.getVeiculoId()).isEqualTo(veiculoId);

        ArgumentCaptor<VeiculoProjecao> captor = ArgumentCaptor.forClass(VeiculoProjecao.class);
        verify(veiculoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void deveRejeitarCpfInvalido() {
        DadosVenda dados = new DadosVenda(veiculoId, "111.111.111-11", LocalDate.now());

        assertThatThrownBy(() -> service.efetuar(dados))
                .isInstanceOf(CpfInvalidoException.class);

        verify(vendaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVeiculoInexistente() {
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.empty());
        DadosVenda dados = new DadosVenda(veiculoId, "111.444.777-35", LocalDate.now());

        assertThatThrownBy(() -> service.efetuar(dados))
                .isInstanceOf(VeiculoNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarVeiculoIndisponivel() {
        VeiculoProjecao reservado = veiculoDisponivel();
        reservado.reservar();
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(reservado));

        DadosVenda dados = new DadosVenda(veiculoId, "111.444.777-35", LocalDate.now());

        assertThatThrownBy(() -> service.efetuar(dados))
                .isInstanceOf(VeiculoIndisponivelException.class);
    }

    @Test
    void deveMarcarVeiculoComoVendidoQuandoWebhookAprovado() {
        Venda venda = vendaPendente();
        VeiculoProjecao veiculo = veiculoDisponivel();
        veiculo.reservar();

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        service.processar(new NotificacaoPagamento(venda.getCodigoPagamento(), NotificacaoPagamento.Status.APROVADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.VENDIDO);
    }

    @Test
    void deveVoltarVeiculoParaDisponivelQuandoWebhookCancelado() {
        Venda venda = vendaPendente();
        VeiculoProjecao veiculo = veiculoDisponivel();
        veiculo.reservar();

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        service.processar(new NotificacaoPagamento(venda.getCodigoPagamento(), NotificacaoPagamento.Status.CANCELADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void webhookDeveSerIdempotenteQuandoVendaJaEstaEmEstadoFinal() {
        Venda venda = vendaAprovada();

        when(vendaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(venda));

        service.processar(new NotificacaoPagamento(venda.getCodigoPagamento(), NotificacaoPagamento.Status.CANCELADO));

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        verify(veiculoRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarWebhookComCodigoPagamentoInexistente() {
        UUID codigo = UUID.randomUUID();
        when(vendaRepository.findByCodigoPagamento(codigo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processar(new NotificacaoPagamento(codigo, NotificacaoPagamento.Status.APROVADO)))
                .isInstanceOf(PagamentoNaoEncontradoException.class);
    }

    private Venda vendaPendente() {
        return Venda.criar(veiculoId, "111.444.777-35", LocalDate.now());
    }

    private Venda vendaAprovada() {
        Venda venda = vendaPendente();
        venda.aprovar();
        return venda;
    }
}
