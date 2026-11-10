# Registro dos ciclos Red–Green–Refactor

Todo passo foi executado com `mvn test` (JDK 21, Maven 3.9). A saída completa de cada execução está em [`logs/`](logs) e a alteração de código feita em cada passo está em [`diffs/`](diffs), na ordem em que aconteceu (`01-…` até `19-…`).

## Ciclo 1: entrada nula

| Etapa | O que aconteceu | Evidência |
|---|---|---|
| **Red** | `deveRejeitarSolicitacaoNulaSemConsultarColaboradores` falhou: `expected: <IllegalArgumentException> but was: <UnsupportedOperationException>` | `logs/ciclo1-red.log` |
| **Green** | Mudança mínima: `if (solicitacao == null) throw new IllegalArgumentException(...)`. 1/1 verde. | `diffs/01-ciclo1-green.diff`, `logs/ciclo1-green.log` |
| **Refactor** | A validação foi extraída para o método `validar(...)`, preparando o lugar das próximas regras. Comportamento igual, 1/1 verde. | `diffs/02-ciclo1-refactor.diff`, `logs/ciclo1-refactor.log` |

## Ciclo 2: campos inválidos (teste parametrizado)

| Etapa | O que aconteceu | Evidência |
|---|---|---|
| **Red** | `deveRejeitarCamposInvalidosSemConsultarColaboradores` com 14 casos (os três textos nulos, vazios e em branco; quantidade zero e negativa; preço nulo, zero e negativo). **15 testes, 14 falhas**, todas por `UnsupportedOperationException`. | `diffs/03-ciclo2-red.diff`, `logs/ciclo2-red.log` |
| **Green** | Um `if` para cada regra dentro de `validar(...)`. 15/15 verde. | `diffs/04-ciclo2-green.diff`, `logs/ciclo2-green.log` |
| **Refactor** | Os três `if`s de texto, repetidos, viraram o auxiliar `exigirTexto(valor, campo)`. 15/15 verde. | `diffs/05-ciclo2-refactor.diff`, `logs/ciclo2-refactor.log` |

## Ciclo 3: compra aprovada

| Etapa | O que aconteceu | Evidência |
|---|---|---|
| **Red** | `deveRegistrarENotificarCompraAprovadaNaOrdemDoContrato` (3 × 19.90): erro `UnsupportedOperationException: Implemente por TDD`. | `diffs/06-ciclo3-red.diff`, `logs/ciclo3-red.log` |
| **Green** | Fluxo linear sem nenhum tratamento de erro: calcular o total, reservar, cobrar, salvar, notificar e devolver o resultado. 16/16 verde. | `diffs/07-ciclo3-green.diff`, `logs/ciclo3-green.log` |
| **Refactor** | Extração de `calcularTotal(...)` e `criarPedido(...)` para deixar `finalizar` legível. 16/16 verde. | `diffs/08-ciclo3-refactor.diff`, `logs/ciclo3-refactor.log` |

## Ciclos seguintes (cenários de erro)

| Ciclo | Red (falha observada) | Green (mudança mínima) | Refactor |
|---|---|---|---|
| 4: sem estoque | `expected: <EstoqueInsuficienteException> but was: <NullPointerException>` (o serviço seguia para a cobrança com o mock devolvendo `null`) | `if (!reservar(...)) throw new EstoqueInsuficienteException()` | Nenhum: a mudança era de uma linha, sem duplicação. |
| 5: pagamento recusado | `Expected PagamentoRecusadoException to be thrown, but nothing was thrown` | `if (!pagamento.aprovado()) { liberar(...); throw ... }` | Nenhum ainda: preferi esperar o ciclo 6 para ver a duplicação de verdade. |
| 6: falha técnica | `Wanted but not invoked: estoqueGateway.liberar("SKU-123", 3)` | `try/catch (FalhaPagamentoException)` que libera a reserva e relança **a mesma instância** | `cobrarOuLiberarReserva(...)` e `liberarReserva(...)` eliminam a liberação duplicada. |
| 7: falha de notificação | `FalhaNotificacaoException: SMTP fora do ar` vazou para o chamador | `notificar(...)` devolve `false` quando captura `FalhaNotificacaoException` | `reservarEstoque(...)` extraído e métodos reordenados na sequência do fluxo. |

No ciclo 4, junto com o teste de falta de estoque, entraram os testes de **quantidade mínima**, **precisão (3 × 0.005 = 0.015)** e **captura do pedido**. Esses três **passaram de primeira**, porque o cálculo com `BigDecimal.multiply` e a construção do pedido já tinham sido implementados no ciclo 3. Eles ficaram como testes de caracterização: protegem essas regras contra regressões.

## Desafio opcional: procurando testes fracos

Cada regra foi quebrada de propósito, uma de cada vez, e o código foi restaurado logo depois (logs em `logs/mutacao-*.log`).

| Mutação | Testes que ficaram vermelhos |
|---|---|
| Enviar uma notificação extra antes de salvar | Só `naoDeveDesfazerCompraQuandoNotificacaoFalhar`. **O teste de compra aprovada não pegou.** |
| Usar outro número de pedido na notificação | Compra aprovada, precisão e falha de notificação |
| Remover a liberação depois da recusa | `deveLiberarReservaUmaVezQuandoPagamentoForRecusado` |
| Arredondar o total para duas casas | `deveCalcularTotalComPrecisaoSemArredondar` |

**Lacuna encontrada:** no teste de compra aprovada, o `InOrder` só conferia que **existia** uma notificação correta depois do `salvar`. Uma notificação a mais, enviada antes, passava sem ser notada. **Melhoria:** acrescentei `verify(notificador, times(1)).enviarConfirmacao(anyString(), anyString(), any(BigDecimal.class))`, que garante uma única notificação no total (`diffs/19-melhoria-desafio.diff`). Repetindo a mutação, o teste de compra aprovada passou a falhar (`logs/mutacao-1-notifica-antes-apos-melhoria.log`).
