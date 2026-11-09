package br.edu.fatec.sjc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final String CODIGO_PRODUTO = "P001";
    private static final int QUANTIDADE = 2;
    private static final BigDecimal VALOR_TOTAL = new BigDecimal("7000.00");

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private PagamentoGateway pagamentoGateway;

    @InjectMocks
    private CheckoutService checkoutService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido(CODIGO_PRODUTO, QUANTIDADE, new BigDecimal("3500.00"));
    }

    @Test
    @DisplayName("Cenário A: com estoque e pagamento aprovado, baixa o estoque e retorna true")
    void deveFinalizarCompraQuandoHaEstoqueEPagamentoAprovado() {
        when(estoqueService.temEstoque(CODIGO_PRODUTO, QUANTIDADE)).thenReturn(true);
        when(pagamentoGateway.processarPagamento(VALOR_TOTAL)).thenReturn(true);

        boolean resultado = checkoutService.finalizarCompra(pedido);

        assertTrue(resultado);
        verify(estoqueService).baixarEstoque(CODIGO_PRODUTO, QUANTIDADE);
    }

    @Test
    @DisplayName("Cenário B: sem estoque, o gateway nunca é chamado e retorna false")
    void naoDeveChamarGatewayQuandoNaoHaEstoque() {
        when(estoqueService.temEstoque(CODIGO_PRODUTO, QUANTIDADE)).thenReturn(false);

        boolean resultado = checkoutService.finalizarCompra(pedido);

        assertFalse(resultado);
        verify(pagamentoGateway, never()).processarPagamento(any());
        verify(estoqueService, never()).baixarEstoque(any(), anyInt());
    }

    @Test
    @DisplayName("Cenário C: com estoque e pagamento negado, não baixa o estoque e retorna false")
    void naoDeveBaixarEstoqueQuandoPagamentoNegado() {
        when(estoqueService.temEstoque(CODIGO_PRODUTO, QUANTIDADE)).thenReturn(true);
        when(pagamentoGateway.processarPagamento(VALOR_TOTAL)).thenReturn(false);

        boolean resultado = checkoutService.finalizarCompra(pedido);

        assertFalse(resultado);
        verify(estoqueService, never()).baixarEstoque(any(), anyInt());
    }

    @Test
    @DisplayName("Cenário D: cobra o valor total e executa estoque -> pagamento -> baixa, nessa ordem")
    void deveExecutarEtapasNaOrdemCorreta() {
        when(estoqueService.temEstoque(CODIGO_PRODUTO, QUANTIDADE)).thenReturn(true);
        when(pagamentoGateway.processarPagamento(VALOR_TOTAL)).thenReturn(true);

        checkoutService.finalizarCompra(pedido);

        InOrder ordem = inOrder(estoqueService, pagamentoGateway);
        ordem.verify(estoqueService).temEstoque(CODIGO_PRODUTO, QUANTIDADE);
        ordem.verify(pagamentoGateway).processarPagamento(VALOR_TOTAL);
        ordem.verify(estoqueService).baixarEstoque(CODIGO_PRODUTO, QUANTIDADE);
        verifyNoMoreInteractions(estoqueService, pagamentoGateway);
    }

    @Test
    @DisplayName("Cenário E: gateway indisponível (lança exceção), não baixa o estoque e retorna false")
    void deveRetornarFalseQuandoGatewayLancaExcecao() {
        when(estoqueService.temEstoque(CODIGO_PRODUTO, QUANTIDADE)).thenReturn(true);
        when(pagamentoGateway.processarPagamento(VALOR_TOTAL))
                .thenThrow(new RuntimeException("Gateway indisponível"));

        boolean resultado = assertDoesNotThrow(() -> checkoutService.finalizarCompra(pedido));

        assertFalse(resultado);
        verify(estoqueService, never()).baixarEstoque(any(), anyInt());
    }

    static Stream<Arguments> pedidosInvalidos() {
        return Stream.of(
                arguments("pedido nulo", null),
                arguments("quantidade zero", new Pedido("P002", 0, new BigDecimal("80.00"))),
                arguments("quantidade negativa", new Pedido("P002", -3, new BigDecimal("80.00"))));
    }

    @ParameterizedTest(name = "Cenário F: {0} lança IllegalArgumentException")
    @MethodSource("pedidosInvalidos")
    void deveRejeitarPedidoInvalidoSemAcionarDependencias(String descricao, Pedido pedidoInvalido) {
        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.finalizarCompra(pedidoInvalido));

        verifyNoInteractions(estoqueService, pagamentoGateway);
    }
}
