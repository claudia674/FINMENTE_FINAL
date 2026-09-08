# Resultado das Correções na ReceitasActivity

As funcionalidades de Cadastro e Consulta foram corrigidas e o driver de conexão com o MySQL foi restaurado.

## Mudanças Realizadas

### 1. Downgrade do Driver MySQL (Compatibilidade)
- A versão 8.0 do conector MySQL tem incompatibilidades com o Android (falta da classe `java.sql.SQLType`).
- Alterei para a versão **5.1.49** no `libs.versions.toml`, que é a mais estável e compatível com Android.
- Driver atualizado para `com.mysql.jdbc.Driver` em `ConexaoMySQL.java`.

### 2. Prevenção de Cracks e Fechamento Inesperado
- Adicionado `try-catch (Throwable)` tanto na conexão quanto nas operações de banco. Isso garante que, se houver um erro, o app mostre um aviso (Toast) em vez de fechar e voltar para o menu.

### 3. Correção de Lançamento (Run Config)
- Habilitei `android:exported="true"` para a `ReceitasActivity` no `AndroidManifest.xml`. Isso resolve o erro "The activity must be exported" que aparecia ao tentar rodar a tela de receitas diretamente.

### 3. Implementação da Lógica de Negócio
- Em [ReceitasActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/ReceitasActivity.java):
    - **Cadastro:** Agora captura o nome e o valor digitados e executa um comando `INSERT` no banco de dados.
    - **Consulta:** Executa um `SELECT *` e exibe a lista de receitas no campo de texto logo abaixo dos botões.
    - **Tratamento de Erros:** Adicionadas mensagens de alerta (Toasts) para informar se a operação foi bem-sucedida ou se houve falha de conexão.

## Verificação

1.  **Build:** O projeto foi compilado com sucesso (`assembleDebug`).
2.  **Fluxo de Dados:** O código agora utiliza `PreparedStatement` para segurança e `Statement` para consulta.

> [!TIP]
> Certifique-se de que o seu servidor MySQL está rodando na porta **3307** e que o banco de dados **finmente** possui a tabela **receitas** com as colunas `id`, `nome` e `valor`.

```sql
CREATE TABLE receitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    valor DOUBLE NOT NULL
);
```
