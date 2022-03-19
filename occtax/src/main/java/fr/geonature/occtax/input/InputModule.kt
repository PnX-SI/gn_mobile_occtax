package fr.geonature.occtax.input

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.input.IInputManager
import fr.geonature.commons.input.InputManagerImpl
import fr.geonature.occtax.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.input.io.OnInputJsonWriterListenerImpl
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
    fun provideInputManager(
        @ApplicationContext appContext: Context,
        @ContentProviderAuthority authority: String
    ): IInputManager<Input> {
        return InputManagerImpl(
            appContext,
            authority,
            OnInputJsonReaderListenerImpl(),
            OnInputJsonWriterListenerImpl()
        )
    }
}