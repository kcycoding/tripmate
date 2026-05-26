package com.tripmate.app.ui.schedule

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tripmate.app.ui.common.BackIconButton
import com.tripmate.app.data.PackingItem
import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.Trip
import com.tripmate.app.data.scheduleCategories
import com.tripmate.app.ui.packing.PackingUiState
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun TripDetailScreen(
    trip: Trip,
    uiState: ScheduleUiState,
    packingUiState: PackingUiState,
    canEditTrip: Boolean,
    onEditTripClick: () -> Unit,
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
    onEditClick: (ScheduleItem) -> Unit,
    onCancelEditClick: () -> Unit,
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

    Column(
        modifier = modifier
            .fillMaxSize()
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canEditTrip) {
                    OutlinedButton(onClick = onEditTripClick) {
                        Text(text = "수정")
                    }
                }
            }
        }

        PackingSection(
            uiState = packingUiState,
            onTitleChange = onPackingTitleChange,
            onAddClick = onPackingAddClick,
            onCheckedChange = onPackingCheckedChange,
            onDeleteClick = onPackingDeleteClick
        )

        ScheduleForm(
            trip = trip,
            uiState = uiState,
            onDateChange = onDateChange,
            onTimeChange = onTimeChange,
            onPlaceNameChange = onPlaceNameChange,
            onTitleChange = onTitleChange,
            onMemoChange = onMemoChange,
            onExpectedCostChange = onExpectedCostChange,
            onCategoryChange = onCategoryChange,
            onTravelTimeMemoChange = onTravelTimeMemoChange,
            onSaveClick = onSaveClick,
            onCancelEditClick = onCancelEditClick
        )

        Text(
            text = "전체 일정",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

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
            days.forEachIndexed { index, date ->
                val dayItems = uiState.items.filter { it.date == date }.sortedWith(compareBy<ScheduleItem> { it.sortOrder }.thenBy { it.time })
                DayScheduleSection(
                    dayNumber = index + 1,
                    date = date,
                    items = dayItems,
                    trip = trip,
                    context = context,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onMoveClick = onMoveClick
                )
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun ScheduleForm(
    trip: Trip,
    uiState: ScheduleUiState,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onPlaceNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onExpectedCostChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTravelTimeMemoChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEditClick: () -> Unit
) {
    val form = uiState.form
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (form.isEditing) "일정 수정" else "일정 추가",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.date,
                    onValueChange = onDateChange,
                    label = { Text("날짜") },
                    placeholder = { Text(trip.startDate) },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.time,
                    onValueChange = onTimeChange,
                    label = { Text("시간") },
                    placeholder = { Text("09:30") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )
            }
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
                minLines = 2,
                enabled = !uiState.isSaving
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving,
                    onClick = onSaveClick
                ) {
                    Text(text = if (uiState.isSaving) "저장 중..." else if (form.isEditing) "수정 저장" else "일정 추가")
                }
                if (form.isEditing) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving,
                        onClick = onCancelEditClick
                    ) {
                        Text(text = "취소")
                    }
                }
            }
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
            if (category == selectedCategory) {
                Button(enabled = enabled, onClick = { onCategoryChange(category) }) {
                    Text(text = category)
                }
            } else {
                OutlinedButton(enabled = enabled, onClick = { onCategoryChange(category) }) {
                    Text(text = category)
                }
            }
        }
    }
}

@Composable
private fun DayScheduleSection(
    dayNumber: Int,
    date: String,
    items: List<ScheduleItem>,
    trip: Trip,
    context: Context,
    onEditClick: (ScheduleItem) -> Unit,
    onDeleteClick: (String) -> Unit,
    onMoveClick: (ScheduleItem, Int) -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Day $dayNumber · $date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (items.isEmpty()) {
                Text(
                    text = "등록된 일정이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEachIndexed { index, item ->
                    ScheduleItemCard(
                        item = item,
                        currency = trip.baseCurrency,
                        canMoveUp = index > 0,
                        canMoveDown = index < items.lastIndex,
                        onMapClick = { openMap(context, trip.mapProvider, item.placeName) },
                        onEditClick = { onEditClick(item) },
                        onDeleteClick = { onDeleteClick(item.id) },
                        onMoveUpClick = { onMoveClick(item, -1) },
                        onMoveDownClick = { onMoveClick(item, 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemCard(
    item: ScheduleItem,
    currency: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMapClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onMapClick) { Text("지도") }
                OutlinedButton(enabled = canMoveUp, onClick = onMoveUpClick) { Text("위") }
                OutlinedButton(enabled = canMoveDown, onClick = onMoveDownClick) { Text("아래") }
                OutlinedButton(onClick = onEditClick) { Text("수정") }
                OutlinedButton(onClick = onDeleteClick) { Text("삭제") }
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



@Composable
private fun PackingSection(
    uiState: PackingUiState,
    onTitleChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCheckedChange: (PackingItem, Boolean) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }
            }
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
}

@Composable
private fun PackingItemRow(
    item: PackingItem,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        OutlinedButton(onClick = onDeleteClick) {
            Text(text = "삭제")
        }
    }
}







