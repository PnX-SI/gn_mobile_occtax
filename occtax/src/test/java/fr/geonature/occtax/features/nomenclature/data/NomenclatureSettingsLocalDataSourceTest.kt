package fr.geonature.occtax.features.nomenclature.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.BaseEditableNomenclatureType
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
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "METH_OBS",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "ETA_BIO",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    true
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "METH_DETERMIN",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "DETERMINER",
                    BaseEditableNomenclatureType.ViewType.TEXT_SIMPLE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "STATUT_BIO",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "OCC_COMPORTEMENT",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "NATURALITE",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "PREUVE_EXIST",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                    false
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.INFORMATION,
                    "COMMENT",
                    BaseEditableNomenclatureType.ViewType.TEXT_MULTIPLE,
                    false
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(BaseEditableNomenclatureType.Type.INFORMATION)
        )

        assertEquals(
            listOf(
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "STADE_VIE",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "SEXE",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "OBJ_DENBR",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "TYP_DENBR",
                    BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "MIN",
                    BaseEditableNomenclatureType.ViewType.MIN_MAX
                ),
                BaseEditableNomenclatureType.from(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    "MAX",
                    BaseEditableNomenclatureType.ViewType.MIN_MAX
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(BaseEditableNomenclatureType.Type.COUNTING)
        )
    }

    @Test
    fun `should get nomenclature type settings by nomenclature main type according to given settings`() =
        runTest {
            assertEquals(
                listOf(
                    BaseEditableNomenclatureType.from(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "METH_OBS",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        true
                    ),
                    BaseEditableNomenclatureType.from(
                        BaseEditableNomenclatureType.Type.INFORMATION,
                        "ETA_BIO",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE,
                        false
                    ),
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    BaseEditableNomenclatureType.Type.INFORMATION,
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
                    ),
                )
            )

            assertEquals(
                listOf(
                    BaseEditableNomenclatureType.from(
                        BaseEditableNomenclatureType.Type.COUNTING,
                        "STADE_VIE",
                        BaseEditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    ),
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    BaseEditableNomenclatureType.Type.COUNTING,
                    PropertySettings(
                        key = "STADE_VIE",
                        visible = true,
                        default = true
                    ),
                    PropertySettings(
                        key = "NO_SUCH_SETTINGS",
                        visible = true,
                        default = true
                    ),
                )
            )
        }
}