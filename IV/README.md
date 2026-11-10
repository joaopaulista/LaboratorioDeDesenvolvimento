# Laboratório de Desenvolvimento IV

**Semestre:** 2º semestre de 2025 — FATEC São José dos Campos
**Professor:** Emanuel Mineda Carneiro

## Sobre a disciplina

Neste semestre, o foco foi o desenvolvimento de uma API REST em Java com **Spring Boot**, conectada a um banco de dados **PostgreSQL**. A aplicação foi construída aos poucos, em aula, acompanhando o professor: cada etapa do sistema virou um commit, desde a configuração do ambiente até o mapeamento das tabelas e a criação dos endpoints.

O sistema gerencia **usuários**, suas **autorizações** (perfis de acesso), **anotações** e **trabalhos** com data de entrega e nota.

## O que aprendi

- **Ambiente de desenvolvimento com Dev Container e Docker Compose:** um container com o Java e outro com o PostgreSQL, para todos terem o mesmo ambiente.
- **Modelagem do banco de dados** em SQL (`DDL.sql`): chaves primárias, chaves estrangeiras, restrições `unique` e tabela associativa para relacionamento N:N.
- **Mapeamento objeto-relacional com JPA/Hibernate:** entidades com `@Entity`, `@Table`, `@Column`, relacionamentos `@ManyToOne`, `@OneToMany` e `@ManyToMany`, e `ddl-auto = validate` para conferir o mapeamento com o banco.
- **Spring Data JPA:** repositórios com `JpaRepository`, consultas por nome de método (`findBy...`) e consultas JPQL com `@Query`.
- **Camada de serviço:** interfaces e implementações (`UsuarioService`, `TrabalhoService`) com validação das regras de negócio e retorno de erros HTTP com `ResponseStatusException`.
- **Controllers REST:** `@RestController`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam` e `ResponseEntity` com os status corretos (`200 OK`, `201 Created`).
- **Injeção de dependências** por construtor e por `@Autowired`.

## Como fiz

1. Configurei o Dev Container com Java e PostgreSQL.
2. Criei o banco com o script `DDL.sql` (tabelas, dados iniciais e usuário `spring`).
3. Mapeei as entidades `Usuario`, `Autorizacao`, `Anotacao` e `Trabalho`.
4. Criei os repositórios e as consultas.
5. Implementei os serviços com as validações.
6. Expus os endpoints `/usuario` e `/trabalho` nos controllers.

## Estrutura

| Pasta | Conteúdo |
|-------|----------|
| `Projeto Spring Lab` | API Spring Boot 3.5 (Java, JPA, PostgreSQL) com `DDL.sql` e Dev Container |

## Tecnologias

Java · Spring Boot 3.5 · Spring Data JPA · Hibernate · PostgreSQL · Maven · Docker / Dev Container
