package fr.geonature.occtax.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.settings.AppSettingsManagerImpl
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.datasync.settings.AppSettingsFilename
import fr.geonature.occtax.settings.io.OnAppSettingsJsonReaderListenerImpl
import javax.inject.Singleton

/**
 * Application settings module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object AppSettingsModule {

    @Singleton
    @Provides
    fun provideAppSettingsManager(
        @ApplicationContext appContext: Context,
        @ContentProviderAuthority authority: String,
        @AppSettingsFilename appSettingsFilename: String
    ): IAppSettingsManager<AppSettings> {
        return AppSettingsManagerImpl(
            appContext,
            authority,
            appSettingsFilename,
            OnAppSettingsJsonReaderListenerImpl()
        )
    }
}