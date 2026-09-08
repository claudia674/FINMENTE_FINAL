# Adicionar Racional de Permissão para Notificações

Adicionar uma mensagem explicativa em português antes de solicitar a permissão de notificações do sistema, para que o usuário entenda por que o aplicativo precisa desse acesso (para os lembretes de tarefas).

## Mudanças Propostas

### Lógica (Java)

#### [MODIFY] [TarefasActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/TarefasActivity.java)
- Alterar a lógica no `onCreate` para exibir um `AlertDialog` em português antes de chamar `requestPermissions`.
- O diálogo terá um título como "Permitir Lembretes" e uma mensagem explicando que as notificações são necessárias para o despertador das tarefas funcionar.

## Plano de Verificação

### Verificação Manual
1. Abrir a tela de Tarefas.
2. Verificar se um diálogo em português aparece explicando a necessidade das notificações.
3. Confirmar que, ao clicar em "Continuar", a janela de permissão do sistema Android é exibida.
