package fr.geonature.occtax.features.nomenclature

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.features.nomenclature.data.AdditionalFieldLocalDataSourceImpl
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.data.NomenclatureLocalDataSourceImpl
import fr.geonature.occtax.features.nomenclature.data.INomenclatureSettingsLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.IPropertyValueLocalDataSource
import fr.geonature.occtax.features.nomenclature.data.InMemoryPropertyValueLocalDataSourceImpl
import fr.geonature.occtax.features.nomenclature.data.NomenclatureSettingsLocalDataSourceImpl
import fr.geonature.occtax.features.nomenclature.repository.AdditionalFieldRepositoryImpl
import fr.geonature.occtax.features.nomenclature.repository.DefaultPropertyValueRepositoryImpl
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.IDefaultPropertyValueRepository
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
    fun providePropertyValueLocalDataSource(): IPropertyValueLocalDataSource {
        return InMemoryPropertyValueLocalDataSourceImpl()
    }

    @Singleton
    @Provides
    fun provideAdditionalFieldDataSource(database: LocalDatabase): IAdditionalFieldLocalDataSource {
        return AdditionalFieldLocalDataSourceImpl(database)
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

    @Singleton
    @Provides
    fun provideDefaultPropertyValueRepository(
        propertyValueLocalDataSource: IPropertyValueLocalDataSource,
        nomenclatureLocalDataSource: INomenclatureLocalDataSource,
    ): IDefaultPropertyValueRepository {
        return DefaultPropertyValueRepositoryImpl(
            propertyValueLocalDataSource,
            nomenclatureLocalDataSource
        )
    }

    @Singleton
    @Provides
    fun provideAdditionalFieldRepository(
        additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource
    ): IAdditionalFieldRepository {
        return AdditionalFieldRepositoryImpl(additionalFieldLocalDataSource)
    }
}