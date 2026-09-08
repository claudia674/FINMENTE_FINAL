# Solução para Erro de Conexão MySQL no Android

O erro `java.lang.NoClassDefFoundError: Failed resolution of: Ljava/sql/SQLType;` está ocorrendo porque as versões mais recentes do Driver do MySQL (8.0 em diante) tentam usar recursos que o Android não suporta nativamente.

## User Review Required

> [!IMPORTANT]
> **Mudança de Driver**: Para que o Android consiga se conectar ao MySQL sem travar, precisamos usar uma versão do driver que seja compatível com o sistema mobile. A versão `5.1.49` é a mais estável e recomendada para Android.

> [!WARNING]
> **Segurança**: Lembre-se que conectar o celular diretamente ao banco de dados expõe sua senha. Para um aplicativo real, o ideal seria usar uma API intermediária.

## Proposed Changes

### Build Configuration
#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/build.gradle.kts)
* Alterar a dependência de `com.mysql:mysql-connector-j:8.3.0` para `mysql:mysql-connector-java:5.1.49`.

### Data Layer
#### [MODIFY] [ConexaoMySQL.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/ConexaoMySQL.java)
* Atualizar o nome da classe do Driver de `com.mysql.cj.jdbc.Driver` para `com.mysql.jdbc.Driver`.
* Ajustar a URL de conexão para o formato compatível com a versão 5.1.

---

## Verification Plan

### Automated Tests
* Executar o build do projeto para garantir que o novo driver foi baixado corretamente.

### Manual Verification
1. Abrir o Logcat.
2. Tentar realizar o login.
3. Verificar se a mensagem `CONEXÃO ESTABELECIDA COM SUCESSO!` aparece sem o erro de `NoClassDefFoundError`.
