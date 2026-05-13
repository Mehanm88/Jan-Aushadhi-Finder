package com.example.janna.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.janna.data.ProfileEntity
import com.example.janna.data.ReminderDao
import com.example.janna.data.ReminderEntity
import com.example.janna.util.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _selectedProfileId = MutableStateFlow(1)
    val selectedProfileId: StateFlow<Int> = _selectedProfileId.asStateFlow()

    val profiles: StateFlow<List<ProfileEntity>> = reminderDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = _selectedProfileId
        .flatMapLatest { id -> reminderDao.getRemindersByProfile(id) }
        .onEach { list ->
            checkStockLevels(list)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun checkStockLevels(reminders: List<ReminderEntity>) {
        reminders.forEach { reminder ->
            if (reminder.isActive && reminder.currentStock <= 5) {
                alarmScheduler.showImmediateNotification(reminder, isStockAlert = true)
            }
        }
    }

    fun testNotification() {
        if (reminders.value.isNotEmpty()) {
            alarmScheduler.showImmediateNotification(reminders.value.first())
        } else {
            alarmScheduler.showImmediateNotification(
                ReminderEntity(medicineName = "Test Medicine", reminderTime = 0, isActive = true, profileId = 1)
            )
        }
    }

    fun selectProfile(id: Int) {
        _selectedProfileId.value = id
    }

    fun addReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val id = reminderDao.insertReminder(reminder)
            if (reminder.isActive) {
                alarmScheduler.schedule(reminder.copy(id = id.toInt()))
            }
        }
    }

    fun toggleReminder(reminder: ReminderEntity, isActive: Boolean) {
        viewModelScope.launch {
            reminderDao.setReminderActive(reminder.id, isActive)
            if (isActive) {
                alarmScheduler.schedule(reminder.copy(isActive = true))
            } else {
                alarmScheduler.cancel(reminder)
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
            alarmScheduler.cancel(reminder)
        }
    }

    fun addProfile(name: String, relation: String, emoji: String) {
        viewModelScope.launch {
            reminderDao.insertProfile(ProfileEntity(name = name, relation = relation, avatarEmoji = emoji))
        }
    }
}
