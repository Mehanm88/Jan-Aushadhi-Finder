package com.example.janna.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.janna.data.MedicineRepository
import com.example.janna.util.PreferenceManager

class ViewModelFactory(
    private val repository: MedicineRepository,
    private val preferenceManager: PreferenceManager? = null,
    private val reminderDao: com.example.janna.data.ReminderDao? = null,
    private val alarmScheduler: com.example.janna.util.AlarmScheduler? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MedicineViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MedicineViewModel(repository, preferenceManager!!) as T
            }
            modelClass.isAssignableFrom(SavingsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SavingsViewModel() as T
            }
            modelClass.isAssignableFrom(ReminderViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ReminderViewModel(reminderDao!!, alarmScheduler!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
