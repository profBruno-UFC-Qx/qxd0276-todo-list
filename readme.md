# To-Do List App com Arquitetura Reativa

Este é um aplicativo de lista de tarefas (To-Do List) desenvolvido com Jetpack Compose. Ele serve como um projeto de estudo para demonstrar uma arquitetura Android moderna e reativa, utilizando componentes como **ViewModel**, **Room**, **DataStore**, **Kotlin Flow** e **Koin** para injeção de dependência.

## Funcionalidades

- **CRUD de Tarefas:** Adicionar, deletar e atualizar tarefas.
- **Persistência de Dados:** As tarefas são salvas localmente em um banco de dados SQLite usando a biblioteca Room.
- **Preferências do Usuário com DataStore:** As opções de filtro e ordenação são salvas de forma assíncrona usando **Jetpack DataStore**.
- **Injeção de Dependência com Koin:** Gerenciamento centralizado das dependências do app, facilitando testes e manutenção.
- **Interface Reativa:** A UI é atualizada automaticamente em resposta a qualquer alteração nos dados ou nas preferências do usuário.
- **Filtragem e Ordenação Dinâmica:** Filtre e ordene as tarefas com as preferências salvas entre as sessões.
- **Gestos de Swipe:** Deslize um item para a direita para marcá-lo como concluído, ou para a esquerda para deletá-lo.
- **Seleção Múltipla:** Delete várias tarefas de uma só vez.

---

## Arquitetura e Conceitos Aplicados

O projeto segue uma arquitetura baseada em **Fluxo de Dados Unidirecional (UDF)** e **Injeção de Dependência (DI)**. O Koin é responsável por criar e fornecer as instâncias de cada componente (`Repository`, `ViewModel`, etc.), enquanto o estado flui do `ViewModel` para a UI, e os eventos fluem no sentido contrário.

O fluxo geral de dados é:

**UI (Compose) → `ViewModel` → `Repository` → (`DAO` (Room) / `DataStore`) → `Flow` → UI**

### 1. Injeção de Dependência com Koin

O Koin é um framework de injeção de dependência pragmático e leve para Kotlin. Ele nos ajuda a desacoplar as classes, fornecendo suas dependências em vez de deixá-las criá-las.

**Por que usar Koin?**
1.  **Simplicidade e Clareza:** A definição das dependências é feita em Kotlin puro usando uma DSL (Domain-Specific Language) simples e legível, sem a necessidade de anotações ou geração de código.
2.  **Desacoplamento:** As classes (como o `ViewModel`) não precisam saber como construir suas dependências (como o `Repository`). Elas simplesmente as recebem como parâmetros no construtor. Isso torna o código mais modular.
3.  **Facilidade de Testes:** Em testes unitários ou de instrumentação, podemos facilmente substituir as dependências reais por implementações falsas (`fakes`) ou `mocks`, permitindo testar cada componente de forma isolada.

No projeto, os módulos do Koin são definidos em `AppModule.kt`, onde declaramos como criar cada parte do aplicativo:

```kotlin
// AppModule.kt
val appModule = module {
    // Singleton do Database e DAOs
    single { Room.databaseBuilder(...).build() }
    single { get<AppDatabase>().taskDao() }

    // Singleton dos Repositories
    single { UserPreferencesRepository(androidContext()) }
    single { TaskRepository(get()) } // Koin injeta o TaskDao aqui

    // Definição do ViewModel
    viewModel { TodoListViewModel(get(), get()) } // Koin injeta os dois repositories
}
```

### 2. Camada de Dados: Room, DataStore e Repository

A persistência de dados é gerenciada por Room (dados estruturados) e DataStore (preferências do usuário). O **Padrão Repository** isola essas fontes de dados do resto do aplicativo.

-   **`@Dao` (`TaskDao.kt`):** Retorna `Flow<List<Task>>` para tornar a base de dados reativa.

-   **`DataStore` (`UserPreferencesRepository.kt`):** Salva as preferências de filtro e ordenação usando uma API assíncrona baseada em `Flow`, garantindo segurança de tipo e evitando bloqueios na thread principal.

-   **`Repository` (`TaskRepository.kt`):** Atua como um intermediário, sendo responsável por obter os dados do DAO.

### 3. ViewModel: O Maestro da Reatividade

O `TodoListViewModel` recebe suas dependências (`TaskRepository` e `UserPreferencesRepository`) via injeção de dependência pelo Koin. Ele combina os `Flows` vindos dos repositórios para construir o estado da UI.

-   **`flatMapLatest`: Consultas Dinâmicas e Eficientes:** O `ViewModel` combina o `Flow` de preferências do usuário com as ações na UI. O `flatMapLatest` escuta as mudanças e, a cada nova alteração, inicia uma nova consulta ao banco de dados com os filtros atualizados, cancelando a anterior para economizar recursos.

    ```kotlin
    // TodoListViewModel.kt
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks = combine(...) { ... }
        .flatMapLatest { (uiState, userPreferences) ->
            taskRepository.getAll(
                sortOrder = userPreferences.sortOrder,
                // ...
            )
        }.stateIn(...)
    ```

### 4. UI (Compose): Consumindo o Estado

A UI, construída com Jetpack Compose, permanece puramente declarativa. A grande mudança aqui é como obtemos a instância do `ViewModel`: em vez de usar `viewModel()`, usamos `koinViewModel()`.

```kotlin
// MainActivity.kt
val viewModel: TodoListViewModel = koinViewModel()
val todolistUiState by viewModel.uiState.collectAsState()
val tasks by viewModel.tasks.collectAsState()

// A UI usa 'tasks' e 'todolistUiState' para se desenhar.
TodoList(tasks = tasks, ...)
```

O `koinViewModel()` busca a instância do `ViewModel` gerenciada pelo Koin, que já vem com todas as suas dependências (`Repositories`) prontas para uso. Isso conclui o ciclo de injeção de dependência, resultando em um código mais limpo, desacoplado e fácil de testar.
