# 💰 Meu Orçamento

Sistema de controle financeiro pessoal desenvolvido em **Java**, com persistência de dados em **PostgreSQL**, integração com **WhatsApp** e utilização da inteligência artificial **Gemini**, do Google.

O projeto permite cadastrar e gerenciar despesas, organizar gastos por categorias, acompanhar o orçamento mensal e criar planejamentos financeiros diretamente pelo WhatsApp.

Além dos comandos para consultar saldos e relatórios, a integração com o Gemini permite fazer perguntas em linguagem natural e receber respostas baseadas nas informações financeiras registradas.

A aplicação utiliza conceitos de **Programação Orientada a Objetos (POO)**, operações **CRUD**, padrão **DAO**, integração com APIs e deploy no **Render**.

## 💡 Como surgiu o projeto

A ideia do Meu Orçamento surgiu de uma necessidade do dia a dia.

Eu e meu esposo costumávamos utilizar um grupo no WhatsApp para anotar nossas despesas e acompanhar os gastos do mês. Porém, os cálculos precisavam ser feitos manualmente.

Quando comecei a estudar Java e Programação Orientada a Objetos, resolvi transformar essa necessidade em um projeto para praticar o que estava aprendendo.

A primeira versão funcionava pelo terminal, com um menu simples para cadastrar despesas e consultar o saldo.

Conforme fui avançando nos estudos, surgiram novas ideias e o projeto foi evoluindo: implementei a persistência dos dados, novas funcionalidades de controle financeiro e, posteriormente, a integração com o WhatsApp e com o Gemini.

Hoje, além de registrar despesas, consigo consultar informações e conversar com um assistente financeiro diretamente pelo WhatsApp.

O projeto continua em desenvolvimento e tem sido uma oportunidade de aprender na prática, enfrentar desafios e compreender melhor como diferentes tecnologias se comunicam.

## 🚀 Funcionalidades

### 💰 Gerenciamento de despesas
- Cadastrar, consultar, editar e excluir despesas (CRUD).
- Organizar os gastos por categorias.
- Consultar o histórico de despesas registradas.

### 📊 Controle do orçamento
- Definir e acompanhar o orçamento mensal.
- Consultar o total gasto e o saldo disponível.
- Receber alertas quando o orçamento é ultrapassado.
- Visualizar resumos financeiros e gastos por categoria.

### 📅 Planejamento financeiro
- Criar planejamentos para despesas futuras.
- Consultar valores planejados e o saldo projetado.
- Utilizar essas informações para auxiliar nas decisões financeiras.

### 📱 Integração com WhatsApp
- Registrar despesas enviando mensagens.
- Consultar saldos, gastos e resumos financeiros.
- Gerenciar informações financeiras por comandos, sem precisar acessar o terminal.

### 🤖 Assistente financeiro com Gemini
- Fazer perguntas em linguagem natural sobre as finanças.
- Identificar as categorias com maiores despesas.
- Receber análises e sugestões considerando os dados financeiros cadastrados.
- Consultar o impacto de possíveis gastos no orçamento disponível.

## 🛠️ Tecnologias utilizadas

- **Java:** linguagem principal utilizada no desenvolvimento da aplicação.
- **PostgreSQL:** banco de dados relacional responsável pelo armazenamento das informações financeiras.
- **JDBC:** comunicação entre a aplicação Java e o banco de dados.
- **WhatsApp (API/Webhook):** integração para receber mensagens, executar comandos e retornar informações financeiras.
- **Google Gemini:** inteligência artificial utilizada para interpretar perguntas em linguagem natural e gerar respostas com base nos dados financeiros.
- **Render:** plataforma utilizada para realizar o deploy da aplicação.
- **Git e GitHub:** versionamento do código-fonte e integração com o processo de deploy.

## ⚙️ Configuração e execução

O Meu Orçamento é uma aplicação Java que utiliza PostgreSQL para persistência dos dados e integrações externas para comunicação com o WhatsApp e com a inteligência artificial Gemini.

### Pré-requisitos

- Java instalado.
- PostgreSQL disponível para conexão.
- Git para clonar o repositório.
- Credenciais válidas para as integrações externas.

### Clonando o projeto

```bash
git clone https://github.com/SamaraMatos/meu-orcamento.git
```

### Configuração do ambiente

A aplicação utiliza variáveis de ambiente para configurar o acesso ao banco de dados e aos serviços externos.

Entre elas:

- `DATABASE_URL`: endereço de conexão com o PostgreSQL.

As demais credenciais devem ser configuradas conforme as integrações utilizadas no projeto.

**Importante:** tokens, senhas, chaves de API e informações de acesso não devem ser incluídos diretamente no código-fonte ou enviados ao GitHub.

### Executando a aplicação

Após configurar o ambiente, abra o projeto em uma IDE compatível com Java, como o Visual Studio Code.

A execução depende da configuração do banco de dados e dos serviços utilizados. Para utilizar a integração com WhatsApp, também é necessário que o serviço responsável pelo recebimento das mensagens esteja ativo.
