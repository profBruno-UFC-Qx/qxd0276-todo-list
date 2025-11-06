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

### 3. Ordenação e Filtragem Eficiente

A manipulação da lista (ordenação e filtragem) é feita *antes* de passá-la para a `LazyColumn`. Isso garante que o Composable da lista só precise se preocupar em exibir os dados, seguindo o princípio de fluxo de dados unidirecional.

**Implementação (`MainActivity.kt`):**
```kotlin
val filteredTasks = tasks.filter { task ->
    // ... lógica de filtro
}.let { tasksToSort ->
    when (sortOrder) {
        SortOrder.ASCENDING -> tasksToSort.sortedBy { it.description }
        SortOrder.DESCENDING -> tasksToSort.sortedByDescending { it.description }
        SortOrder.NONE -> tasksToSort
    }
}

TodoList(
    tasks = filteredTasks,
    // ...
)
```
**Explicação:** O estado (`tasks`, `sortOrder`) é mantido no `TodoMainScreen`, e a lista é transformada (filtrada e ordenada) com base nesse estado. A `LazyColumn` recebe a lista já pronta, tornando o código mais limpo e o fluxo de dados, mais claro.

### 4. Animações de Itens

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

### 5. Gestos de Clique e Clique Longo (`combinedClickable`)

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
