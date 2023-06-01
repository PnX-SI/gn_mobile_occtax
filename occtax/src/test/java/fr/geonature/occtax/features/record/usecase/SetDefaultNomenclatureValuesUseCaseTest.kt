package fr.geonature.occtax.features.record.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.CoroutineTestRule
import fr.geonature.occtax.features.nomenclature.domain.EditableNomenclatureType
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

    private lateinit var setDefaultNomenclatureValuesUseCase: SetDefaultNomenclatureValuesUseCase

    @Before
    fun setUp() {
        init(this)

        setDefaultNomenclatureValuesUseCase =
            SetDefaultNomenclatureValuesUseCase(
                nomenclatureRepository
            )
    }

    @Test
    fun `should return an observation record with all default property values`() =
        runTest {
            // given some empty observation record
            val observationRecord = ObservationRecord(internalId = 1234)

            // and some default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.DEFAULT,
                        "TYP_GRP",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
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
                            PropertyValue.Nomenclature(
                                "ETA_BIO",
                                null,
                                158
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
                                            1
                                        )
                                    ).map { it.toPair() }
                                        .forEach {
                                            properties[it.first] = it.second
                                        }
                                })
                    }
            }

            // and some default nomenclature values
            coEvery {
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.DEFAULT)
            } returns Result.success(
                listOf(
                    EditableNomenclatureType(
                        EditableNomenclatureType.Type.DEFAULT,
                        "TYP_GRP",
                        EditableNomenclatureType.ViewType.NOMENCLATURE_TYPE
                    ).apply {
                        value = PropertyValue.Nomenclature(
                            "TYP_GRP",
                            "NSP",
                            129L
                        )
                    }
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
                        id = 158,
                        code = "2",
                        hierarchy = "007.002",
                        defaultLabel = "Observé vivant",
                        typeId = 7
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
                        id = 1,
                        code = "0",
                        hierarchy = "010.000",
                        defaultLabel = "Inconnu",
                        typeId = 10
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
                                    158
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
                                                "Inconnu",
                                                1
                                            )
                                        ).map { it.toPair() }
                                            .forEach {
                                                properties[it.first] = it.second
                                            }
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
                nomenclatureRepository.getEditableNomenclatures(EditableNomenclatureType.Type.DEFAULT)
            } returns Result.success(emptyList())

            // when trying to load all default nomenclature values from use case
            val result =
                setDefaultNomenclatureValuesUseCase.run(SetDefaultNomenclatureValuesUseCase.Params(observationRecord))

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ObservationRecordException.NoDefaultNomenclatureValuesFoundException)
        }
}