package fr.geonature.occtax.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.datasync.settings.AppSettingsFilename
import javax.inject.Singleton

/**
 * Data synchronization settings module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSyncSettingsModule {

    @Singleton
    @Provides
    @AppSettingsFilename
    fun provideAppSettingsFilename(@GeoNatureModuleName moduleName: String): String {
        return "settings_$moduleName.json"
    }
}