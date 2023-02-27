package fr.geonature.occtax.features.record

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.taxon.data.ITaxonLocalDataSource
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.packageinfo.ISynchronizeObservationRecordRepository
import fr.geonature.occtax.api.IOcctaxAPIClient
import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IMediaRecordRemoteDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.data.MediaRecordLocalDataSourceImpl
import fr.geonature.occtax.features.record.data.MediaRecordRemoteDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordFileDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordLocalDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordRemoteDataSourceImpl
import fr.geonature.occtax.features.record.repository.IMediaRecordRepository
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.features.record.repository.MediaRecordRepositoryImpl
import fr.geonature.occtax.features.record.repository.ObservationRecordRepositoryImpl
import fr.geonature.occtax.features.record.repository.SynchronizeObservationRecordRepositoryImpl
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class ObservationRecordLocalDataSource

@Qualifier
annotation class ObservationRecordFileDataSource

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
    @ObservationRecordLocalDataSource
    fun provideObservationRecordLocalDataSource(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String
    ): IObservationRecordLocalDataSource {
        return ObservationRecordLocalDataSourceImpl(
            appContext,
            moduleName
        )
    }

    @Singleton
    @Provides
    @ObservationRecordFileDataSource
    fun provideObservationRecordFileDataSource(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String
    ): IObservationRecordLocalDataSource {
        return ObservationRecordFileDataSourceImpl(
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
    fun provideMediaRecordLocalDataSource(@ApplicationContext appContext: Context): IMediaRecordLocalDataSource {
        return MediaRecordLocalDataSourceImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideMediaRecordRemoteDataSource(
        geoNatureAPIClient: IGeoNatureAPIClient,
        nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    ): IMediaRecordRemoteDataSource {
        return MediaRecordRemoteDataSourceImpl(
            geoNatureAPIClient,
            nomenclatureLocalDataSource
        )
    }

    @Singleton
    @Provides
    fun provideObservationRecordRepository(
        @ObservationRecordLocalDataSource observationRecordLocalDataSource: IObservationRecordLocalDataSource,
        @ObservationRecordFileDataSource observationRecordFileDataSource: IObservationRecordLocalDataSource,
        taxonLocalDataSource: ITaxonLocalDataSource
    ): IObservationRecordRepository {
        return ObservationRecordRepositoryImpl(
            observationRecordLocalDataSource,
            observationRecordFileDataSource,
            taxonLocalDataSource
        )
    }

    @Singleton
    @Provides
    fun provideMediaRecordRepository(
        @ApplicationContext appContext: Context,
        authManager: IAuthManager,
        mediaRecordLocalDataSource: IMediaRecordLocalDataSource,
        mediaRecordRemoteDataSource: IMediaRecordRemoteDataSource
    ): IMediaRecordRepository {
        return MediaRecordRepositoryImpl(
            appContext,
            authManager,
            mediaRecordLocalDataSource,
            mediaRecordRemoteDataSource
        )
    }

    @Singleton
    @Provides
    fun provideSynchronizeObservationRecordRepository(
        @ApplicationContext appContext: Context,
        geoNatureAPIClient: IGeoNatureAPIClient,
        authManager: IAuthManager,
        appSettingsManager: IAppSettingsManager<AppSettings>,
        nomenclatureLocalDataSource: INomenclatureLocalDataSource,
        @ObservationRecordLocalDataSource observationRecordLocalDataSource: IObservationRecordLocalDataSource,
        observationRecordRemoteDataSource: IObservationRecordRemoteDataSource,
        mediaRecordLocalDataSource: IMediaRecordLocalDataSource
    ): ISynchronizeObservationRecordRepository {
        return SynchronizeObservationRecordRepositoryImpl(
            appContext,
            geoNatureAPIClient,
            authManager,
            appSettingsManager,
            nomenclatureLocalDataSource,
            observationRecordLocalDataSource,
            observationRecordRemoteDataSource,
            mediaRecordLocalDataSource
        )
    }
}