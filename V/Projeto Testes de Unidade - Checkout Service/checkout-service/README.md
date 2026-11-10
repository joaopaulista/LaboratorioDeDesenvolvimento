# Checkout Service - Testes de Unidade (JUnit 5 + Mockito)

Aluno: João Tuschtler Paulista

## Como executar

- VS Code (Test Runner for Java): aba **Testing** → **Run Tests with Coverage**.
- Maven: `mvn clean verify` roda os testes, gera o relatório de cobertura em
  `target/site/jacoco/index.html` e falha se alguma linha ou condicional ficar sem teste.

## Regra de negócio - `CheckoutService.finalizarCompra(Pedido pedido)`

1. Verifica se há estoque disponível para o produto (`EstoqueService.temEstoque`).
2. Se houver estoque, tenta processar o pagamento via gateway (`PagamentoGateway.processarPagamento`).
3. Se o pagamento for aprovado, baixa o estoque (`EstoqueService.baixarEstoque`).
4. Retorna `true` apenas se houver estoque e o pagamento for aprovado; caso contrário, `false`.

## Condicionais x casos de teste

| Condicional | Lado verdadeiro | Lado falso |
|---|---|---|
| 1. `pedido == null \|\| quantidade <= 0` | F (nulo, zero, negativa) | A, B, C, D, E |
| 2. `!temEstoque(...)` | B | A, C, D, E |
| try/catch do gateway | E (exceção) | A, C, D (sem exceção) |
| 3. `!pagamentoAprovado` | C | A, D |

## Cenários testados (`CheckoutServiceTest`)

| Cenário | Situação | Verificações |
|---|---|---|
| A | Há estoque e pagamento aprovado | `verify` da baixa de estoque; retorno `true` |
| B | Sem estoque | gateway nunca chamado (`never()`); retorno `false` |
| C | Há estoque, pagamento negado | estoque não baixado (`never()`); retorno `false` |
| D (aluno) | Fluxo de sucesso completo | valor total cobrado correto; ordem estoque → pagamento → baixa (`InOrder`); nenhuma chamada extra (`verifyNoMoreInteractions`) |
| E (aluno) | Gateway lança exceção (indisponível) | exceção não propaga; estoque não baixado; retorno `false` |
| F (aluno) | Pedido inválido: nulo, quantidade zero ou negativa (`@ParameterizedTest`) | lança `IllegalArgumentException`; nenhum mock acionado (`verifyNoInteractions`) |
