package br.edu.fatec.sjc;

import java.math.BigDecimal;

public class Pedido {
    private final String codigoProduto;
    private final int quantidade;
    private final BigDecimal valorUnitario;

    public Pedido(String codigoProduto, int quantidade, BigDecimal valorUnitario) {
        this.codigoProduto = codigoProduto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
