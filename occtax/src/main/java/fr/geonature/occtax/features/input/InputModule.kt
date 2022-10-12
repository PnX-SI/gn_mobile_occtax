package fr.geonature.occtax.features.input

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.features.input.data.IInputLocalDataSource
import fr.geonature.commons.features.input.data.InputLocalDataSourceImpl
import fr.geonature.commons.features.input.repository.IInputRepository
import fr.geonature.commons.features.input.repository.InputRepositoryImpl
import fr.geonature.commons.features.input.usecase.DeleteInputUseCase
import fr.geonature.commons.features.input.usecase.ExportInputUseCase
import fr.geonature.commons.features.input.usecase.ReadInputsUseCase
import fr.geonature.commons.features.input.usecase.SaveInputUseCase
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.features.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.features.input.io.OnInputJsonWriterListenerImpl
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Singleton

/**
 * Input module.
 *
 * @author S. Grimault
 */
@InstallIn(SingletonComponent::class)
@Module
object InputModule {

    @Singleton
    @Provides
    fun provideInputLocalDataSource(@ApplicationContext appContext: Context): IInputLocalDataSource<Input, AppSettings> {
        return InputLocalDataSourceImpl(
            appContext,
            OnInputJsonReaderListenerImpl(),
            OnInputJsonWriterListenerImpl()
        )
    }

    @Singleton
    @Provides
    fun provideInputRepository(inputLocalDataSource: IInputLocalDataSource<Input, AppSettings>): IInputRepository<Input, AppSettings> {
        return InputRepositoryImpl(inputLocalDataSource)
    }

    @Singleton
    @Provides
    fun provideDeleteInputUseCase(inputRepository: IInputRepository<Input, AppSettings>): DeleteInputUseCase<Input, AppSettings> {
        return DeleteInputUseCase(inputRepository)
    }

    @Singleton
    @Provides
    fun provideExportInputUseCase(inputRepository: IInputRepository<Input, AppSettings>): ExportInputUseCase<Input, AppSettings> {
        return ExportInputUseCase(inputRepository)
    }

    @Singleton
    @Provides
    fun provideReadInputsUseCase(inputRepository: IInputRepository<Input, AppSettings>): ReadInputsUseCase<Input, AppSettings> {
        return ReadInputsUseCase(inputRepository)
    }

    @Singleton
    @Provides
    fun provideSaveInputUseCase(inputRepository: IInputRepository<Input, AppSettings>): SaveInputUseCase<Input, AppSettings> {
        return SaveInputUseCase(inputRepository)
    }
}