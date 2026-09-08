# Redesign do Dashboard para "Meu Espaço" e Expansão de Funcionalidades

O objetivo desta tarefa é renomear a interface principal para "Meu Espaço" e reorganizar as funcionalidades para destacar a nova seção "Privado", que incluirá Humor, Energia, Autocuidado e Diário.

## Propostas de Mudança

### Recursos de Texto e Internacionalização

#### [MODIFY] [strings.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/values/strings.xml)
- Atualizar `title_dashboard` para "MEU ESPAÇO".
- Adicionar strings para as novas categorias e itens:
  - `label_privado`: "Privado"
  - `label_financas`: "Finanças"
  - `label_energia`: "Energia"
  - `label_autocuidado`: "Autocuidado"
  - `label_diario`: "Diário"

### Interface de Usuário (Layout)

#### [MODIFY] [activity_menu.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/layout/activity_menu.xml)
- Alterar o título do cabeçalho para "MEU ESPAÇO".
- Reorganizar o Grid de Menu em duas seções distintas para melhor clareza:
  - **Seção Privado**: Contendo Humor, Energia, Autocuidado e Diário.
  - **Seção Finanças**: Contendo Fluxo, Receitas, Despesas, Metas, Alertas e Credores.
- Adicionar ícones representativos para os novos itens:
  - Energia: `@android:drawable/ic_menu_recent_history`
  - Autocuidado: `@android:drawable/ic_menu_myplaces`
  - Diário: `@android:drawable/ic_menu_edit`
- Remover o item redundante "Dashboard" do grid.

### Lógica da Aplicação

#### [MODIFY] [MenuActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/MenuActivity.java)
- Configurar listeners de clique para as novas funcionalidades (Energia, Autocuidado, Diário).
- Garantir que todas as novas ações exibam o aviso de "Funcionalidade em desenvolvimento" por enquanto.

## Plano de Verificação

### Verificação Manual
- Validar se o título superior agora é "MEU ESPAÇO".
- Verificar a nova organização do grid com as seções "Privado" e "Finanças".
- Testar os cliques nos novos itens (Energia, Autocuidado, Diário) e confirmar a exibição do Toast.
- Confirmar se o item "Sair" permanece funcional e bem localizado.
