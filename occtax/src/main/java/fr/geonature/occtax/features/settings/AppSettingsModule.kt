package fr.geonature.occtax.features.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.datasync.settings.AppSettingsFilename
import fr.geonature.occtax.features.settings.data.AppSettingsFileLocalDataSourceImpl
import fr.geonature.occtax.features.settings.data.IAppSettingsLocalDataSource
import fr.geonature.occtax.features.settings.domain.AppSettings
import fr.geonature.occtax.features.settings.repository.AppSettingsRepositoryImpl
import fr.geonature.occtax.features.settings.repository.IAppSettingsRepository
import javax.inject.Singleton

/**
 * [AppSettings] module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object AppSettingsModule {

    @Singleton
    @Provides
    fun provideAppSettingsDataSource(
        @ApplicationContext appContext: Context,
        @AppSettingsFilename appSettingsFilename: String
    ): IAppSettingsLocalDataSource {
        return AppSettingsFileLocalDataSourceImpl(
            appContext,
            appSettingsFilename
        )
    }

    @Singleton
    @Provides
    fun provideAppSettingsRepository(
        appSettingsLocalDataSource: IAppSettingsLocalDataSource
    ): IAppSettingsRepository {
        return AppSettingsRepositoryImpl(appSettingsLocalDataSource)
    }
}