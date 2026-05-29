package com.tripmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tripmate.app.auth.AuthUiState
import com.tripmate.app.auth.AuthViewModel
import com.tripmate.app.data.PackingItem
import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.Trip
import com.tripmate.app.data.UserProfile
import com.tripmate.app.ui.auth.LoginScreen
import com.tripmate.app.ui.budget.BudgetUiState
import com.tripmate.app.ui.budget.BudgetViewModel
import com.tripmate.app.ui.home.HomeScreen
import com.tripmate.app.ui.home.TripListUiState
import com.tripmate.app.ui.home.TripListViewModel
import com.tripmate.app.ui.join.JoinTripScreen
import com.tripmate.app.ui.join.JoinTripUiState
import com.tripmate.app.ui.join.JoinTripViewModel
import com.tripmate.app.ui.packing.PackingUiState
import com.tripmate.app.ui.packing.PackingViewModel
import com.tripmate.app.ui.schedule.ScheduleUiState
import com.tripmate.app.ui.schedule.ScheduleViewModel
import com.tripmate.app.ui.schedule.ScheduleEditorScreen
import com.tripmate.app.ui.schedule.TripDetailScreen
import com.tripmate.app.ui.theme.TripMateTheme
import com.tripmate.app.ui.trip.CreateTripScreen
import com.tripmate.app.ui.trip.CreateTripUiState
import com.tripmate.app.ui.trip.CreateTripViewModel
import com.tripmate.app.ui.trip.EditTripScreen
import com.tripmate.app.ui.trip.EditTripUiState
import com.tripmate.app.ui.trip.EditTripViewModel

private enum class AppScreen {
    Home,
    CreateTrip,
    EditTrip,
    JoinTrip,
    TripDetail,
    ScheduleEditor
}

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val tripListViewModel: TripListViewModel by viewModels()
    private val createTripViewModel: CreateTripViewModel by viewModels()
    private val editTripViewModel: EditTripViewModel by viewModels()
    private val joinTripViewModel: JoinTripViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val packingViewModel: PackingViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripMateTheme {
                val authUiState by authViewModel.uiState.collectAsState()
                val tripListUiState by tripListViewModel.uiState.collectAsState()
                val createTripUiState by createTripViewModel.uiState.collectAsState()
                val editTripUiState by editTripViewModel.uiState.collectAsState()
                val joinTripUiState by joinTripViewModel.uiState.collectAsState()
                val scheduleUiState by scheduleViewModel.uiState.collectAsState()
                val packingUiState by packingViewModel.uiState.collectAsState()
                val budgetUiState by budgetViewModel.uiState.collectAsState()
                TripMateApp(
                    authUiState = authUiState,
                    tripListUiState = tripListUiState,
                    createTripUiState = createTripUiState,
                    editTripUiState = editTripUiState,
                    joinTripUiState = joinTripUiState,
                    scheduleUiState = scheduleUiState,
                    packingUiState = packingUiState,
                    budgetUiState = budgetUiState,
                    onGoogleSignInClick = { authViewModel.signIn(this) },
                    onCreateTrip = createTripViewModel::createTrip,
                    onTitleChange = createTripViewModel::updateTitle,
                    onDestinationChange = createTripViewModel::updateDestination,
                    onStartDateChange = createTripViewModel::updateStartDate,
                    onEndDateChange = createTripViewModel::updateEndDate,
                    onMemoChange = createTripViewModel::updateMemo,
                    onDomesticChange = createTripViewModel::updateDomestic,
                    onBaseCurrencyChange = createTripViewModel::updateBaseCurrency,
                    onExchangeRateChange = createTripViewModel::updateExchangeRate,
                    onMapProviderChange = createTripViewModel::updateMapProvider,
                    onCreateTripErrorShown = createTripViewModel::clearError,
                    onCreateTripFinished = createTripViewModel::resetSaved,
                    onLoadEditTrip = editTripViewModel::load,
                    onEditTripTitleChange = editTripViewModel::updateTitle,
                    onEditTripDestinationChange = editTripViewModel::updateDestination,
                    onEditTripStartDateChange = editTripViewModel::updateStartDate,
                    onEditTripEndDateChange = editTripViewModel::updateEndDate,
                    onEditTripMemoChange = editTripViewModel::updateMemo,
                    onEditTripDomesticChange = editTripViewModel::updateDomestic,
                    onEditTripBaseCurrencyChange = editTripViewModel::updateBaseCurrency,
                    onEditTripExchangeRateChange = editTripViewModel::updateExchangeRate,
                    onEditTripMapProviderChange = editTripViewModel::updateMapProvider,
                    onEditTripSave = editTripViewModel::save,
                    onEditTripDelete = editTripViewModel::delete,
                    onEditTripFinished = editTripViewModel::reset,
                    onEditTripErrorShown = editTripViewModel::clearError,
                    onJoinInviteCodeChange = joinTripViewModel::updateInviteCode,
                    onJoinTrip = joinTripViewModel::join,
                    onJoinTripFinished = joinTripViewModel::reset,
                    onJoinTripErrorShown = joinTripViewModel::clearError,
                    onSignOutClick = authViewModel::signOut,
                    onAuthErrorShown = authViewModel::clearError,
                    onTripListErrorShown = tripListViewModel::clearError,
                    onObserveTrips = tripListViewModel::observeTrips,
                    onObserveSchedule = scheduleViewModel::observeTrip,
                    onObservePacking = packingViewModel::observeTrip,
                    onObserveBudget = budgetViewModel::observeTrip,
                    onScheduleStartCreate = scheduleViewModel::startCreate,
                    onScheduleDateChange = scheduleViewModel::updateDate,
                    onScheduleTimeChange = scheduleViewModel::updateTime,
                    onSchedulePlaceNameChange = scheduleViewModel::updatePlaceName,
                    onScheduleTitleChange = scheduleViewModel::updateTitle,
                    onScheduleMemoChange = scheduleViewModel::updateMemo,
                    onScheduleExpectedCostChange = scheduleViewModel::updateExpectedCost,
                    onScheduleCategoryChange = scheduleViewModel::updateCategory,
                    onScheduleTravelTimeMemoChange = scheduleViewModel::updateTravelTimeMemo,
                    onScheduleSave = scheduleViewModel::save,
                    onScheduleEdit = scheduleViewModel::startEdit,
                    onScheduleDelete = scheduleViewModel::delete,
                    onScheduleMove = scheduleViewModel::move,
                    onScheduleMoveToIndex = scheduleViewModel::moveToIndex,
                    onScheduleSaved = scheduleViewModel::resetSaved,
                    onScheduleErrorShown = scheduleViewModel::clearError,
                    onPackingTitleChange = packingViewModel::updateNewItemTitle,
                    onPackingAdd = packingViewModel::add,
                    onPackingCheckedChange = packingViewModel::setChecked,
                    onPackingDelete = packingViewModel::delete,
                    onPackingErrorShown = packingViewModel::clearError,
                    onBudgetStartCreate = budgetViewModel::startCreate,
                    onBudgetStartEdit = budgetViewModel::startEdit,
                    onBudgetSelectScheduleItem = budgetViewModel::selectScheduleItem,
                    onBudgetDateChange = budgetViewModel::updateDate,
                    onBudgetTitleChange = budgetViewModel::updateTitle,
                    onBudgetCategoryChange = budgetViewModel::updateCategory,
                    onBudgetAmountChange = budgetViewModel::updateAmount,
                    onBudgetMemoChange = budgetViewModel::updateMemo,
                    onBudgetSave = budgetViewModel::save,
                    onBudgetDelete = budgetViewModel::delete,
                    onBudgetErrorShown = budgetViewModel::clearError
                )
            }
        }
    }
}

@Composable
fun TripMateApp(
    authUiState: AuthUiState,
    tripListUiState: TripListUiState,
    createTripUiState: CreateTripUiState,
    editTripUiState: EditTripUiState,
    joinTripUiState: JoinTripUiState,
    scheduleUiState: ScheduleUiState,
    packingUiState: PackingUiState,
    budgetUiState: BudgetUiState,
    onGoogleSignInClick: () -> Unit,
    onCreateTrip: (UserProfile) -> Unit,
    onTitleChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onDomesticChange: (Boolean) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onExchangeRateChange: (String) -> Unit,
    onMapProviderChange: (String) -> Unit,
    onCreateTripErrorShown: () -> Unit,
    onCreateTripFinished: () -> Unit,
    onLoadEditTrip: (Trip) -> Unit,
    onEditTripTitleChange: (String) -> Unit,
    onEditTripDestinationChange: (String) -> Unit,
    onEditTripStartDateChange: (String) -> Unit,
    onEditTripEndDateChange: (String) -> Unit,
    onEditTripMemoChange: (String) -> Unit,
    onEditTripDomesticChange: (Boolean) -> Unit,
    onEditTripBaseCurrencyChange: (String) -> Unit,
    onEditTripExchangeRateChange: (String) -> Unit,
    onEditTripMapProviderChange: (String) -> Unit,
    onEditTripSave: () -> Unit,
    onEditTripDelete: () -> Unit,
    onEditTripFinished: () -> Unit,
    onEditTripErrorShown: () -> Unit,
    onJoinInviteCodeChange: (String) -> Unit,
    onJoinTrip: (UserProfile) -> Unit,
    onJoinTripFinished: () -> Unit,
    onJoinTripErrorShown: () -> Unit,
    onSignOutClick: () -> Unit,
    onAuthErrorShown: () -> Unit,
    onTripListErrorShown: () -> Unit,
    onObserveTrips: (String) -> Unit,
    onObserveSchedule: (Trip) -> Unit,
    onObservePacking: (Trip) -> Unit,
    onObserveBudget: (Trip) -> Unit,
    onScheduleStartCreate: (String) -> Unit,
    onScheduleDateChange: (String) -> Unit,
    onScheduleTimeChange: (String) -> Unit,
    onSchedulePlaceNameChange: (String) -> Unit,
    onScheduleTitleChange: (String) -> Unit,
    onScheduleMemoChange: (String) -> Unit,
    onScheduleExpectedCostChange: (String) -> Unit,
    onScheduleCategoryChange: (String) -> Unit,
    onScheduleTravelTimeMemoChange: (String) -> Unit,
    onScheduleSave: (Trip, UserProfile) -> Unit,
    onScheduleEdit: (ScheduleItem) -> Unit,
    onScheduleDelete: (String, String) -> Unit,
    onScheduleMove: (String, ScheduleItem, Int, String) -> Unit,
    onScheduleMoveToIndex: (String, ScheduleItem, Int, String) -> Unit,
    onScheduleSaved: () -> Unit,
    onScheduleErrorShown: () -> Unit,
    onPackingTitleChange: (String) -> Unit,
    onPackingAdd: (Trip, UserProfile) -> Unit,
    onPackingCheckedChange: (String, PackingItem, Boolean, String) -> Unit,
    onPackingDelete: (String, String) -> Unit,
    onPackingErrorShown: () -> Unit,
    onBudgetStartCreate: (String) -> Unit,
    onBudgetStartEdit: (com.tripmate.app.data.ExpenseItem) -> Unit,
    onBudgetSelectScheduleItem: (ScheduleItem?) -> Unit,
    onBudgetDateChange: (String) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetCategoryChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetMemoChange: (String) -> Unit,
    onBudgetSave: (Trip, UserProfile) -> Unit,
    onBudgetDelete: (String, String) -> Unit,
    onBudgetErrorShown: () -> Unit
) {
    val user = authUiState.user
    var appScreen by remember(user?.id) { mutableStateOf(AppScreen.Home) }
    var selectedTripId by remember(user?.id) { mutableStateOf<String?>(null) }
    val selectedTrip = tripListUiState.trips.firstOrNull { it.id == selectedTripId }

    LaunchedEffect(user?.id) {
        if (user != null) {
            onObserveTrips(user.id)
        }
    }

    LaunchedEffect(selectedTrip?.id) {
        if (selectedTrip != null) {
            onObserveSchedule(selectedTrip)
            onObservePacking(selectedTrip)
            onObserveBudget(selectedTrip)
        }
    }

    LaunchedEffect(createTripUiState.isSaved) {
        if (createTripUiState.isSaved) {
            appScreen = AppScreen.Home
            onCreateTripFinished()
        }
    }

    LaunchedEffect(editTripUiState.isSaved) {
        if (editTripUiState.isSaved) {
            appScreen = AppScreen.TripDetail
            onEditTripFinished()
        }
    }

    LaunchedEffect(editTripUiState.isDeleted) {
        if (editTripUiState.isDeleted) {
            selectedTripId = null
            appScreen = AppScreen.Home
            onEditTripFinished()
        }
    }

    LaunchedEffect(joinTripUiState.isJoined, joinTripUiState.joinedTripId) {
        val joinedTripId = joinTripUiState.joinedTripId
        if (joinTripUiState.isJoined && joinedTripId != null) {
            selectedTripId = joinedTripId
            appScreen = AppScreen.Home
            onJoinTripFinished()
        }
    }

    LaunchedEffect(scheduleUiState.isSaved) {
        if (scheduleUiState.isSaved) {
            appScreen = AppScreen.TripDetail
            onScheduleSaved()
        }
    }
    LaunchedEffect(appScreen, selectedTrip) {
        if (appScreen == AppScreen.TripDetail && selectedTrip == null) {
            appScreen = AppScreen.Home
        }
    }

    BackHandler(enabled = user != null && appScreen != AppScreen.Home) {
        when (appScreen) {
            AppScreen.CreateTrip -> {
                onCreateTripFinished()
                appScreen = AppScreen.Home
            }
            AppScreen.EditTrip -> {
                onEditTripFinished()
                appScreen = AppScreen.TripDetail
            }
            AppScreen.JoinTrip -> {
                onJoinTripFinished()
                appScreen = AppScreen.Home
            }
            AppScreen.ScheduleEditor -> appScreen = AppScreen.TripDetail
            AppScreen.TripDetail -> appScreen = AppScreen.Home
            AppScreen.Home -> Unit
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when {
            user == null -> LoginScreen(
                uiState = authUiState,
                onGoogleSignInClick = onGoogleSignInClick,
                onErrorShown = onAuthErrorShown,
                modifier = Modifier.padding(innerPadding)
            )

            appScreen == AppScreen.CreateTrip -> CreateTripScreen(
                uiState = createTripUiState,
                onTitleChange = onTitleChange,
                onDestinationChange = onDestinationChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onMemoChange = onMemoChange,
                onDomesticChange = onDomesticChange,
                onBaseCurrencyChange = onBaseCurrencyChange,
                onExchangeRateChange = onExchangeRateChange,
                onMapProviderChange = onMapProviderChange,
                onSaveClick = { onCreateTrip(user) },
                onBackClick = {
                    onCreateTripFinished()
                    appScreen = AppScreen.Home
                },
                onErrorShown = onCreateTripErrorShown,
                modifier = Modifier.padding(innerPadding)
            )

            appScreen == AppScreen.EditTrip -> EditTripScreen(
                uiState = editTripUiState,
                onTitleChange = onEditTripTitleChange,
                onDestinationChange = onEditTripDestinationChange,
                onStartDateChange = onEditTripStartDateChange,
                onEndDateChange = onEditTripEndDateChange,
                onMemoChange = onEditTripMemoChange,
                onDomesticChange = onEditTripDomesticChange,
                onBaseCurrencyChange = onEditTripBaseCurrencyChange,
                onExchangeRateChange = onEditTripExchangeRateChange,
                onMapProviderChange = onEditTripMapProviderChange,
                onSaveClick = onEditTripSave,
                onDeleteClick = onEditTripDelete,
                onBackClick = {
                    onEditTripFinished()
                    appScreen = AppScreen.TripDetail
                },
                onErrorShown = onEditTripErrorShown,
                modifier = Modifier.padding(innerPadding)
            )

            appScreen == AppScreen.JoinTrip -> JoinTripScreen(
                uiState = joinTripUiState,
                onInviteCodeChange = onJoinInviteCodeChange,
                onJoinClick = { onJoinTrip(user) },
                onBackClick = {
                    onJoinTripFinished()
                    appScreen = AppScreen.Home
                },
                onErrorShown = onJoinTripErrorShown,
                modifier = Modifier.padding(innerPadding)
            )

            appScreen == AppScreen.TripDetail && selectedTrip != null -> TripDetailScreen(
                trip = selectedTrip,
                uiState = scheduleUiState,
                packingUiState = packingUiState,
                budgetUiState = budgetUiState,
                canEditTrip = selectedTrip.ownerId == user.id,
                onEditTripClick = {
                    onLoadEditTrip(selectedTrip)
                    appScreen = AppScreen.EditTrip
                },
                onBackClick = { appScreen = AppScreen.Home },
                onAddScheduleClick = { date ->
                    onScheduleStartCreate(date)
                    appScreen = AppScreen.ScheduleEditor
                },
                onEditScheduleClick = { item ->
                    onScheduleEdit(item)
                    appScreen = AppScreen.ScheduleEditor
                },
                onDeleteClick = { itemId -> onScheduleDelete(selectedTrip.id, itemId) },
                onMoveClick = { item, direction -> onScheduleMove(selectedTrip.id, item, direction, user.id) },
                onMoveToIndex = { item, targetIndex -> onScheduleMoveToIndex(selectedTrip.id, item, targetIndex, user.id) },
                onPackingTitleChange = onPackingTitleChange,
                onPackingAddClick = { onPackingAdd(selectedTrip, user) },
                onPackingCheckedChange = { item, isChecked -> onPackingCheckedChange(selectedTrip.id, item, isChecked, user.id) },
                onPackingDeleteClick = { itemId -> onPackingDelete(selectedTrip.id, itemId) },
                onPackingErrorShown = onPackingErrorShown,
                onBudgetStartCreate = onBudgetStartCreate,
                onBudgetStartEdit = onBudgetStartEdit,
                onBudgetSelectScheduleItem = onBudgetSelectScheduleItem,
                onBudgetDateChange = onBudgetDateChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetMemoChange = onBudgetMemoChange,
                onBudgetSaveClick = { onBudgetSave(selectedTrip, user) },
                onBudgetDeleteClick = { itemId -> onBudgetDelete(selectedTrip.id, itemId) },
                onBudgetErrorShown = onBudgetErrorShown,
                onErrorShown = onScheduleErrorShown,
                modifier = Modifier.padding(innerPadding)
            )

            appScreen == AppScreen.ScheduleEditor && selectedTrip != null -> ScheduleEditorScreen(
                trip = selectedTrip,
                uiState = scheduleUiState,
                onBackClick = { appScreen = AppScreen.TripDetail },
                onDateChange = onScheduleDateChange,
                onTimeChange = onScheduleTimeChange,
                onPlaceNameChange = onSchedulePlaceNameChange,
                onTitleChange = onScheduleTitleChange,
                onMemoChange = onScheduleMemoChange,
                onExpectedCostChange = onScheduleExpectedCostChange,
                onCategoryChange = onScheduleCategoryChange,
                onTravelTimeMemoChange = onScheduleTravelTimeMemoChange,
                onSaveClick = { onScheduleSave(selectedTrip, user) },
                onErrorShown = onScheduleErrorShown,
                modifier = Modifier.padding(innerPadding)
            )
            else -> HomeScreen(
                user = user,
                tripListUiState = tripListUiState,
                onCreateTripClick = { appScreen = AppScreen.CreateTrip },
                onJoinTripClick = { appScreen = AppScreen.JoinTrip },
                onTripClick = { trip ->
                    selectedTripId = trip.id
                    appScreen = AppScreen.TripDetail
                },
                onSignOutClick = onSignOutClick,
                onErrorShown = onTripListErrorShown,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    TripMateTheme {
        TripMateApp(
            authUiState = AuthUiState(isLoading = false),
            tripListUiState = TripListUiState(isLoading = false),
            createTripUiState = CreateTripUiState(),
            editTripUiState = EditTripUiState(),
            joinTripUiState = JoinTripUiState(),
            scheduleUiState = ScheduleUiState(),
            packingUiState = PackingUiState(),
            budgetUiState = BudgetUiState(),
            onGoogleSignInClick = {},
            onCreateTrip = {},
            onTitleChange = {},
            onDestinationChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onMemoChange = {},
            onDomesticChange = {},
            onBaseCurrencyChange = {},
            onExchangeRateChange = {},
            onMapProviderChange = {},
            onCreateTripErrorShown = {},
            onCreateTripFinished = {},
            onLoadEditTrip = {},
            onEditTripTitleChange = {},
            onEditTripDestinationChange = {},
            onEditTripStartDateChange = {},
            onEditTripEndDateChange = {},
            onEditTripMemoChange = {},
            onEditTripDomesticChange = {},
            onEditTripBaseCurrencyChange = {},
            onEditTripExchangeRateChange = {},
            onEditTripMapProviderChange = {},
            onEditTripSave = {},
            onEditTripDelete = {},
            onEditTripFinished = {},
            onEditTripErrorShown = {},
            onJoinInviteCodeChange = {},
            onJoinTrip = {},
            onJoinTripFinished = {},
            onJoinTripErrorShown = {},
            onSignOutClick = {},
            onAuthErrorShown = {},
            onTripListErrorShown = {},
            onObserveTrips = {},
            onObserveSchedule = {},
            onObservePacking = {},
            onObserveBudget = {},
            onScheduleStartCreate = {},
            onScheduleDateChange = {},
            onScheduleTimeChange = {},
            onSchedulePlaceNameChange = {},
            onScheduleTitleChange = {},
            onScheduleMemoChange = {},
            onScheduleExpectedCostChange = {},
            onScheduleCategoryChange = {},
            onScheduleTravelTimeMemoChange = {},
            onScheduleSave = { _, _ -> },
            onScheduleEdit = {},
            onScheduleDelete = { _, _ -> },
            onScheduleMove = { _, _, _, _ -> },
            onScheduleMoveToIndex = { _, _, _, _ -> },
            onScheduleSaved = {},
            onScheduleErrorShown = {},
            onPackingTitleChange = {},
            onPackingAdd = { _, _ -> },
            onPackingCheckedChange = { _, _, _, _ -> },
            onPackingDelete = { _, _ -> },
            onPackingErrorShown = {},
            onBudgetStartCreate = {},
            onBudgetStartEdit = {},
            onBudgetSelectScheduleItem = {},
            onBudgetDateChange = {},
            onBudgetTitleChange = {},
            onBudgetCategoryChange = {},
            onBudgetAmountChange = {},
            onBudgetMemoChange = {},
            onBudgetSave = { _, _ -> },
            onBudgetDelete = { _, _ -> },
            onBudgetErrorShown = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    TripMateTheme {
        TripMateApp(
            authUiState = AuthUiState(
                isLoading = false,
                user = UserProfile(
                    id = "preview",
                    displayName = "홍길동",
                    email = "user@example.com"
                )
            ),
            tripListUiState = TripListUiState(isLoading = false),
            createTripUiState = CreateTripUiState(),
            editTripUiState = EditTripUiState(),
            joinTripUiState = JoinTripUiState(),
            scheduleUiState = ScheduleUiState(),
            packingUiState = PackingUiState(),
            budgetUiState = BudgetUiState(),
            onGoogleSignInClick = {},
            onCreateTrip = {},
            onTitleChange = {},
            onDestinationChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onMemoChange = {},
            onDomesticChange = {},
            onBaseCurrencyChange = {},
            onExchangeRateChange = {},
            onMapProviderChange = {},
            onCreateTripErrorShown = {},
            onCreateTripFinished = {},
            onLoadEditTrip = {},
            onEditTripTitleChange = {},
            onEditTripDestinationChange = {},
            onEditTripStartDateChange = {},
            onEditTripEndDateChange = {},
            onEditTripMemoChange = {},
            onEditTripDomesticChange = {},
            onEditTripBaseCurrencyChange = {},
            onEditTripExchangeRateChange = {},
            onEditTripMapProviderChange = {},
            onEditTripSave = {},
            onEditTripDelete = {},
            onEditTripFinished = {},
            onEditTripErrorShown = {},
            onJoinInviteCodeChange = {},
            onJoinTrip = {},
            onJoinTripFinished = {},
            onJoinTripErrorShown = {},
            onSignOutClick = {},
            onAuthErrorShown = {},
            onTripListErrorShown = {},
            onObserveTrips = {},
            onObserveSchedule = {},
            onObservePacking = {},
            onObserveBudget = {},
            onScheduleStartCreate = {},
            onScheduleDateChange = {},
            onScheduleTimeChange = {},
            onSchedulePlaceNameChange = {},
            onScheduleTitleChange = {},
            onScheduleMemoChange = {},
            onScheduleExpectedCostChange = {},
            onScheduleCategoryChange = {},
            onScheduleTravelTimeMemoChange = {},
            onScheduleSave = { _, _ -> },
            onScheduleEdit = {},
            onScheduleDelete = { _, _ -> },
            onScheduleMove = { _, _, _, _ -> },
            onScheduleMoveToIndex = { _, _, _, _ -> },
            onScheduleSaved = {},
            onScheduleErrorShown = {},
            onPackingTitleChange = {},
            onPackingAdd = { _, _ -> },
            onPackingCheckedChange = { _, _, _, _ -> },
            onPackingDelete = { _, _ -> },
            onPackingErrorShown = {},
            onBudgetStartCreate = {},
            onBudgetStartEdit = {},
            onBudgetSelectScheduleItem = {},
            onBudgetDateChange = {},
            onBudgetTitleChange = {},
            onBudgetCategoryChange = {},
            onBudgetAmountChange = {},
            onBudgetMemoChange = {},
            onBudgetSave = { _, _ -> },
            onBudgetDelete = { _, _ -> },
            onBudgetErrorShown = {}
        )
    }
}
















