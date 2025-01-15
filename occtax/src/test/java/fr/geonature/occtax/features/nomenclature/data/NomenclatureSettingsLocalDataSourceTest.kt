package fr.geonature.occtax.features.nomenclature.data

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [INomenclatureSettingsLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NomenclatureSettingsLocalDataSourceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var nomenclatureSettingsLocalDataSource: INomenclatureSettingsLocalDataSource

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        nomenclatureSettingsLocalDataSource = NomenclatureSettingsLocalDataSourceImpl(application)
    }

    @Test
    fun `should get default nomenclature type settings by nomenclature main type`() = runTest {
        assertEquals(
            listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.DEFAULT,
                    label = application.getString(R.string.nomenclature_typ_grp),
                    nomenclatureType = "TYP_GRP",
                    value = PropertyValue.Nomenclature(code = "TYP_GRP")
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.DEFAULT)
        )

        assertEquals(
            listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_obs),
                    nomenclatureType = "METH_OBS",
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_eta_bio),
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_determin),
                    default = false,
                    nomenclatureType = "METH_DETERMIN",
                    value = PropertyValue.Nomenclature(code = "METH_DETERMIN")
                ),
                FormField.Text(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_determiner),
                    default = false,
                    value = PropertyValue.Text(code = "determiner")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_statut_bio),
                    default = false,
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(code = "STATUT_BIO")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_occ_comportement),
                    default = false,
                    nomenclatureType = "OCC_COMPORTEMENT",
                    value = PropertyValue.Nomenclature(code = "OCC_COMPORTEMENT")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_naturalite),
                    default = false,
                    nomenclatureType = "NATURALITE",
                    value = PropertyValue.Nomenclature(code = "NATURALITE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_preuve_exist),
                    default = false,
                    nomenclatureType = "PREUVE_EXIST",
                    value = PropertyValue.Nomenclature(code = "PREUVE_EXIST")
                ),
                FormField.TextMultiple(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_comment),
                    default = false,
                    value = PropertyValue.Text(code = "comment")
                ),
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.INFORMATION)
        )

        assertEquals(
            listOf(
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_stade_vie),
                    nomenclatureType = "STADE_VIE",
                    value = PropertyValue.Nomenclature(code = "STADE_VIE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_sexe),
                    nomenclatureType = "SEXE",
                    value = PropertyValue.Nomenclature(code = "SEXE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_obj_denbr),
                    nomenclatureType = "OBJ_DENBR",
                    value = PropertyValue.Nomenclature(code = "OBJ_DENBR")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_typ_denbr),
                    nomenclatureType = "TYP_DENBR",
                    value = PropertyValue.Nomenclature(code = "TYP_DENBR")
                ),
                FormField.MinMax(
                    type = FormField.Type.COUNTING,
                    label = "",
                    min = FormField.Number(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_count_min),
                        value = PropertyValue.Number(
                            CountingRecord.MIN_KEY,
                            1
                        )
                    ),
                    max = FormField.Number(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_count_max),
                        value = PropertyValue.Number(
                            CountingRecord.MAX_KEY,
                            1
                        )
                    ),
                ),
                FormField.Media(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_media),
                    value = PropertyValue.Media(AllMediaRecord.MEDIAS_KEY)
                )
            ),
            nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.COUNTING)
        )
    }

    @Test
    fun `should get the default nomenclature type settings if nomenclature default main type is requested`() =
        runTest {
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.DEFAULT,
                        label = application.getString(R.string.nomenclature_typ_grp),
                        nomenclatureType = "TYP_GRP",
                        value = PropertyValue.Nomenclature(code = "TYP_GRP")
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(FormField.Type.DEFAULT)
            )

            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.DEFAULT,
                        label = application.getString(R.string.nomenclature_typ_grp),
                        nomenclatureType = "TYP_GRP",
                        value = PropertyValue.Nomenclature(code = "TYP_GRP")
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    FormField.Type.DEFAULT,
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
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_meth_obs),
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_eta_bio),
                        visible = true,
                        default = false,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_occ_comportement),
                        visible = false,
                        nomenclatureType = "OCC_COMPORTEMENT",
                        value = PropertyValue.Nomenclature(code = "OCC_COMPORTEMENT")
                    ),
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    FormField.Type.INFORMATION,
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
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(code = "STADE_VIE")
                    )
                ),
                nomenclatureSettingsLocalDataSource.getNomenclatureTypeSettings(
                    FormField.Type.COUNTING,
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