# NexusBusiness

Aplicativo Android nativo e offline para controle comercial de pequenos estabelecimentos. O NexusBusiness reúne cadastro de produtos, estoque, rotas, pedidos, vendas, histórico e fluxo de caixa em uma interface escura com identidade visual roxa e ciano.

O projeto é independente do NexusStock: utiliza o pacote `com.example.nexusbusiness`, o banco local `nexus_business_database` e recursos visuais próprios.

## Tecnologias

- Kotlin
- Android nativo (SDK mínimo 24, alvo 35)
- Jetpack Compose e Material 3
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

## Funcionalidades principais

- CRUD de produtos com fotografia opcional.
- Controle e ajuste de estoque e preços.
- Cadastro e seleção de rotas.
- Registro de pedidos e vendas.
- Histórico de vendas e fluxo de caixa.
- Miniaturas nas listagens e ampliação por duplo toque.
- Operação totalmente offline.

## Persistência local

Os dados comerciais são mantidos no Room/SQLite. A versão atual do banco é 6 e inclui uma migração não destrutiva da versão 5 para adicionar `photoPath` aos produtos. O projeto não utiliza `fallbackToDestructiveMigration()`.

As fotografias ficam em `files/product_images`, diretório privado do aplicativo. O Room armazena apenas o caminho do arquivo. Imagens capturadas são corrigidas conforme a orientação EXIF, redimensionadas para no máximo 1600 pixels no maior lado e comprimidas em JPEG. Arquivos substituídos ou removidos só são apagados depois que a alteração é persistida e nenhuma entidade os referencia.

## Integração com a câmera

A tela de cadastro usa `ActivityResultContracts.TakePicture` com um `FileProvider`. A câmera grava a imagem completa em um arquivo temporário privado. Depois da captura, o processamento ocorre fora da thread principal e uma prévia é apresentada. Cancelar a câmera ou a edição preserva a fotografia anteriormente salva.

O uso da câmera atende ao requisito acadêmico de recurso nativo do dispositivo. O CRUD de produtos e a persistência Room atendem aos requisitos de cadastro, consulta, atualização, exclusão e armazenamento local.

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
- O aplicativo não sincroniza dados nem imagens com serviços externos.
