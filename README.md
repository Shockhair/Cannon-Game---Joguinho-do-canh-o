# Cannon Game — Programação III

Projeto Android em Java baseado nas regras e na organização do Capítulo 6 de *Android 6 for Programmers: An App-Driven Approach*, 3ª edição. O objetivo é praticar Canvas, Paint, SurfaceView, SurfaceHolder, sons, eventos de toque e um game loop em thread separada.

## Integrantes

- Fernanda D’ark Moreira Santos.
- Henrique Augusto de Campos Nazario

**Apoio utilizado:** implementação inicial e documentação elaboradas com assistência de ChatGPT/Codex. Revisão, testes no dispositivo e alterações dos estudantes devem ser registrados com autoria real. Não foram criados commits em nome dos integrantes. A ferramenta de IA não substitui formalmente um integrante exigido pela disciplina.

## Como abrir e executar

1. Extraia o pacote no computador. Abra a pasta **CannonGame** pelo botão **Open** do Android Studio. Essa é a pasta que contém `settings.gradle`, `gradlew.bat` e `app`.
2. Use uma instalação do Android Studio compatível com Android Gradle Plugin 8.7.3 ou posterior. Não é necessário criar outro projeto.
3. Configure o **Gradle JDK como 17** em Settings → Build, Execution, Deployment → Build Tools → Gradle.
4. No SDK Manager, instale **Android SDK Platform 35**. Aceite a instalação de ferramentas adicionais solicitadas pelo Android Studio.
5. Aguarde a sincronização do Gradle com internet. O projeto usa Gradle 8.9, AGP 8.7.3 e Java 17.
6. Selecione um emulador ou celular com **Android 6.0 / API 23 ou superior** e clique em **Run ▶** no módulo `app`.
7. Leia as instruções iniciais, toque em **Jogar** e toque na área branca para disparar.

Pelo terminal do Android Studio no Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

No Linux/macOS:

```sh
chmod +x gradlew
./gradlew testDebugUnitTest assembleDebug lintDebug
```

O APK de depuração, após compilação bem-sucedida, ficará em `app/build/outputs/apk/debug/app-debug.apk`.

## Regras do capítulo

- Canhão à esquerda, apontado pela posição de toque; arrastar também atualiza a mira e permite disparar quando não há outro projétil.
- Somente um projétil em tela por vez.
- Nove alvos com movimento vertical, velocidades variadas e sentidos alternados.
- Bloqueador móvel entre o canhão e os alvos.
- Tempo inicial: **10 segundos**.
- Acerto em um alvo: remove o alvo e o projétil; adiciona **3 segundos**.
- Colisão com o bloqueador: rebate horizontalmente o projétil e desconta **2 segundos** uma única vez por projétil.
- Projétil que sai da tela é removido. Sair da tela não desconta os 2 segundos: essa penalidade pertence ao bloqueador.
- Vitória ao eliminar todos os alvos; derrota quando o tempo termina.
- Resultado apresenta disparos e duração da partida, com opção de reiniciar.
- Três efeitos sonoros enviados com a atividade: disparo, alvo e bloqueador.

## Recursos extras implementados

- **Pontuação por sequência:** primeiro acerto vale 100 pontos; seguintes valem 200, 300, 400 e até 500 pontos. A sequência é interrompida ao errar ou acertar o bloqueador.
- **Recorde local:** maior pontuação salva usando SharedPreferences.
- **Tela final ampliada:** pontuação, recorde, alvos, tiros, precisão e duração.
- **Colisão círculo × retângulo:** evita contar como acerto apenas a sobreposição dos cantos das caixas.
- **Subpassos de física:** reduzem a possibilidade de um projétil atravessar um alvo entre frames.

Não há dificuldade progressiva: as velocidades e dimensões da regra original foram preservadas. O recorde é do aparelho, sem serviço online.

## Organização

| Classe | Função |
|---|---|
| MainActivity | Janela, modo imersivo e hospedagem do fragmento |
| MainActivityFragment | Ciclo de vida da interface |
| CannonView | SurfaceView, desenho, toque e diálogos |
| CannonThread | Loop separado de atualização e desenho |
| GameEngine | Regras, relógio, pontuação e colisões |
| GameElement | Geometria retangular e movimento vertical |
| Target / Blocker | Especializações e suas recompensas/penalidades |
| Cannon | Mira e criação do projétil |
| Cannonball | Movimento e colisão circular |
| SoundManager | SoundPool e carregamento dos três sons |

Os objetos de jogo são modificados pela thread do jogo. A interface envia o toque por AtomicReference e recebe os resultados por Handler. A thread é parada com sinalização e `join()` antes de destruir a Surface. A pausa preserva a partida enquanto o processo e as dimensões da janela forem mantidos. Mudança de tamanho ou recriação completa abre uma nova partida; não há restauração de partida após encerramento do processo.

O fragmento nativo foi mantido para aproximar a organização da utilizada no livro e reduzir dependências. Essa API está obsoleta, mas permanece disponível na plataforma selecionada. Em um produto novo, uma migração para AndroidX seria apropriada. O modo imersivo usa WindowInsetsController em Android 11+ e a API compatível nos anteriores. Uma margem separa o HUD da área de jogo.

## Testes e situação da validação

- **23 verificações da física e regras executadas e aprovadas** em Java 17.
- **Todas as classes Java compiladas** contra a API Android 35. Foram emitidos avisos de APIs obsoletas, sem erros de Java.
- Verificação de recursos e manifestação: consultar `docs/VALIDACAO.md`.
- **A compilação completa pelo Gradle e a execução em aparelho/emulador ainda precisam ser confirmadas.** Este ambiente não tinha o SDK Android completo; a tentativa de baixar o Gradle via wrapper falhou na resolução de rede.
- `.github/workflows/android.yml` está preparado para executar testes, lint e gerar APK no GitHub Actions. O fluxo só terá resultado após o projeto ser publicado e executado no GitHub.

Os testes automatizados ficam em `app/src/test`. Consulte `docs/TESTES_MANUAIS.md` antes de entregar.

Jogo em execução:
<video controls src="gif.mp4" title="Title"></video>