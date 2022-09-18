package fr.geonature.occtax.features.nomenclature

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.occtax.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.NomenclatureLocalDataSourceImpl
import fr.geonature.occtax.features.nomenclature.data.NomenclatureSettingsLocalDataSourceImpl
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.nomenclature.repository.NomenclatureRepositoryImpl
import javax.inject.Singleton

/**
 * Nomenclature module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object NomenclatureModule {

    @Singleton
    @Provides
    fun provideNomenclatureLocalDataSource(
        @GeoNatureModuleName moduleName: String,
        nomenclatureTypeDao: NomenclatureTypeDao,
        nomenclatureDao: NomenclatureDao
    ): INomenclatureLocalDataSource {
        return NomenclatureLocalDataSourceImpl(
            moduleName,
            nomenclatureTypeDao,
            nomenclatureDao
        )
    }

    @Singleton
    @Provides
    fun provideNomenclatureSettingsLocalDataSource(): INomenclatureSettingsLocalDataSource {
        return NomenclatureSettingsLocalDataSourceImpl()
    }

    @Singleton
    @Provides
    fun provideNomenclatureRepository(
        nomenclatureLocalDataSource: INomenclatureLocalDataSource,
        nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource
    ): INomenclatureRepository {
        return NomenclatureRepositoryImpl(
            nomenclatureLocalDataSource,
            nomenclatureSettingsLocalDataSource
        )
    }
}