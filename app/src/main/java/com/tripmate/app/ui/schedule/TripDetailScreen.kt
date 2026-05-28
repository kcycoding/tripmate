package com.tripmate.app.ui.schedule

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tripmate.app.data.PackingItem
import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.Trip
import com.tripmate.app.data.scheduleCategories
import com.tripmate.app.ui.common.BackIconButton
import com.tripmate.app.ui.packing.PackingUiState
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun TripDetailScreen(
    trip: Trip,
    uiState: ScheduleUiState,
    packingUiState: PackingUiState,
    canEditTrip: Boolean,
    onEditTripClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddScheduleClick: (String) -> Unit,
    onEditScheduleClick: (ScheduleItem) -> Unit,
    onDeleteClick: (String) -> Unit,
    onMoveClick: (ScheduleItem, Int) -> Unit,
    onPackingTitleChange: (String) -> Unit,
    onPackingAddClick: () -> Unit,
    onPackingCheckedChange: (PackingItem, Boolean) -> Unit,
    onPackingDeleteClick: (String) -> Unit,
    onPackingErrorShown: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val days = remember(trip.startDate, trip.endDate) { tripDays(trip.startDate, trip.endDate) }
    var selectedTab by rememberSaveable(trip.id) { mutableStateOf(0) }
    var selectedDate by rememberSaveable(trip.id) { mutableStateOf(days.firstOrNull().orEmpty()) }
    var isManageMode by rememberSaveable(trip.id) { mutableStateOf(false) }
    var selectedItemIds by rememberSaveable(trip.id) { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(days) {
        if (selectedDate !in days) {
            selectedDate = days.firstOrNull().orEmpty()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    LaunchedEffect(packingUiState.errorMessage) {
        val message = packingUiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onPackingErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { onAddScheduleClick(selectedDate.ifBlank { trip.startDate }) }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "일정 추가")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TripHeader(
                trip = trip,
                canEditTrip = canEditTrip,
                onBackClick = onBackClick,
                onEditTripClick = onEditTripClick
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("일정 관리") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("준비물") }
                )
            }

            if (selectedTab == 0) {
                ScheduleTabContent(
                    trip = trip,
                    uiState = uiState,
                    days = days,
                    selectedDate = selectedDate,
                    isManageMode = isManageMode,
                    selectedItemIds = selectedItemIds,
                    onDateSelected = {
                        selectedDate = it
                        selectedItemIds = emptySet()
                    },
                    onManageModeChange = {
                        isManageMode = it
                        if (!it) selectedItemIds = emptySet()
                    },
                    onItemSelectedChange = { itemId, checked ->
                        selectedItemIds = if (checked) selectedItemIds + itemId else selectedItemIds - itemId
                    },
                    onDeleteSelectedClick = {
                        selectedItemIds.forEach(onDeleteClick)
                        selectedItemIds = emptySet()
                        isManageMode = false
                    },
                    context = context,
                    onEditClick = onEditScheduleClick,
                    onDeleteClick = onDeleteClick,
                    onMoveClick = onMoveClick
                )
            } else {
                PackingTabContent(
                    uiState = packingUiState,
                    onTitleChange = onPackingTitleChange,
                    onAddClick = onPackingAddClick,
                    onCheckedChange = onPackingCheckedChange,
                    onDeleteClick = onPackingDeleteClick
                )
            }
        }
    }
}

@Composable
fun ScheduleEditorScreen(
    trip: Trip,
    uiState: ScheduleUiState,
    onBackClick: () -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onPlaceNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onExpectedCostChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTravelTimeMemoChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val form = uiState.form
    val days = remember(trip.startDate, trip.endDate) { tripDays(trip.startDate, trip.endDate) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackIconButton(onClick = onBackClick)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (form.isEditing) "일정 수정" else "일정 추가",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = trip.title.ifBlank { "여행" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DateSelector(
                days = days,
                selectedDate = form.date,
                items = uiState.items,
                currency = trip.baseCurrency,
                onDateSelected = onDateChange
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.time,
                onValueChange = onTimeChange,
                label = { Text("시간") },
                placeholder = { Text("09:30") },
                singleLine = true,
                enabled = !uiState.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text("일정 제목") },
                singleLine = true,
                enabled = !uiState.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.placeName,
                onValueChange = onPlaceNameChange,
                label = { Text("장소명") },
                singleLine = true,
                enabled = !uiState.isSaving
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.expectedCost,
                    onValueChange = onExpectedCostChange,
                    label = { Text("예상 비용") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !uiState.isSaving
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.travelTimeMemo,
                    onValueChange = onTravelTimeMemoChange,
                    label = { Text("이동 예상 시간") },
                    placeholder = { Text("30분") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )
            }
            CategorySelector(
                selectedCategory = form.category,
                enabled = !uiState.isSaving,
                onCategoryChange = onCategoryChange
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.memo,
                onValueChange = onMemoChange,
                label = { Text("메모") },
                minLines = 4,
                enabled = !uiState.isSaving
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                onClick = onSaveClick
            ) {
                Text(text = if (uiState.isSaving) "저장 중..." else "저장")
            }
        }
    }
}

@Composable
private fun TripHeader(
    trip: Trip,
    canEditTrip: Boolean,
    onBackClick: () -> Unit,
    onEditTripClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackIconButton(onClick = onBackClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.title.ifBlank { "제목 없는 여행" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${trip.destination.ifBlank { "여행지 미정" }} · ${trip.dateRangeText}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "초대 코드 ${trip.inviteCode}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (canEditTrip) {
            IconButton(onClick = onEditTripClick) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "여행 수정")
            }
        }
    }
}

@Composable
private fun ScheduleTabContent(
    trip: Trip,
    uiState: ScheduleUiState,
    days: List<String>,
    selectedDate: String,
    isManageMode: Boolean,
    selectedItemIds: Set<String>,
    onDateSelected: (String) -> Unit,
    onManageModeChange: (Boolean) -> Unit,
    onItemSelectedChange: (String, Boolean) -> Unit,
    onDeleteSelectedClick: () -> Unit,
    context: Context,
    onEditClick: (ScheduleItem) -> Unit,
    onDeleteClick: (String) -> Unit,
    onMoveClick: (ScheduleItem, Int) -> Unit
) {
    val selectedItems = uiState.items
        .filter { it.date == selectedDate }
        .sortedWith(compareBy<ScheduleItem> { it.sortOrder }.thenBy { it.time })

    DateSelector(
        days = days,
        selectedDate = selectedDate,
        items = uiState.items,
        currency = trip.baseCurrency,
        onDateSelected = onDateSelected
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "전체 일정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedDate.ifBlank { "날짜 없음" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isManageMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onManageModeChange(false) }) { Text("완료") }
                Button(
                    enabled = selectedItemIds.isNotEmpty(),
                    onClick = onDeleteSelectedClick
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    Text("삭제")
                }
            }
        } else {
            OutlinedButton(
                enabled = selectedItems.isNotEmpty(),
                onClick = { onManageModeChange(true) }
            ) { Text("편집") }
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "일정을 불러오는 중입니다.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedItems.isEmpty()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "등록된 일정이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                selectedItems.forEachIndexed { index, item ->
                    ScheduleItemCard(
                        item = item,
                        currency = trip.baseCurrency,
                        isManageMode = isManageMode,
                        isSelected = item.id in selectedItemIds,
                        canMoveUp = index > 0,
                        canMoveDown = index < selectedItems.lastIndex,
                        onLongClick = { onManageModeChange(true) },
                        onSelectedChange = { checked -> onItemSelectedChange(item.id, checked) },
                        onMapClick = { openMap(context, trip.mapProvider, item.placeName) },
                        onEditClick = { onEditClick(item) },
                        onDeleteClick = { onDeleteClick(item.id) },
                        onMoveUpClick = { onMoveClick(item, -1) },
                        onMoveDownClick = { onMoveClick(item, 1) }
                    )
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun DateSelector(
    days: List<String>,
    selectedDate: String,
    items: List<ScheduleItem>,
    currency: String,
    onDateSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, date ->
            val dayItems = items.filter { it.date == date }
            val totalCost = dayItems.mapNotNull { it.expectedCost }.sum()
            FilterChip(
                selected = date == selectedDate,
                onClick = { onDateSelected(date) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Day ${index + 1}", fontWeight = FontWeight.SemiBold)
                        Text(text = shortDateLabel(date), style = MaterialTheme.typography.bodySmall)
                        if (totalCost > 0.0) {
                            Text(
                                text = "${"%,.0f".format(totalCost)} $currency",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "일정 ${dayItems.size}개",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    enabled: Boolean,
    onCategoryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        scheduleCategories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                enabled = enabled,
                onClick = { onCategoryChange(category) },
                label = { Text(text = category) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScheduleItemCard(
    item: ScheduleItem,
    currency: String,
    isManageMode: Boolean,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onLongClick: () -> Unit,
    onSelectedChange: (Boolean) -> Unit,
    onMapClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isManageMode) {
                Checkbox(checked = isSelected, onCheckedChange = onSelectedChange)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listOf(item.time, item.title).filter { it.isNotBlank() }.joinToString("  "),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.placeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "비용 ${item.costText} $currency · 이동 ${item.travelTimeMemo.ifBlank { "-" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.memo.isNotBlank()) {
                    Text(
                        text = item.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onMapClick) {
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "지도")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "일정 수정")
                    }
                    if (!isManageMode) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "일정 삭제")
                        }
                    }
                }
            }
            if (isManageMode) {
                Column {
                    IconButton(enabled = canMoveUp, onClick = onMoveUpClick) {
                        Icon(imageVector = Icons.Filled.KeyboardArrowUp, contentDescription = "위로 이동")
                    }
                    IconButton(enabled = canMoveDown, onClick = onMoveDownClick) {
                        Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "아래로 이동")
                    }
                }
            }
        }
    }
}

@Composable
private fun PackingTabContent(
    uiState: PackingUiState,
    onTitleChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCheckedChange: (PackingItem, Boolean) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "준비물",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.items.count { it.isChecked }} / ${uiState.items.size} 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.newItemTitle,
                        onValueChange = onTitleChange,
                        label = { Text("준비물 이름") },
                        singleLine = true,
                        enabled = !uiState.isSaving
                    )
                    Button(
                        enabled = !uiState.isSaving,
                        onClick = onAddClick
                    ) {
                        Text(text = "추가")
                    }
                }
            }
        }

        when {
            uiState.isLoading -> Text(
                text = "준비물을 불러오는 중입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            uiState.items.isEmpty() -> Text(
                text = "등록된 준비물이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> uiState.items.forEach { item ->
                PackingItemRow(
                    item = item,
                    onCheckedChange = { isChecked -> onCheckedChange(item, isChecked) },
                    onDeleteClick = { onDeleteClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun PackingItemRow(
    item: PackingItem,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )
            Text(
                modifier = Modifier.weight(1f),
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "준비물 삭제")
            }
        }
    }
}

private fun tripDays(startDate: String, endDate: String): List<String> {
    return runCatching {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)
        val days = ChronoUnit.DAYS.between(start, end).toInt()
        if (days < 0) emptyList() else (0..days).map { start.plusDays(it.toLong()).format(formatter) }
    }.getOrElse { emptyList() }
}

private fun shortDateLabel(date: String): String {
    return runCatching {
        val parsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfWeek = parsed.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        "${parsed.monthValue}/${parsed.dayOfMonth} $dayOfWeek"
    }.getOrElse { date }
}

private fun openMap(context: Context, provider: String, placeName: String) {
    if (placeName.isBlank()) return
    val encodedPlace = URLEncoder.encode(placeName, "UTF-8")
    val primaryUri = mapUri(provider, encodedPlace)
    val fallbackUri = mapFallbackUri(provider, encodedPlace)
    val intent = Intent(Intent.ACTION_VIEW, primaryUri)
    runCatching { context.startActivity(intent) }
        .recoverCatching {
            if (it is ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
            } else {
                throw it
            }
        }
}

private fun mapUri(provider: String, encodedPlace: String): Uri {
    return if (provider == "google") {
        Uri.parse("geo:0,0?q=$encodedPlace")
    } else {
        Uri.parse("nmap://search?query=$encodedPlace&appname=com.tripmate.app")
    }
}

private fun mapFallbackUri(provider: String, encodedPlace: String): Uri {
    return if (provider == "google") {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedPlace")
    } else {
        Uri.parse("https://map.naver.com/p/search/$encodedPlace")
    }
}

