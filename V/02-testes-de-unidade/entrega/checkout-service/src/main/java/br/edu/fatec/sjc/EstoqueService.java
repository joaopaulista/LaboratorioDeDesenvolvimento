package br.edu.fatec.sjc;

public interface EstoqueService {
    boolean temEstoque(String codigoProduto, int quantidade);

    void baixarEstoque(String codigoProduto, int quantidade);
}
