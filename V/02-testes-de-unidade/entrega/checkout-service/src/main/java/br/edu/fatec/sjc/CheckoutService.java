package br.edu.fatec.sjc;

public class CheckoutService {
    private final EstoqueService estoqueService;
    private final PagamentoGateway pagamentoGateway;

    public CheckoutService(EstoqueService estoqueService, PagamentoGateway pagamentoGateway) {
        this.estoqueService = estoqueService;
        this.pagamentoGateway = pagamentoGateway;
    }

    public boolean finalizarCompra(Pedido pedido) {
        // Condicional 1: pedido inválido é rejeitado antes de acionar as dependências
        if (pedido == null || pedido.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Pedido inválido");
        }

        String codigoProduto = pedido.getCodigoProduto();
        int quantidade = pedido.getQuantidade();

        // Condicional 2: sem estoque, a compra termina aqui
        if (!estoqueService.temEstoque(codigoProduto, quantidade)) {
            return false;
        }

        // Tratamento de erro: falha no gateway equivale a pagamento não aprovado
        boolean pagamentoAprovado;
        try {
            pagamentoAprovado = pagamentoGateway.processarPagamento(pedido.getValorTotal());
        } catch (RuntimeException e) {
            return false;
        }

        // Condicional 3: pagamento negado não baixa o estoque
        if (!pagamentoAprovado) {
            return false;
        }

        estoqueService.baixarEstoque(codigoProduto, quantidade);
        return true;
    }
}
