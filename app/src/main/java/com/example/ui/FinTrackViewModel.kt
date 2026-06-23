package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FinTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "fintrack_database"
    ).build()

    private val repository = TransactionRepository(db.transactionDao())

    // UI state for navigation
    private val _currentTab = MutableStateFlow(0) // 0: Dashboard, 1: History, 2: Add, 3: Reports
    val currentTab = _currentTab.asStateFlow()

    // Real-time transactions list from Room
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // History screen filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All") // "All", "Income", "Expense", "Savings"
    val categoryFilter = _categoryFilter.asStateFlow()

    // Reports screen filter
    private val _selectedMonthOffset = MutableStateFlow(0) // 0: current month, -1: last month, etc.
    val selectedMonthOffset = _selectedMonthOffset.asStateFlow()

    // Add transaction screen form state
    private val _amountInput = MutableStateFlow("")
    val amountInput = _amountInput.asStateFlow()

    private val _titleInput = MutableStateFlow("")
    val titleInput = _titleInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Food")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow("Expense") // "Expense" or "Income"
    val selectedType = _selectedType.asStateFlow()

    private val _dateInput = MutableStateFlow(System.currentTimeMillis())
    val dateInput = _dateInput.asStateFlow()

    private val _notesInput = MutableStateFlow("")
    val notesInput = _notesInput.asStateFlow()

    init {
        // Pre-populate with beautiful sample items if DB is empty
        viewModelScope.launch {
            repository.allTransactions.first().let { list ->
                if (list.isEmpty()) {
                    populateSampleData()
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(filter: String) {
        _categoryFilter.value = filter
    }

    fun changeMonthOffset(offset: Int) {
        _selectedMonthOffset.value = _selectedMonthOffset.value + offset
    }

    // Form modifications
    fun updateAmountInput(amount: String) {
        _amountInput.value = amount
    }

    fun updateTitleInput(title: String) {
        _titleInput.value = title
    }

    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }

    fun updateDateInput(timestamp: Long) {
        _dateInput.value = timestamp
    }

    fun updateNotesInput(notes: String) {
        _notesInput.value = notes
    }

    fun addTransaction(): Boolean {
        val title = _titleInput.value.trim()
        val amountStr = _amountInput.value.trim()
        val category = _selectedCategory.value
        val type = _selectedType.value
        val date = _dateInput.value
        val notes = _notesInput.value.trim()

        if (title.isEmpty()) return false
        val amount = amountStr.toDoubleOrNull() ?: return false
        if (amount <= 0) return false

        viewModelScope.launch {
            val transaction = TransactionEntity(
                title = title,
                category = category,
                amount = amount,
                type = type,
                timestamp = date,
                notes = notes
            )
            repository.insert(transaction)
            
            // Success: clear fields and redirect to Dashboard
            clearForm()
            selectTab(0)
        }
        return true
    }

    private fun clearForm() {
        _titleInput.value = ""
        _amountInput.value = ""
        _selectedCategory.value = "Food"
        _selectedType.value = "Expense"
        _dateInput.value = System.currentTimeMillis()
        _notesInput.value = ""
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private suspend fun populateSampleData() {
        val cal = Calendar.getInstance()
        val today = cal.timeInMillis

        // Yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis

        // 2 days ago
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgo = cal.timeInMillis

        // 5 days ago
        cal.add(Calendar.DAY_OF_YEAR, -3)
        val fiveDaysAgo = cal.timeInMillis

        // 20 days ago (for monthly trends comparison)
        cal.add(Calendar.DAY_OF_YEAR, -15)
        val twentyDaysAgo = cal.timeInMillis

        val sampleTransactions = listOf(
            TransactionEntity(
                title = "Salary Deposit",
                category = "Salary",
                amount = 4500.0,
                type = "Income",
                timestamp = today,
                notes = "Monthly payroll payout"
            ),
            TransactionEntity(
                title = "Whole Foods Market",
                category = "Food",
                amount = 84.20,
                type = "Expense",
                timestamp = today,
                notes = "Weekly grocery stock up"
            ),
            TransactionEntity(
                title = "Apple Store",
                category = "Shopping",
                amount = 129.0,
                type = "Expense",
                timestamp = yesterday,
                notes = "MagSafe charger and accessories"
            ),
            TransactionEntity(
                title = "Blue Ginger Sushi",
                category = "Food",
                amount = 84.50,
                type = "Expense",
                timestamp = yesterday,
                notes = "Dinner with partners"
            ),
            TransactionEntity(
                title = "The Coffee Bean",
                category = "Food",
                amount = 6.75,
                type = "Expense",
                timestamp = yesterday,
                notes = "Morning flat white"
            ),
            TransactionEntity(
                title = "Uber Trip",
                category = "Transport",
                amount = 24.50,
                type = "Expense",
                timestamp = twoDaysAgo,
                notes = "Ride to conference"
            ),
            TransactionEntity(
                title = "Cash Deposit",
                category = "Salary",
                amount = 200.0,
                type = "Income",
                timestamp = twoDaysAgo,
                notes = "Petty cash savings cash back"
            ),
            TransactionEntity(
                title = "Shell Station",
                category = "Transport",
                amount = 65.0,
                type = "Expense",
                timestamp = fiveDaysAgo,
                notes = "Fuel full tank"
            ),
            TransactionEntity(
                title = "Monthly Rent",
                category = "Rent",
                amount = 1250.0,
                type = "Expense",
                timestamp = twentyDaysAgo,
                notes = "Apartment rent"
            ),
            TransactionEntity(
                title = "Delta Airlines",
                category = "Other",
                amount = 189.0,
                type = "Expense",
                timestamp = twentyDaysAgo,
                notes = "Flight insurance and ticket checkin"
            )
        )

        for (tx in sampleTransactions) {
            repository.insert(tx)
        }
    }
}
