package fr.geonature.occtax.input

import android.app.Application
import fr.geonature.occtax.input.io.OnInputJsonReaderListenerImpl
import fr.geonature.occtax.input.io.OnInputJsonWriterListenerImpl
import fr.geonature.commons.input.InputViewModel as BaseInputModel

/**
 * [Input] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputViewModel(application: Application) : BaseInputModel<Input>(application,
                                                                       OnInputJsonReaderListenerImpl(),
                                                                       OnInputJsonWriterListenerImpl())
