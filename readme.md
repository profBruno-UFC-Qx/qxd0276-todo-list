# To-Do List App em Jetpack Compose

Este é um aplicativo de lista de tarefas (To-Do List) desenvolvido com Jetpack Compose, que serve como um exemplo prático de implementação de funcionalidades modernas em apps Android, com foco em performance e experiência de usuário.

## Funcionalidades Implementadas

- **Adicionar e Deletar Tarefas:** Adicione novas tarefas com descrição e categoria, ou delete-as individualmente.
- **Marcar como Concluída:** Marque e desmarque tarefas como concluídas.
- **Gestos de Swipe:** Deslize um item para a direita para marcá-lo como concluído, ou para a esquerda para deletá-lo.
- **Seleção Múltipla:** Pressione e segure um item para entrar no modo de seleção e deletar várias tarefas de uma só vez.
- **Filtro por Categoria:** Filtre a lista para ver apenas as tarefas de categorias específicas.
- **Filtro por Status:** Alterne a visualização entre "Todas as tarefas" e "Somente não concluídas".
- **Ordenação Alfabética:** Ordene a lista de tarefas por descrição em ordem ascendente (A-Z) ou descendente (Z-A).
- **Barra de Progresso:** Uma barra de progresso animada na parte inferior da tela mostra a porcentagem de tarefas concluídas.
- **Botão "Rolar para o Topo":** Um botão flutuante aparece ao rolar a lista para baixo, permitindo que o usuário volte ao topo com um único clique.

---

## Arquitetura com ViewModel e StateFlow

Para garantir que o aplicativo seja robusto, testável e resiliente a mudanças de configuração (como rotação de tela), a lógica de negócios e o gerenciamento de estado foram movidos para um `TodoListViewModel`. Esta abordagem segue as melhores práticas de arquitetura recomendadas pelo Google.

### 1. Centralização do Estado e Lógica de Negócios

O `ViewModel` atua como a única fonte da verdade para o estado da tela. Ele expõe o estado da UI e a lista de tarefas através de `StateFlow`, um fluxo de dados observável que sobrevive a mudanças de configuração.

**Implementação (`TodoListViewModel.kt`):**
```kotlin
class TodoListViewModel : ViewModel() {

    // Estado privado e mutável
    private val _uiState = MutableStateFlow(TodoListUiState())
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    // Expõe o estado como um StateFlow imutável para a UI
    val uiState = _uiState.asStateFlow()

    // Lógica de negócio que modifica o estado
    fun onCategorySelected(category: Category) {
        // ...
        _uiState.update { ... }
    }

    fun add(task: Task) {
        _tasks.value = _tasks.value + task
    }
    // ... outras funções
}
```
**Boas Práticas Adotadas:**
- **Encapsulamento:** A UI só tem acesso à versão imutável do estado (`StateFlow`), enquanto a versão mutável (`MutableStateFlow`) permanece privada no `ViewModel`.
- **Sobrevivência:** O estado no `ViewModel` sobrevive à recriação da `Activity`, preservando os dados do usuário.

### 2. Fluxo de Dados Unidirecional (UDF)

A UI (Composable) consome o estado do `ViewModel` e notifica o `ViewModel` sobre eventos do usuário. Isso cria um ciclo de dados claro e previsível: o estado flui para baixo (ViewModel -> UI) e os eventos fluem para cima (UI -> ViewModel).

**Implementação (`MainActivity.kt`):**
```kotlin
@Composable
fun TodoMainScreen(modifier: Modifier = Modifier, viewModel: TodoListViewModel = viewModel()) {

    // 1. A UI consome o estado do ViewModel
    val todolistUiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TodoTopBar(
                // ...
                // 2. A UI envia eventos para o ViewModel
                onFilterChange = viewModel::changeVisualization,
                onSortOrderChange = viewModel::sort,
                onDeleteSelected = viewModel::removeAll
            )
        },
        //...
    )
}
```
**Boas Práticas Adotadas:**
- **Clareza e Manutenibilidade:** Fica muito fácil entender como a UI é renderizada e como as interações do usuário afetam o estado.
- **Desacoplamento:** A UI não sabe *como* o estado é alterado, apenas notifica sobre a intenção do usuário.

### 3. Derivando Estado de Forma Reativa com `combine`

Uma prática de alta performance é derivar um estado a partir de outros. A lista de tarefas visível (`tasks`) depende tanto da lista original (`_tasks`) quanto dos filtros aplicados (`_uiState`). Usamos o operador `combine` para criar um novo `StateFlow` que é recalculado automaticamente sempre que uma de suas dependências muda.

**Implementação (`TodoListViewModel.kt`):**
```kotlin
// Este StateFlow é derivado de _tasks e _uiState
val tasks = combine(_tasks, _uiState) { tasks, uiState ->
    // Lógica de filtro e ordenação
    tasks.filter { ... }.sortedBy { ... }
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = emptyList()
)
```
**Boas Práticas Adotadas:**
- **Eficiência:** A lógica de filtragem/ordenação só é executada quando estritamente necessário (quando `_tasks` ou `_uiState` mudam), evitando cálculos desnecessários em cada recomposição.
- **Reatividade:** A UI que coleta `tasks` será atualizada automaticamente e de forma eficiente, não importa qual filtro ou dado tenha sido alterado.

---

## Destaques Técnicos e Melhores Práticas com `LazyColumn`

A `LazyColumn` é um componente essencial para exibir listas de forma performática. Abaixo estão os destaques de implementação neste projeto, seguindo as melhores práticas recomendadas.

### 1. Chave Única para Itens (`key`)

Para garantir que o Compose possa identificar cada item de forma única, otimizando a recomposição e permitindo animações, usamos a propriedade `key`. Isso evita que o Compose recrie itens desnecessariamente quando a lista muda.

**Implementação (`TodoList.kt`):**
```kotlin
LazyColumn(...) {
    items(tasks, key = { it.id }) { task ->
        // ... item da lista
    }
}
```
**Explicação:** Ao fornecer uma chave estável e única (como o `id` da tarefa), o Compose entende a identidade de cada item. Se um item for movido, o Compose apenas o move, em vez de recriá-lo em outra posição, o que melhora muito a performance.

### 2. Gerenciamento do Estado de Rolagem (`LazyListState`)

Para interagir com a posição da rolagem da lista (como no botão "Rolar para o Topo"), utilizamos o `rememberLazyListState`. Ele nos dá controle e informações sobre o estado da `LazyColumn`.

**Implementação (`MainActivity.kt`):**
```kotlin
// Cria e lembra do estado da lista
val listState = rememberLazyListState()

// Usa o estado para detectar se o botão deve ser exibido
val showScrollToTopButton by remember { 
    derivedStateOf { listState.firstVisibleItemIndex > 0 } 
}

// Passa o estado para a LazyColumn
TodoList(
    listState = listState,
    // ...
)
```
**Explicação:**
- `rememberLazyListState`: Cria e mantém o estado da `LazyColumn` entre as recomposições.
- `derivedStateOf`: É uma otimização de performance. Ele garante que o `showScrollToTopButton` seja recalculado apenas quando o `firstVisibleItemIndex` realmente mudar, evitando recomposições desnecessárias em cada pequeno movimento de rolagem.

### 3. Animações de Itens

Para dar um feedback visual suave quando itens são adicionados ou removidos, usamos o modifier `animateItem`.

**Implementação (`TodoList.kt`):**
```kotlin
SwipeToDismissBox(
    // ...
    modifier = Modifier.animateItem(tween(250))
) {
    // ... item
}
```
**Explicação:** Quando combinado com a `key` dos itens, o `animateItem` anima automaticamente a mudança de posição dos itens na lista. O `tween(250)` especifica que a animação deve durar 250 milissegundos.

### 4. Gestos de Clique e Clique Longo (`combinedClickable`)

Para lidar com múltiplos tipos de interação em um mesmo item (clique para selecionar, clique longo para iniciar o modo de seleção), usamos o `combinedClickable`.

**Implementação (`TodoListItem.kt`):**
```kotlin
Card(
    modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
) { ... }
```
**Explicação:** Essa é a forma recomendada de lidar com múltiplos gestos em um mesmo componente, pois é acessível e integra-se bem com o sistema de design do Material, incluindo o efeito visual de "ripple".
