# Checkout com colaboradores simulados

Implementação, por TDD, do `ServicoCheckout`, que coordena a reserva de estoque, a cobrança, o registro do pedido e a notificação. Os quatro colaboradores externos foram substituídos por mocks do Mockito.

| Pasta / arquivo | Conteúdo |
|---|---|
| `checkout-alunos/` | Projeto Maven com o serviço implementado e a suíte de testes (`ServicoCheckoutTest`) |
| `registro-tdd/` | Registro dos ciclos Red–Green–Refactor, com logs reais do `mvn test` e diffs de cada passo |
| `RESPOSTAS.md` | Respostas às quatro perguntas da entrega |
| `material/` | Enunciado em PDF e o `.zip` original do projeto inicial |

## Como executar

```bash
cd checkout-alunos
mvn test
```

Resultado: **23 testes, 0 falhas.** São 1 teste de solicitação nula, 14 casos parametrizados de entrada inválida e 8 cenários de negócio: quantidade mínima, cálculo, precisão, sem estoque, pagamento recusado, falha técnica, persistência com captor e falha de notificação.

## Recursos do Mockito usados

`when(...).thenReturn(...)`, `thenThrow(...)`, `doThrow(...)`, `verify(...)`, `never()`, `times(1)`, `verifyNoInteractions(...)`, `ArgumentCaptor` e `InOrder`, todos com `@ExtendWith(MockitoExtension.class)` e `Strictness.STRICT_STUBS`.
