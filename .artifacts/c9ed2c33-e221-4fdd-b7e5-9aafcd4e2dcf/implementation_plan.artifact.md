# Correção das funcionalidades de Cadastro e Consulta de Receitas

Este plano visa resolver dois problemas principais na `ReceitasActivity`:
1.  **Botão "Cadastrar" voltando para o menu:** Provavelmente causado por um erro (crash) ao tentar carregar o driver do MySQL que está faltando.
2.  **Botão "Consultar" não mostrando nada:** O código atual apenas limpa o campo de texto em vez de buscar dados no banco.

## User Review Required

> [!IMPORTANT]
> O uso de conexão direta com o MySQL (JDBC) em aplicativos Android não é recomendado para produção devido a riscos de segurança e performance. Recomenda-se o uso de uma API intermediária (como Node.js, PHP ou Python) futuramente. Para fins educacionais, prosseguiremos com a correção da conexão direta.

> [!WARNING]
> Para que a conexão funcione, o servidor MySQL deve estar acessível pelo emulador no endereço `10.0.2.2` (localhost do computador) e a porta `3307` deve estar correta.

## Proposed Changes

### [Dependências]

#### [MODIFY] [build.gradle.kts](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/build.gradle.kts)
- Adicionar a dependência do conector MySQL para que o driver seja encontrado.

### [Banco de Dados]

#### [MODIFY] [ConexaoMySQL.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/ConexaoMySQL.java)
- Melhorar o tratamento de erros para evitar que o app feche se a conexão falhar.
- Garantir que o driver correto seja carregado.

### [Atividade de Receitas]

#### [MODIFY] [ReceitasActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/ReceitasActivity.java)
- Capturar os dados digitados nos campos `edtNomeReceita` e `edtValorReceita`.
- Implementar a lógica de `INSERT` no banco de dados ao clicar em "Cadastrar".
- Implementar a lógica de `SELECT` no banco de dados ao clicar em "Consultar" e exibir os resultados no `txtResultado`.

## Verification Plan

### Manual Verification
1.  **Teste de Cadastro:** Inserir um nome e valor, clicar em "Cadastrar" e verificar se aparece o Toast de sucesso sem fechar a tela.
2.  **Teste de Consulta:** Clicar em "Consultar" e verificar se os dados cadastrados aparecem na tela.
3.  **Verificação de Erro:** Tentar cadastrar sem conexão e verificar se o app exibe uma mensagem de erro amigável em vez de crashar.
