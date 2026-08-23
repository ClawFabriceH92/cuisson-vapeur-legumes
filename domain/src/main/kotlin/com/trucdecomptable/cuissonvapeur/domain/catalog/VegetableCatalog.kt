package com.trucdecomptable.cuissonvapeur.domain.catalog

import com.trucdecomptable.cuissonvapeur.domain.model.ALL_YEAR
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.ANTIOXYDANTS
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.FIBRES
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.HYDRATATION
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.PROTEINES
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.VITAMINE_C
import com.trucdecomptable.cuissonvapeur.domain.model.Season.AUTOMNE
import com.trucdecomptable.cuissonvapeur.domain.model.Season.ETE
import com.trucdecomptable.cuissonvapeur.domain.model.Season.HIVER
import com.trucdecomptable.cuissonvapeur.domain.model.Season.PRINTEMPS
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * The 28-vegetable catalog (EF-01), copied **exactly** from the spec's
 * Annexe A: same names, ranges, engine durations, benefits, categories,
 * seasons and kcal/100g as the source web page. Do not edit these values
 * without validation from Fabrice Heuvrard (see Annexe A, NB 3).
 *
 * Two things are *not* pinned down by the spec text and were judgment calls
 * for this port (documented in the root README):
 *  - [Vegetable.id]: a stable ascii slug, needed for Room/nav keys, absent
 *    from the spec (which only has display names).
 *  - [Vegetable.emoji]: standard Unicode has no dedicated emoji for several
 *    of these vegetables (turnip, parsnip, leek, artichoke, cauliflower...);
 *    a reasonable close/generic emoji was picked and duplicates happen.
 *
 * Since 23/08/2026 (Fabrice's request): each row also carries [Vegetable.origin]
 * (short editorial origin/history) and [Vegetable.funFacts] (cultural facts),
 * shown in the detail screen under "Origine" and "Le saviez-vous ?".
 */
object VegetableCatalog {

    val vegetables: List<Vegetable> = listOf(
        Vegetable(
            id = "courgettes",
            name = "Courgettes",
            displayedRange = "5-7 min",
            durationMinutes = 7,
            benefits = listOf("Hydratation", "Faible en calories"),
            category = HYDRATATION,
            seasons = setOf(ETE),
            kcalPer100g = 17,
            emoji = "🥒", // 🥒
            origin = "La courgette descend des courges cultivées en Mésoamérique il y a plus de 7 000 ans. La forme allongée que l'on connaît aujourd'hui a été sélectionnée en Italie au XIXe siècle.",
            funFacts = listOf(
                "C'est une courge cueillie immature : laissée sur pied, elle devient une grosse courge.",
                "Ses fleurs sont comestibles et se dégustent frites ou farcies (fiori di zucca en Italie).",
                "Le record de la plus grosse courgette dépasse les 65 kg.",
            ),
        ),
        Vegetable(
            id = "carottes",
            name = "Carottes",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Bêta-carotène", "Fibres"),
            category = FIBRES,
            seasons = ALL_YEAR,
            kcalPer100g = 41,
            emoji = "🥕", // 🥕
            origin = "Originaire d'Afghanistan et de Perse, la carotte y était violette, blanche ou jaune. La carotte orange s'est imposée aux Pays-Bas à partir du XVIIe siècle.",
            funFacts = listOf(
                "L'orange vient du bêta-carotène, que le corps transforme en vitamine A.",
                "Les carottes violettes existent toujours et sont revenues sur les marchés.",
                "À l'origine, on cultivait surtout la carotte pour ses graines et ses feuilles aromatiques.",
            ),
        ),
        Vegetable(
            id = "haricots_verts",
            name = "Haricots verts",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Fibres", "Vitamine A"),
            category = FIBRES,
            seasons = setOf(ETE),
            kcalPer100g = 31,
            emoji = "🫘", // 🫘
            origin = "Le haricot est originaire d'Amérique (Mésoamérique et Andes), où il est cultivé depuis environ 7 000 ans. Les conquistadors l'ont rapporté en Europe au XVIe siècle.",
            funFacts = listOf(
                "Le haricot vert est la gousse immature de la plante : les grains sont les haricots secs.",
                "C'est une légumineuse : ses racines fixent l'azote de l'air et fertilisent le sol.",
                "Il en existe des centaines de variétés, dont les fameux haricots beurre jaunes.",
            ),
        ),
        Vegetable(
            id = "petits_pois",
            name = "Petits pois",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Protéines végétales", "Fibres"),
            category = PROTEINES,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 84,
            emoji = "🫛", // 🫛
            origin = "L'une des plus anciennes légumineuses cultivées : originaire du Proche-Orient (Croissant fertile), le pois est cultivé depuis près de 10 000 ans.",
            funFacts = listOf(
                "Le moine Gregor Mendel a découvert les lois de la génétique en croisant des petits pois (1865).",
                "Les « petits pois » sont des pois récoltés immatures, avant que l'amidon ne durcisse.",
                "Comme toutes les légumineuses, ils enrichissent le sol en azote.",
            ),
        ),
        Vegetable(
            id = "pommes_de_terre",
            name = "Pommes de terre",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Potassium", "Glucides"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 77,
            emoji = "🥔", // 🥔
            origin = "Domestiquée dans les Andes (Pérou, Bolivie) il y a environ 8 000 ans, elle est arrivée en Europe au XVIe siècle. Parmentier la popularise en France au XVIIIe siècle.",
            funFacts = listOf(
                "Ce n'est pas une racine mais un tubercule : une tige souterraine gonflée de réserves.",
                "Plus de 4 000 variétés sont cultivées dans les Andes.",
                "C'est la 4e culture vivrière mondiale, après le riz, le blé et le maïs.",
            ),
        ),
        Vegetable(
            id = "brocoli",
            name = "Brocoli",
            displayedRange = "7-10 min",
            durationMinutes = 10,
            benefits = listOf("Vitamine C & K", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 34,
            emoji = "🥦", // 🥦
            origin = "Le brocoli est né en Italie, dans la région de Calabre, et était déjà cultivé à l'époque romaine — Pline l'Ancien le décrit au Ier siècle.",
            funFacts = listOf(
                "Le brocoli est un chou (Brassica oleracea) : on mange ses boutons floraux immatures.",
                "Chou-fleur, chou de Bruxelles, chou kale et chou pommé sont de la même espèce botanique.",
                "Le mot italien « broccolo » signifie « petite pousse ».",
            ),
        ),
        Vegetable(
            id = "chou_fleur",
            name = "Chou-fleur",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Antioxydants", "Vitamine C"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 25,
            emoji = "🥦", // 🥦 (no dedicated cauliflower emoji, see class doc)
            origin = "Originaire du bassin méditerranéen oriental, le chou-fleur a été amélioré en Italie au Moyen Âge avant de conquérir l'Europe.",
            funFacts = listOf(
                "La partie blanche que l'on mange est une inflorescence hypertrophiée : des milliers de fleurs avortées.",
                "Il existe des choux-fleurs verts, violets et même orange, aux pigments antioxydants.",
                "Le romanesco, à la forme fractale, est une variété proche.",
            ),
        ),
        Vegetable(
            id = "asperges",
            name = "Asperges",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Folates", "Diurétique"),
            category = null,
            seasons = setOf(PRINTEMPS),
            kcalPer100g = 20,
            emoji = "🌱", // 🌱
            origin = "Appréciée des Égyptiens, des Grecs et des Romains, l'asperge est cultivée en Méditerranée orientale depuis l'Antiquité — un légume de luxe déjà à l'époque.",
            funFacts = listOf(
                "L'asperge blanche est une asperge verte cultivée sous terre, privée de lumière.",
                "Un turion d'asperge peut pousser de 10 cm par jour au printemps.",
                "Les Romains l'exportaient déjà à dos de mule, emballée dans la neige.",
            ),
        ),
        Vegetable(
            id = "choux_de_bruxelles",
            name = "Choux de Bruxelles",
            displayedRange = "10-12 min",
            durationMinutes = 12,
            benefits = listOf("Vitamine C & K"),
            category = VITAMINE_C,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 43,
            emoji = "🍃", // 🍃
            origin = "Comme son nom l'indique, il a été popularisé dans la région de Bruxelles (Belgique) à partir du XVIe siècle, où il pousse le long de la côte flamande.",
            funFacts = listOf(
                "Les petits « choux » sont des bourgeons qui poussent le long d'une tige pouvant en porter 20 à 40.",
                "Le gel améliore leur goût en transformant l'amidon en sucres.",
                "Il appartient à la même espèce que le chou pommé et le brocoli.",
            ),
        ),
        Vegetable(
            id = "epinards",
            name = "Épinards",
            displayedRange = "2-3 min",
            durationMinutes = 3,
            benefits = listOf("Fer", "Calcium"),
            category = null,
            seasons = setOf(PRINTEMPS, AUTOMNE),
            kcalPer100g = 23,
            emoji = "🥬", // 🥬
            origin = "Originaire de Perse (l'Iran actuel), l'épinard a été introduit en Europe par les Arabes, en Espagne, vers le XIIe siècle.",
            funFacts = listOf(
                "Le mythe de Popeye (1932) a fait bondir la consommation d'épinards aux États-Unis.",
                "Une erreur de virgule dans une étude de 1870 a longtemps fait croire que l'épinard était 10 fois plus riche en fer qu'il ne l'est.",
                "Il contient des oxalates, responsables de sa légère astringence.",
            ),
        ),
        Vegetable(
            id = "poivrons",
            name = "Poivrons",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Vitamine C", "Antioxydants"),
            category = VITAMINE_C,
            seasons = setOf(ETE),
            kcalPer100g = 31,
            emoji = "🫑", // 🫑
            origin = "Cultivé au Mexique et en Amérique centrale depuis environ 6 000 ans, le poivron a été rapporté en Europe par Christophe Colomb et ses successeurs.",
            funFacts = listOf(
                "Le poivron vert est un poivron rouge ou orange cueilli avant maturité.",
                "Poivrons et piments sont la même espèce : Capsicum annuum.",
                "Un poivron rouge contient plus de vitamine C qu'une orange.",
            ),
        ),
        Vegetable(
            id = "fenouil",
            name = "Fenouil",
            displayedRange = "10-12 min",
            durationMinutes = 12,
            benefits = listOf("Digestion", "Fibres"),
            category = null,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 31,
            emoji = "🌿", // 🌿
            origin = "Le fenouil est originaire du bassin méditerranéen, où Grecs et Romains l'utilisaient comme légume et comme plante médicinale.",
            funFacts = listOf(
                "Le « bulbe » de fenouil est en réalité la base charnue des feuilles, pas une racine.",
                "Le mot grec « marathon » désignait un champ de fenouil : le site de la bataille de Marathon en était couvert.",
                "L'anéthole du fenouil parfume l'absinthe et le pastis.",
            ),
        ),
        Vegetable(
            id = "navets",
            name = "Navets",
            displayedRange = "15-18 min",
            durationMinutes = 18,
            benefits = listOf("Fibres", "Vitamine C"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 28,
            emoji = "⚪", // ⚪
            origin = "Le navet est cultivé en Europe et en Asie depuis l'Antiquité : les Grecs et les Romains le consommaient déjà couramment.",
            funFacts = listOf(
                "Avant la pomme de terre, le navet était un aliment de base des Européens.",
                "Le rutabaga est un croisement entre navet et chou.",
                "Ses fanes (feuilles) sont comestibles et riches en vitamines.",
            ),
        ),
        Vegetable(
            id = "panais",
            name = "Panais",
            displayedRange = "15-18 min",
            durationMinutes = 18,
            benefits = listOf("Vitamine C", "Potassium"),
            category = null,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 75,
            emoji = "🟠", // 🟠
            origin = "Le panais est originaire d'Eurasie et fut l'un des légumes racines les plus consommés d'Europe au Moyen Âge, avant d'être supplanté par la pomme de terre.",
            funFacts = listOf(
                "Ressemblant à une carotte blanche, il devient plus sucré après les premières gelées.",
                "Il était cultivé par les Romains, qui l'appréciaient déjà.",
                "Redécouvert par la cuisine moderne, il revient en force sur les marchés.",
            ),
        ),
        Vegetable(
            id = "patate_douce",
            name = "Patate douce",
            displayedRange = "10-15 min",
            durationMinutes = 15,
            benefits = listOf("Bêta-carotène", "Fibres"),
            category = null,
            seasons = setOf(AUTOMNE),
            kcalPer100g = 86,
            emoji = "🍠", // 🍠
            origin = "Domestiquée en Amérique du Sud il y a plus de 5 000 ans, la patate douce a été diffusée par les Polynésiens jusqu'au Pacifique, bien avant Christophe Colomb.",
            funFacts = listOf(
                "Elle n'a rien à voir avec la pomme de terre : c'est une racine tubéreuse de la famille des convolvulacées.",
                "Les variétés orange doivent leur couleur au bêta-carotène.",
                "La NASA l'a testée comme culture pour les missions spatiales longues.",
            ),
        ),
        Vegetable(
            id = "betteraves",
            name = "Betteraves",
            displayedRange = "20-25 min",
            durationMinutes = 25,
            benefits = listOf("Nitrates", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 43,
            emoji = "🔴", // 🔴
            origin = "La betterave descend de la betterave maritime sauvage des côtes méditerranéennes, cultivée depuis l'Antiquité par les Romains.",
            funFacts = listOf(
                "La betterave sucrière a été sélectionnée en Allemagne à la fin du XVIIIe siècle.",
                "Bette et betterave sont la même espèce : la bette est cultivée pour ses feuilles.",
                "Le sucre de betterave représente environ 20 % du sucre produit dans le monde.",
            ),
        ),
        Vegetable(
            id = "chou_kale",
            name = "Chou kale",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Vitamine A, C, K"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 49,
            emoji = "🥬", // 🥬
            origin = "Le kale descend du chou sauvage méditerranéen et fait partie des plus anciens choux, déjà consommé par les Grecs il y a plus de 2 000 ans.",
            funFacts = listOf(
                "Contrairement au chou pommé, le kale ne forme pas de tête : ce sont ses feuilles que l'on récolte.",
                "Il résiste au gel, qui lui donne même un goût plus doux.",
                "Vieux légume paysan devenu « superfood », il a été relancé par la cuisine santé.",
            ),
        ),
        Vegetable(
            id = "radis",
            name = "Radis",
            displayedRange = "2-3 min",
            durationMinutes = 3,
            benefits = listOf("Antioxydants", "Détox"),
            category = ANTIOXYDANTS,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 16,
            emoji = "🌱", // 🌱
            origin = "Le radis est cultivé depuis l'Antiquité en Asie et en Europe : les Égyptiens le consommaient et les Grecs l'offraient en offrande à Apollon.",
            funFacts = listOf(
                "Le radis de 18 jours pousse en 3 à 4 semaines : l'un des légumes les plus rapides du potager.",
                "Le daikon japonais, sa variété géante, peut peser plusieurs kilos.",
                "Les fanes de radis se cuisinent en soupe ou en pesto.",
            ),
        ),
        Vegetable(
            id = "aubergines",
            name = "Aubergines",
            displayedRange = "8-12 min",
            durationMinutes = 12,
            benefits = listOf("Fibres", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(ETE),
            kcalPer100g = 25,
            emoji = "🍆", // 🍆
            origin = "L'aubergine a été domestiquée en Inde il y a environ 4 000 ans et introduite en Europe par les Arabes au Moyen Âge.",
            funFacts = listOf(
                "Comme la tomate, c'est botaniquement une baie.",
                "Sa chair agit comme une éponge : elle absorbe beaucoup d'huile à la cuisson.",
                "Le mot français vient de l'arabe « al-badinjan ».",
            ),
        ),
        Vegetable(
            id = "tomates",
            name = "Tomates",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Lycopène", "Vitamine C"),
            category = ANTIOXYDANTS,
            seasons = setOf(ETE),
            kcalPer100g = 18,
            emoji = "🍅", // 🍅
            origin = "La tomate est originaire des Andes, domestiquée au Mexique, et a été rapportée en Europe par les Espagnols au XVIe siècle.",
            funFacts = listOf(
                "Longtemps suspectée d'être toxique en Europe (c'est une solanacée), elle a d'abord été cultivée comme plante ornementale.",
                "C'est botaniquement un fruit, consommé comme un légume.",
                "Sa couleur rouge vient du lycopène, un puissant antioxydant.",
            ),
        ),
        Vegetable(
            id = "oignons",
            name = "Oignons",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Quercétine", "Soufre"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 40,
            emoji = "🧅", // 🧅
            origin = "L'oignon est cultivé depuis plus de 5 000 ans en Asie centrale et en Perse ; les Égyptiens en déposaient dans les tombes des pharaons.",
            funFacts = listOf(
                "C'est l'un des légumes les plus cultivés au monde, présent sur tous les continents.",
                "Les larmes viennent d'un gaz irritant, le syn-propanethial-S-oxyde, libéré à la découpe.",
                "L'oignon égyptien était un symbole de l'éternité, par ses couches concentriques.",
            ),
        ),
        Vegetable(
            id = "ail",
            name = "Ail",
            displayedRange = "5-7 min",
            durationMinutes = 7,
            benefits = listOf("Allicine", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = ALL_YEAR,
            kcalPer100g = 149,
            emoji = "🧄", // 🧄
            origin = "L'ail est originaire d'Asie centrale (Kirghizistan) et cultivé depuis plus de 5 000 ans, notamment en Égypte ancienne et en Chine.",
            funFacts = listOf(
                "Les bâtisseurs de pyramides en recevaient une ration quotidienne pour la force.",
                "L'allicine, son composé soufré, n'est libérée que lorsque la gousse est écrasée ou coupée.",
                "La Chine produit environ les trois quarts de l'ail mondial.",
            ),
        ),
        Vegetable(
            id = "poireaux",
            name = "Poireaux",
            displayedRange = "8-12 min",
            durationMinutes = 12,
            benefits = listOf("Vitamine K", "Fibres"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 61,
            emoji = "🟢", // 🟢
            origin = "Le poireau est cultivé en Méditerranée depuis l'Antiquité : les Romains l'appréciaient, et l'empereur Néron en mangeait pour sa voix.",
            funFacts = listOf(
                "Le poireau est l'emblème national du Pays de Galles.",
                "Le blanc du poireau se développe grâce au buttage : on enterre le pied pour le blanchir.",
                "En cuisine médiévale française, il était appelé le « légume du pauvre ».",
            ),
        ),
        Vegetable(
            id = "celeri",
            name = "Céleri",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Vitamine K", "Hydratation"),
            category = HYDRATATION,
            seasons = ALL_YEAR,
            kcalPer100g = 16,
            emoji = "🥬", // 🥬
            origin = "Le céleri pousse à l'état sauvage autour de la Méditerranée ; les Grecs en couronnaient les vainqueurs des Jeux de Némée.",
            funFacts = listOf(
                "Le céleri branche moderne a été sélectionné en Italie et en France au XVIIIe siècle.",
                "Le céleri-rave est une autre forme de la même espèce, cultivée pour sa racine.",
                "Ses feuilles aromatiques parfument les bouillons et les sauces.",
            ),
        ),
        Vegetable(
            id = "endives",
            name = "Endives",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Vitamine B9", "Fibres"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 17,
            emoji = "🍃", // 🍃
            origin = "L'endive, ou « chicon », a été découverte par hasard en Belgique vers 1850 par un cultivateur bruxellois qui avait laissé des racines de chicorée dans une cave obscure.",
            funFacts = listOf(
                "Elle naît du forçage de racines de chicorée, à l'obscurité totale et au chaud.",
                "La Belgique et le Nord de la France en sont les principaux producteurs.",
                "Sa légère amertume vient de l'intybine, un composé de la chicorée.",
            ),
        ),
        Vegetable(
            id = "artichauts",
            name = "Artichauts",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Cynarine", "Fibres"),
            category = FIBRES,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 47,
            emoji = "🌿", // 🌿
            origin = "L'artichaut descend du cardon, une plante sauvage du bassin méditerranéen. Catherine de Médicis l'aurait introduit en France au XVIe siècle.",
            funFacts = listOf(
                "On mange l'inflorescence immature : le « fond » et la base des feuilles (les bractées).",
                "Si on le laisse fleurir, l'artichaut produit un superbe chardon bleu-violet.",
                "La cynarine, qui lui donne son amertume, stimule la digestion du foie.",
            ),
        ),
        Vegetable(
            id = "champignons",
            name = "Champignons",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Vitamine D", "Sélénium"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 22,
            emoji = "🍄", // 🍄
            origin = "Le champignon de Paris a été cultivé en France à partir du XVIIe siècle, puis dans les carrières souterraines de la région parisienne au XIXe siècle.",
            funFacts = listOf(
                "Ce n'est pas une plante : le champignon appartient à son propre règne, le règne fongique.",
                "Le champignon que l'on voit est le « fruit » d'un mycélium souterrain.",
                "Les carrières de Paris (anciennes) ont servi de champignonnières dès les années 1810.",
            ),
        ),
        Vegetable(
            id = "mais",
            name = "Maïs",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Lutéine", "Fibres"),
            category = FIBRES,
            seasons = setOf(ETE),
            kcalPer100g = 86,
            emoji = "🌽", // 🌽
            origin = "Le maïs a été domestiqué au Mexique il y a environ 9 000 ans à partir de la téosinte, une herbe sauvage. Les peuples mésoaméricains en ont fait leur céréale sacrée.",
            funFacts = listOf(
                "C'est une céréale, mais le maïs doux se consomme comme un légume.",
                "Il ne peut pas se reproduire sans l'homme : ses grains restent prisonniers des épis.",
                "C'est la céréale la plus produite au monde, devant le blé et le riz.",
            ),
        ),
    )
}
