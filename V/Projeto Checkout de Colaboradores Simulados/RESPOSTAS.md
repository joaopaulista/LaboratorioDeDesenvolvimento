# Respostas

**O que foi mockado e por quê?**
Só as quatro interfaces externas: `EstoqueGateway`, `PagamentoGateway`, `PedidoRepository` e `Notificador`. Elas representam sistemas fora do serviço (estoque, operadora de pagamento, banco de dados e e-mail). Com mocks no lugar delas, os testes rodam rápido, sem rede nem banco, e dá para provocar respostas difíceis de reproduzir de verdade, como uma recusa, uma falha técnica ou um SMTP fora do ar. O `ServicoCheckout` e os records (`SolicitacaoCheckout`, `Pagamento`, `PedidoConfirmado`, `ResultadoCheckout`), além do `BigDecimal`, são reais, porque são exatamente o comportamento que está sendo testado.

**Qual teste verifica estado e qual verifica interação?**
`deveCalcularTotalComPrecisaoSemArredondar` e os testes de entrada inválida verificam **estado**: o valor devolvido (`resultado.total()` igual a `0.015`) ou a exceção lançada. `deveRecusarSemEstoqueSemCobrarRegistrarNotificarOuLiberar` verifica **interação**: confere com `verify`, `never()` e `verifyNoInteractions` quais colaboradores foram chamados ou não. O teste de compra aprovada faz as duas coisas: confere os campos do `ResultadoCheckout` e também a ordem e os argumentos das chamadas.

**Que defeito seria revelado pelo captor?**
Um `PedidoConfirmado` montado errado. Por exemplo: campos trocados (`email` no lugar de `clienteId`), quantidade ou total diferentes do calculado, ou o `transacaoId` faltando ou fixo em vez do valor devolvido pelo pagamento. Um `verify(repositorio).salvar(any())` aceitaria qualquer objeto; o `ArgumentCaptor` permite conferir os seis campos do pedido que de fato foi salvo.

**Por que verificar a ordem aqui não é apenas verificar um detalhe de implementação?**
Porque a ordem faz parte do contrato de negócio, e não da forma como o código foi escrito por dentro. Reservar antes de cobrar evita cobrar um cliente sem ter o produto. Cobrar antes de salvar evita registrar um pedido que não foi pago. Salvar antes de notificar garante que o e-mail leve um número de pedido que existe. Liberar depois de uma cobrança que falhou é a compensação exigida. Inverter qualquer um desses passos muda o efeito visível para o cliente e para os sistemas externos. O `InOrder` só verifica esses efeitos externos, e não chamadas a métodos privados.
