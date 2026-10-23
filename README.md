# NexusBusiness

Aplicativo Android nativo e offline para controle comercial de pequenos estabelecimentos. O NexusBusiness reúne cadastro de produtos, estoque, rotas, pedidos, vendas, histórico e fluxo de caixa em uma interface escura com identidade visual roxa e ciano.

O aplicativo utiliza o pacote `com.example.nexusbusiness`, o banco local `nexus_business_database` e identidade visual própria.

## Tecnologias

- Kotlin
- Android nativo (SDK mínimo 24, alvo 35)
- Jetpack Compose e Material 3
- Animações de interação com APIs nativas do Compose
- MVVM com Repository
- Room sobre SQLite
- Kotlin Coroutines e Flow
- Navigation Compose
- Activity Result API e `FileProvider` para câmera

## Arquitetura

- `data/model`: entidades comerciais persistidas pelo Room.
- `data/local`: banco, DAOs e migrações.
- `data/repository`: acesso centralizado aos dados.
- `viewmodel`: estado da interface e regras de coordenação.
- `ui/screens`: telas Compose.
- `ui/components`: componentes reutilizáveis, incluindo miniaturas e modal de fotografia.
- `util`: formatação monetária e processamento seguro das imagens.
- `security`: proteção local por senha e controle persistente de tentativas.

## Funcionalidades principais

- CRUD de produtos com fotografia opcional.
- Controle e ajuste de estoque e preços.
- Cadastro e seleção de rotas com custo operacional.
- Rateio automático do custo da rota entre os produtos.
- Registro de pedidos e vendas.
- Histórico de vendas e fluxo de caixa.
- Miniaturas nas listagens e ampliação por duplo toque.
- Proteção opcional por senha na inicialização.
- Bloqueio por 30 minutos após cinco senhas incorretas.
- Preferência para habilitar ou desabilitar a navegação entre telas por gesto lateral.
- Preferência para exibir o Menu de Rodapé com ícones e nomes ou somente com ícones.
- Efeito 3D de pressionamento nos botões, com escala, deslocamento e sombra interna.
- Operação totalmente offline.

## Interação dos botões

Os botões das telas utilizam um efeito **3D Press** reutilizável. Ao pressionar, o componente reduz levemente sua escala, desloca-se para baixo, diminui a elevação externa e recebe uma sombra interna que simula o afundamento. Ao soltar, retorna suavemente ao estado normal.

O comportamento é aplicado aos botões preenchidos, contornados, de texto, de ícone e aos botões neon. A seta de expansão e os itens do Menu de Rodapé também seguem a mesma resposta visual. Estados desabilitados, ações, formatos e recursos de acessibilidade permanecem preservados.

A implementação central está em `ui/components/PressEffectButtons.kt`, evitando duplicação e mantendo a animação consistente entre as telas.

## Rotas e custo operacional

O NexusBusiness permite cadastrar rotas, informar o custo operacional de cada deslocamento e selecionar qual rota está ativa. Essa funcionalidade atende comerciantes que precisam se deslocar para comprar, transportar ou vender mercadorias.

Com uma rota ativa, o sistema distribui seu custo proporcionalmente entre os produtos, considerando o peso financeiro de cada item no valor total do estoque. O rateio unitário calculado é incorporado ao preço de venda juntamente com o custo do produto e sua margem, oferecendo uma composição de preço mais adequada às despesas do deslocamento.

## Persistência local

Os dados comerciais são mantidos no Room/SQLite. A versão atual do banco é 6 e inclui uma migração não destrutiva da versão 5 para adicionar `photoPath` aos produtos. O projeto não utiliza `fallbackToDestructiveMigration()`.

As fotografias ficam em `files/product_images`, diretório privado do aplicativo. O Room armazena apenas o caminho do arquivo. Imagens capturadas são corrigidas conforme a orientação EXIF, redimensionadas para no máximo 1600 pixels no maior lado e comprimidas em JPEG. Arquivos substituídos ou removidos só são apagados depois que a alteração é persistida e nenhuma entidade os referencia.

## Segurança de acesso

A proteção pode ser ativada na aba **Ajustes** com uma senha de no mínimo seis caracteres. A senha não é armazenada em texto puro: o aplicativo persiste somente um hash derivado por PBKDF2, acompanhado de um salt aleatório, em preferências privadas. Ao iniciar, o conteúdo do aplicativo permanece inacessível até a autenticação.

São permitidas cinco tentativas. Após o quinto erro, o acesso fica bloqueado por 30 minutos, inclusive se o aplicativo for encerrado e aberto novamente. Ao término desse período, cinco novas tentativas são liberadas. Para desativar a proteção, a senha atual deve ser confirmada.

A aba **Ajustes** também permite desativar o slide entre as telas principais. Quando desligado, a navegação ocorre exclusivamente pelos botões do Menu de Rodapé, cujo scroll horizontal permanece disponível. Essa preferência é mantida após reiniciar o aplicativo.

O Menu de Rodapé pode manter as legendas dos botões visíveis ou apresentar somente os ícones. A alteração preserva o efeito glass, a seleção, a navegação e o scroll lateral, e também permanece após reiniciar o aplicativo.

## Integração com a câmera

A tela de cadastro usa `ActivityResultContracts.TakePicture` com um `FileProvider`. A câmera grava a imagem completa em um arquivo temporário privado. Depois da captura, o processamento ocorre fora da thread principal e uma prévia é apresentada. Cancelar a câmera ou a edição preserva a fotografia anteriormente salva.

Essa integração permite fotografar produtos em resolução adequada e manter as imagens vinculadas aos respectivos cadastros no armazenamento privado do aplicativo.

## Como abrir

1. Instale o Android Studio com JDK 21.
2. Abra a pasta raiz `NexusBusiness`.
3. Aguarde a sincronização do Gradle.
4. Confirme que o Gradle JVM selecionado é o JDK 21.
5. Selecione um dispositivo com Android 7.0 (API 24) ou superior.

## Compilação e testes

No Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

O APK de depuração é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Limitações conhecidas

- A captura depende de um aplicativo de câmera disponível no dispositivo.
- A migração explícita cobre a atualização do banco NexusBusiness da versão 5 para a 6; bancos de outros aplicativos não são importados.
- Testes de câmera, gestos e reinicialização exigem validação manual ou instrumentação UI em dispositivo com instalação via USB autorizada.
- O fluxo visual de criação, desativação, autenticação e bloqueio da senha requer validação manual em dispositivo ou emulador.
- O aplicativo não sincroniza dados nem imagens com serviços externos.
