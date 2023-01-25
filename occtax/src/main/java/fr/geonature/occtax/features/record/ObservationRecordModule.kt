package fr.geonature.occtax.features.record

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.occtax.features.record.data.IObservationRecordDataSource
import fr.geonature.occtax.features.record.data.ObservationRecordDataSourceImpl
import fr.geonature.occtax.features.record.repository.IObservationRecordRepository
import fr.geonature.occtax.features.record.repository.ObservationRecordRepositoryImpl
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
    fun provideObservationRecordRepository(observationRecordDataSource: IObservationRecordDataSource): IObservationRecordRepository {
        return ObservationRecordRepositoryImpl(observationRecordDataSource)
    }
}