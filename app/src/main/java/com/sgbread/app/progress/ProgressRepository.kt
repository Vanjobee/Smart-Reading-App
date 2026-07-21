package com.sgbread.app.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sgbread.app.data.Modules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sgb_read_progress")

private val COMPLETED_ACTIVITIES_KEY = stringSetPreferencesKey("completed_activities")

data class ProgressState(
    val completedActivityIds: Set<String> = emptySet()
) {
    val totalActivities: Int get() = Modules.totalActivityCount()
    val fraction: Float get() = if (totalActivities == 0) 0f else completedActivityIds.size / totalActivities.toFloat()

    fun isModuleComplete(moduleId: String): Boolean {
        val module = Modules.all.firstOrNull { it.id == moduleId } ?: return false
        return module.activities.all { it.id in completedActivityIds }
    }

    fun moduleFraction(moduleId: String): Float {
        val module = Modules.all.firstOrNull { it.id == moduleId } ?: return 0f
        if (module.activities.isEmpty()) return 0f
        val done = module.activities.count { it.id in completedActivityIds }
        return done / module.activities.size.toFloat()
    }
}

/** Persists which activities a child has completed so the farm keeps growing between sessions. */
class ProgressRepository(private val context: Context) {

    val progress: Flow<ProgressState> = context.dataStore.data.map { prefs ->
        ProgressState(prefs[COMPLETED_ACTIVITIES_KEY] ?: emptySet())
    }

    suspend fun markActivityComplete(activityId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_ACTIVITIES_KEY] ?: emptySet()
            prefs[COMPLETED_ACTIVITIES_KEY] = current + activityId
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { prefs -> prefs[COMPLETED_ACTIVITIES_KEY] = emptySet() }
    }
}
