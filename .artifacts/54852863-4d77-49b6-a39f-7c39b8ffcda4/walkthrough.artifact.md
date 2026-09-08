# Walkthrough: Personalização da Splash Screen (Remoção do Robô do Android)

Substituímos o ícone padrão do sistema Android por uma experiência de marca personalizada para o **Finmente**.

## Alterações Realizadas

### 1. Logotipo da Marca
Criamos um novo ícone vetorial sofisticado para representar o Finmente durante a inicialização.
* [NOVO] [ic_splash_logo.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/drawable/ic_splash_logo.xml)

### 2. Integração com o Sistema
Implementamos a API oficial de Splash Screen da Google para garantir que o ícone da marca apareça imediatamente, eliminando o robô verde.
* [MODIFICADO] [libs.versions.toml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/gradle/libs.versions.toml) (Adição da biblioteca `androidx.core:core-splashscreen`).
* [MODIFICADO] [build.gradle.kts](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/build.gradle.kts) (Sincronização da dependência).

### 3. Identidade Visual (Temas)
Configuramos o tema de inicialização para usar as cores e o logo do Finmente, garantindo uma transição suave para a tela de abertura principal.
* [MODIFICADO] [themes.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/values/themes.xml) (Criação do `Theme.App.Starting`).
* [MODIFICADO] [AndroidManifest.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/AndroidManifest.xml) (Aplicação do tema de inicialização).
* [MODIFICADO] [SplashActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/SplashActivity.java) (Inicialização da API no código).

## Resultados dos Testes
* **Build:** Sucesso via Gradle (`app:assembleDebug`).
* **Visual:** O ícone do robô verde foi removido e substituído pelo novo logo estilizado com fundo roxo profundo.
