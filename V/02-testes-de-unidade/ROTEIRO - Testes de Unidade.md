# Roteiro: Testes de Unidade com Mockito + Cobertura (Test Runner for Java)

Projeto: `checkout-service` (nesta mesma pasta)
Entrega: `TESTES DE UNIDADE - JOAO TUSCHTLER PAULISTA.zip`

Siga os passos em ordem. Cada passo diz **o que fazer** e **o que você deve ver**.

---

## Checklist do que o professor pede

- [x] `finalizarCompra(Pedido)` com a regra estoque → pagamento → baixa
- [x] **2 condicionais ou mais**: o código tem **3 `if`** e **1 `try/catch`**
- [x] **Toda condicional coberta por teste**, nos dois lados (verdadeiro e falso)
- [x] **Cobertura (coverage)** aparecendo no Test Runner for Java
- [x] Cenários A, B e C do enunciado + **3 cenários extras** (D, E, F)
- [x] ZIP com a pasta raiz do projeto e **sem arquivos .class**

---

## Passo 1: Preparar o ambiente (5 min)

1. Abra um terminal e confira o Java:
   ```
   java -version
   ```
   Precisa ser **11 ou superior** (17 ou 21 é o ideal).
2. No VS Code, instale a extensão **Extension Pack for Java** (Microsoft).
   Ela já traz o **Test Runner for Java** e o **Maven for Java**.
3. `File > Open Folder...` e abra **a pasta `checkout-service`**, e não a pasta da disciplina.
   Assim o VS Code não mistura com o `tdd-stack`.
4. Espere aparecer **"Java: Ready"** na barra de status (canto inferior).
   Na primeira vez ele baixa as dependências do Maven (JUnit, Mockito), então **precisa de internet**.

---

## Passo 2: Entender o código (5 min)

Abra `src/main/java/br/edu/fatec/sjc/CheckoutService.java`.

| Arquivo | Papel |
|---|---|
| `CheckoutService` | Classe testada. Contém a regra de negócio e as condicionais |
| `Pedido` | Dados do pedido: código do produto, quantidade e valor unitário. `getValorTotal()` = unitário × quantidade |
| `EstoqueService` | **Interface**: vira **mock** no teste (`temEstoque`, `baixarEstoque`) |
| `PagamentoGateway` | **Interface**: vira **mock** no teste (`processarPagamento(valor)`) |

### Mapa: condicional → teste que cobre cada lado

É essa tabela que prova que "todas as condicionais estão respaldadas nos testes":

| # | Condicional no código | Lado **verdadeiro** coberto por | Lado **falso** coberto por |
|---|---|---|---|
| 1 | `if (pedido == null \|\| pedido.getQuantidade() <= 0)` | **F** (pedido nulo, quantidade 0, quantidade -3) | A, B, C, D, E |
| 2 | `if (!estoqueService.temEstoque(...))` | **B** (sem estoque) | A, C, D, E |
| - | `try { processarPagamento } catch (RuntimeException e)` | **E** (gateway lança exceção) | A, C, D |
| 3 | `if (!pagamentoAprovado)` | **C** (pagamento negado) | A, D |

---

## Passo 3: Rodar os testes com cobertura no Test Runner for Java (5 min)

1. Clique no ícone do **frasco (Testing)** na barra lateral esquerda.
2. Expanda `checkout-service > br.edu.fatec.sjc > CheckoutServiceTest`.
   Devem aparecer **8 testes**: A, B, C, D, E e F três vezes (nulo, zero, negativa).
3. No topo do painel, clique em **"Run Tests with Coverage"** (ícone de play com um escudo).
   Você também pode clicar com o botão direito em `CheckoutServiceTest` e escolher **Run Test with Coverage**.
4. **O que você deve ver:**
   - os **8 testes verdes**;
   - o painel **Test Coverage** mostrando **`CheckoutService` 100%** e **`Pedido` 100%**;
   - no editor do `CheckoutService.java`, todas as linhas marcadas como cobertas (verde) e nenhum `if` com branch faltando.

> Se o botão "Run Tests with Coverage" não aparecer, atualize a extensão **Test Runner for Java**
> (o recurso de cobertura só existe nas versões mais novas) e reinicie o VS Code.

---

## Passo 4 (opcional): Rodar pelo Maven no terminal

Só funciona se o Maven (`mvn`) estiver instalado. Dentro da pasta `checkout-service`:

```
mvn clean verify
```

- Roda os 8 testes e gera o relatório **`target/site/jacoco/index.html`**. Abra no navegador e mostre ao professor.
- O `pom.xml` tem uma **regra do JaCoCo que exige 100% de linhas e de branches**.
  Se alguma condicional ficar sem teste, o build **falha**. Isso é a garantia automática de que tudo está respaldado.
- **Use sempre o `clean`.** O JaCoCo acumula os dados de cobertura em `target/jacoco.exec`,
  então, sem o `clean`, ele soma com a execução anterior e pode mostrar 100% mesmo faltando teste.

Resultado esperado (já conferido com JDK 21 + Maven 3.9.9):

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
All coverage checks have been met.
BUILD SUCCESS
```

| Classe | Linhas cobertas | Branches cobertos |
|---|---|---|
| `CheckoutService` | 18 de 18 | 8 de 8 |
| `Pedido` | 8 de 8 | não tem branches |

---

## Passo 5: Demonstração para o professor (o que falar)

Faça a demonstração **na frente dele**. Ela prova que os testes realmente cobrem as condicionais.

1. Rode com cobertura e mostre **100%**.
2. No `CheckoutServiceTest`, **comente o método do Cenário C** (`naoDeveBaixarEstoqueQuandoPagamentoNegado`).
3. Rode com cobertura de novo. A linha `if (!pagamentoAprovado)` fica **amarela/parcial** (só 1 de 2 branches)
   e o `return false;` dentro dela fica **vermelho**.
4. Explique: *"sem o cenário C, o lado verdadeiro dessa condicional não é testado. Por isso cada condicional tem teste para os dois lados."*
5. **Descomente o Cenário C** e rode de novo até voltar a 100%.

Pelo Maven, a mesma demonstração dá para fazer sem editar nenhum arquivo:

```
mvn clean verify "-Dtest=!CheckoutServiceTest#naoDeveBaixarEstoqueQuandoPagamentoNegado"
```

Roda 7 testes (todos menos o C) e o build **falha** com:

```
Rule violated for bundle checkout-service: lines covered ratio is 0.96, but expected minimum is 1.00
Rule violated for bundle checkout-service: branches covered ratio is 0.87, but expected minimum is 1.00
```

Ou seja: 1 linha e 1 branch ficaram sem teste, justamente o `return false` do pagamento negado.

### Explicação de cada cenário (uma frase cada)

- **A (sucesso):** estoque `true` + pagamento `true` → `assertTrue` no retorno e `verify` de que `baixarEstoque` foi chamado.
- **B (sem estoque):** estoque `false` → `assertFalse` e `verify(gateway, never())`, porque o pagamento nem é tentado.
- **C (pagamento negado):** estoque `true` + pagamento `false` → `assertFalse` e `verify(estoque, never()).baixarEstoque`.
- **D (extra, ordem):** `InOrder` prova a sequência estoque → pagamento → baixa, e que o valor cobrado foi **7000.00** (2 × 3500.00).
  O `verifyNoMoreInteractions` garante que não houve nenhuma chamada a mais.
- **E (extra, gateway fora do ar):** o mock lança `RuntimeException` → o método **não quebra**, retorna `false` e não baixa estoque.
- **F (extra, pedido inválido):** `@ParameterizedTest` com 3 entradas → lança `IllegalArgumentException`
  e `verifyNoInteractions` mostra que **nenhum mock foi tocado**.

### Perguntas prováveis do professor

- **Por que usar mock?** Para testar só o `CheckoutService`, sem estoque nem gateway reais.
  Eu controlo o que eles retornam (`when...thenReturn`) e verifico como foram chamados (`verify`).
- **Qual a diferença entre `assert` e `verify`?** `assert` confere o **resultado** (o retorno).
  `verify` confere o **comportamento** (se um método do mock foi ou não chamado, e com quais argumentos).
- **O que é `@InjectMocks`?** Cria o `CheckoutService` injetando os dois `@Mock` pelo construtor.
- **O que é cobertura de branch?** É a porcentagem de caminhos de decisão executados.
  Cada `if` tem 2 caminhos, e 100% significa que os dois lados de todos os `if` foram testados.
- **Por que `MockitoExtension`?** Ela inicializa os mocks e é *strict*: se eu configurar um `when` que o teste não usa, o teste falha.
  Isso mantém os testes limpos.

---

## Passo 6: Gerar o ZIP de entrega

O ZIP **já está pronto** na pasta da disciplina: `TESTES DE UNIDADE - JOAO TUSCHTLER PAULISTA.zip`.
Ele contém só os **6 arquivos mínimos**, dentro da pasta raiz `checkout-service/`:

| Arquivo | Por que é obrigatório |
|---|---|
| `pom.xml` | Declara JUnit 5, Mockito e JaCoCo. Sem ele o projeto não compila nem roda os testes |
| `src/main/java/.../CheckoutService.java` | A classe com `finalizarCompra` e as condicionais |
| `src/main/java/.../Pedido.java` | Usada pelo `CheckoutService` |
| `src/main/java/.../EstoqueService.java` | Interface que vira mock |
| `src/main/java/.../PagamentoGateway.java` | Interface que vira mock |
| `src/test/java/.../CheckoutServiceTest.java` | Os testes (cenários A a F) |

Ficam de fora: `README.md` e `.gitignore` (opcionais) e `target/` (onde ficam os `.class`, que são proibidos).

Se você **mudar algum arquivo** e precisar gerar de novo:

1. Apague o ZIP antigo.
2. No PowerShell, dentro da pasta onde está o `checkout-service`, rode o comando abaixo. Ele coloca no ZIP
   **só o `pom.xml` e a pasta `src`**, então nunca entra `.class`, nem mesmo se a pasta `target` existir:
   ```
   tar.exe -a -c -f "TESTES DE UNIDADE - JOAO TUSCHTLER PAULISTA.zip" checkout-service/pom.xml checkout-service/src
   ```
3. Confira que o ZIP tem exatamente os 6 arquivos da tabela acima.

---

## Problemas comuns

| Problema | Solução |
|---|---|
| Testes não aparecem no painel Testing | `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → Restart |
| Aviso "Mockito is currently self-attaching..." ou "A Java agent has been loaded dynamically" | É só um aviso do Mockito em Java 21+. Pode ignorar, os testes funcionam |
| Erro ao baixar dependências | Sem internet no lab. Use o 4G do celular ou rode em casa antes da aula |
| `mvn` não reconhecido | O Maven não está instalado. Use só o VS Code (Passo 3), que não precisa dele |
