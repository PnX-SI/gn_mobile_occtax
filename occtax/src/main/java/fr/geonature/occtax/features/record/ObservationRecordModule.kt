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
import fr.geonature.commons.util.getFile
import fr.geonature.commons.util.getPrimaryExternalStorage
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.occtax.api.IOcctaxAPIClient
import fr.geonature.occtax.features.record.data.IMediaRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordLocalDataSource
import fr.geonature.occtax.features.record.data.IObservationRecordRemoteDataSource
import fr.geonature.occtax.features.record.data.MediaRecordLocalDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordLocalDataSourceImpl
import fr.geonature.occtax.features.record.data.ObservationRecordRemoteDataSourceImpl
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.repository.IMediaRecordRepository
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.features.record.repository.ISynchronizeObservationRecordRepository
import fr.geonature.occtax.features.record.repository.MediaRecordRepositoryImpl
import fr.geonature.occtax.features.record.repository.ObservationRecordRepositoryImpl
import fr.geonature.occtax.features.record.repository.SynchronizeObservationRecordRepositoryImpl
import fr.geonature.occtax.features.settings.repository.IAppSettingsRepository
import org.tinylog.Logger
import java.io.File
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * The base folder for all [ObservationRecord]s.
 */
@MustBeDocumented
@Qualifier
annotation class ObservationRecordsRootFolder

/**
 * Observation record module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object ObservationRecordModule {

    /**
     * Provides the base folder for all [ObservationRecord]s.
     */
    @Singleton
    @Provides
    @ObservationRecordsRootFolder
    fun provideObservationRecordsRootFolder(@ApplicationContext appContext: Context): File {
        return appContext.getPrimaryExternalStorage()
            .getFile("inputs")
            .also {
                it.mkdirs()
                Logger.info { "inputs root path: '${it.absolutePath}'" }
            }
    }

    @Singleton
    @Provides
    fun provideObservationRecordLocalDataSource(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String,
        clock: Clock,
        @ObservationRecordsRootFolder observationRecordsRootFolder: File
    ): IObservationRecordLocalDataSource {
        return ObservationRecordLocalDataSourceImpl(
            appContext,
            moduleName,
            clock,
            observationRecordsRootFolder
        )
    }

    @Singleton
    @Provides
    fun provideObservationRecordRemoteDataSource(occtaxAPIClient: IOcctaxAPIClient): IObservationRecordRemoteDataSource {
        return ObservationRecordRemoteDataSourceImpl(occtaxAPIClient)
    }

    @Singleton
    @Provides
    fun provideMediaRecordLocalDataSource(@ObservationRecordsRootFolder observationRecordsRootFolder: File): IMediaRecordLocalDataSource {
        return MediaRecordLocalDataSourceImpl(observationRecordsRootFolder)
    }

    @Singleton
    @Provides
    fun provideObservationRecordRepository(
        observationRecordLocalDataSource: IObservationRecordLocalDataSource,
        taxonLocalDataSource: ITaxonLocalDataSource
    ): IObservationRecordRepository {
        return ObservationRecordRepositoryImpl(
            observationRecordLocalDataSource,
            taxonLocalDataSource
        )
    }

    @Singleton
    @Provides
    fun provideMediaRecordRepository(mediaRecordLocalDataSource: IMediaRecordLocalDataSource): IMediaRecordRepository {
        return MediaRecordRepositoryImpl(mediaRecordLocalDataSource)
    }

    @Singleton
    @Provides
    fun provideSynchronizeObservationRecordRepository(
        @ApplicationContext appContext: Context,
        geoNatureAPIClient: IGeoNatureAPIClient,
        authManager: IAuthManager,
        appSettingsRepository: IAppSettingsRepository,
        nomenclatureLocalDataSource: INomenclatureLocalDataSource,
        observationRecordLocalDataSource: IObservationRecordLocalDataSource,
        observationRecordRemoteDataSource: IObservationRecordRemoteDataSource,
        mediaRecordLocalDataSource: IMediaRecordLocalDataSource
    ): ISynchronizeObservationRecordRepository {
        return SynchronizeObservationRecordRepositoryImpl(
            appContext,
            geoNatureAPIClient,
            authManager,
            appSettingsRepository,
            nomenclatureLocalDataSource,
            observationRecordLocalDataSource,
            observationRecordRemoteDataSource,
            mediaRecordLocalDataSource
        )
    }
}