package fr.geonature.occtax.features.record.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.R
import fr.geonature.occtax.features.nomenclature.domain.FormField
import fr.geonature.occtax.features.nomenclature.repository.IAdditionalFieldRepository
import fr.geonature.occtax.features.nomenclature.repository.INomenclatureRepository
import fr.geonature.occtax.features.record.domain.ObservationRecord
import fr.geonature.occtax.features.record.domain.PropertyValue
import fr.geonature.occtax.features.record.error.ObservationRecordException
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [SetDefaultNomenclatureValuesUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SetDefaultNomenclatureValuesUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application

    @MockK
    private lateinit var nomenclatureRepository: INomenclatureRepository

    @MockK
    private lateinit var additionalFieldRepository: IAdditionalFieldRepository

    private lateinit var setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        setDefaultNomenclatureValuesUseCase =
            SetDefaultNomenclatureValuesUseCase(
                nomenclatureRepository,
                additionalFieldRepository
            )
    }

    @Test
    fun `should return an observation record with all default property values`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            // and some default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.DEFAULT,
                        label = application.getString(R.string.nomenclature_typ_grp),
                        nomenclatureType = "TYP_GRP",
                        value = PropertyValue.Nomenclature(
                            code = "TYP_GRP",
                            label = "NSP",
                            value = 129L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "ETA_BIO",
                            label = "NSP",
                            value = 152L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.COUNTING)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        default = false,
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(
                            code = "STADE_VIE",
                            label = "Indéterminé",
                            value = 2L
                        )
                    )
                )
            )
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.INFORMATION
                )
            } returns Result.success(listOf())
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.COUNTING
                )
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Protocole",
                        nomenclatureType = "TYPE_PROTOCOLE",
                        value = PropertyValue.Nomenclature(
                            code = "as_TYPE_PROTOCOLE",
                            label = "Inconnu",
                            value = 387L
                        )
                    )
                )
            )

            // when loading all default nomenclature values from use case
            val result =
                setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(observationRecord))

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                ObservationRecord(internalId = 1234).apply {
                    listOf(
                        PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129
                        )
                    ).map { it.toPair() }
                        .forEach {
                            properties[it.first] = it.second
                        }
                },
                result.getOrThrow()
            )
        }

    @Test
    fun `should return an observation records with all property values with additional fields`() =
        runTest {
            // given some observation record
            val observationRecord = ObservationRecord(internalId = 1234).apply {
                taxa.add(
                    Taxon(
                        8L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
                    .apply {
                        listOf(
                            PropertyValue.Number(
                                "ETA_BIO",
                                158L
                            )
                        ).map { it.toPair() }
                            .forEach {
                                properties[it.first] = it.second
                            }
                        counting.addOrUpdate(
                            counting.create()
                                .apply {
                                    listOf(
                                        PropertyValue.Nomenclature(
                                            "STADE_VIE",
                                            null,
                                            2L
                                        ),
                                        PropertyValue.Number(
                                            "count_min",
                                            1
                                        ),
                                        PropertyValue.Number(
                                            "count_max",
                                            2
                                        )
                                    ).map { it.toPair() }
                                        .forEach {
                                            properties[it.first] = it.second
                                        }
                                    additionalFields = listOf(
                                        PropertyValue.Number(
                                            "as_TYPE_PROTOCOLE",
                                            387L
                                        ),
                                        PropertyValue.Number(
                                            "some_attribute_as_number",
                                            42L
                                        )
                                    )
                                })
                    }
            }

            // and some default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.DEFAULT,
                        label = application.getString(R.string.nomenclature_typ_grp),
                        nomenclatureType = "TYP_GRP",
                        value = PropertyValue.Nomenclature(
                            code = "TYP_GRP",
                            label = "NSP",
                            value = 129L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "ETA_BIO",
                            label = "NSP",
                            value = 152L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.COUNTING)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(
                            code = "STADE_VIE",
                            label = "Indéterminé",
                            value = 2L
                        )
                    )
                )
            )
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.INFORMATION
                )
            } returns Result.success(listOf())
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.COUNTING
                )
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Protocole",
                        nomenclatureType = "TYPE_PROTOCOLE",
                        value = PropertyValue.Nomenclature(code = "as_TYPE_PROTOCOLE")
                    )
                )
            )

            // and with some nomenclature values
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "ETA_BIO",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        152L,
                        "0",
                        "007.000",
                        "NSP",
                        typeId = 7L
                    ),
                    Nomenclature(
                        id = 158L,
                        code = "2",
                        hierarchy = "007.002",
                        defaultLabel = "Observé vivant",
                        typeId = 7L
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "STADE_VIE",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        2L,
                        "1",
                        "010.001",
                        "Indéterminé",
                        typeId = 10L
                    ),
                    Nomenclature(
                        id = 1L,
                        code = "0",
                        hierarchy = "010.000",
                        defaultLabel = "Inconnu",
                        typeId = 10L
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "TYPE_PROTOCOLE",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        id = 387L,
                        code = "0",
                        hierarchy = "112.000",
                        defaultLabel = "Inconnu",
                        typeId = 112L
                    ),
                    Nomenclature(
                        id = 388L,
                        code = "1",
                        hierarchy = "112.001",
                        defaultLabel = "Protocole de collecte",
                        typeId = 112L
                    )
                )
            )

            // when loading all default nomenclature values from use case
            val result =
                setDefaultNomenclatureValuesUseCase.run(
                    SetDefaultNomenclatureValuesUseCase.Params(
                        observationRecord,
                        withAdditionalFields = true
                    )
                )

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                ObservationRecord(internalId = 1234).apply {
                    listOf(
                        PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
                        )
                    ).map { it.toPair() }
                        .forEach {
                            properties[it.first] = it.second
                        }

                    taxa.add(
                        Taxon(
                            8L,
                            "taxon_01",
                            Taxonomy(
                                "Animalia",
                                "Ascidies"
                            )
                        )
                    )
                        .apply {
                            listOf(
                                PropertyValue.Nomenclature(
                                    "ETA_BIO",
                                    "Observé vivant",
                                    158L
                                )
                            ).map { it.toPair() }
                                .forEach {
                                    properties[it.first] = it.second
                                }
                            counting.addOrUpdate(
                                counting.create()
                                    .apply {
                                        listOf(
                                            PropertyValue.Nomenclature(
                                                "STADE_VIE",
                                                "Indéterminé",
                                                2L
                                            ),
                                            PropertyValue.Number(
                                                "count_min",
                                                1
                                            ),
                                            PropertyValue.Number(
                                                "count_max",
                                                2
                                            )
                                        ).map { it.toPair() }
                                            .forEach {
                                                properties[it.first] = it.second
                                            }
                                        additionalFields = listOf(
                                            PropertyValue.Nomenclature(
                                                "as_TYPE_PROTOCOLE",
                                                "Inconnu",
                                                387L
                                            ),
                                            PropertyValue.Number(
                                                "some_attribute_as_number",
                                                42L
                                            )
                                        )
                                    })
                        }
                },
                result.getOrThrow()
            )
        }

    @Test
    fun `should return an observation records with all property values with no additional fields`() =
        runTest {
            // given some observation record
            val observationRecord = ObservationRecord(internalId = 1234).apply {
                taxa.add(
                    Taxon(
                        8L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
                    .apply {
                        listOf(
                            PropertyValue.Number(
                                "ETA_BIO",
                                158L
                            )
                        ).map { it.toPair() }
                            .forEach {
                                properties[it.first] = it.second
                            }
                        counting.addOrUpdate(
                            counting.create()
                                .apply {
                                    listOf(
                                        PropertyValue.Nomenclature(
                                            "STADE_VIE",
                                            null,
                                            2L
                                        ),
                                        PropertyValue.Number(
                                            "count_min",
                                            1
                                        ),
                                        PropertyValue.Number(
                                            "count_max",
                                            2
                                        )
                                    ).map { it.toPair() }
                                        .forEach {
                                            properties[it.first] = it.second
                                        }
                                    additionalFields = listOf(
                                        PropertyValue.Number(
                                            "as_TYPE_PROTOCOLE",
                                            387L
                                        ),
                                        PropertyValue.Number(
                                            "some_attribute_as_number",
                                            42L
                                        )
                                    )
                                })
                    }
            }

            // and some default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.DEFAULT,
                        label = application.getString(R.string.nomenclature_typ_grp),
                        nomenclatureType = "TYP_GRP",
                        value = PropertyValue.Nomenclature(
                            code = "TYP_GRP",
                            label = "NSP",
                            value = 129L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.INFORMATION)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Etat biologique de l'observation",
                        nomenclatureType = "ETA_BIO",
                        value = PropertyValue.Nomenclature(
                            code = "ETA_BIO",
                            label = "NSP",
                            value = 152L
                        )
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.COUNTING)
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.COUNTING,
                        label = application.getString(R.string.nomenclature_stade_vie),
                        nomenclatureType = "STADE_VIE",
                        value = PropertyValue.Nomenclature(
                            code = "STADE_VIE",
                            label = "Indéterminé",
                            value = 2L
                        )
                    )
                )
            )
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.INFORMATION
                )
            } returns Result.success(listOf())
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    FormField.Type.COUNTING
                )
            } returns Result.success(
                listOf(
                    FormField.NomenclatureType(
                        type = FormField.Type.INFORMATION,
                        label = "Protocole",
                        nomenclatureType = "TYPE_PROTOCOLE",
                        value = PropertyValue.Nomenclature(code = "as_TYPE_PROTOCOLE")
                    )
                )
            )

            // and with some nomenclature values
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "ETA_BIO",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        152L,
                        "0",
                        "007.000",
                        "NSP",
                        typeId = 7L
                    ),
                    Nomenclature(
                        id = 158L,
                        code = "2",
                        hierarchy = "007.002",
                        defaultLabel = "Observé vivant",
                        typeId = 7L
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "STADE_VIE",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        2L,
                        "1",
                        "010.001",
                        "Indéterminé",
                        typeId = 10L
                    ),
                    Nomenclature(
                        id = 1L,
                        code = "0",
                        hierarchy = "010.000",
                        defaultLabel = "Inconnu",
                        typeId = 10L
                    )
                )
            )
            coEvery {
                nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                    "TYPE_PROTOCOLE",
                    any()
                )
            } returns Result.success(
                listOf(
                    Nomenclature(
                        id = 387L,
                        code = "0",
                        hierarchy = "112.000",
                        defaultLabel = "Inconnu",
                        typeId = 112L
                    ),
                    Nomenclature(
                        id = 388L,
                        code = "1",
                        hierarchy = "112.001",
                        defaultLabel = "Protocole de collecte",
                        typeId = 112L
                    )
                )
            )

            // when loading all default nomenclature values from use case
            val result = setDefaultNomenclatureValuesUseCase.run(
                SetDefaultNomenclatureValuesUseCase.Params(observationRecord)
            )

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                ObservationRecord(internalId = 1234).apply {
                    listOf(
                        PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
                        )
                    ).map { it.toPair() }
                        .forEach {
                            properties[it.first] = it.second
                        }

                    taxa.add(
                        Taxon(
                            8L,
                            "taxon_01",
                            Taxonomy(
                                "Animalia",
                                "Ascidies"
                            )
                        )
                    )
                        .apply {
                            listOf(
                                PropertyValue.Nomenclature(
                                    "ETA_BIO",
                                    "Observé vivant",
                                    158L
                                )
                            ).map { it.toPair() }
                                .forEach {
                                    properties[it.first] = it.second
                                }
                            counting.addOrUpdate(
                                counting.create()
                                    .apply {
                                        listOf(
                                            PropertyValue.Nomenclature(
                                                "STADE_VIE",
                                                "Indéterminé",
                                                2L
                                            ),
                                            PropertyValue.Number(
                                                "count_min",
                                                1
                                            ),
                                            PropertyValue.Number(
                                                "count_max",
                                                2
                                            )
                                        ).map { it.toPair() }
                                            .forEach {
                                                properties[it.first] = it.second
                                            }
                                        additionalFields = listOf(
                                            PropertyValue.Number(
                                                "as_TYPE_PROTOCOLE",
                                                387L
                                            ),
                                            PropertyValue.Number(
                                                "some_attribute_as_number",
                                                42L
                                            )
                                        )
                                    })
                        }
                },
                result.getOrThrow()
            )
        }

    @Test
    fun `should return a NoDefaultNomenclatureValuesFoundException failure if failed to load default nomenclature values`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            // with no default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableFields(FormField.Type.DEFAULT)
            } returns Result.success(emptyList())

            // when trying to load all default nomenclature values from use case
            val result =
                setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(observationRecord))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NoDefaultNomenclatureValuesFoundException)
        }
}