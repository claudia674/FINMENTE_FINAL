# Remoção do Alerta de Permissão

A mensagem de alerta personalizada que explicava a necessidade de permissão para notificações foi removida, simplificando o fluxo do usuário.

## Alterações Realizadas

### [TarefasActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/TarefasActivity.java)
- Removido o `AlertDialog` customizado ("Permitir Lembretes").
- O aplicativo agora solicita a permissão diretamente através do sistema Android ao entrar na tela de tarefas.

## Verificação
- **Build**: O projeto foi compilado com sucesso.
- **Fluxo**: A solicitação de permissão agora ocorre sem o diálogo intermediário.

render_diffs(file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/TarefasActivity.java)
