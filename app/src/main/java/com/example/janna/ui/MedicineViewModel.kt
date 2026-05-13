package com.example.janna.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.janna.data.Medicine
import com.example.janna.data.MedicineRepository
import com.example.janna.util.PreferenceManager
import com.example.janna.util.SearchUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

data class SavingsDetails(
    val absoluteSavings: Double,
    val percentageSavings: Int,
    val isHighSavings: Boolean
)

@OptIn(FlowPreview::class)
class MedicineViewModel(
    private val repository: MedicineRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allMedicines = MutableStateFlow<List<Medicine>>(emptyList())
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val searchResults: StateFlow<List<Medicine>> = _searchQuery
        .debounce(300)
        .onEach { query -> if (query.isNotBlank()) preferenceManager.trackSearch(query) }
        .combine(_allMedicines) { query, medicines ->
            if (query.isBlank()) {
                medicines
            } else {
                medicines.filter { medicine ->
                    SearchUtils.isFuzzyMatch(query, medicine.brandName) ||
                    SearchUtils.isFuzzyMatch(query, medicine.genericName)
                }.sortedBy { medicine ->
                    val bDist = SearchUtils.levenshteinDistance(query, medicine.brandName)
                    val gDist = SearchUtils.levenshteinDistance(query, medicine.genericName)
                    minOf(bDist, gDist)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _allMedicines.value = repository.getAllMedicinesList()
                Timber.d("Loaded ${_allMedicines.value.size} medicines from database")
            } catch (e: Exception) {
                Timber.e(e, "Error loading medicines")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMedicine(query: String) {
        _searchQuery.value = query
    }

    fun getSavingsDetails(medicine: Medicine): SavingsDetails {
        val result = com.example.janna.util.SavingsCalculator.calculateSavings(
            medicine.brandPrice, 
            medicine.genericPrice
        )
        return SavingsDetails(
            absoluteSavings = result.absoluteSavings,
            percentageSavings = result.percentageSavings,
            isHighSavings = result.isHighSavings
        )
    }
}
