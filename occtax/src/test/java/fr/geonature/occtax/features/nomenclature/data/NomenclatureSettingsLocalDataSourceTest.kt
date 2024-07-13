package fr.geonature.occtax.features.nomenclature.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableField
import fr.geonature.occtax.features.record.domain.AllMediaRecord
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.settings.domain.PropertySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class NomenclatureSettingsLocalDataSourceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource

    @Before
    fun setUp() {
        nomenclatureSettingsLocalDataSource = NomenclatureSettingsLocalDataSourceImpl()
    }

    @Test
    fun `should get default nomenclature type settings by nomenclature main type`() = runTest {
        assertEquals(
            listOf(
                EditableField(
                    type = EditableField.Type.DEFAULT,
                    code = "TYP_GRP",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "TYP_GRP"
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.DEFAULT)
        )

        assertEquals(
            listOf(
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_OBS",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_OBS"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "ETA_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "ETA_BIO"
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "METH_DETERMIN",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "METH_DETERMIN",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "determiner",
                    viewType = EditableField.ViewType.TEXT_SIMPLE,
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "STATUT_BIO",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STATUT_BIO",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "OCC_COMPORTEMENT",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "OCC_COMPORTEMENT",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "NATURALITE",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "NATURALITE",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "PREUVE_EXIST",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "PREUVE_EXIST",
                    default = false
                ),
                EditableField(
                    type = EditableField.Type.INFORMATION,
                    code = "comment",
                    viewType = EditableField.ViewType.TEXT_MULTIPLE,
                    default = false
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.INFORMATION)
        )

        assertEquals(
            listOf(
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = "STADE_VIE",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "STADE_VIE"
                ),
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = "SEXE",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "SEXE"
                ),
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = "OBJ_DENBR",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "OBJ_DENBR"
                ),
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = "TYP_DENBR",
                    viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                    nomenclatureType = "TYP_DENBR"
                ),
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = CountingRecord.MIN_KEY,
                    viewType = EditableField.ViewType.MIN_MAX
                ).apply {
                    value = PropertyValue.Number(
                        code,
                        1
                    )
                },
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = CountingRecord.MAX_KEY,
                    viewType = EditableField.ViewType.MIN_MAX
                ).apply {
                    value = PropertyValue.Number(
                        code,
                        1
                    )
                },
                EditableField(
                    type = EditableField.Type.COUNTING,
                    code = AllMediaRecord.MEDIAS_KEY,
                    viewType = EditableField.ViewType.MEDIA
                ).apply {
                    value = PropertyValue.Media(code)
                }
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.COUNTING)
        )
    }

    @Test
    fun `should get the default nomenclature type settings if nomenclature default main type is requested`() =
        runTest {
            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.DEFAULT,
                        code = "TYP_GRP",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "TYP_GRP"
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableField.Type.DEFAULT)
            )

            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.DEFAULT,
                        code = "TYP_GRP",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "TYP_GRP"
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableField.Type.DEFAULT,
                    PropertySettings(
                        key = "TYP_GRP",
                        visible = false,
                        default = false
                    )
                )
            )
        }

    @Test
    fun `should get nomenclature type settings by nomenclature main type according to given settings`() =
        runTest {
            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "METH_OBS",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "METH_OBS"
                    ),
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "ETA_BIO",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "ETA_BIO",
                        visible = true,
                        default = false
                    ),
                    EditableField(
                        type = EditableField.Type.INFORMATION,
                        code = "OCC_COMPORTEMENT",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "OCC_COMPORTEMENT",
                        visible = false
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableField.Type.INFORMATION,
                    PropertySettings(
                        key = "METH_OBS",
                        visible = true,
                        default = true
                    ),
                    PropertySettings(
                        key = "ETA_BIO",
                        visible = true,
                        default = false
                    ),
                    PropertySettings(
                        key = "OCC_COMPORTEMENT",
                        visible = false,
                        default = true
                    )
                )
            )

            assertEquals(
                listOf(
                    EditableField(
                        type = EditableField.Type.COUNTING,
                        code = "STADE_VIE",
                        viewType = EditableField.ViewType.NOMENCLATURE_TYPE,
                        nomenclatureType = "STADE_VIE",
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableField.Type.COUNTING,
                    PropertySettings(
                        key = "STADE_VIE",
                        visible = true,
                        default = true
                    ),
                    PropertySettings(
                        key = "NO_SUCH_SETTINGS",
                        visible = true,
                        default = true
                    )
                )
            )
        }
}