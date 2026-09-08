# Plano de Implementação: Redesign do Menu (Estilo Squircle & Emojis)

Este plano visa transformar o menu principal para o estilo visual apresentado na imagem, utilizando recipientes arredondados ("squircles") para os ícones e emojis para uma interface mais amigável e moderna.

## Mudanças Propostas

### 1. Recursos de Estilo
- [MODIFY] [themes.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/values/themes.xml):
    - Aumentar o `cardCornerRadius` do `DashboardCard` para 24dp para um visual mais arredondado.
    - Criar um estilo `IconSquircle` para os recipientes dos ícones.

### 2. Layout do Menu (`activity_menu.xml`)
- [MODIFY] [activity_menu.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/layout/activity_menu.xml):
    - Reestruturar cada item do menu para incluir um `MaterialCardView` pequeno como fundo do ícone.
    - Substituir os `ImageView` por `TextView` contendo emojis, conforme a imagem:
        - **Meu Momento**: ☕
        - **Diário Pessoal**: 📓
        - **Tarefas de Hoje**: ✅
        - **Receitas**: ➕
        - **Despesas**: ➖
        - **Metas**: 🎯
        - **Fluxo**: 📅
        - **Alertas**: 🔔 (Fundo amarelo suave)
        - **Relatórios**: 📊
    - Ajustar cores de texto e margens para bater com a imagem.

### 3. Lógica do Menu (`MenuActivity.java`)
- Nenhuma alteração lógica é necessária, pois apenas o XML de layout será modificado.

## Plano de Verificação

### Testes Manuais
- [ ] Verificar se o layout 3x1 (Privado) e 3x2 (Finanças) está alinhado corretamente.
- [ ] Confirmar se os recipientes dos ícones estão com o arredondamento correto.
- [ ] Validar se os emojis estão centralizados nos recipientes.
- [ ] Testar a responsividade em diferentes tamanhos de tela.
