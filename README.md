# RAG — Assistente de estudos com PDFs

API REST para responder perguntas sobre materiais de Inteligência Artificial em PDF usando RAG (*Retrieval-Augmented Generation*, ou geração aumentada por recuperação). As respostas são geradas localmente pelo Ollama a partir dos trechos recuperados dos documentos.

## Como funciona

1. Na inicialização, o `DocumentService` lê os PDFs listados no código, em `src/main/resources/documents/`, usando Apache PDFBox.
2. O texto é dividido em trechos de até 1.000 caracteres, com sobreposição de 200 caracteres, e mantido em memória.
3. Ao receber uma pergunta, a aplicação seleciona até três trechos por correspondência de palavras com mais de três caracteres.
4. O `ChatService` envia os trechos e a pergunta ao modelo `llama3.2:3b`, com instruções para responder apenas com base no contexto. Se nenhum trecho for encontrado, retorna uma mensagem informando isso.

O projeto já possui dependências e configurações de PostgreSQL/pgvector e embeddings com `nomic-embed-text`, mas a recuperação implementada atualmente usa busca textual em memória. Os PDFs ainda não são indexados no banco vetorial.

## Tecnologias

- Java 21 e Maven, com Maven Wrapper incluído.
- Spring Boot 4.1.2, Spring Web MVC e Spring Data JDBC.
- Spring AI 2.0.1 e Ollama para integração com o modelo local.
- Apache PDFBox 3.0.5 para extração de texto dos PDFs.
- PostgreSQL com pgvector, configurado para vetores de 768 dimensões.
- Lombok.

## Requisitos para execução local

- JDK 21, com `JAVA_HOME` configurado.
- Ollama instalado e em execução em `http://localhost:11434`.
- PostgreSQL com a extensão pgvector disponível e um banco chamado `rag`. A configuração atual habilita a inicialização do esquema vetorial; o usuário do banco deve ter permissão para criar os objetos necessários.
- Acesso à internet na primeira execução para baixar o Maven, as dependências e os modelos.

## Como rodar

1. Baixe os modelos usados na configuração:

   ```sh
   ollama pull llama3.2:3b
   ollama pull nomic-embed-text
   ```

   Se o serviço do Ollama ainda não estiver ativo, execute `ollama serve` em outro terminal.

2. Crie o banco `rag` no PostgreSQL e ajuste `src/main/resources/application.yaml` conforme seu ambiente. Os valores atuais são:

   | Configuração | Valor |
   | --- | --- |
   | URL do banco | `jdbc:postgresql://localhost:5432/rag` |
   | Usuário | `postgres` |
   | Senha local | `123` |
   | URL do Ollama | `http://localhost:11434` |
   | Porta da API | `8080` |

   Também é possível sobrescrever a conexão sem editar o arquivo usando as variáveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.

3. Confira os PDFs em `src/main/resources/documents/`. Apenas os arquivos listados em `DocumentService.carregarDocumentos()` são carregados; para adicionar materiais, atualize essa lista. Os nomes precisam coincidir exatamente, incluindo espaços e acentos. Atualmente, o último PDF está na pasta como `9-Redes-neurais-Fundamentações-New (1).pdf`, mas o código procura `9-Redes-neurais-Fundamentacoes-New (1).pdf`; ajuste um dos nomes para carregá-lo.

4. Na raiz do projeto, inicie a aplicação:

   **Windows (PowerShell):**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux/macOS:**

   ```sh
   sh mvnw spring-boot:run
   ```

## Exemplo de uso

Com a aplicação em execução, envie um `POST` para `http://localhost:8080/api/chat/ask`. No PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/chat/ask' `
  -ContentType 'application/json; charset=utf-8' `
  -Body '{"question":"O que são agentes inteligentes?"}'
```

A resposta é um JSON com o campo `answer` contendo o texto gerado. A interação ocorre pela API REST; não há interface web implementada.
