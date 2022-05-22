package fr.geonature.occtax.input

import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.input.IInputManager
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Inject
import fr.geonature.commons.input.InputViewModel as BaseInputModel

/**
 * [Input] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class InputViewModel @Inject constructor(inputManager: IInputManager<Input, AppSettings>) :
    BaseInputModel<Input, AppSettings>(inputManager)
