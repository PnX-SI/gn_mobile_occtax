package fr.geonature.occtax.features.nomenclature.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.DefaultNomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTaxonomyDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.occtax.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Unit tests about [INomenclatureLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NomenclatureLocalDataSourceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var taxonomyDao: TaxonomyDao
    private lateinit var nomenclatureTypeDao: NomenclatureTypeDao
    private lateinit var nomenclatureTaxonomyDao: NomenclatureTaxonomyDao
    private lateinit var nomenclatureDao: NomenclatureDao
    private lateinit var defaultNomenclatureDao: DefaultNomenclatureDao
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(
                context,
                LocalDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        taxonomyDao = db.taxonomyDao()
        nomenclatureTypeDao = db.nomenclatureTypeDao()
        nomenclatureTaxonomyDao = db.nomenclatureTaxonomyDao()
        nomenclatureDao = db.nomenclatureDao()
        defaultNomenclatureDao = db.defaultNomenclatureDao()

        nomenclatureLocalDataSource = NomenclatureLocalDataSourceImpl(
            "occtax",
            nomenclatureTypeDao,
            nomenclatureDao
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should find all nomenclature types`() = runTest {
        val expectedNomenclatureTypes = initializeNomenclatureTypes()
        val nomenclatureTypesFromDb = nomenclatureLocalDataSource.getAllNomenclatureTypes()

        assertEquals(
            expectedNomenclatureTypes.sortedBy { it.defaultLabel },
            nomenclatureTypesFromDb
        )
    }

    @Test
    fun `should find all default nomenclature values`() = runTest {
        initializeNomenclatureTypes()
        val expectedNomenclatures = initializeNomenclaturesByTypes()
        val expectedDefaultNomenclatureValues = initializeDefaultNomenclatureValues()

        val nomenclatureTypesWithDefaultValuesFromDb =
            nomenclatureLocalDataSource.getAllDefaultNomenclatureValues()

        assertEquals(
            expectedNomenclatures.filter { expectedDefaultNomenclatureValues.any { defaultNomenclature -> defaultNomenclature.nomenclatureId == it.id } },
            nomenclatureTypesWithDefaultValuesFromDb
        )
    }

    @Test
    fun `should find nomenclatures by type with no taxonomy`() = runTest {
        initializeTaxonomy()
        initializeNomenclatureTypes()
        val expectedNomenclatures = initializeNomenclaturesByTypes()
        val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

        val nomenclaturesForStatutBioAndNoTaxonomy =
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(mnemonic = "STATUT_BIO")

        assertEquals(expectedNomenclatureTaxonomy
            .filter { it.taxonomy == Taxonomy(kingdom = Taxonomy.ANY) }
            .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
            .sortedBy { it.defaultLabel },
            nomenclaturesForStatutBioAndNoTaxonomy
        )
    }

    @Test
    fun `should find nomenclatures by type with any taxonomy`() = runTest {
        initializeTaxonomy()
        initializeNomenclatureTypes()
        val expectedNomenclatures = initializeNomenclaturesByTypes()
        val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

        val nomenclaturesForStatutBioAndAnyKingdomTaxonomy =
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(kingdom = Taxonomy.ANY)
            )

        assertEquals(expectedNomenclatureTaxonomy
            .filter { it.taxonomy == Taxonomy(kingdom = Taxonomy.ANY) }
            .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
            .sortedBy { it.defaultLabel },
            nomenclaturesForStatutBioAndAnyKingdomTaxonomy
        )
    }

    @Test
    fun `should find nomenclatures by type matching given taxonomy kingdom`() = runTest {
        initializeTaxonomy()
        initializeNomenclatureTypes()
        val expectedNomenclatures = initializeNomenclaturesByTypes()
        val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

        val nomenclaturesForStatutBioAndAnyTaxonomy =
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(kingdom = "Animalia")
            )

        assertEquals(expectedNomenclatureTaxonomy
            .filter {
                listOf(
                    Taxonomy(kingdom = Taxonomy.ANY),
                    Taxonomy(kingdom = "Animalia")
                ).contains(it.taxonomy)
            }
            .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
            .sortedBy { it.defaultLabel },
            nomenclaturesForStatutBioAndAnyTaxonomy
        )
    }

    @Test
    fun `should find nomenclatures by type matching given taxonomy kingdom and group`() = runTest {
        initializeTaxonomy()
        initializeNomenclatureTypes()
        val expectedNomenclatures = initializeNomenclaturesByTypes()
        val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

        val nomenclaturesForStatutBioAndAnyTaxonomy =
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )

        assertEquals(expectedNomenclatureTaxonomy
            .filter {
                listOf(
                    Taxonomy(kingdom = Taxonomy.ANY),
                    Taxonomy(kingdom = "Animalia"),
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                ).contains(it.taxonomy)
            }
            .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
            .sortedBy { it.defaultLabel },
            nomenclaturesForStatutBioAndAnyTaxonomy
        )
    }

    private fun initializeTaxonomy(): List<Taxonomy> {
        val expectedTaxonomy = listOf(
            Taxonomy(kingdom = Taxonomy.ANY),
            Taxonomy(kingdom = "Animalia"),
            Taxonomy(
                kingdom = "Animalia",
                group = "Amphibiens"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Mammifères"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Reptiles"
            ),
            Taxonomy(kingdom = "Fungi"),
            Taxonomy(kingdom = "Plantae")
        )

        taxonomyDao.insert(*expectedTaxonomy.toTypedArray())

        return expectedTaxonomy
    }

    private fun initializeNomenclatureTypes(): List<NomenclatureType> {
        val expectedNomenclatureTypes = listOf(
            NomenclatureType(
                id = 7,
                mnemonic = "ETA_BIO",
                defaultLabel = "Etat biologique de l'observation"
            ),
            NomenclatureType(
                id = 13,
                mnemonic = "STATUT_BIO",
                defaultLabel = "Statut biologique"
            ),
            NomenclatureType(
                id = 14,
                mnemonic = "METH_OBS",
                defaultLabel = "Méthodes d'observation"
            )
        )

        nomenclatureTypeDao.insert(*expectedNomenclatureTypes.toTypedArray())

        return expectedNomenclatureTypes
    }

    private fun initializeNomenclaturesByTypes(): List<Nomenclature> {
        val expectedNomenclatures = listOf(
            Nomenclature(
                id = 29,
                code = "1",
                hierarchy = "013.001",
                defaultLabel = "Non renseigné",
                typeId = 13
            ),
            Nomenclature(
                id = 31,
                code = "3",
                hierarchy = "013.003",
                defaultLabel = "Reproduction",
                typeId = 13
            ),
            Nomenclature(
                id = 32,
                code = "4",
                hierarchy = "013.004",
                defaultLabel = "Hibernation",
                typeId = 13
            ),
            Nomenclature(
                id = 33,
                code = "5",
                hierarchy = "013.005",
                defaultLabel = "Estivation",
                typeId = 13
            ),
            Nomenclature(
                id = 157,
                code = "1",
                hierarchy = "007.001",
                defaultLabel = "Non renseigné",
                typeId = 7
            ),
            Nomenclature(
                id = 158,
                code = "2",
                hierarchy = "007.002",
                defaultLabel = "Observé vivant",
                typeId = 7
            ),
            Nomenclature(
                id = 159,
                code = "3",
                hierarchy = "007.003",
                defaultLabel = "Trouvé mort",
                typeId = 7
            ),
            Nomenclature(
                id = 160,
                code = "4",
                hierarchy = "007.004",
                defaultLabel = "Indice de présence",
                typeId = 7
            ),
            Nomenclature(
                id = 161,
                code = "5",
                hierarchy = "007.005",
                defaultLabel = "Issu d'élevage",
                typeId = 7
            )
        )

        nomenclatureDao.insert(*expectedNomenclatures.toTypedArray())

        return expectedNomenclatures
    }

    private fun initializeNomenclaturesTaxonomy(): List<NomenclatureTaxonomy> {
        val expectedNomenclatureTaxonomy = listOf(
            NomenclatureTaxonomy(
                nomenclatureId = 29,
                taxonomy = Taxonomy(kingdom = Taxonomy.ANY)
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 31,
                taxonomy = Taxonomy(kingdom = "Animalia")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 31,
                taxonomy = Taxonomy(kingdom = "Fungi")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 31,
                taxonomy = Taxonomy(kingdom = "Plantae")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Amphibiens"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Mammifères"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Reptiles"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Mammifères"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Reptiles"
                )
            )
        )

        nomenclatureTaxonomyDao.insert(*expectedNomenclatureTaxonomy.toTypedArray())

        return expectedNomenclatureTaxonomy
    }

    private fun initializeDefaultNomenclatureValues(): List<DefaultNomenclature> {
        val expectedDefaultNomenclatureValues = listOf(
            DefaultNomenclature(
                "occtax",
                29
            )
        )

        defaultNomenclatureDao.insert(*expectedDefaultNomenclatureValues.toTypedArray())

        return expectedDefaultNomenclatureValues
    }
}