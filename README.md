# 💰 MeuOrcamento

Aplicação desenvolvida em **Java** para controle de orçamento e gastos pessoais, utilizando **SQLite** para persistência dos dados.

## 💡 Como surgiu o projeto

O MeuOrcamento nasceu de uma necessidade do meu dia a dia.

Todos os meses separo um determinado valor para os gastos mensais e, junto com meu esposo, utilizava um grupo no WhatsApp para registrar cada despesa. Conforme os gastos aconteciam, eu anotava os valores e fazia manualmente os cálculos para acompanhar quanto ainda estava disponível.

A partir desse hábito surgiu a ideia de transformar esse controle em uma aplicação.

Além de facilitar uma rotina que já fazia parte da minha vida, encontrei no projeto uma oportunidade de colocar em prática meus estudos de programação e aprofundar meus conhecimentos em Java.

O projeto começou de forma simples, com o controle do orçamento e dos gastos pelo terminal, e vem evoluindo conforme avanço nos estudos e adiciono novos conhecimentos e funcionalidades.

## 🚀 Funcionalidades atuais

- Definição do orçamento mensal
- Cadastro de gastos
- Listagem dos gastos cadastrados
- Edição de gastos
- Exclusão de gastos
- Cálculo do total gasto
- Cálculo do saldo disponível
- Alerta quando o orçamento é ultrapassado
- Persistência dos gastos em banco de dados

## 🛠️ Tecnologias utilizadas

- Java
- Programação Orientada a Objetos (POO)
- SQLite
- JDBC
- Maven
- Git e GitHub

## 🗂️ Estrutura do projeto

O projeto utiliza classes separadas para representar as responsabilidades da aplicação, incluindo:

- `Main` — execução da aplicação e interação com o usuário
- `Gasto` — representação dos gastos
- `Orcamento` — regras relacionadas ao orçamento e cálculo do saldo
- `GastoDAO` — operações de persistência dos gastos
- `BancoDeDados` — gerenciamento da conexão com o SQLite

## ▶️ Executando o projeto

### Pré-requisitos

- Java instalado
- Maven instalado

Clone o repositório e acesse a pasta do projeto.

Compile:

```bash
mvn clean compile