package br.edu.fatec.sjc;

import java.math.BigDecimal;

public interface PagamentoGateway {
    boolean processarPagamento(BigDecimal valor);
}
