package com.trucdecomptable.cuissonvapeur.domain.conservation

/**
 * Jusqu'à quand un produit reste consommable — et surtout **pourquoi** deux
 * familles de dates ne disent pas la même chose.
 *
 * Le découpage suit la réglementation, pas une intuition :
 * - la DLC (« à consommer jusqu'au ») est **impérative** : au-delà, la denrée
 *   peut être dangereuse (règlement (UE) n° 1169/2011, dit INCO, art. 24) ;
 * - la DDM (« à consommer de préférence avant ») ne parle que de **qualité** :
 *   au-delà, le produit perd du goût, de la texture, des vitamines, mais reste
 *   consommable sans danger ;
 * - certaines denrées n'ont **aucune date obligatoire** (annexe X du même
 *   règlement) : fruits et légumes entiers, vin, sel, sucre, vinaigre, pain
 *   consommé sous 24 h…
 *
 * Les repères chiffrés de ce catalogue viennent soit d'un texte
 * réglementaire (œufs, crème, lait), soit de recommandations publiques
 * (Anses, ministère de l'Agriculture, Institut national de la consommation).
 * La date imprimée sur l'emballage prime toujours sur ce qui est affiché ici.
 */
enum class FamilleDate(
    /** Étiquette courte affichée sur la fiche produit. */
    val libelle: String,
    /** La mention exacte à chercher sur l'emballage. */
    val mention: String,
    /** Ce que le dépassement de cette date veut dire. */
    val consequence: String,
    /** Vrai pour la seule date dont le dépassement met en danger. */
    val imperatif: Boolean,
) {
    DLC(
        libelle = "DLC",
        mention = "À consommer jusqu'au",
        consequence = "Date impérative : au-delà, la denrée peut être dangereuse pour la santé.",
        imperatif = true,
    ),
    DDM(
        libelle = "DDM (ex-DLUO)",
        mention = "À consommer de préférence avant",
        consequence = "Date indicative : au-delà, le produit perd des qualités, sans danger pour la santé.",
        imperatif = false,
    ),
    AUCUNE(
        libelle = "Sans date obligatoire",
        mention = "Aucune mention imposée",
        consequence = "Denrée dispensée de date : c'est l'aspect, l'odeur et le goût qui décident.",
        imperatif = false,
    );

    companion object {
        /** Les deux familles qui appellent une lecture différente. */
        val dateImperative: FamilleDate get() = DLC

        fun depuisNom(nom: String): FamilleDate = entries.firstOrNull { it.name == nom } ?: DDM
    }
}

/** Rayon de magasin, pour retrouver un produit d'un coup d'œil. */
enum class Rayon(val libelle: String) {
    OEUFS("Œufs"),
    VIANDES("Viandes et volailles"),
    POISSONS("Poissons et fruits de mer"),
    CHARCUTERIE("Charcuterie"),
    LAITIERS("Produits laitiers"),
    TRAITEUR("Plats cuisinés et traiteur"),
    EPICERIE("Épicerie et conserves"),
    SURGELES("Surgelés"),
    FRUITS_LEGUMES("Fruits et légumes"),
    BOISSONS("Boissons"),
}

/**
 * Un produit, sa famille de date et les repères utiles.
 *
 * @param repereReglementaire durée fixée par un texte, quand elle existe
 *   (ex. « 28 jours après le jour de ponte »).
 * @param conservation conditions à respecter pour que la date garde son sens.
 * @param apresOuverture ce qui change une fois l'emballage ouvert — moment où
 *   la date de l'emballage ne protège plus rien.
 * @param congelation ce que la congélation domestique permet, et ce qu'elle
 *   interdit.
 * @param vigilance signes qui imposent de jeter même avant la date.
 */
data class ProduitConservation(
    val nom: String,
    val rayon: Rayon,
    val famille: FamilleDate,
    val repereReglementaire: String = "",
    val conservation: String = "",
    val apresOuverture: String = "",
    val congelation: String = "",
    val vigilance: String = "",
) {
    /** Texte dans lequel la recherche de l'écran va chercher. */
    val texteRecherche: String
        get() = listOf(nom, rayon.libelle, famille.libelle, repereReglementaire, conservation)
            .joinToString(" ")

    /** Vrai quand ce produit se garde au froid : sert à rappeler les 4 °C. */
    val auFroid: Boolean
        get() = famille == FamilleDate.DLC || conservation.contains("réfrigérateur") ||
            conservation.contains("°C")
}

/**
 * Le référentiel affiché par l'écran « Conservation ».
 *
 * Il est volontairement séparé en deux : ce qui relève de la sécurité
 * ([FamilleDate.DLC]) et ce qui ne relève que de la qualité
 * ([FamilleDate.DDM], [FamilleDate.AUCUNE]). Mélanger les deux fait jeter des
 * produits encore bons — ou manger un produit dangereux.
 */
object ConservationCatalog {

    val produits: List<ProduitConservation> = listOf(
        // ---------------------------------------------------------- DLC
        ProduitConservation(
            nom = "Œufs en coquille",
            rayon = Rayon.OEUFS,
            famille = FamilleDate.DLC,
            repereReglementaire = "Date de consommation recommandée : 28 jours après le jour de ponte " +
                "(règlement (CE) n° 589/2008). La vente au consommateur s'arrête 21 jours après la ponte.",
            conservation = "Au réfrigérateur de préférence, ou à température ambiante comme en magasin : " +
                "l'important est de ne pas alterner les deux.",
            apresOuverture = "Un œuf cassé se consomme tout de suite ; un blanc d'œuf cru se garde 24 h au froid.",
            congelation = "Oui, hors coquille (jaune et blanc battus), jamais un œuf entier en coquille.",
            vigilance = "Œuf fêlé, sale ou odorant : à écarter.",
        ),
        ProduitConservation(
            nom = "Viande fraîche (bœuf, porc, agneau)",
            rayon = Rayon.VIANDES,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur, zone la plus froide (4 °C), dans son emballage d'origine.",
            apresOuverture = "Entamée, à consommer dans les 3 jours (recommandation Anses).",
            congelation = "Oui, dès l'achat plutôt qu'en fin de date, à -18 °C.",
        ),
        ProduitConservation(
            nom = "Viande hachée",
            rayon = Rayon.VIANDES,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C. C'est le produit le plus fragile du rayon frais : " +
                "sa date est courte, souvent 1 à 2 jours après l'achat.",
            apresOuverture = "Hachée devant vous par le boucher : à cuire le jour même.",
            congelation = "Oui, dès l'achat, à -18 °C.",
            vigilance = "Ne jamais goûter une viande hachée crue, ni la consommer peu cuite.",
        ),
        ProduitConservation(
            nom = "Volaille crue",
            rayon = Rayon.VIANDES,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C, sur une assiette pour éviter les écoulements vers les autres aliments.",
            apresOuverture = "À cuire dans les 3 jours (recommandation Anses).",
            congelation = "Oui, à -18 °C ; la décongélation se fait au réfrigérateur.",
            vigilance = "Cuisson à cœur obligatoire : le jus doit être clair.",
        ),
        ProduitConservation(
            nom = "Abats (foie, rognons, cervelle)",
            rayon = Rayon.VIANDES,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C, très peu de temps : ce sont des denrées très périssables.",
            apresOuverture = "À cuire rapidement après l'achat.",
            congelation = "Déconseillée pour la cervelle ; possible pour le foie, dès l'achat.",
        ),
        ProduitConservation(
            nom = "Poisson frais et crevettes cuites",
            rayon = Rayon.POISSONS,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C, zone la plus froide, dès le retour des courses.",
            apresOuverture = "À cuire et consommer rapidement : sa date est courte.",
            congelation = "Oui, dès l'achat ; les produits déjà surgelés ne sont pas recongelables.",
            vigilance = "Œil creux, odeur forte, chair qui se détache : à jeter.",
        ),
        ProduitConservation(
            nom = "Coquillages et crustacés vivants",
            rayon = Rayon.POISSONS,
            famille = FamilleDate.DLC,
            conservation = "Au réfrigérateur, dans leur emballage, sans glaçons d'eau douce ; " +
                "à cuire le jour ou le lendemain de l'achat.",
            apresOuverture = "À consommer immédiatement après cuisson.",
            vigilance = "Un coquillage qui reste ouvert et ne se referme pas ne se mange pas.",
        ),
        ProduitConservation(
            nom = "Charcuterie et jambon tranchés",
            rayon = Rayon.CHARCUTERIE,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C. Une barquette de tranché dure moins qu'un jambon entier sous vide.",
            apresOuverture = "3 jours au réfrigérateur, en refermant l'emballage ou dans une boîte.",
            congelation = "Possible, mais la texture rend de l'eau à la décongélation.",
        ),
        ProduitConservation(
            nom = "Lait frais pasteurisé et micro-filtré",
            rayon = Rayon.LAITIERS,
            famille = FamilleDate.DLC,
            repereReglementaire = "Une date limite de consommation est obligatoire sur les récipients " +
                "(décret n° 55-771 du 21 mai 1955).",
            conservation = "Réfrigérateur 0 à 6 °C.",
            apresOuverture = "À consommer dans les 3 jours (recommandation Anses).",
            congelation = "Possible, avec une texture modifiée après décongélation.",
        ),
        ProduitConservation(
            nom = "Crème fraîche et crème liquide",
            rayon = Rayon.LAITIERS,
            famille = FamilleDate.DLC,
            repereReglementaire = "« À consommer avant le » suivi du jour et du mois " +
                "(article 6 du décret n° 80-313 du 23 avril 1980).",
            conservation = "Réfrigérateur, entre 4 et 6 °C.",
            apresOuverture = "3 jours au réfrigérateur.",
        ),
        ProduitConservation(
            nom = "Yaourts et laits fermentés",
            rayon = Rayon.LAITIERS,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur, entre 4 et 6 °C.",
            apresOuverture = "À consommer dans les 3 jours (recommandation Anses).",
            vigilance = "Moisissure, gonflement du pot, odeur anormale : à jeter.",
        ),
        ProduitConservation(
            nom = "Fromages frais et fromages au lait cru",
            rayon = Rayon.LAITIERS,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur, dans le bac à légumes ou un bac fermé.",
            apresOuverture = "À consommer dans les 3 jours pour un fromage frais ; " +
                "une croûte qui s'étend sur un fromage pressé se coupe.",
            vigilance = "Les fromages au lait cru sont déconseillés aux jeunes enfants, femmes enceintes et personnes immunodéprimées.",
        ),
        ProduitConservation(
            nom = "Plats cuisinés réfrigérés, salades, soupes fraîches, jus de fruits frais",
            rayon = Rayon.TRAITEUR,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C, en haut du réfrigérateur, sans rupture de la chaîne du froid.",
            apresOuverture = "À consommer dans les 3 jours, souvent moins : suivre la mention de l'emballage.",
        ),
        ProduitConservation(
            nom = "Pâtisseries à la crème, entremets, sandwichs, produits traiteur",
            rayon = Rayon.TRAITEUR,
            famille = FamilleDate.DLC,
            conservation = "Réfrigérateur 4 °C. Ce sont des denrées très périssables.",
            apresOuverture = "À consommer rapidement, sans attendre la date pour un produit entamé.",
        ),
        ProduitConservation(
            nom = "Préparations maison à base d'œufs crus (mayonnaise, mousse au chocolat)",
            rayon = Rayon.OEUFS,
            famille = FamilleDate.DLC,
            repereReglementaire = "À consommer immédiatement après préparation, ou maintenues au froid " +
                "et consommées dans les 24 heures (recommandation Anses).",
            conservation = "Réfrigérateur si la préparation n'est pas mangée tout de suite.",
            apresOuverture = "24 heures maximum au froid, jamais à température ambiante.",
        ),

        // ---------------------------------------------------------- DDM
        ProduitConservation(
            nom = "Conserves (boîtes et bocaux)",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            repereReglementaire = "Depuis le décret n° 2022-1440, l'emballage peut préciser " +
                "« ce produit peut être consommé après cette date ».",
            conservation = "À l'abri de la lumière, au sec, entre 10 et 20 °C.",
            apresOuverture = "Le contenu entamé passe au réfrigérateur, dans une boîte fermée, 2 à 3 jours.",
            vigilance = "Bombage, rouille, déformation, pas de « pop » à l'ouverture : à jeter, " +
                "même avant la date (Anses).",
        ),
        ProduitConservation(
            nom = "Pâtes, riz, semoule, farine",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Au sec, dans un bocal fermé, à l'abri de l'humidité.",
            apresOuverture = "La DDM reste valable pour les produits secs, même emballage ouvert (INC).",
            vigilance = "Farine qui sent le rance, pâtes qui ont pris l'humidité : le goût est perdu.",
        ),
        ProduitConservation(
            nom = "Légumes secs (lentilles, pois chiches, haricots)",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Au sec et à l'abri de la lumière.",
            vigilance = "Des légumes secs trop vieux cuisent mal : la date est un repère de qualité de cuisson.",
        ),
        ProduitConservation(
            nom = "Café moulu ou en grains, thé",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Boîte hermétique, au sec, loin de la lumière et des odeurs.",
            apresOuverture = "Les arômes s'échappent vite : à boire dans les semaines qui suivent l'ouverture.",
        ),
        ProduitConservation(
            nom = "Chocolat, biscuits secs, céréales, gâteaux secs",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Au sec, entre 15 et 20 °C, loin de la chaleur.",
            vigilance = "Un chocolat qui blanchit reste consommable, mais peut avoir pris le goût du placard.",
        ),
        ProduitConservation(
            nom = "Épices et herbes séchées",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Bocal fermé, à l'abri de la lumière et de la chaleur.",
            apresOuverture = "Elles perdent leur parfum bien avant de devenir dangereuses.",
        ),
        ProduitConservation(
            nom = "Huiles",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Bouteille fermée, à l'abri de la lumière ; certaines huiles se conservent plusieurs années.",
            apresOuverture = "À consommer dans les mois qui suivent, une huile rance ne se rattrape pas.",
            vigilance = "Odeur de peinture ou de noix rance : le produit est oxydé.",
        ),
        ProduitConservation(
            nom = "Miel",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            conservation = "Pot fermé, à température ambiante ; la cristallisation n'est pas une altération.",
        ),
        ProduitConservation(
            nom = "Lait UHT et lait en poudre",
            rayon = Rayon.LAITIERS,
            famille = FamilleDate.DDM,
            conservation = "Température ambiante tant que la brique n'est pas ouverte.",
            apresOuverture = "Au réfrigérateur et à consommer dans les 3 jours.",
        ),
        ProduitConservation(
            nom = "Jus, sauces, compotes, purées en brique ou bocal stérilisé",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.DDM,
            repereReglementaire = "Produits non secs portant une DDM : une fois ouverts, ils doivent être " +
                "conservés au frais et consommés très rapidement (INC).",
            apresOuverture = "Au réfrigérateur, à consommer dans les 3 jours.",
        ),
        ProduitConservation(
            nom = "Produits surgelés",
            rayon = Rayon.SURGELES,
            famille = FamilleDate.DDM,
            repereReglementaire = "Les viandes congelées, préparations de viandes congelées et produits de la " +
                "pêche congelés portent en plus la date de congélation (annexe III du règlement INCO).",
            conservation = "Congélateur à -18 °C, jamais décongelés puis remis au froid.",
            apresOuverture = "Ne pas recongeler un produit décongelé ; consommer un produit décongelé dans les 3 jours (Anses).",
            vigilance = "Un produit décongelé qui a coulé ou changé de couleur ne se recongèle pas.",
        ),

        // ---------------------------------------------------------- Sans date
        ProduitConservation(
            nom = "Fruits et légumes frais entiers",
            rayon = Rayon.FRUITS_LEGUMES,
            famille = FamilleDate.AUCUNE,
            repereReglementaire = "Aucune date obligatoire tant qu'ils ne sont ni épluchés, ni coupés " +
                "(annexe X du règlement INCO).",
            conservation = "C'est l'aspect et le toucher qui décident ; la plupart se gardent mieux au frais, " +
                "sauf tomates, courgettes et pommes de terre, qui préfèrent un endroit frais et sec.",
            vigilance = "Tache molle, moisissure, odeur de fermentation : la partie abîmée s'enlève ou le produit se jette.",
        ),
        ProduitConservation(
            nom = "Légumes frais coupés, prêts à consommer, salades en sachet",
            rayon = Rayon.FRUITS_LEGUMES,
            famille = FamilleDate.DLC,
            repereReglementaire = "Dès qu'un légume est épluché, découpé ou mélangé, il perd l'exemption de date " +
                "et devient une denrée très périssable.",
            conservation = "Réfrigérateur 4 °C.",
            apresOuverture = "À consommer rapidement, dans les 3 jours (recommandation Anses).",
        ),
        ProduitConservation(
            nom = "Vin, vins mousseux, alcools à 10 % et plus",
            rayon = Rayon.BOISSONS,
            famille = FamilleDate.AUCUNE,
            repereReglementaire = "Dispensés de DDM par l'annexe X du règlement INCO.",
            conservation = "Bouteille fermée, à l'abri de la lumière et des variations de température.",
            apresOuverture = "Quelques jours au frais et bien bouché pour un vin ; des mois pour un alcool fort.",
        ),
        ProduitConservation(
            nom = "Sel, sucre solide, vinaigre",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.AUCUNE,
            repereReglementaire = "Aucune date obligatoire (annexe X du règlement INCO) ; l'inscription d'une DDM " +
                "y est même interdite depuis la loi n° 2015-992.",
            conservation = "Au sec ; le sel et le sucre peuvent durcir mais restent utilisables.",
        ),
        ProduitConservation(
            nom = "Pain, viennoiseries, pâtisseries fraîches",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.AUCUNE,
            repereReglementaire = "Dispensés de date car normalement consommés dans les 24 heures après fabrication " +
                "(annexe X du règlement INCO).",
            conservation = "Sac en papier à température ambiante ; congélation dès le premier jour pour prolonger.",
            vigilance = "Moisissure : ne pas se contenter de retirer la partie verte sur du pain moelleux.",
        ),
        ProduitConservation(
            nom = "Confiseries et chewing-gums",
            rayon = Rayon.EPICERIE,
            famille = FamilleDate.AUCUNE,
            repereReglementaire = "Dispensés de DDM (annexe X du règlement INCO).",
            conservation = "Au sec, à l'abri de l'humidité.",
        ),
    )

    /** Les produits d'une famille, dans l'ordre du catalogue. */
    fun parFamille(famille: FamilleDate): List<ProduitConservation> =
        produits.filter { it.famille == famille }

    /** Ceux qui portent une DLC : la seule famille où dépasser la date fait courir un risque. */
    val dateImperative: List<ProduitConservation> get() = parFamille(FamilleDate.DLC)

    /** Tout le reste : DDM puis denrées sans date obligatoire. */
    val autresDates: List<ProduitConservation>
        get() = parFamille(FamilleDate.DDM) + parFamille(FamilleDate.AUCUNE)

    fun parRayon(rayon: Rayon): List<ProduitConservation> = produits.filter { it.rayon == rayon }

    /** Recherche libre sur le nom, le rayon, la mention et la consigne de conservation. */
    fun chercher(texte: String): List<ProduitConservation> {
        val aiguille = texte.trim()
        if (aiguille.isEmpty()) return produits
        return produits.filter { it.texteRecherche.contains(aiguille, ignoreCase = true) }
    }

    /** Les rappels qui valent pour tout le catalogue. */
    val rappels: List<String> = listOf(
        "La date imprimée sur l'emballage prime toujours sur les repères affichés ici.",
        "Réfrigérateur à 4 °C dans la zone la plus froide ; congélateur à -18 °C.",
        "Une DLC n'est valable que si la température de conservation de l'emballage a été respectée.",
        "Aliment sensible entamé : 3 jours au froid au maximum (Anses).",
        "Un emballage gonflé, une odeur anormale ou une couleur douteuse : on jette, même avant la date.",
    )

    /** Sources citées dans l'écran, pour ne pas avoir à les deviner. */
    val sources: List<String> = listOf(
        "Règlement (UE) n° 1169/2011 (INCO), art. 24 et annexe X — DLC, DDM et denrées sans date",
        "Règlement (CE) n° 589/2008 — date de consommation recommandée des œufs",
        "Décret n° 2022-1440 du 17 novembre 2022 — mention complémentaire à la DDM",
        "Anses — conservation des aliments et respect de la chaîne du froid",
        "Ministère de l'Agriculture — bonnes pratiques de la congélation",
        "Institut national de la consommation — durée de vie des denrées alimentaires",
    )
}
