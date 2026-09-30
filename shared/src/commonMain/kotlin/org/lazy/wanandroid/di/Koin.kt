package org.lazy.wanandroid.di

import com.russhwolf.settings.ExperimentalSettingsApi
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.lazy.wanandroid.AppViewModel
import org.lazy.wanandroid.core.data.repository.PreferencesRepository
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.data.source.PreferencesDataSource
import org.lazy.wanandroid.createArticleService
import org.lazy.wanandroid.feature.feed.FeedViewModel
import org.lazy.wanandroid.feature.navigation.entryModule
import org.lazy.wanandroid.feature.settings.SettingsViewModel
import org.lazy.wanandroid.platformModule
import org.koin.core.module.dsl.viewModel as parameterizedViewModel

val viewModelModule = module {
    viewModel<AppViewModel>()
    viewModel<SettingsViewModel>()
    parameterizedViewModel { parameters -> FeedViewModel(parameters.get(), get()) }
}

@OptIn(ExperimentalSettingsApi::class)
val dataModule = module {
    single<ArticleService> { createArticleService() } onClose { it?.close() }

    single<PreferencesDataSource>()

    single<PreferencesRepository>()
}

val navigationModule = module {
    includes(entryModule)
}

val appModule = module {
    includes(platformModule, dataModule, navigationModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        includes(configuration)
        modules(appModule)
        printLogger(Level.DEBUG)
    }
}
