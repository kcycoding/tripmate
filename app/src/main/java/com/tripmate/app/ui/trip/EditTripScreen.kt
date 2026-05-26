package com.tripmate.app.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tripmate.app.ui.common.BackIconButton

@Composable
fun EditTripScreen(
    uiState: EditTripUiState,
    onTitleChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onDomesticChange: (Boolean) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onExchangeRateChange: (String) -> Unit,
    onMapProviderChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isBusy = uiState.isSaving || uiState.isDeleting

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
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
                    text = "여행 수정",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "여행 기본 정보와 지도 설정을 바꿀 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("여행 제목") },
            singleLine = true,
            enabled = !isBusy
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.destination,
            onValueChange = onDestinationChange,
            label = { Text("여행지") },
            singleLine = true,
            enabled = !isBusy
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = uiState.startDate,
                onValueChange = onStartDateChange,
                label = { Text("시작일") },
                singleLine = true,
                enabled = !isBusy
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = uiState.endDate,
                onValueChange = onEndDateChange,
                label = { Text("종료일") },
                singleLine = true,
                enabled = !isBusy
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.memo,
            onValueChange = onMemoChange,
            label = { Text("메모") },
            minLines = 3,
            enabled = !isBusy
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "국내 여행", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (uiState.isDomestic) "원화만 사용합니다." else "통화와 환율을 직접 입력합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = uiState.isDomestic, onCheckedChange = onDomesticChange, enabled = !isBusy)
        }

        if (!uiState.isDomestic) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.baseCurrency,
                    onValueChange = onBaseCurrencyChange,
                    label = { Text("통화") },
                    singleLine = true,
                    enabled = !isBusy
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.exchangeRateToKrw,
                    onValueChange = onExchangeRateChange,
                    label = { Text("환율") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !isBusy
                )
            }
        }

        Text(text = "기본 지도 앱", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = !isBusy,
                onClick = { onMapProviderChange("naver") }
            ) {
                Text(text = if (uiState.mapProvider == "naver") "네이버지도 선택됨" else "네이버지도")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !isBusy,
                onClick = { onMapProviderChange("google") }
            ) {
                Text(text = if (uiState.mapProvider == "google") "구글지도 선택됨" else "구글지도")
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            onClick = onSaveClick
        ) {
            Text(text = if (uiState.isSaving) "저장 중..." else "수정 저장")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            onClick = { showDeleteConfirm = true }
        ) {
            Text(text = if (uiState.isDeleting) "삭제 중..." else "여행 삭제")
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) showDeleteConfirm = false },
            title = { Text(text = "여행 삭제") },
            text = { Text(text = "이 여행과 일정, 준비물, 초대 코드가 함께 삭제됩니다. 계속할까요?") },
            confirmButton = {
                Button(
                    enabled = !isBusy,
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    }
                ) {
                    Text(text = "삭제")
                }
            },
            dismissButton = {
                OutlinedButton(
                    enabled = !isBusy,
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(text = "취소")
                }
            }
        )
    }

    SnackbarHost(hostState = snackbarHostState)
}





