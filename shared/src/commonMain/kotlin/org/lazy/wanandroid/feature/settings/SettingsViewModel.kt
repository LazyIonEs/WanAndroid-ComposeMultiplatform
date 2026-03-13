package org.lazy.wanandroid.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lazy.wanandroid.core.data.model.DarkThemeConfig
import org.lazy.wanandroid.core.data.repository.PreferencesRepository

class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {

    val darkThemeConfig = repository.darkThemeConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DarkThemeConfig.FOLLOW_SYSTEM
        )

    fun saveAppThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            repository.saveThemeConfig(darkThemeConfig)
        }
    }
}