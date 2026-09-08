# Personalização da Splash Screen (Remoção do Robô do Android)

O usuário deseja remover o ícone padrão do Android (o robô verde) que aparece na inicialização e substituí-lo por algo mais bonito, preferencialmente com a palavra "Finmente".

No Android 12+, o sistema exibe uma Splash Screen obrigatória usando o ícone do aplicativo. Para personalizar isso, utilizaremos a API de Splash Screen da Android Jetpack.

## Mudanças Propostas

### Infraestrutura e Dependências

#### [MODIFICAR] [libs.versions.toml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/gradle/libs.versions.toml)
* Adicionar a dependência `androidx.core:core-splashscreen:1.0.1` (ou a versão mais estável compatível).

#### [MODIFICAR] [build.gradle.kts](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/build.gradle.kts)
* Implementar a biblioteca de splash screen.

### Recursos Visuais

#### [NOVO] [ic_splash_logo.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/drawable/ic_splash_logo.xml)
* Criar um ícone em vetor sofisticado que represente a marca "Finmente". Como criar texto complexo em vetor manualmente é difícil, criaremos um logotipo estilizado (um foguete ou uma letra "F" elegante) que combine com a identidade visual do app.

### Temas e Estilo

#### [MODIFICAR] [themes.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/res/values/themes.xml)
* Criar um novo estilo `Theme.App.Starting` herdando de `Theme.SplashScreen`.
* Configurar `windowSplashScreenAnimatedIcon` para o novo logo.
* Configurar `windowSplashScreenBackground` para as cores da marca (`deep_purple_bg`).
* Configurar `postSplashScreenTheme` para o tema atual da SplashActivity.

### Código e Manifesto

#### [MODIFICAR] [AndroidManifest.xml](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/AndroidManifest.xml)
* Alterar o tema da `SplashActivity` para `Theme.App.Starting`.

#### [MODIFICAR] [SplashActivity.java](file:///C:/Users/claudia32967876/AndroidStudioProjects/Finmente/app/src/main/java/com/example/finmente/SplashActivity.java)
* Chamar `SplashScreen.installSplashScreen(this)` antes de `super.onCreate()`.

## Plano de Verificação

### Testes Automáticos
* `gradlew assembleDebug` para garantir que as dependências foram adicionadas corretamente.

### Verificação Manual
* Abrir o aplicativo no emulador (Android 12+) e verificar se o ícone do robô foi substituído pelo novo logotipo da marca.
* Conferir se a transição entre a splash do sistema e a splash personalizada está fluida.
