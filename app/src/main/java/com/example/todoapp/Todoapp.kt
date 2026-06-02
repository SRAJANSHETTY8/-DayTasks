package com.example.todoapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.todoapp.ui.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────

enum class Priority { HIGH, MEDIUM, LOW }

var _todoIdCounter = 0L

data class TodoItem(
    val id: Long = _todoIdCounter++,
    val title: String,
    val note: String = "",
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM
)

// ─────────────────────────────────────────────
//  Root composable
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    var todos by remember {
        mutableStateOf(
            listOf(
                TodoItem(id = 1, title = "Review design system", priority = Priority.HIGH),
                TodoItem(id = 2, title = "Prepare quarterly report", note = "Include KPIs", priority = Priority.MEDIUM),
                TodoItem(id = 3, title = "Book flight to Singapore", priority = Priority.LOW),
                TodoItem(id = 4, title = "Read Atomic Habits", isDone = true, priority = Priority.LOW),
            )
        )
    }

    var showSheet by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val filters = listOf("All", "Active", "Done")

    val filtered = when (selectedFilter) {
        "Active" -> todos.filter { !it.isDone }
        "Done"   -> todos.filter { it.isDone }
        else     -> todos
    }

    val pending = todos.count { !it.isDone }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
        // Subtle radial gradient hero glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldSubtle.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.08f),
                    radius = size.width * 0.55f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // ── Header ──────────────────────────────
            PremiumHeader(pending = pending)

            // ── Filter Tabs ─────────────────────────
            FilterRow(
                filters = filters,
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Task List ───────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item { EmptyState(filter = selectedFilter) }
                } else {
                    items(filtered, key = { it.id }) { todo ->
                        TodoCard(
                            item = todo,
                            onToggle = {
                                todos = todos.map {
                                    if (it.id == todo.id) it.copy(isDone = !it.isDone) else it
                                }
                            },
                            onDelete = {
                                todos = todos.filter { it.id != todo.id }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // ── FAB ─────────────────────────────────────
        PremiumFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onClick = { showSheet = true }
        )

        // ── Add Sheet ────────────────────────────────
        if (showSheet) {
            AddTodoSheet(
                sheetState = sheetState,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                    }
                },
                onAdd = { title, note, priority ->
                    todos = listOf(TodoItem(title = title, note = note, priority = priority)) + todos
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Header
// ─────────────────────────────────────────────

@Composable
fun PremiumHeader(pending: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "DAY-TASKS",
            style = MaterialTheme.typography.labelLarge,
            color = GoldPrimary,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pending > 0) {
                PendingBadge(count = pending)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Gold divider line
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(GoldPrimary, GoldPrimary.copy(alpha = 0f))
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
fun PendingBadge(count: Int) {
    Box(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .background(GoldSubtle, RoundedCornerShape(8.dp))
            .border(1.dp, GoldDim.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$count pending",
            style = MaterialTheme.typography.labelSmall,
            color = GoldLight,
            letterSpacing = 0.5.sp
        )
    }
}

// ─────────────────────────────────────────────
//  Filter Row
// ─────────────────────────────────────────────

@Composable
fun FilterRow(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) GoldPrimary else SurfaceCard,
                animationSpec = tween(220),
                label = "filterBg"
            )
            val txtColor by animateColorAsState(
                targetValue = if (isSelected) DeepCharcoal else TextSecondary,
                animationSpec = tween(220),
                label = "filterTxt"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else SurfaceBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelLarge,
                    color = txtColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Todo Card
// ─────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoCard(item: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (item.priority) {
        Priority.HIGH   -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW    -> PriorityLow
    }
    val priorityLabel = when (item.priority) {
        Priority.HIGH   -> "HIGH"
        Priority.MEDIUM -> "MED"
        Priority.LOW    -> "LOW"
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (item.isDone) 0.38f else 1f,
        animationSpec = tween(300),
        label = "textAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(
            width = 1.dp,
            brush = if (item.isDone) {
                Brush.horizontalGradient(listOf(SurfaceBorder, SurfaceBorder))
            } else {
                Brush.horizontalGradient(
                    listOf(priorityColor.copy(alpha = 0.25f), SurfaceBorder.copy(alpha = 0.5f))
                )
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(
                        color = if (item.isDone) TextDisabled else priorityColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Checkbox
            PremiumCheckbox(checked = item.isDone, onCheckedChange = { onToggle() })

            Spacer(modifier = Modifier.width(14.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary.copy(alpha = textAlpha),
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary.copy(alpha = textAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Priority tag
                Box(
                    modifier = Modifier
                        .background(
                            color = priorityColor.copy(alpha = if (item.isDone) 0.06f else 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priorityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isDone) TextTertiary else priorityColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Premium Checkbox
// ─────────────────────────────────────────────

@Composable
fun PremiumCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) GoldPrimary else Color.Transparent,
        animationSpec = tween(250),
        label = "checkBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) GoldPrimary else CheckStroke,
        animationSpec = tween(250),
        label = "checkBorder"
    )

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn(tween(200)) + fadeIn(tween(200)),
            exit  = scaleOut(tween(150)) + fadeOut(tween(150))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = DeepCharcoal,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  FAB
// ─────────────────────────────────────────────

@Composable
fun PremiumFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(60.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = GoldPrimary.copy(alpha = 0.3f),
                spotColor = GoldPrimary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GoldLight, GoldPrimary),
                    start = Offset(0f, 0f),
                    end = Offset(60f, 60f)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add task",
            tint = DeepCharcoal,
            modifier = Modifier.size(26.dp)
        )
    }
}

// ─────────────────────────────────────────────
//  Empty State
// ─────────────────────────────────────────────

@Composable
fun EmptyState(filter: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SurfaceCard, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (filter == "Done") Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = GoldDim,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                "Done"   -> "Nothing completed yet"
                "Active" -> "All caught up!"
                else     -> "No tasks yet"
            },
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap + to add your first task",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
    }
}

// ─────────────────────────────────────────────
//  Add Task Bottom Sheet
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAdd: (title: String, note: String, priority: Priority) -> Unit
) {
    var title    by remember { mutableStateOf("") }
    var note     by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(SurfaceBorder, RoundedCornerShape(2.dp))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            // Sheet header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "NEW TASK",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Add to your list",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceCard, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title field
            PremiumTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Task title",
                isTitle = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Note field
            PremiumTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = "Add a note (optional)",
                isTitle = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Priority selector
            Text(
                text = "PRIORITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrioritySelector(selected = priority, onSelect = { priority = it })

            Spacer(modifier = Modifier.height(28.dp))

            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (title.isNotBlank()) {
                            Brush.horizontalGradient(listOf(GoldLight, GoldPrimary))
                        } else {
                            Brush.horizontalGradient(listOf(SurfaceCard, SurfaceCard))
                        }
                    )
                    .clickable(enabled = title.isNotBlank()) {
                        onAdd(title.trim(), note.trim(), priority)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add Task",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (title.isNotBlank()) DeepCharcoal else TextTertiary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Premium Text Field
// ─────────────────────────────────────────────

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isTitle: Boolean
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = if (isTitle) {
            MaterialTheme.typography.titleLarge.copy(color = TextPrimary)
        } else {
            MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
        },
        cursorBrush = SolidColor(GoldPrimary),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = if (isTitle) {
                            MaterialTheme.typography.titleLarge.copy(color = TextTertiary)
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(color = TextTertiary)
                        }
                    )
                }
                inner()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// ─────────────────────────────────────────────
//  Priority Selector
// ─────────────────────────────────────────────

@Composable
fun PrioritySelector(selected: Priority, onSelect: (Priority) -> Unit) {
    val options = listOf(
        Priority.HIGH   to "High",
        Priority.MEDIUM to "Medium",
        Priority.LOW    to "Low",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { (p, label) ->
            val isSelected = p == selected
            val color = when (p) {
                Priority.HIGH   -> PriorityHigh
                Priority.MEDIUM -> PriorityMedium
                Priority.LOW    -> PriorityLow
            }
            val bgAlpha by animateFloatAsState(
                targetValue = if (isSelected) 0.18f else 0.07f,
                label = "priorityAlpha"
            )
            val borderAlpha by animateFloatAsState(
                targetValue = if (isSelected) 0.8f else 0.2f,
                label = "priorityBorder"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = bgAlpha))
                    .border(1.dp, color.copy(alpha = borderAlpha), RoundedCornerShape(10.dp))
                    .clickable { onSelect(p) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) color else color.copy(alpha = 0.6f),
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Needed import for BasicTextField
// ─────────────────────────────────────────────
// Add this at the top of your file:
// import androidx.compose.foundation.text.BasicTextField
// import androidx.compose.ui.graphics.SolidColor