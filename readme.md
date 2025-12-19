# To-Do List App com Arquitetura Reativa

Este é um aplicativo de lista de tarefas (To-Do List) desenvolvido com Jetpack Compose. Ele serve como um projeto de estudo para demonstrar uma arquitetura Android moderna e reativa, utilizando componentes como **ViewModel**, **Room**, **DataStore**, **Kotlin Flow**, e operadores avançados como **`flatMapLatest`** e **`combine`**.

## Funcionalidades

- **CRUD de Tarefas:** Adicionar, deletar e atualizar tarefas.
- **Persistência de Dados:** As tarefas são salvas localmente em um banco de dados SQLite usando a biblioteca Room.
- **Preferências do Usuário com DataStore:** As opções de filtro e ordenação são salvas de forma assíncrona usando **Jetpack DataStore**.
- **Interface Reativa:** A UI é atualizada automaticamente em resposta a qualquer alteração nos dados ou nas preferências do usuário.
- **Filtragem Dinâmica:** Filtre tarefas por categoria ou por status (concluídas ou não).
- **Ordenação Dinâmica:** Ordene as tarefas por ordem alfabética (A-Z, Z-A) ou pela ordem de criação.
- **Gestos de Swipe:** Deslize um item para a direita para marcá-lo como concluído, ou para a esquerda para deletá-lo.
- **Seleção Múltipla:** Delete várias tarefas de uma só vez.
- **Feedback Visual:** Barra de progresso animada e animações na lista.

---

## Arquitetura e Conceitos Aplicados

O projeto segue uma arquitetura baseada nos princípios de **Fluxo de Dados Unidirecional (UDF)**, onde o estado flui do `ViewModel` para a UI, e os eventos fluem da UI para o `ViewModel`. Isso torna o app mais previsível e fácil de depurar.

O fluxo geral de dados é:

**UI (Compose) → `ViewModel` → `Repository` → (`DAO` (Room) / `DataStore`) → `Flow` → UI**

### 1. Camada de Dados: Room, DataStore e Repository

A persistência de dados é gerenciada por duas bibliotecas principais:
-   **Room:** Para dados estruturados (a lista de tarefas).
-   **DataStore:** Para dados simples de chave-valor (as preferências do usuário).

O **Padrão Repository** isola essas fontes de dados do resto do aplicativo.

-   **`@Dao` (`TaskDao.kt`):** A interface `TaskDao` define como acessamos as tarefas. Seus métodos de consulta retornam um **`Flow<List<Task>>`**, tornando a base de dados reativa a qualquer alteração.

-   **`DataStore` (`UserPreferencesRepository.kt`):** Substituindo o antigo `SharedPreferences`, o **DataStore** é usado para salvar as preferências de filtro e ordenação do usuário.

    **Por que usar DataStore em vez de SharedPreferences?**
    1.  **Segurança de Tipo e Nulabilidade:** O DataStore usa `Preferences` com chaves tipadas, evitando erros de `ClassCastException` em tempo de execução.
    2.  **API Assíncrona com Flow:** Ele expõe os dados através de um `Flow<Preferences>`, o que o integra perfeitamente a uma arquitetura reativa com corrotinas. Isso elimina o risco de bloquear a thread principal, um problema comum com as chamadas síncronas do `SharedPreferences`.
    3.  **Resiliência a Erros:** A API de transação do DataStore garante a consistência dos dados, mesmo que o processo do app seja interrompido durante uma escrita.

    ```kotlin
    // UserPreferencesRepository.kt
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception -> ... }
        .map { preferences ->
            // Mapeia as preferências para um objeto de dados
        }
    ```

-   **`Repository` (`TaskRepository.kt`):** Atua como um intermediário entre o `ViewModel` e as fontes de dados (`TaskDao` e `UserPreferencesRepository`). Ele combina os dados das tarefas com as preferências do usuário para fornecer a lista filtrada e ordenada.

### 2. ViewModel: O Maestro da Reatividade

O `TodoListViewModel` orquestra a busca e a combinação de dados de forma eficiente.

-   **`StateFlow` para o Estado da UI (`_uiState`):** Um `MutableStateFlow` armazena o estado volátil da UI, como o status da tela e as categorias selecionadas.

-   **`flatMapLatest`: Consultas Dinâmicas e Eficientes:** O `ViewModel` agora combina dois `Flows`: um vindo do `UserPreferencesRepository` (preferências do usuário) e outro vindo das ações do usuário na UI. O `flatMapLatest` escuta as mudanças em ambos e, a cada nova alteração, **cancela a consulta anterior ao banco de dados e inicia uma nova** com os filtros e a ordenação atualizados.

    ```kotlin
    // TodoListViewModel.kt
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks = combine(
        _uiState,
        userPreferencesRepository.userPreferencesFlow
    ) { uiState, userPreferences ->
        // Objeto que contém os filtros combinados
        Pair(uiState, userPreferences)
    }.flatMapLatest { (uiState, userPreferences) ->
        taskRepository.getAll(
            sortOrder = userPreferences.sortOrder,
            visualization = userPreferences.visualization,
            categories = uiState.selectedCategories
        )
    }.stateIn(...)
    ```

    **Como funciona?**
    1.  O `combine` reage a qualquer mudança, seja no `_uiState` (ex: usuário seleciona uma nova categoria) ou no `userPreferencesFlow` (ex: `DataStore` emite as preferências salvas).
    2.  O `flatMapLatest` recebe os filtros mais recentes e chama `taskRepository.getAll(...)`, que retorna um novo `Flow` com a consulta SQL atualizada.
    3.  Crucialmente, ele **cancela a coleta do Flow anterior**, garantindo que apenas a consulta mais recente esteja ativa. Isso evita o processamento de dados obsoletos e economiza recursos.

### 3. UI (Compose): Consumindo o Estado

A UI, construída com Jetpack Compose, permanece puramente declarativa. Ela coleta o estado exposto pelo `ViewModel` usando `collectAsState()` e se redesenha quando o estado muda.

```kotlin
// MainActivity.kt
val todolistUiState by viewModel.uiState.collectAsState()
val tasks by viewModel.tasks.collectAsState()

// A UI usa 'tasks' e 'todolistUiState' para se desenhar.
TodoList(tasks = tasks, ...)
```
Graças a essa arquitetura, a UI não precisa saber *como* buscar, filtrar ou salvar preferências; ela apenas exibe o estado mais recente fornecido pelo `ViewModel`, resultando em um código limpo, desacoplado e fácil de testar.
