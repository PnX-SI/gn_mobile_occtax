package fr.geonature.occtax.features.record

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.features.taxon.data.ITaxonLocalDataSource
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.packageinfo.ISynchronizeObservationRecordRepository
import fr.geonature.occtax.api.IOcctaxAPIClient
import fr.geonature.occtax.features.record.data.IObservationRecordDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.data.ObservationRecordDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordRemoteDataSourceImpl
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.features.record.repository.ObservationRecordRepositoryImpl
import fr.geonature.occtax.features.record.repository.SynchronizeObservationRecordRepositoryImpl
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Singleton

/**
 * Observation record module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object ObservationRecordModule {

    @Singleton
    @Provides
    fun provideObservationRecordLocalDataSource(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String
    ): IObservationRecordDataSource {
        return ObservationRecordDataSourceImpl(
            appContext,
            moduleName
        )
    }

    @Singleton
    @Provides
    fun provideObservationRecordRemoteDataSource(occtaxAPIClient: IOcctaxAPIClient): IObservationRecordRemoteDataSource {
        return ObservationRecordRemoteDataSourceImpl(occtaxAPIClient)
    }

    @Singleton
    @Provides
    fun provideObservationRecordRepository(
        observationRecordDataSource: IObservationRecordDataSource,
        taxonLocalDataSource: ITaxonLocalDataSource
    ): IObservationRecordRepository {
        return ObservationRecordRepositoryImpl(
            observationRecordDataSource,
            taxonLocalDataSource
        )
    }

    @Singleton
    @Provides
    fun provideSynchronizeObservationRecordRepository(
        authManager: IAuthManager,
        appSettingsManager: IAppSettingsManager<AppSettings>,
        observationRecordDataSource: IObservationRecordDataSource,
        observationRecordRemoteDataSource: IObservationRecordRemoteDataSource
    ): ISynchronizeObservationRecordRepository {
        return SynchronizeObservationRecordRepositoryImpl(
            authManager,
            appSettingsManager,
            observationRecordDataSource,
            observationRecordRemoteDataSource
        )
    }
}