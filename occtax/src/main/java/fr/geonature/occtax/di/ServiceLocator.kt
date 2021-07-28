package fr.geonature.occtax.di

import android.app.Application
import fr.geonature.commons.input.IInputManager
import fr.geonature.commons.input.InputManagerImpl
import fr.geonature.occtax.input.Input
import fr.geonature.occtax.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.input.io.OnInputJsonWriterListenerImpl

/**
 * Service Locator
 *
 * @author S. Grimault
 */
class ServiceLocator(private val application: Application) {

    val inputManager: IInputManager<Input> by lazy {
        InputManagerImpl(
            application,
            OnInputJsonReaderListenerImpl(),
            OnInputJsonWriterListenerImpl()
        )
    }
}