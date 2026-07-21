package com.sgbread.app.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProgressRepository(application)

    val state: StateFlow<ProgressState> = repository.progress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressState()
    )

    /** Records an activity as finished. Safe to call repeatedly; completion is idempotent. */
    fun completeActivity(activityId: String) {
        viewModelScope.launch { repository.markActivityComplete(activityId) }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.resetProgress() }
    }
}
