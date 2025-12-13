# To-Do List App com Arquitetura Reativa

Este é um aplicativo de lista de tarefas (To-Do List) desenvolvido com Jetpack Compose. Ele serve como um projeto de estudo para demonstrar uma arquitetura Android moderna e reativa, utilizando componentes como **ViewModel**, **Room**, **Kotlin Flow**, e operadores avançados como **`flatMapLatest`** e **`combine`**.

## Funcionalidades

- **CRUD de Tarefas:** Adicionar, deletar e atualizar tarefas.
- **Persistência de Dados:** As tarefas são salvas localmente em um banco de dados SQLite usando a biblioteca Room.
- **Interface Reativa:** A UI é atualizada automaticamente em resposta a qualquer alteração nos dados.
- **Filtragem Dinâmica:** Filtre tarefas por categoria ou por status (concluídas ou não).
- **Ordenação Dinâmica:** Ordene as tarefas por ordem alfabética (A-Z, Z-A) ou pela ordem de criação.
- **Gestos de Swipe:** Deslize um item para a direita para marcá-lo como concluído, ou para a esquerda para deletá-lo.
- **Seleção Múltipla:** Delete várias tarefas de uma só vez.
- **Feedback Visual:** Barra de progresso animada e animações na lista.

---

## Arquitetura e Conceitos Aplicados

O projeto segue uma arquitetura baseada nos princípios de **Fluxo de Dados Unidirecional (UDF)**, onde o estado flui do `ViewModel` para a UI, e os eventos fluem da UI para o `ViewModel`. Isso torna o app mais previsível e fácil de depurar.

O fluxo geral de dados é:

**UI (Compose) → `ViewModel` → `Repository` → `DAO` (Room) → `Flow` → UI**

### 1. Camada de Dados: Room e Repository

A persistência de dados é gerenciada pela biblioteca **Room**, que fornece uma abstração sobre o SQLite, e pelo **Padrão Repository**, que isola a fonte de dados do resto do aplicativo.

-   **`@Entity` (`Task.kt`):** A classe `Task` é anotada como `@Entity` para que o Room saiba como mapeá-la para uma tabela no banco de dados.
-   **`@Dao` (`TaskDao.kt`):** A interface `TaskDao` define como acessamos os dados. O mais importante é que seus métodos de consulta retornam um **`Flow<List<Task>>`**. Isso significa que qualquer alteração na tabela de tarefas (insert, update, delete) emitirá automaticamente a nova lista para quem estiver "ouvindo", tornando a base de dados reativa.

    ```kotlin
    // TaskDao.kt
    @Query("SELECT * FROM tasks WHERE ...")
    fun getAll(...): Flow<List<Task>>
    ```

-   **`Repository` (`TaskRepository.kt`):** Atua como um intermediário entre o `ViewModel` e a fonte de dados (`TaskDao`). Ele traduz os filtros solicitados pelo `ViewModel` em chamadas concretas ao DAO. Essa camada de abstração facilita a manutenção e os testes, e permitiria, no futuro, adicionar outras fontes de dados (como uma API remota) sem impactar o `ViewModel`.

### 2. ViewModel: O Maestro da Reatividade

O `TodoListViewModel` é o cérebro do aplicativo. Ele mantém o estado da UI e reage aos eventos do usuário, orquestrando a busca de dados de forma eficiente.

-   **`StateFlow` para o Estado da UI (`_uiState`):** Um `MutableStateFlow` é usado para armazenar todo o estado relacionado à UI: a ordem de classificação (`sortOrder`), o filtro de visualização (`visualizationOption`) e as categorias selecionadas (`selectedCategories`).

    ```kotlin
    // TodoListViewModel.kt
    private val _uiState = MutableStateFlow(TodoListUiState())
    ```

-   **`flatMapLatest`: Consultas Dinâmicas e Eficientes:** Este é o conceito central para a reatividade da tela. O `flatMapLatest` é um operador de `Flow` que "escuta" as mudanças no `_uiState` e, a cada nova mudança, **cancela a consulta anterior ao banco de dados e inicia uma nova** com os filtros atualizados.

Para entender melhor como funciona o `flatMapLatest` e outra funções relacionadas ao tipo `Flow`, recomendo a leitura do [seguinte artigo](https://codeint.medium.com/flatmapconcat-flatmapmerge-and-flatmaplatest-internal-working-de1a7c3e0c63).


    ```kotlin
    // TodoListViewModel.kt
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks = _uiState.flatMapLatest { uiState ->
        taskRepository.getAll(
            sortOrder = uiState.sortOrder,
            visualization = uiState.visualizationOption,
            categories = uiState.selectedCategories
        )
    }.stateIn(...)
    ```

    **Como funciona?**
    1.  O `_uiState` emite um novo valor sempre que o usuário altera um filtro.
    2.  O `flatMapLatest` recebe esse novo `uiState` e chama `taskRepository.getAll(...)`, que retorna um novo `Flow` com a consulta SQL atualizada.
    3.  Crucialmente, ele **cancela a coleta do Flow anterior**, garantindo que apenas a consulta mais recente (e relevante) esteja ativa. Isso evita o processamento de dados obsoletos e economiza recursos.

-   **`combine`: Derivando o Estado Final:** O operador `combine` é usado para "combinar" múltiplos `Flows` e derivar um estado final. No `ViewModel`, ele é usado para determinar o status geral da UI (`TodoListState`), como, por exemplo, se a tela deve exibir uma mensagem de "Nenhuma tarefa encontrada" ou "Todas as tarefas concluídas".

    ```kotlin
    // TodoListViewModel.kt
    val uiState = combine(allTasksFromDb, tasks, _uiState) { allTasks, filteredTasks, uiState ->
        when {
            allTasks.isEmpty() -> uiState.copy(status = TodoListState.NoTaskRegistered)
            filteredTasks.isEmpty() -> uiState.copy(status = TodoListState.NoTasksToShow)
            // ... outras condições
            else -> uiState.copy(status = TodoListState.TaskToShow)
        }
    }.stateIn(...)
    ```

### 3. UI (Compose): Consumindo o Estado

A UI, construída com Jetpack Compose, é puramente declarativa. Ela apenas "coleta" o estado exposto pelo `ViewModel` usando `collectAsState()` e se redesenha quando o estado muda.

```kotlin
// MainActivity.kt
val todolistUiState by viewModel.uiState.collectAsState()
val tasks by viewModel.tasks.collectAsState()

// A UI usa 'tasks' e 'todolistUiState' para se desenhar.
TodoList(tasks = tasks, ...)
```

Graças a essa arquitetura, a UI não precisa saber *como* buscar ou filtrar os dados; ela apenas exibe o estado mais recente fornecido pelo `ViewModel`, resultando em um código mais limpo, desacoplado e fácil de testar.
