package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinTrackApp(viewModel: FinTrackViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    Scaffold(
        topBar = {
            FinTrackHeader()
        },
        bottomBar = {
            FinTrackBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(300),
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardTab(
                        transactions = allTransactions,
                        onViewAllClick = { viewModel.selectTab(1) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                    1 -> HistoryTab(
                        transactions = allTransactions,
                        searchQueryFlow = viewModel.searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        categoryFilterFlow = viewModel.categoryFilter,
                        onFilterChange = { viewModel.updateCategoryFilter(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                    2 -> AddTransactionTab(viewModel = viewModel)
                    3 -> ReportsTab(
                        transactions = allTransactions,
                        monthOffsetFlow = viewModel.selectedMonthOffset,
                        onMonthOffsetChange = { viewModel.changeMonthOffset(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FinTrackHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // High-fidelity User Avatar Mock with Teal gradient overlay holding initials
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF86B9D0),
                                TealPrimary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "FinTrack",
                style = MaterialTheme.typography.headlineLarge,
                color = TealPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        IconButton(
            onClick = { /* Handle Notifications */ },
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, BorderSoft, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = TealPrimary
            )
        }
    }
}

@Composable
fun FinTrackBottomNavBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                selected = currentTab == 0,
                icon = Icons.Outlined.GridView,
                iconSelected = Icons.Filled.GridView,
                label = "Dashboard",
                onClick = { onTabSelected(0) },
                tag = "nav_dashboard"
            )
            NavBarItem(
                selected = currentTab == 1,
                icon = Icons.Outlined.History,
                iconSelected = Icons.Filled.History,
                label = "History",
                onClick = { onTabSelected(1) },
                tag = "nav_history"
            )
            NavBarItem(
                selected = currentTab == 2,
                icon = Icons.Outlined.AddCircleOutline,
                iconSelected = Icons.Filled.AddCircle,
                label = "Add",
                onClick = { onTabSelected(2) },
                tag = "nav_add"
            )
            NavBarItem(
                selected = currentTab == 3,
                icon = Icons.Outlined.Analytics,
                iconSelected = Icons.Filled.Analytics,
                label = "Reports",
                onClick = { onTabSelected(3) },
                tag = "nav_reports"
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    selected: Boolean,
    icon: ImageVector,
    iconSelected: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    val backgroundBrush = if (selected) {
        Brush.horizontalGradient(listOf(TealContainer, TealPrimary))
    } else {
        null
    }

    val contentColor = if (selected) Color.White else TextMuted

    Column(
        modifier = Modifier
            .testTag(tag)
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .run {
                    if (backgroundBrush != null) background(backgroundBrush) else this
                }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) iconSelected else icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) TealPrimary else TextMuted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ---------------------- TABS IMPLEMENTATIONS ----------------------

@Composable
fun DashboardTab(
    transactions: List<TransactionEntity>,
    onViewAllClick: () -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val totalBalance = transactions.sumOf { if (it.type == "Income") it.amount else -it.amount }
    val monthlyIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val monthlyExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Card: Total Balance
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("hero_balance_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TealContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Decorative Canvas Grid Layer for subtle visual glow
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF86B9D0).copy(alpha = 0.15f), Color.Transparent),
                                center = Offset(size.width, 0f),
                                radius = 250.dp.toPx()
                            )
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Total Balance",
                            fontSize = 14.sp,
                            color = TealOnContainer.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(totalBalance),
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.02).sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trend Up",
                                tint = GreenContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+2.4% from last month",
                                fontSize = 12.sp,
                                color = GreenContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Bento scroll for Income & Expenses
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                Card(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GreenContainer.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Income icon",
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "+12%",
                                color = GreenSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Monthly Income",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrency(monthlyIncome),
                                fontSize = 20.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Expenses Card
                Card(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(RedContainer.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Expense icon",
                                    tint = RedError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "-4%",
                                color = RedError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Monthly Expenses",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrency(monthlyExpenses),
                                fontSize = 20.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bento Addition: Travel Goal Fund
                Card(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Savings icon",
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Savings Goal",
                                fontSize = 12.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Travel Fund (75%)",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Styled indicator bar
                            LinearProgressIndicator(
                                progress = { 0.75f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TealPrimary,
                                trackColor = TealPrimary.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAllClick) {
                    Text(
                        text = "View All",
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Recent List
        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(message = "No record yet. Use 'Add' tab to log dynamic expenses.")
            }
        } else {
            val limit = transactions.take(4)
            items(limit, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onDelete = { onDeleteTransaction(tx.id) }
                )
            }
        }
    }
}

@Composable
fun HistoryTab(
    transactions: List<TransactionEntity>,
    searchQueryFlow: StateFlow<String>,
    onSearchChange: (String) -> Unit,
    categoryFilterFlow: StateFlow<String>,
    onFilterChange: (String) -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val searchQuery by searchQueryFlow.collectAsState()
    val categoryFilter by categoryFilterFlow.collectAsState()

    // Filter transactions in real-time
    val filteredTransactions = remember(transactions, searchQuery, categoryFilter) {
        transactions.filter { tx ->
            // Search query filter
            val matchesQuery = tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.notes.contains(searchQuery, ignoreCase = true)

            // Category category selection filter
            val matchesFilter = when (categoryFilter) {
                "All" -> true
                "Income" -> tx.type == "Income"
                "Expense" -> tx.type == "Expense"
                "Savings" -> tx.category == "Salary" // In mock framework salaries act as savings indicator
                else -> tx.category.equals(categoryFilter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }

    // Grouping by Date using formatting
    val groupedTx = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            formatTransactionDate(tx.timestamp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search TextField Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search transactions...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = BorderOutline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Filters horizontal caps scroll
        val filterOptions = listOf("All", "Income", "Expense", "Savings")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { filter ->
                val isSelected = categoryFilter == filter
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = if (!isSelected) BorderStroke(1.dp, BorderSoft) else null,
                    modifier = Modifier.testTag("filter_chip_$filter")
                )
            }
        }

        // Lazy dynamic items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (groupedTx.isEmpty()) {
                item {
                    EmptyStateCard(message = "No transactions found matching criteria.")
                }
            } else {
                groupedTx.forEach { (dateHeader, txsInGroup) ->
                    item {
                        Text(
                            text = dateHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(txsInGroup, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            onDelete = { onDeleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTransactionTab(viewModel: FinTrackViewModel) {
    val amountInput by viewModel.amountInput.collectAsState()
    val titleInput by viewModel.titleInput.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val dateInput by viewModel.dateInput.collectAsState()
    val notesInput by viewModel.notesInput.collectAsState()

    val context = LocalContext.current
    var showErrorMessage by remember { mutableStateOf(false) }

    // Date Dialog Setup
    val calendar = remember(dateInput) { Calendar.getInstance().apply { timeInMillis = dateInput } }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                viewModel.updateDateInput(newCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Toggle capsule Expense/Income
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(BorderSoft, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.updateSelectedType("Expense") }
                        .background(if (selectedType == "Expense") Color.White else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedType == "Expense") TealPrimary else TextMuted,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.updateSelectedType("Income") }
                        .background(if (selectedType == "Income") Color.White else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedType == "Income") TealPrimary else TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Centered Big Amount field
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AMOUNT",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == "Expense") TealPrimary else GreenSuccess
                    )
                    TextField(
                        value = amountInput,
                        onValueChange = { if (it.length <= 10) viewModel.updateAmountInput(it) },
                        placeholder = {
                            Text(
                                "0.00",
                                color = BorderOutline,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.displayLarge.copy(
                            textAlign = TextAlign.Center,
                            color = if (selectedType == "Expense") TealPrimary else GreenSuccess,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .testTag("amount_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Title Input Field
        item {
            OutlinedTextField(
                value = titleInput,
                onValueChange = viewModel::updateTitleInput,
                label = { Text("Title / Store name") },
                placeholder = { Text("e.g. Starbucks, Salary, Apple") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderOutline
                )
            )
        }

        // Categories Grid (2x4)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                val categoriesList = listOf(
                    "Food" to Icons.Default.Restaurant,
                    "Transport" to Icons.Default.DirectionsCar,
                    "Shopping" to Icons.Default.ShoppingBag,
                    "Salary" to Icons.Default.Payments,
                    "Rent" to Icons.Default.Home,
                    "Health" to Icons.Default.FitnessCenter,
                    "Fun" to Icons.Default.Movie,
                    "Other" to Icons.Default.MoreHoriz
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = categoriesList.chunked(4)
                    chunked.forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCategories.forEach { (catName, icon) ->
                                val isSelected = selectedCategory == catName
                                GridCategoryChip(
                                    name = catName,
                                    icon = icon,
                                    selected = isSelected,
                                    onClick = { viewModel.updateSelectedCategory(catName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Date Picker field
        item {
            val formattedDate = remember(dateInput) {
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(dateInput))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BorderSoft.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, BorderOutline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = TextMuted)
                    Column {
                        Text("Date", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text(formattedDate, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Notes area
        item {
            OutlinedTextField(
                value = notesInput,
                onValueChange = viewModel::updateNotesInput,
                label = { Text("Notes (optional)") },
                placeholder = { Text("What was this for?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("notes_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderOutline
                )
            )
        }

        // Validation Error Display
        if (showErrorMessage) {
            item {
                Text(
                    text = "Please enter both a valid Title and positive numeric Amount.",
                    color = RedError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    val success = viewModel.addTransaction()
                    if (success) {
                        showErrorMessage = false
                    } else {
                        showErrorMessage = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 8.dp)
                    .testTag("save_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Check", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GridCategoryChip(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleBg = if (selected) TealPrimary else BorderSoft
    val circleIconTint = if (selected) Color.White else TealPrimary

    Column(
        modifier = modifier
            .testTag("category_cell_$name")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (selected) TealPrimary else BorderOutline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(circleBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = circleIconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) TealPrimary else TextMuted
        )
    }
}

@Composable
fun ReportsTab(
    transactions: List<TransactionEntity>,
    monthOffsetFlow: StateFlow<Int>,
    onMonthOffsetChange: (Int) -> Unit
) {
    val monthOffset by monthOffsetFlow.collectAsState()

    // Determine target Month and Year based on current instant + offset
    val targetCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetYear = targetCalendar.get(Calendar.YEAR)

    val targetMonthName = remember(monthOffset) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(targetCalendar.time)
    }

    // Filter transactions happening in this target month and index
    val filteredMonthlyTx = remember(transactions, targetMonth, targetYear) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            txCal.get(Calendar.MONTH) == targetMonth && txCal.get(Calendar.YEAR) == targetYear
        }
    }

    val monthlyIncome = filteredMonthlyTx.filter { it.type == "Income" }.sumOf { it.amount }
    val monthlyExpenses = filteredMonthlyTx.filter { it.type == "Expense" }.sumOf { it.amount }
    val netBalance = monthlyIncome - monthlyExpenses

    // Spending categories calculations
    val categoryExpList = remember(filteredMonthlyTx) {
        filteredMonthlyTx
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { (_, valueList) -> valueList.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalExpense = categoryExpList.sumOf { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Month Switcher Row Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BorderSoft.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, BorderOutline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthOffsetChange(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Past month", tint = TealPrimary)
                    }
                    Text(
                        text = targetMonthName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.testTag("report_month_indicator")
                    )
                    IconButton(onClick = { onMonthOffsetChange(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = TealPrimary)
                    }
                }
            }
        }

        // Summary Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "NET BALANCE THIS MONTH",
                        fontSize = 11.sp,
                        color = TealOnContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatCurrency(netBalance),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Net arrow",
                                tint = if (netBalance >= 0) GreenContainer else Color.Red,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "%.1f%%".format(if (monthlyIncome > 0) (netBalance / monthlyIncome) * 100 else 0f),
                                color = if (netBalance >= 0) GreenContainer else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Income / Expense dual column bento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("INCOME", fontSize = 9.sp, color = TealOnContainer, fontWeight = FontWeight.Bold)
                            Text(formatCurrency(monthlyIncome), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("EXPENSES", fontSize = 9.sp, color = TealOnContainer, fontWeight = FontWeight.Bold)
                            Text(formatCurrency(monthlyExpenses), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Real-Time Spending Donut Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending Split",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }

                    if (totalExpense <= 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses recorded this month.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Drawing custom arcs based on ratios
                        val chartColors = listOf(
                            TealPrimary,
                            Color(0xFF306579),
                            GreenSuccess,
                            Color(0xFFBA1A1A),
                            Color(0xFFE28B00),
                            Color(0xFF7A4BFF),
                            Color(0xFF009688),
                            Color(0xFF9E9E9E)
                        )

                        SpendDonutChart(
                            categorySplits = categoryExpList,
                            totalAmount = totalExpense,
                            colors = chartColors
                        )
                    }
                }
            }
        }

        // Highest Spend Limit Listing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Highest Spending",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )

                    val expenseItems = filteredMonthlyTx
                        .filter { it.type == "Expense" }
                        .sortedByDescending { it.amount }
                        .take(3)

                    if (expenseItems.isEmpty()) {
                        Text(
                            text = "No expenses reported.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        expenseItems.forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(TealPrimary.copy(alpha = 0.05f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (tx.category) {
                                            "Food" -> Icons.Default.Restaurant
                                            "Transport" -> Icons.Default.DirectionsCar
                                            "Shopping" -> Icons.Default.ShoppingBag
                                            "Salary" -> Icons.Default.Payments
                                            "Rent" -> Icons.Default.Home
                                            "Health" -> Icons.Default.FitnessCenter
                                            "Fun" -> Icons.Default.Movie
                                            else -> Icons.Default.MoreHoriz
                                        }
                                        Icon(icon, contentDescription = tx.category, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                    }
                                    Column {
                                        Text(tx.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                                        Text(sdf.format(Date(tx.timestamp)), fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "-${formatCurrency(tx.amount)}",
                                        color = RedError,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val percentOfTotal = if (totalExpense > 0) (tx.amount / totalExpense) * 100 else 0.0
                                    Text(
                                        text = "%.0f%% OF TOTAL".format(percentOfTotal),
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Savings Trend (Asymmetric UI Detail)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "SAVINGS TREND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "You saved $420 more than last month",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // 5 Columns Custom Bar Chart representing trends
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = listOf(0.4f, 0.61f, 0.35f, 0.65f, 0.95f)
                        heights.forEachIndexed { idx, weight ->
                            val color = if (idx == 4) TealPrimary else TealPrimary.copy(alpha = 0.25f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp)
                                    .background(BorderSoft, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(weight)
                                        .align(Alignment.BottomCenter)
                                        .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpendDonutChart(
    categorySplits: List<Pair<String, Double>>,
    totalAmount: Double,
    colors: List<Color>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SVG representation / Drawing custom circle segment arcs in high-fidelity
        Box(
            modifier = Modifier
                .size(140.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                categorySplits.forEachIndexed { idx, (_, amt) ->
                    val sweepAngle = ((amt / totalAmount) * 360f).toFloat()
                    val colorIndex = idx % colors.size
                    drawArc(
                        color = colors[colorIndex],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 30f, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(totalAmount),
                    fontSize = 16.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Legends list
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorySplits.take(4).forEachIndexed { idx, (catName, amt) ->
                val colorIndex = idx % colors.size
                val ratio = (amt / totalAmount) * 100

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(colors[colorIndex], CircleShape)
                        )
                        Text(
                            text = catName,
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "%.0f%%".format(ratio),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }
    }
}


// ---------------------- SHARED HELPER CHIPS & ROWS ----------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete '${tx.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${tx.id}")
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { /* Detail screen placeholder optionally */ },
                onLongClick = { showDialog = true }
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Circle Icon background based on dynamic theme color indicators matching mockups
            val (iconBg, iconTint) = if (tx.type == "Income") {
                GreenContainer.copy(alpha = 0.2f) to GreenSuccess
            } else {
                TealPrimary.copy(alpha = 0.05f) to TealPrimary
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (tx.category) {
                    "Food" -> Icons.Default.Restaurant
                    "Transport" -> Icons.Default.DirectionsCar
                    "Shopping" -> Icons.Default.ShoppingBag
                    "Salary" -> Icons.Default.Payments
                    "Rent" -> Icons.Default.Home
                    "Health" -> Icons.Default.FitnessCenter
                    "Fun" -> Icons.Default.Movie
                    else -> Icons.Default.MoreHoriz
                }
                Icon(
                    imageVector = icon,
                    contentDescription = tx.category,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tx.category,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("•", fontSize = 12.sp, color = BorderOutline)

                    // Relative label helper
                    Text(
                        text = formatRelativeTime(tx.timestamp),
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Amount Label on Right Pane
        val amountSign = if (tx.type == "Income") "+" else "-"
        val amountColor = if (tx.type == "Income") GreenSuccess else RedError

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "$amountSign${formatCurrency(tx.amount)}",
                color = amountColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BorderSoft.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, BorderOutline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted)
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------- GENERAL STRING FORMATTING HELPERS ----------------------

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

fun formatTransactionDate(timestamp: Long): String {
    val currentCal = Calendar.getInstance()
    val todayDate = currentCal.get(Calendar.DAY_OF_YEAR)
    val todayYear = currentCal.get(Calendar.YEAR)

    val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val txDate = txCal.get(Calendar.DAY_OF_YEAR)
    val txYear = txCal.get(Calendar.YEAR)

    return when {
        todayYear == txYear && todayDate == txDate -> "Today"
        todayYear == txYear && todayDate - txDate == 1 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val currentCal = Calendar.getInstance()
    val todayDate = currentCal.get(Calendar.DAY_OF_YEAR)
    val todayYear = currentCal.get(Calendar.YEAR)

    val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val txDate = txCal.get(Calendar.DAY_OF_YEAR)
    val txYear = txCal.get(Calendar.YEAR)

    return when {
        todayYear == txYear && todayDate == txDate -> "Today"
        todayYear == txYear && todayDate - txDate == 1 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
