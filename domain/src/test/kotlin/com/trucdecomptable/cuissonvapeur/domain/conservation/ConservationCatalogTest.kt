package com.trucdecomptable.cuissonvapeur.domain.conservation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Le catalogue de conservation porte des informations de sécurité
 * alimentaire : ces tests verrouillent les deux erreurs qui coûtent cher —
 * mélanger DLC et DDM, et afficher un produit sans repère.
 */
class ConservationCatalogTest {

    @Test
    fun `les deux familles sont bien separees`() {
        val dlc = ConservationCatalog.dateImperative
        val autres = ConservationCatalog.autresDates

        assertTrue(dlc.isNotEmpty(), "la famille DLC ne peut pas être vide")
        assertTrue(autres.isNotEmpty(), "la famille DDM / sans date ne peut pas être vide")
        assertTrue(dlc.all { it.famille == FamilleDate.DLC })
        assertTrue(autres.none { it.famille == FamilleDate.DLC })
        assertEquals(ConservationCatalog.produits.size, dlc.size + autres.size)
    }

    @Test
    fun `seule la DLC est imperative`() {
        assertTrue(FamilleDate.DLC.imperatif)
        assertFalse(FamilleDate.DDM.imperatif)
        assertFalse(FamilleDate.AUCUNE.imperatif)
    }

    @Test
    fun `les oeufs portent le repere des 28 jours apres ponte`() {
        val oeufs = ConservationCatalog.produits.first { it.nom.startsWith("Œufs") }

        assertEquals(FamilleDate.DLC, oeufs.famille)
        assertTrue(oeufs.repereReglementaire.contains("28 jours"))
        assertTrue(oeufs.repereReglementaire.contains("589/2008"))
    }

    @Test
    fun `les denrees sans date obligatoire sont celles de l annexe X`() {
        val sansDate = ConservationCatalog.parFamille(FamilleDate.AUCUNE).map { it.nom }

        assertTrue(sansDate.any { it.contains("Fruits et légumes frais entiers") })
        assertTrue(sansDate.any { it.contains("Vin") })
        assertTrue(sansDate.any { it.contains("Sel") })
        assertTrue(sansDate.any { it.contains("Pain") })
    }

    @Test
    fun `un legume cru coupe n est plus exempte de date`() {
        val coupes = ConservationCatalog.produits.first { it.nom.startsWith("Légumes frais coupés") }

        assertEquals(FamilleDate.DLC, coupes.famille)
        assertTrue(coupes.repereReglementaire.contains("perd l'exemption"))
    }

    @Test
    fun `chaque produit porte un nom, un rayon et une consigne`() {
        ConservationCatalog.produits.forEach { produit ->
            assertTrue(produit.nom.isNotBlank(), "nom vide : $produit")
            assertTrue(
                produit.conservation.isNotBlank() || produit.repereReglementaire.isNotBlank(),
                "aucun repère pour ${produit.nom}",
            )
        }
    }

    @Test
    fun `aucun doublon de nom`() {
        val noms = ConservationCatalog.produits.map { it.nom.lowercase() }

        assertEquals(noms.size, noms.distinct().size, "deux produits portent le même nom")
    }

    @Test
    fun `les produits dangereux a depasser ne sont jamais classes en DDM`() {
        val noms = ConservationCatalog.parFamille(FamilleDate.DDM).map { it.nom.lowercase() }

        listOf("viande", "poisson", "œufs", "volaille", "charcuterie", "yaourt").forEach { fragile ->
            assertTrue(
                noms.none { it.contains(fragile) },
                "$fragile ne peut pas être en DDM : sa date est impérative",
            )
        }
    }

    @Test
    fun `la recherche trouve par nom, par rayon et par mot de la consigne`() {
        assertTrue(ConservationCatalog.chercher("œufs").any { it.nom.startsWith("Œufs") })
        assertTrue(ConservationCatalog.chercher("surgelés").isNotEmpty())
        assertTrue(ConservationCatalog.chercher("réfrigérateur").isNotEmpty())
        assertEquals(ConservationCatalog.produits, ConservationCatalog.chercher("   "))
        assertTrue(ConservationCatalog.chercher("produit qui n'existe pas").isEmpty())
    }

    @Test
    fun `la recherche est insensible a la casse`() {
        assertEquals(
            ConservationCatalog.chercher("conserve").map { it.nom },
            ConservationCatalog.chercher("CONSERVE").map { it.nom },
        )
    }

    @Test
    fun `les rappels et les sources sont affiches avec le catalogue`() {
        assertTrue(ConservationCatalog.rappels.any { it.contains("prime toujours") })
        assertTrue(ConservationCatalog.sources.any { it.contains("1169/2011") })
        assertTrue(ConservationCatalog.sources.any { it.contains("Anses") })
    }

    @Test
    fun `le catalogue couvre les rayons du magasin`() {
        val rayons = ConservationCatalog.produits.map { it.rayon }.distinct()

        listOf(
            Rayon.OEUFS, Rayon.VIANDES, Rayon.POISSONS, Rayon.CHARCUTERIE, Rayon.LAITIERS,
            Rayon.TRAITEUR, Rayon.EPICERIE, Rayon.SURGELES, Rayon.FRUITS_LEGUMES, Rayon.BOISSONS,
        ).forEach { rayon ->
            assertTrue(rayon in rayons, "aucun produit pour le rayon ${rayon.libelle}")
        }
    }
}
