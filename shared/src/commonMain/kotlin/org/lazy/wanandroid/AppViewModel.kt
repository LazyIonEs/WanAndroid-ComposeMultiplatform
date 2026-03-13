package org.lazy.wanandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lazy.wanandroid.core.data.model.DarkThemeConfig
import org.lazy.wanandroid.core.data.repository.PreferencesRepository

class AppViewModel(private val repository: PreferencesRepository) : ViewModel() {

    val darkThemeConfig: StateFlow<DarkThemeConfig> =
        repository.darkThemeConfig.stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = DarkThemeConfig.FOLLOW_SYSTEM
        )

    fun saveAppThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            repository.saveThemeConfig(darkThemeConfig)
        }
    }
}