package fr.geonature.occtax.features.nomenclature.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
import fr.geonature.occtax.features.record.domain.AllMediaRecord
import fr.geonature.occtax.features.record.domain.CountingRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.settings.PropertySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.DEFAULT,
                    "TYP_GRP",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.DEFAULT)
        )

        assertEquals(
            listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "METH_DETERMIN",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "determiner",
                    EditableNomenclatureType.ViewType.TEXT_SIMPLE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "OCC_COMPORTEMENT",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "NATURALITE",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "PREUVE_EXIST",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    default = false
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.INFORMATION,
                    "comment",
                    EditableNomenclatureType.ViewType.TEXT_MULTIPLE,
                    default = false
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.INFORMATION)
        )

        assertEquals(
            listOf(
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    "STADE_VIE",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    "SEXE",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    "OBJ_DENBR",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    "TYP_DENBR",
                    EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    CountingRecord.MIN_KEY,
                    EditableNomenclatureType.ViewType.MIN_MAX
                ).apply {
                    value = PropertyValue.Number(
                        code,
                        1
                    )
                },
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    CountingRecord.MAX_KEY,
                    EditableNomenclatureType.ViewType.MIN_MAX
                ).apply {
                    value = PropertyValue.Number(
                        code,
                        1
                    )
                },
                EditableNomenclatureType(
                    EditableNomenclatureType.Type.COUNTING,
                    AllMediaRecord.MEDIAS_KEY,
                    EditableNomenclatureType.ViewType.MEDIA
                ).apply {
                    value = PropertyValue.Media(code)
                }
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.COUNTING)
        )
    }

    @Test
    fun `should get the default nomenclature type settings if nomenclature default main type is requested`() =
        runTest {
            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.DEFAULT,
                        "TYP_GRP",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(EditableNomenclatureType.Type.DEFAULT)
            )

            assertEquals(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.DEFAULT,
                        "TYP_GRP",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableNomenclatureType.Type.DEFAULT,
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
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        visible = true,
                        default = false
                    ),
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.INFORMATION,
                        "OCC_COMPORTEMENT",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        visible = false
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableNomenclatureType.Type.INFORMATION,
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
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.COUNTING,
                        "STADE_VIE",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    EditableNomenclatureType.Type.COUNTING,
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