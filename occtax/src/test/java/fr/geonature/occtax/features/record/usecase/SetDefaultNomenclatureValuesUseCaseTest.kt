package fr.geonature.occtax.features.record.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableField
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

/**
 * Unit tests about [SetDefaultNomenclatureValuesUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class SetDefaultNomenclatureValuesUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var nomenclatureRepository: INomenclatureRepository

    @MockK
    private lateinit var additionalFieldRepository: IAdditionalFieldRepository

    private lateinit var setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase

    @Before
    fun setUp() {
        init(this)

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
                nomenclatureRepository.getEditableFields(EditableField.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.DEFAULT,
                        "TYP_GRP",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "TYP_GRP"
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
                        )
                    }
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "ETA_BIO",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "ETA_BIO",
                            "NSP",
                            152L
                        )
                    }
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.COUNTING)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.COUNTING,
                        "STADE_VIE",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "STADE_VIE",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "STADE_VIE",
                            "Indéterminé",
                            2L
                        )
                    }
                )
            )
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    EditableField.Type.INFORMATION
                )
            } returns Result.success(listOf())
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    EditableField.Type.COUNTING
                )
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "as_TYPE_PROTOCOLE",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "TYPE_PROTOCOLE",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "as_TYPE_PROTOCOLE",
                            "Inconnu",
                            387L
                        )
                    }
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
    fun `should return an observation records with all property values`() =
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
                nomenclatureRepository.getEditableFields(EditableField.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.DEFAULT,
                        "TYP_GRP",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "TYP_GRP",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
                        )
                    }
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.INFORMATION)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "ETA_BIO",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "ETA_BIO",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "ETA_BIO",
                            "NSP",
                            152L
                        )
                    }
                )
            )
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.COUNTING)
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.COUNTING,
                        "STADE_VIE",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "STADE_VIE",
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "STADE_VIE",
                            "Indéterminé",
                            2L
                        )
                    }
                )
            )
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    EditableField.Type.INFORMATION
                )
            } returns Result.success(listOf())
            coEvery {
                additionalFieldRepository.getAllAdditionalFields(
                    any(),
                    EditableField.Type.COUNTING
                )
            } returns Result.success(
                listOf(
                    EditableField(
                        EditableField.Type.INFORMATION,
                        "as_TYPE_PROTOCOLE",
                        EditableField.ViewType.NOMENCLATURE_TYPE,
                        "TYPE_PROTOCOLE"
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
                setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(observationRecord))

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
    fun `should return a NoDefaultNomenclatureValuesFoundException failure if failed to load default nomenclature values`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            // with no default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableFields(EditableField.Type.DEFAULT)
            } returns Result.success(emptyList())

            // when trying to load all default nomenclature values from use case
            val result =
                setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(observationRecord))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NoDefaultNomenclatureValuesFoundException)
        }
}