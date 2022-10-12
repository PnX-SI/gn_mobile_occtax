package fr.geonature.occtax.features.input.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.features.input.usecase.DeleteInputUseCase
import fr.geonature.commons.features.input.usecase.ExportInputUseCase
import fr.geonature.commons.features.input.usecase.ReadInputsUseCase
import fr.geonature.commons.features.input.usecase.SaveInputUseCase
import fr.geonature.occtax.features.input.domain.Input
import fr.geonature.occtax.settings.AppSettings
import javax.inject.Inject
import fr.geonature.commons.features.input.presentation.InputViewModel as BaseInputModel

/**
 * [Input] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class InputViewModel @Inject constructor(
    readInputsUseCase: ReadInputsUseCase<Input, AppSettings>,
    saveInputUseCase: SaveInputUseCase<Input, AppSettings>,
    deleteInputUseCase: DeleteInputUseCase<Input, AppSettings>,
    exportInputUseCase: ExportInputUseCase<Input, AppSettings>
) :
    BaseInputModel<Input, AppSettings>(
        readInputsUseCase,
        saveInputUseCase,
        deleteInputUseCase,
        exportInputUseCase
    )
