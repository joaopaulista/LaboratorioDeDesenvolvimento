package br.edu.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServicoCheckoutTest {

    private static final String CLIENTE = "cliente-1";
    private static final String EMAIL = "cliente@exemplo.com";
    private static final String SKU = "SKU-123";

    @Mock
    private EstoqueGateway estoqueGateway;

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private Notificador notificador;

    private ServicoCheckout servico;

    @BeforeEach
    void criarServicoReal() {
        servico = new ServicoCheckout(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }

    @Test
    void deveRejeitarSolicitacaoNulaSemConsultarColaboradores() {
        assertThrows(IllegalArgumentException.class, () -> servico.finalizar(null));
        verifyNoInteractions(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }

    static Stream<Arguments> solicitacoesInvalidas() {
        BigDecimal preco = new BigDecimal("10.00");
        return Stream.of(
                Arguments.of("clienteId nulo", new SolicitacaoCheckout(null, EMAIL, SKU, 1, preco)),
                Arguments.of("clienteId vazio", new SolicitacaoCheckout("", EMAIL, SKU, 1, preco)),
                Arguments.of("clienteId em branco", new SolicitacaoCheckout("   ", EMAIL, SKU, 1, preco)),
                Arguments.of("email nulo", new SolicitacaoCheckout(CLIENTE, null, SKU, 1, preco)),
                Arguments.of("email vazio", new SolicitacaoCheckout(CLIENTE, "", SKU, 1, preco)),
                Arguments.of("email em branco", new SolicitacaoCheckout(CLIENTE, "   ", SKU, 1, preco)),
                Arguments.of("sku nulo", new SolicitacaoCheckout(CLIENTE, EMAIL, null, 1, preco)),
                Arguments.of("sku vazio", new SolicitacaoCheckout(CLIENTE, EMAIL, "", 1, preco)),
                Arguments.of("sku em branco", new SolicitacaoCheckout(CLIENTE, EMAIL, "   ", 1, preco)),
                Arguments.of("quantidade zero", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 0, preco)),
                Arguments.of("quantidade negativa", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, -1, preco)),
                Arguments.of("preço nulo", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, null)),
                Arguments.of("preço zero", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, BigDecimal.ZERO)),
                Arguments.of("preço negativo", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, new BigDecimal("-0.01"))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solicitacoesInvalidas")
    void deveRejeitarCamposInvalidosSemConsultarColaboradores(
            String cenario, SolicitacaoCheckout solicitacao) {
        assertThrows(IllegalArgumentException.class, () -> servico.finalizar(solicitacao));
        verifyNoInteractions(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }

    // ---------- Compra aprovada ----------

    @Test
    void deveRegistrarENotificarCompraAprovadaNaOrdemDoContrato() {
        BigDecimal total = new BigDecimal("59.70");
        PedidoConfirmado pedidoEsperado = new PedidoConfirmado(CLIENTE, EMAIL, SKU, 3, total, "TX-1");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenReturn(new Pagamento(true, "TX-1"));
        when(pedidoRepository.salvar(pedidoEsperado)).thenReturn("PED-42");

        ResultadoCheckout resultado = servico.finalizar(solicitacaoValida(3, "19.90"));

        assertEquals("PED-42", resultado.numeroPedido());
        assertEquals(total, resultado.total());
        assertTrue(resultado.notificacaoEnviada());

        InOrder ordem = inOrder(estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
        ordem.verify(estoqueGateway, times(1)).reservar(SKU, 3);
        ordem.verify(pagamentoGateway, times(1)).cobrar(CLIENTE, total);
        ordem.verify(pedidoRepository, times(1)).salvar(pedidoEsperado);
        ordem.verify(notificador, times(1)).enviarConfirmacao(EMAIL, "PED-42", total);
        verify(notificador, times(1)).enviarConfirmacao(anyString(), anyString(), any(BigDecimal.class));
        verify(estoqueGateway, never()).liberar(anyString(), anyInt());
    }

    @Test
    void deveSalvarPedidoComTodosOsDadosRecebidosETransacaoDoPagamento() {
        BigDecimal total = new BigDecimal("59.70");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenReturn(new Pagamento(true, "TX-99"));
        when(pedidoRepository.salvar(any(PedidoConfirmado.class))).thenReturn("PED-1");

        servico.finalizar(solicitacaoValida(3, "19.90"));

        ArgumentCaptor<PedidoConfirmado> captor = ArgumentCaptor.forClass(PedidoConfirmado.class);
        verify(pedidoRepository, times(1)).salvar(captor.capture());
        PedidoConfirmado salvo = captor.getValue();
        assertEquals(CLIENTE, salvo.clienteId());
        assertEquals(EMAIL, salvo.email());
        assertEquals(SKU, salvo.sku());
        assertEquals(3, salvo.quantidade());
        assertEquals(total, salvo.total());
        assertEquals("TX-99", salvo.transacaoId());
    }

    @Test
    void deveAceitarQuantidadeMinimaMantendoOPrecoComoTotal() {
        BigDecimal preco = new BigDecimal("42.50");
        when(estoqueGateway.reservar(SKU, 1)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, preco)).thenReturn(new Pagamento(true, "TX-2"));
        when(pedidoRepository.salvar(any(PedidoConfirmado.class))).thenReturn("PED-2");

        ResultadoCheckout resultado = servico.finalizar(solicitacaoValida(1, "42.50"));

        assertEquals(preco, resultado.total());
        verify(pagamentoGateway, times(1)).cobrar(CLIENTE, preco);
    }

    @Test
    void deveCalcularTotalComPrecisaoSemArredondar() {
        BigDecimal total = new BigDecimal("0.015");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenReturn(new Pagamento(true, "TX-3"));
        when(pedidoRepository.salvar(any(PedidoConfirmado.class))).thenReturn("PED-3");

        ResultadoCheckout resultado = servico.finalizar(solicitacaoValida(3, "0.005"));

        assertEquals(total, resultado.total());
        verify(pagamentoGateway, times(1)).cobrar(CLIENTE, total);
        verify(notificador, times(1)).enviarConfirmacao(EMAIL, "PED-3", total);
    }

    // ---------- Falhas ----------

    @Test
    void deveRecusarSemEstoqueSemCobrarRegistrarNotificarOuLiberar() {
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(false);

        assertThrows(EstoqueInsuficienteException.class,
                () -> servico.finalizar(solicitacaoValida(3, "19.90")));

        verify(estoqueGateway, times(1)).reservar(SKU, 3);
        verify(estoqueGateway, never()).liberar(anyString(), anyInt());
        verifyNoInteractions(pagamentoGateway, pedidoRepository, notificador);
    }

    @Test
    void deveLiberarReservaUmaVezQuandoPagamentoForRecusado() {
        BigDecimal total = new BigDecimal("59.70");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenReturn(new Pagamento(false, null));

        assertThrows(PagamentoRecusadoException.class,
                () -> servico.finalizar(solicitacaoValida(3, "19.90")));

        InOrder ordem = inOrder(estoqueGateway, pagamentoGateway);
        ordem.verify(estoqueGateway, times(1)).reservar(SKU, 3);
        ordem.verify(pagamentoGateway, times(1)).cobrar(CLIENTE, total);
        ordem.verify(estoqueGateway, times(1)).liberar(SKU, 3);
        verifyNoInteractions(pedidoRepository, notificador);
    }

    @Test
    void deveLiberarReservaEPropagarMesmaExcecaoNaFalhaTecnicaDoPagamento() {
        BigDecimal total = new BigDecimal("59.70");
        FalhaPagamentoException falha = new FalhaPagamentoException("gateway indisponível");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenThrow(falha);

        FalhaPagamentoException lancada = assertThrows(FalhaPagamentoException.class,
                () -> servico.finalizar(solicitacaoValida(3, "19.90")));

        assertSame(falha, lancada);
        InOrder ordem = inOrder(estoqueGateway, pagamentoGateway);
        ordem.verify(estoqueGateway, times(1)).reservar(SKU, 3);
        ordem.verify(pagamentoGateway, times(1)).cobrar(CLIENTE, total);
        ordem.verify(estoqueGateway, times(1)).liberar(SKU, 3);
        verifyNoInteractions(pedidoRepository, notificador);
    }

    @Test
    void naoDeveDesfazerCompraQuandoNotificacaoFalhar() {
        BigDecimal total = new BigDecimal("59.70");
        when(estoqueGateway.reservar(SKU, 3)).thenReturn(true);
        when(pagamentoGateway.cobrar(CLIENTE, total)).thenReturn(new Pagamento(true, "TX-7"));
        when(pedidoRepository.salvar(any(PedidoConfirmado.class))).thenReturn("PED-7");
        doThrow(new FalhaNotificacaoException("SMTP fora do ar"))
                .when(notificador).enviarConfirmacao(EMAIL, "PED-7", total);

        ResultadoCheckout resultado = servico.finalizar(solicitacaoValida(3, "19.90"));

        assertEquals("PED-7", resultado.numeroPedido());
        assertEquals(total, resultado.total());
        assertFalse(resultado.notificacaoEnviada());
        verify(pagamentoGateway, times(1)).cobrar(CLIENTE, total);
        verify(pedidoRepository, times(1)).salvar(any(PedidoConfirmado.class));
        verify(estoqueGateway, never()).liberar(anyString(), anyInt());
    }

    // ---------- Auxiliares ----------

    private static SolicitacaoCheckout solicitacaoValida(int quantidade, String precoUnitario) {
        return new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, quantidade, new BigDecimal(precoUnitario));
    }
}
