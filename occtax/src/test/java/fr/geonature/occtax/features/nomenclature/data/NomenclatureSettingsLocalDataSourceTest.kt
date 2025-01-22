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
                    order = 0,
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
                    order = 0,
                    value = PropertyValue.Nomenclature(code = "METH_OBS")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_eta_bio),
                    order = 1,
                    nomenclatureType = "ETA_BIO",
                    value = PropertyValue.Nomenclature(code = "ETA_BIO")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_meth_determin),
                    default = false,
                    order = 2,
                    nomenclatureType = "METH_DETERMIN",
                    value = PropertyValue.Nomenclature(code = "METH_DETERMIN")
                ),
                FormField.Text(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_determiner),
                    default = false,
                    order = 3,
                    value = PropertyValue.Text(code = "determiner")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_statut_bio),
                    default = false,
                    order = 4,
                    nomenclatureType = "STATUT_BIO",
                    value = PropertyValue.Nomenclature(code = "STATUT_BIO")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_occ_comportement),
                    default = false,
                    order = 5,
                    nomenclatureType = "OCC_COMPORTEMENT",
                    value = PropertyValue.Nomenclature(code = "OCC_COMPORTEMENT")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_naturalite),
                    default = false,
                    order = 6,
                    nomenclatureType = "NATURALITE",
                    value = PropertyValue.Nomenclature(code = "NATURALITE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_preuve_exist),
                    default = false,
                    order = 7,
                    nomenclatureType = "PREUVE_EXIST",
                    value = PropertyValue.Nomenclature(code = "PREUVE_EXIST")
                ),
                FormField.TextMultiple(
                    type = FormField.Type.INFORMATION,
                    label = application.getString(R.string.nomenclature_comment),
                    default = false,
                    order = 8,
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
                    order = 0,
                    nomenclatureType = "STADE_VIE",
                    value = PropertyValue.Nomenclature(code = "STADE_VIE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_sexe),
                    order = 1,
                    nomenclatureType = "SEXE",
                    value = PropertyValue.Nomenclature(code = "SEXE")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_obj_denbr),
                    order = 2,
                    nomenclatureType = "OBJ_DENBR",
                    value = PropertyValue.Nomenclature(code = "OBJ_DENBR")
                ),
                FormField.NomenclatureType(
                    type = FormField.Type.COUNTING,
                    label = application.getString(R.string.nomenclature_typ_denbr),
                    order = 3,
                    nomenclatureType = "TYP_DENBR",
                    value = PropertyValue.Nomenclature(code = "TYP_DENBR")
                ),
                FormField.MinMax(
                    type = FormField.Type.COUNTING,
                    label = "",
                    order = 4,
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
                    order = 5,
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
                        order = 0,
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
                        order = 0,
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
                        order = 0,
                        nomenclatureType = "METH_OBS",
                        value = PropertyValue.Nomenclature(code = "METH_OBS")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_eta_bio),
                        visible = true,
                        default = false,
                        order = 1,
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(code = "ETA_BIO")
                    ),
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = application.getString(R.string.nomenclature_occ_comportement),
                        visible = false,
                        order = 2,
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
        }

    @Test
    fun `should get only valid nomenclature type settings by nomenclature main type according to given settings`() =
        runTest {
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        order = 0,
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

    @Test
    fun `should get nomenclature type settings of type MinMax with min and max settings defined`() =
        runTest {
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        order = 0,
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(code = "STADE_VIE")
                    ),
                    FormField.MinMax(
                        type = FormField.Type.COUNTING,
                        label = "",
                        order = 1,
                        min = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_min),
                            visible = true,
                            default = false,
                            value = PropertyValue.Number(
                                CountingRecord.MIN_KEY,
                                1
                            )
                        ),
                        max = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_max),
                            visible = true,
                            default = false,
                            value = PropertyValue.Number(
                                CountingRecord.MAX_KEY,
                                1
                            )
                        ),
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
                        key = CountingRecord.MIN_KEY,
                        visible = true,
                        default = false
                    ),
                    PropertySettings(
                        key = CountingRecord.MAX_KEY,
                        visible = true,
                        default = false
                    )
                )
            )
        }

    @Test
    fun `should get nomenclature type settings of type MinMax with only min settings defined`() =
        runTest {
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        order = 0,
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(code = "STADE_VIE")
                    ),
                    FormField.MinMax(
                        type = FormField.Type.COUNTING,
                        label = "",
                        order = 1,
                        min = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_min),
                            visible = true,
                            default = true,
                            value = PropertyValue.Number(
                                CountingRecord.MIN_KEY,
                                1
                            )
                        ),
                        max = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_max),
                            visible = false,
                            default = false,
                            value = PropertyValue.Number(
                                CountingRecord.MAX_KEY,
                                1
                            )
                        ),
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
                        key = CountingRecord.MIN_KEY,
                        visible = true,
                        default = true
                    )
                )
            )
        }

    @Test
    fun `should get nomenclature type settings of type MinMax with only max settings defined`() =
        runTest {
            assertEquals(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        order = 0,
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(code = "STADE_VIE")
                    ),
                    FormField.MinMax(
                        type = FormField.Type.COUNTING,
                        label = "",
                        order = 1,
                        min = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_min),
                            visible = false,
                            default = false,
                            value = PropertyValue.Number(
                                CountingRecord.MIN_KEY,
                                1
                            )
                        ),
                        max = FormField.Number(
                            type = FormField.Type.COUNTING,
                            label = application.getString(R.string.nomenclature_count_max),
                            visible = true,
                            default = false,
                            value = PropertyValue.Number(
                                CountingRecord.MAX_KEY,
                                1
                            )
                        ),
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
                        key = CountingRecord.MAX_KEY,
                        visible = true,
                        default = false
                    )
                )
            )
        }
}