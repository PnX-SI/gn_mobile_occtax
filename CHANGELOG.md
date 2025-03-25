# Changelog

## [2.8.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.8.0-rc0) (2025-03-25, pre-release)

### 🚀 Nouveautés

* Refonte des champs additionnels (https://github.com/PnX-SI/gn_mobile_occtax/issues/122, https://github.com/PnX-SI/gn_mobile_occtax/issues/285).
* Petites améliorations autour du module cartographique, notamment sur le chargement des couches
  vectorielles (https://github.com/PnX-SI/gn_mobile_occtax/issues/170) et d'une refonte du
  gestionnaire des couches géographiques (https://github.com/PnX-SI/gn_mobile_occtax/issues/193).
* Au niveau de la saisie du relevé, la saisie de la position se fait maintenant à la première étape
  (avant la saisie du choix des observateurs, du jeu de données, etc.) (https://github.com/PnX-SI/gn_mobile_occtax/issues/288).
* Le champ `STATUT_OBS` issu de la nomenclature, fait maintenant partie des champs éditables dans la
  partie "Informations" (https://github.com/PnX-SI/gn_mobile_occtax/issues/66). Par défaut, il est
  non visible (donc non éditable par défaut), non obligatoire et prend la valeur par défaut remontée
  par l'API (`GET -> /api/occtax/defaultNomenclatures`). On peut tout à fait le rendre visible (donc
  éditable) via le paramétrage de la nomenclature.
* Légère refonte de la gestion des taxons dans le relevé afin de pouvoir ajouter plusieurs même
  occurrences de taxon (https://github.com/PnX-SI/gn_mobile_occtax/issues/182).

### 🐛 Corrections

* Correction autour de la gestion de la date de build de l'application (https://github.com/PnX-SI/gn_mobile_occtax/issues/275).
* Correction pour déterminer l'orientation des images ou des photos prises dans les dénombrements
  des taxons du relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/278).

### ⚠️ Notes de version

* Code de version : **3323**
* Cette version nécessite la version [1.14.2 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.4) (2026-05-06, release)

### 🐛 Corrections
* Bascule sur le nouveau certificat de signature pour le build de l'APK au format v3 et abandon de
  la rotation des certificats qui embarquait l'ancien (https://github.com/PnX-SI/gn_mobile_occtax/issues/309).

### ⚠️ Notes de version

* Code de version : **3326**
* ⚠️ Cette version n'assure plus de compatibilité au niveau des signatures avec les versions
  précédentes inférieures à la 2.7.0. Par exemple, mettre à jour une ancienne version comme la
  version 2.6.0 directement à cette version ne fonctionnera pas (conflit au niveau des signatures).
  Il faudra à minima passer à la version 2.7.0 puis à cette version.
* Cette version nécessite la version [1.14.2 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.3) (2026-01-07, release)

### 🐛 Corrections

* Prise en compte de la date et l'heure locale selon le fuseau horaire du terminal dans la gestion
  des relevés (https://github.com/PnX-SI/gn_mobile_occtax/issues/304).

### ⚠️ Notes de version

* Code de version : **3324**
* Cette version nécessite la version [1.14.2 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.2) (2025-08-29, release)

### 🐛 Corrections

* Correction de la construction du nom de fichier lors de l'import de médias depuis la galerie,
  non-fonctionnelle sur certains modèles de téléphones Xiaomi (https://github.com/PnX-SI/gn_mobile_occtax/issues/268)

### ⚠️ Notes de version

* Code de version : **3322**
* Cette version nécessite la version [1.14.2 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.1) (2024-11-17, release)

### 🚀 Nouveautés

* Support d'Android 14 (API 34).
* Ajout d'un paramètre `shown_by_default` coté [module cartographique](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps#layer-properties)
  permettant d'afficher par défaut ou non les couches vectorielles. Par défaut, l'ensemble des
  couches vectorielles déclarées sont affichées par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/262).
* Support des nouvelles URL des fonds IGN en ligne (https://github.com/PnX-SI/gn_mobile_occtax/issues/267).
* Possibilité de surcharger l'identifiant de l'application (https://github.com/PnX-SI/gn_mobile_occtax/issues/264).

### 🐛 Corrections

* Correction d'une erreur lors de la synchronisation des champs additionnels, certains champs
  additionnels peuvent être rattachés à des jeux de données non présents lors de la synchronisation.
  Ces champs additionnels sont donc tout simplement ignorés lors de la synchronisation des données (https://github.com/PnX-SI/gn_mobile_occtax/issues/269).

### ⚠️ Notes de version

* Code de version : **3320**
* Cette version nécessite la version [1.14.2 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.1-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.1-rc1) (2024-11-16, pre-release)

### 🐛 Corrections

* Erreur lors de la synchronisation des champs additionnels, certains champs additionnels peuvent
  être rattachés à des jeux de données non présents lors de la synchronisation. Ces champs
  additionnels sont donc tout simplement ignorés lors de la synchronisation des données (https://github.com/PnX-SI/gn_mobile_occtax/issues/269).

### ⚠️ Notes de version

* Code de version : **3319**
* Cette version nécessite la version [1.13.1 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.1-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.1-rc0) (2024-10-21, pre-release)

### 🚀 Nouveautés

* Support Android 14 (API 34).

### 🐛 Corrections

* Pouvoir surcharger l'identifiant de l'application (https://github.com/PnX-SI/gn_mobile_occtax/issues/264).
* Ajout d'un paramètre `shown_by_default` coté [module cartographique](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps#layer-properties)
  permettant d'afficher par défaut ou non les couches vectorielles. Par défaut, l'ensemble des
  couches vectorielles déclarées sont affichées par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/262).
* Support des nouveaux fonds IGN (https://github.com/PnX-SI/gn_mobile_occtax/issues/267). 

### ⚠️ Notes de version

* Code de version : **3313**
* Cette version nécessite la version [1.13.1 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0) (2024-07-13, release)

### 🚀 Nouveautés

* Nouveau mécanisme de synchronisation des taxons. On synchronise désormais tout _Taxref_ mais seulement
  quand le champ `taxonomie.t_meta_taxref.update_date` indique que celui-ci a été mis à jour depuis 
  la dernière synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).
* Suppression de contrôles additionnels lors de la synchronisation des taxons, permettant d'obtenir
  un gain non négligeable sur le temps de traitement (https://github.com/PnX-SI/gn_mobile_occtax/issues/247).
* La liste des taxons est automatiquement filtrée selon le jeu de données sélectionné. Si ce dernier
  n'est lié à aucune liste de taxons, la liste des taxons est filtrée selon la valeur du paramètre
  `taxa_list_id`. Si ce paramètre n'est pas renseigné et que le jeu de données n'est lié à aucune 
  liste de taxons, la liste des taxons ne sera pas filtrée et renvoie donc tout _Taxref_.
  Si l'identifiant de liste associé à un JDD est égal à `-1`, on n'applique aucun filtre sur les
  identifiants de liste même si le paramètre `taxa_list_id` est défini (https://github.com/PnX-SI/gn_mobile_core/issues/41, https://github.com/PnX-SI/gn_mobile_occtax/issues/133).
* Utilisation de la nouvelle API pour la récupération des jeux de données en fonction des permissions
  de l'utilisateur authentifié (https://github.com/PnX-SI/gn_mobile_occtax/issues/239).
* Possibilité d'afficher les relevés non synchronisés sur une carte en plus de la vue sous forme de
  liste (https://github.com/PnX-SI/gn_mobile_occtax/issues/224).
* Possibilité de charger une couche cartographique locale non spécifiée dans la configuration (https://github.com/PnX-SI/gn_mobile_occtax/issues/170).
* Support des champs additionnels sur les taxons et les dénombrements, désactivé par défaut avec le
  nouveau paramètre `additional_fields` (https://github.com/PnX-SI/gn_mobile_occtax/issues/122).
* Possibilité de surcharger localement la configuration (https://github.com/PnX-SI/gn_mobile_occtax/issues/95).
* Meilleure gestion des photos prises en haute résolution. Par défaut, les photos prises sont
  compressées avec une qualité de 80% et ne dépassent pas 2048px en largeur ou en hauteur selon son
  orientation (portrait ou paysage) (https://github.com/PnX-SI/gn_mobile_occtax/issues/254).
* Meilleur affichage des photos qu'elles soient prises en mode portrait ou en mode paysage (https://github.com/PnX-SI/gn_mobile_occtax/issues/84).
* Les identifiants des objets géographiques dans les couches géographiques vectorielles deviennent
  optionnels (https://github.com/PnX-SI/gn_mobile_maps/issues/11, https://github.com/PnX-SI/gn_mobile_occtax/issues/175).
* Synchronisation périodique des identifiants de liste rattachés aux taxons (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).
* La création d'un nouveau relevé est désactivée tant qu'une première synchronisation complète n'a
  pas été terminée.

### 🐛 Corrections

* Prise en compte des caractères spéciaux lors de la recherche par nom depuis la liste des taxons (https://github.com/PnX-SI/gn_mobile_occtax/issues/230).
* Prise en compte de la date et heure locale du terminal dans la gestion des relevés (https://github.com/PnX-SI/gn_mobile_occtax/issues/225).
* Correction de la synchronisation des données si le champ `nom_valide` n'est pas renseigné coté
  _Taxref_ (https://github.com/PnX-SI/gn_mobile_occtax/issues/233).
* Correction de la gestion des valeurs numériques nulles dans la base de données
* Petites corrections ergonomiques
* La synchronisation des taxons interroge désormais l'API de TaxHub paginée avec un paramètre
  d'ordre pour être certain de récupérer tous les taxons. Idem pour la récupération des couleurs de
  taxons par unité géographique.
* Le prénom de l'utilisateur connecté peut être non défini (https://github.com/PnX-SI/gn_mobile_occtax/issues/258).
* Récupération des couleurs de taxons seulement si le paramètre `code_area_type` est défini (https://github.com/PnX-SI/gn_mobile_occtax/issues/232, https://github.com/PnX-SI/gn_mobile_occtax/issues/252)
* Suppression du suffixe du nom de l'application lors du build de l'APK (https://github.com/PnX-SI/gn_mobile_occtax/issues/134).
* Gestion des signatures lors du build de l'APK au format v3 pour y inclure par rotation des
  certificats, avec un certificat plus officiel que celui actuellement utilisé par défaut.
  À terme, ce nouveau certificat remplacera l'actuel. (https://github.com/PnX-SI/gn_mobile_occtax/issues/244, https://github.com/PnX-SI/gn_mobile_occtax/issues/134).

### ⚠️ Notes de version

* Code de version : **3310**
* Cette version nécessite la version [1.13.1 minimum de TaxHub](https://github.com/PnX-SI/TaxHub/releases/)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature.

## [2.7.0-rc9](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc9) (2024-07-07, pre-release)

### 🐛 Corrections

* Le prénom de l'utilisateur connecté peut être non défini (https://github.com/PnX-SI/gn_mobile_occtax/issues/258).
* Le paramètre `taxa_list_id` peut être non défini (-1 par défaut) (https://github.com/PnX-SI/gn_mobile_core/issues/41).
* La base des taxons peut être incomplète suite à une synchronisation complète lors des appels en
  cascade selon les données paginées remontées par l'API, car l'ordre n'est pas garanti sur les
  données paginées pouvant donc créer des doublons entre deux appels. La solution de contournement
  est d'appliquer systématiquement un tri par défaut sur les identifiants des taxons.

### ⚠️ Notes de version

* Code de version : **3301**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14.1 de GeoNature (pas encore disponible).

## [2.7.0-rc8](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc8) (2024-05-19, pre-release)

### 🐛 Corrections

* Meilleure gestion des photos prises en haute résolution (https://github.com/PnX-SI/gn_mobile_occtax/issues/254) :
  * Par défaut, les photos prises sont compressées avec une qualité de 80% et ne dépassent pas
    2048px en largeur ou en hauteur selon son orientation (portrait ou paysage).
* Gestion des signatures lors du build de l'APK au format v3 pour y inclure par rotation des
  certificats un certificat plus officiel que celui actuellement utilisé par défaut. À terme, ce
  nouveau certificat remplacera l'actuel. (https://github.com/PnX-SI/gn_mobile_occtax/issues/244, https://github.com/PnX-SI/gn_mobile_occtax/issues/134).
* Les identifiants des objets géographiques dans les couches géographiques vectorielles deviennent
  optionnels (https://github.com/PnX-SI/gn_mobile_maps/issues/11, https://github.com/PnX-SI/gn_mobile_occtax/issues/175).
* Synchronisation périodique des identifiants de liste rattachés aux taxons (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).
* Série de corrections autour de la gestion des champs additionnels (https://github.com/PnX-SI/gn_mobile_occtax/issues/122).
  En vrac :
  * Synchronisation des relevés avec ou sans champs additionnels
  * Filtrage des champs additionnels selon le jeu de données sélectionné
  * Gestion des valeurs décimales sur les champs additionnels de type `number`
* Meilleur affichage des photos qu'elles soient prises en mode portrait ou en mode paysage (https://github.com/PnX-SI/gn_mobile_occtax/issues/84).
* Affichage des taxons filtrés selon l'identifiant de liste provenant du jeu de données (https://github.com/PnX-SI/gn_mobile_core/issues/41) :
  * Un jeu de données peut définir un identifiant de liste et celle-ci peut être `null` ou égale à `-1`
  * Si l'identifiant de liste est non défini, on prend la valeur par défaut venant du paramétrage de
    l'application (`sync.taxa_list_id`) pour filtrer la liste des taxons
  * Si l'identifiant de liste est égale à `-1`, on n'applique aucun filtre sur les identifiants de
    liste même si c'est configuré coté paramétrage

### ⚠️ Notes de version

* Code de version : **3299**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14.1 de GeoNature (pas encore disponible).

## [2.7.0-rc7](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc7) (2024-03-05, pre-release)

### 🐛 Corrections

* Suppression de contrôles additionnels lors de la synchronisation des taxons ce qui permet
  d'obtenir un gain non négligeable sur le temps de traitement (https://github.com/PnX-SI/gn_mobile_occtax/issues/247).
* Petites corrections d'ordre ergonomique.

### ⚠️ Notes de version

* Code de version : **3271**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature (pas encore disponible).

## [2.7.0-rc6](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc6) (2024-02-14, pre-release)

### 🐛 Corrections

* Les valeurs numériques nulles en base n'étaient pas correctement interprétées depuis leurs
  lectures via un cursor

### ⚠️ Notes de version

* Code de version : **3259**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature (pas encore disponible).

## [2.7.0-rc5](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc5) (2024-01-10, pre-release)

### 🐛 Corrections

* Erreur entraînant un plantage de l'application lors du chargement du jeu de données par défaut
  suite aux changements des URIs du fournisseur de données sur la partie jeu de données.

### ⚠️ Notes de version

* Code de version : **3257**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature (pas encore disponible).

## [2.7.0-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc4) (2023-12-29, pre-release)

### 🐛 Corrections

* Utilisation de la nouvelle API pour la récupération des jeux de données (https://github.com/PnX-SI/gn_mobile_occtax/issues/239).

### ⚠️ Notes de version

* Code de version : **3253**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature (pas encore disponible).

## [2.7.0-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc3) (2023-12-05, pre-release)

### 🚀 Nouveautés

* La liste des taxons est automatiquement filtrée selon le jeu de données sélectionné. Si ce dernier
  n'est lié à aucune liste de taxons, la liste des taxons est filtrée selon la valeur du paramètre
  `taxa_list_id`. Si ce paramètre n'est pas renseigné et que le jeu de donnée n'est lié à aucune 
  liste de taxons, la liste des taxons ne sera pas filtrée (https://github.com/PnX-SI/gn_mobile_core/issues/41, https://github.com/PnX-SI/gn_mobile_occtax/issues/133).

### ⚠️ Notes de version

* Code de version : **3251**
* Cette version nécessite la version [1.13.1 de TaxHub](https://github.com/PnX-SI/TaxHub/releases/tag/1.13.1)
  qui elle-même nécessite au minimum la version 2.14 de GeoNature (pas encore disponible).

## [2.7.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc2) (2023-10-14, pre-release)

### 🚀 Nouveautés

* Ne pas synchroniser les taxons à chaque synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).

### 🐛 Corrections

* Erreur transparente lors de la synchronisation des données si le champ 'nom_valide' est non
  renseigné coté _Taxref_ (https://github.com/PnX-SI/gn_mobile_occtax/issues/233).

### ⚠️ Notes de version

* Code de version : **3229**

## [2.7.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc1) (2023-09-13, pre-release)

### 🐛 Corrections

* Prise en compte du paramètre 'additional_fields' lors de la synchronisation des données concernant
  les champs additionnels (https://github.com/PnX-SI/gn_mobile_occtax/issues/122).

### ⚠️ Notes de version

* Code de version : **3221**

## [2.7.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.7.0-rc0) (2023-09-03, pre-release)

### 🚀 Nouveautés

* Affichage des relevés en cours sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/224)
  en plus de la vue sous forme de liste.
* Possibilité de charger une couche cartographique locale non spécifiée dans la configuration (https://github.com/PnX-SI/gn_mobile_occtax/issues/170).
* Support des champs additionnels, désactivé par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/122).
* Possibilité de surcharger la configuration (https://github.com/PnX-SI/gn_mobile_occtax/issues/95).

### 🐛 Corrections

* Prise en compte des caractères spéciaux lors de la recherche par nom depuis la liste des taxons (https://github.com/PnX-SI/gn_mobile_occtax/issues/230).
* Prise en compte de la date et l'heure locale du terminal dans la gestion des relevés (https://github.com/PnX-SI/gn_mobile_occtax/issues/225).
* Suppression du suffix du nom de l'application lors du build de l'APK (https://github.com/PnX-SI/gn_mobile_occtax/issues/134).

### ⚠️ Notes de version

* Code de version : **3217**

## [2.6.2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.2) (2024-03-02, release)

### 🐛 Corrections

* Ignorer les attributs inconnus dans le fichier de configuration (https://github.com/PnX-SI/gn_mobile_occtax/issues/248).

### ⚠️ Notes de version

* Code de version : **3220**

## [2.6.1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.1) (2023-05-24, release)

### 🐛 Corrections

* Correction sur l'affichage des valeurs de nomenclature en doublon (#223).
* Prise en compte de la configuration par défaut de la périodicité de la synchronisation des données.
* Relance automatique de la synchronisation des données si la date de la dernière effectuée est trop
  ancienne suite à un changement de configuration coté fichier de paramétrage.
* Corrections visuelles diverses, notamment sur l'écran d'accueil.

### ⚠️ Notes de version

* Code de version : **3210**

## [2.6.1-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.1-rc1) (2023-05-16, pre-release)

### 🐛 Corrections

* Correction sur l'affichage des valeurs de nomenclature en doublon (#223).

### ⚠️ Notes de version

* Code de version : **3209**

## [2.6.1-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.1-rc0) (2023-05-13, pre-release)

### 🐛 Corrections

* Prise en compte de la configuration par défaut de la périodicité de la synchronisation des données.
* Relance automatique de la synchronisation des données si la date de la dernière effectuée est trop
  ancienne suite à un changement de configuration coté fichier de paramétrage.

### ⚠️ Notes de version

* Code de version : **3203**

## [2.6.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.0) (2023-05-08, release)

### 🚀 Nouveautés

* Support Android 13 (API 33).
* Support des fonds Géoportail (https://github.com/PnX-SI/gn_mobile_maps/issues/8).
  Le module ["maps"](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps) supporte
  officiellement les fonds suivants :
  * [Géoportail WMTS](https://www.geoportail.gouv.fr)
  * [OpenTopoMap](https://www.opentopomap.org)
  * [OpenStreetMap](https://www.openstreetmap.org)
  * [Wikimedia Maps](https://maps.wikimedia.org)
* Gestion automatique des attributions sur les fonds en ligne (https://github.com/PnX-SI/gn_mobile_occtax/issues/191).
  L'attribution est définie automatiquement selon la nature de la source si aucune n'a été précisée
  dans la configuration. L'attribution n'est valable que pour les fonds en ligne.
* Petites améliorations sur la documentation, notamment sur la gestion, la configuration et
    l'ordonnancement des couches coté module ["maps"](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps)
    (https://github.com/PnX-SI/gn_mobile_occtax/issues/192)
* La synchronisation périodique de l'ensemble des données issues de GeoNature est configuré par
    défaut à 7 jours. Cette configuration n'est active uniquement que si la synchronisation périodique
    n'est pas configurée (les paramètres `sync_periodicity_data` et `sync_periodicity_data_essential`
    ne sont pas renseignés).
* La synchronisation des données est maintenant décoléré de la synchronisation des relevés (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).
* La synchronisation des relevés se fait à la demande de l'utilisateur (https://github.com/PnX-SI/gn_mobile_occtax/issues/137).
* La synchronisation des données s'exécute maintenant dans un contexte transactionnel afin de
  toujours garantir une cohérence des données présentes localement.
* Refonte de l'écran d'accueil pour mettre en valeur les relevés en cours ou prêt à être
  synchronisés. La partie paramétrage et synchronisation des données sont déportées dans le menu
  latéral.

### 🐛 Corrections

* Meilleur support de la taille des textes de l'interface selon la densité et la configuration
  d'affichage du terminal (https://github.com/PnX-SI/gn_mobile_occtax/issues/217).

### ⚠️ Notes de version

* Code de version : **3200**
* Depuis sa version 2.12.0, GeoNature permet de gérer le contenu de la table
  `gn_commons.t_mobile_apps` directement dans le back-office du module "Admin" de GeoNature (https://github.com/PnX-SI/gn_mobile_occtax/issues/214)
* Dans cette même version, les médias (incluant le dossier `mobile/` comprenant les fichiers APK et
  le fichier `settings.json` d'Occtax-mobile) ont été déplacés du dossier `~/geonature/backend/static/`
  à `~/geonature/backend/media/` (https://github.com/PnX-SI/gn_mobile_occtax/issues/214)

## [2.6.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.0-rc2) (2023-04-29, pre-release)

### 🚀 Nouveautés

* La synchronisation périodique de l'ensemble des données issues de GeoNature est configuré par
  défaut à 7 jours. Cette configuration n'est active uniquement que si la synchronisation périodique
  n'est pas configurée (les paramètres `sync_periodicity_data` et `sync_periodicity_data_essential`
  ne sont pas renseignés).

### 🐛 Corrections

* Meilleur support de la taille des textes de l'interface selon la densité et la configuration
  d'affichage du terminal (https://github.com/PnX-SI/gn_mobile_occtax/issues/217).
* Le bouton "Envoyer les relevés" présenté sous forme de bouton icône dans la barre de menu en page
  d'accueil est affiché sous forme de texte simple "Envoyer" et non plus sous forme d'icône pour
  plus de clarté.
* Petites améliorations sur la documentation, notamment sur la gestion, la configuration et
  l'ordonnancement des couches coté module ["maps"](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps)
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/192).

### ⚠️ Notes de version

* Code de version : **3191**

## [2.6.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.0-rc1) (2023-04-19, pre-release)

### 🚀 Nouveautés

* Support Android 13 (API 33).
* Support des fonds Géoportail (https://github.com/PnX-SI/gn_mobile_maps/issues/8).
  Le module ["maps"](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps) supporte
  officiellement les fonds suivants :
  * [Géoportail WMTS](https://www.geoportail.gouv.fr)
  * [OpenTopoMap](https://www.opentopomap.org)
  * [OpenStreetMap](https://www.openstreetmap.org)
  * [Wikimedia Maps](https://maps.wikimedia.org)
* Gestion automatique des attributions sur les fonds en ligne (https://github.com/PnX-SI/gn_mobile_occtax/issues/191).
  L'attribution est définie automatiquement selon la nature de la source si aucune n'a été précisée
  dans la configuration. L'attribution n'est valable que pour les fonds en ligne.
* Petites améliorations sur la documentation, notamment sur la gestion des couches coté module
  ["maps"](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps) (https://github.com/PnX-SI/gn_mobile_occtax/issues/192).

### 🐛 Corrections

* Mise à jour de la liste des relevés et de leurs statuts pendant la synchronisation.

### ⚠️ Notes de version

* Code de version : **3187**

## [2.6.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.6.0-rc0) (2023-03-25, pre-release)

### 🚀 Nouveautés

* La synchronisation des données est maintenant décoléré de la synchronisation des relevés (https://github.com/PnX-SI/gn_mobile_occtax/issues/133). 
* La synchronisation des relevés se fait à la demande de l'utilisateur (https://github.com/PnX-SI/gn_mobile_occtax/issues/137).
* La synchronisation des données s'exécute maintenant dans un contexte transactionnel afin de
  toujours garantir une cohérence des données présentes localement.
* Refonte de l'écran d'accueil pour mettre en valeur les relevés en cours ou prêt à être
  synchronisés. La partie paramétrage et synchronisation des données sont déportées dans le menu
  latéral.

### ⚠️ Notes de version

* Code de version : **3181**

## [2.5.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.5.0) (2023-03-21, release)

### 🚀 Nouveautés

* Gestion des médias sur la partie dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/84)
* Refonte de la synchronisation des relevés en consommant les APIs v2 du module "Occtax".
* Refonte de la gestion des relevés.
* Accélérer la saisie en permettant de mémoriser les dernières nomenclatures saisies sur la partie
  dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/169).
* Possibilité de reprendre en édition un relevé terminé prêt à être synchronisé (https://github.com/PnX-SI/gn_mobile_occtax/issues/78).

### 🐛 Corrections

* Valeur par défaut des champs "Min"et "Max" dans la partie dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/209, https://github.com/PnX-SI/gn_mobile_occtax/issues/210)
* Quelques petits ajustements sur la documentation de l'installation, notamment sur la récupération
  des fichiers de logs (https://github.com/PnX-SI/gn_mobile_occtax/issues/203)

### ⚠️ Notes de version

* Code de version : **3170**
* Nécessite la version 2.10 (ou plus) de GeoNature.
* Suite à la refonte sur la partie gestion des relevés, le paramétrage de la nomenclature en
  configuration avancée a évolué aussi (cf. [README.md](https://github.com/PnX-SI/gn_mobile_occtax#nomenclature-settings)),
  notamment sur le nommage des attributs et du respect de la casse (Par exemple `MIN` devient `count_min`).

## [2.4.1-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.1-rc4) (2023-02-21, pre-release)

### 🐛 Corrections

* Gestion des médias sur la partie dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/84)
* Accélérer la saisie en permettant de mémoriser les dernières nomenclatures saisies sur la partie
  dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/169).

### ⚠️ Notes de version

* Code de version : **3163**

## [2.4.1-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.1-rc3) (2023-02-15, pre-release)

### 🚀 Nouveautés

* Gestion des médias sur la partie dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/84)

### ⚠️ Notes de version

* Code de version : **3137**

## [2.4.1-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.1-rc2) (2023-02-05, pre-release)

### 🐛 Corrections

* Suppression locale des relevés synchronisés avec succès.

### ⚠️ Notes de version

* Code de version : **3121**

## [2.4.1-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.1-rc1) (2023-02-04, pre-release)

### 🚀 Nouveautés

* Refonte de la synchronisation des relevés en consommant les nouvelles APIs du module "Occtax".

### ⚠️ Notes de version

* Code de version : **3119**

## [2.4.1-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.1-rc0) (2023-01-25, pre-release)

### 🚀 Nouveautés

* Refonte de la gestion des relevés. 
* Accélérer la saisie en permettant de mémoriser les dernières nomenclatures saisies sur la partie
dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/169).
* Possibilité de reprendre en édition un relevé terminé prêt à être synchronisé (https://github.com/PnX-SI/gn_mobile_occtax/issues/78).

### ⚠️ Notes de version

* Code de version : **3109**

## [2.4.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.0) (2022-10-02, release)

### 🚀 Nouveautés

* Refonte ergonomique des listes de choix des nomenclatures. Cette refonte ne concerne pour l'instant
  que l'étape "Informations" lors de la saisie d'un taxon.
* Accélérer la saisie en permettant de mémoriser les dernières nomenclatures saisies (https://github.com/PnX-SI/gn_mobile_occtax/issues/169).
  Cette fonctionnalité est accessible via la propriété `nomenclature/save_default_values` dans le
  [fichier de paramétrage](https://github.com/PnX-SI/gn_mobile_occtax/blob/develop/README.md#nomenclature-settings).
* Amélioration sur la recherche des taxons, notamment sur la distinction des mots (avec ou sans
  majuscules, avec ou sans accents) (https://github.com/PnX-SI/gn_mobile_occtax/issues/91).
* Petites améliorations sur la présentation des jeux de données, aussi bien dans la page de
  sélection des jeux de données que dans l'affichage du jeu de données sélectionné dans la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Petites améliorations sur la page de sélection des observateurs et sur la fonction de recherche
  des observateurs (https://github.com/PnX-SI/gn_mobile_occtax/issues/142).
* Petites améliorations sur les messages d'information lors de la synchronisation des données (https://github.com/PnX-SI/gn_mobile_occtax/issues/143).
* Affichage du nom vernaculaire du taxon dans le bilan de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/153).
* Ajout d'une fonction de filtre sur les rangs taxonomique des taxons dans la page du bilan de la
  saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/166).
* Affichage du nombre de taxons en en-tête de page (https://github.com/PnX-SI/gn_mobile_occtax/issues/167).
* Permettre de modifier la date et l'heure de fin des relevés en fin de saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/168).
* Refonte ergonomique sur l'enchaînement des écrans de la saisie. Le bilan de la saisie intervient
  notamment après le pointage sur la carte si le relevé contient au moins un taxon (https://github.com/PnX-SI/gn_mobile_occtax/issues/177).

### 🐛 Corrections

* Défilement automatique du nom vernaculaire du taxon sélectionné (https://github.com/PnX-SI/gn_mobile_occtax/issues/49).
* Validation sur l'ensemble des taxons ajoutés au relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/177).
* Correction concernant la mémorisation de la sélection des observateurs lors de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Validation automatique du compte utilisateur lors de l'authentification (https://github.com/PnX-SI/gn_mobile_occtax/issues/184).

### ⚠️ Notes de version

* Code de version : **3090**

## [2.4.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.0-rc2) (2022-09-26, pre-release)

### 🚀 Nouveautés

* Refonte ergonomique des listes de choix des nomenclatures. 
* Accélérer la saisie en permettant de mémoriser les dernières nomenclatures saisies (https://github.com/PnX-SI/gn_mobile_occtax/issues/169).

### ⚠️ Notes de version

* Code de version : **3083**

## [2.4.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.0-rc1) (2022-09-10, pre-release)

### 🐛 Corrections

* Défilement automatique du nom vernaculaire du taxon sélectionné (https://github.com/PnX-SI/gn_mobile_occtax/issues/49).
* Validation sur l'ensemble des taxons ajoutés au relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/177).

### ⚠️ Notes de version

* Code de version : **3079**

## [2.4.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.0-rc0) (2022-09-07, pre-release)

### 🚀 Nouveautés

* Amélioration sur la recherche des taxons, notamment sur la distinction des mots (avec ou sans
  majuscules, avec ou sans accents) (https://github.com/PnX-SI/gn_mobile_occtax/issues/91).
* Petites améliorations sur la présentation des jeux de données, aussi bien dans la page de 
  sélection des jeux de données que dans l'affichage du jeu de données sélectionné dans la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Petites améliorations sur la page de sélection des observateurs et sur la fonction de recherche
  des observateurs (https://github.com/PnX-SI/gn_mobile_occtax/issues/142).
* Petites améliorations sur les messages d'information lors de la synchronisation des données (https://github.com/PnX-SI/gn_mobile_occtax/issues/143).
* Affichage du nom vernaculaire du taxon dans le bilan de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/153).
* Ajout d'une fonction de filtre sur les rangs taxonomique des taxons dans la page du bilan de la
  saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/166).
* Affichage du nombre de taxons en en-tête de page (https://github.com/PnX-SI/gn_mobile_occtax/issues/167).
* Permettre de modifier la date et l'heure de fin des relevés en fin de saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/168).
* Refonte ergonomique sur l'enchaînement des écrans de la saisie. Le bilan de la saisie intervient
  notamment après le pointage sur la carte si le relevé contient au moins un taxon (https://github.com/PnX-SI/gn_mobile_occtax/issues/177).

### 🐛 Corrections

* Correction concernant la mémorisation de la sélection des observateurs lors de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).

### ⚠️ Notes de version

* Code de version : **3075**

## [2.3.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0) (2022-07-14, release)

### 🚀 Nouveautés

* Possibilité d'ajouter directement un marqueur via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).
* La rotation de la carte est bloquée par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/139).
  Son paramétrage reste accessible par configuration (cf. [README.md](https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/README.md#parameters-description) du module).
* Le commentaire du relevé est présent également à l'étape 1 du relevé, sous le choix de la date (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).
* Refonte ergonomique des champs de saisie "Min" et "Max" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Mise en place des boîtes de dialogue de confirmation pour la suppression des éléments saisis (https://github.com/PnX-SI/gn_mobile_occtax/issues/77)

### 🐛 Corrections

* Suppression de la double vérification de la validité du cookie de session et du token de session (https://github.com/PnX-SI/gn_mobile_occtax/issues/163).
  L'application reste "connectée" par défaut tant qu'elle ne reçoit pas en retour d'appel d'API une
  erreur 401.
* Libellé du champ date plus clair quand seule la date de début du relevé est configurée (https://github.com/PnX-SI/gn_mobile_occtax/issues/165).
* Ajustement d'ordre cosmétique sur le commentaire présenté à l'étape 1 du relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).

### ⚠️ Notes de version

* Code de version : **3070**
* L'application est officiellement compatible avec les terminaux tournant sur Android 8.0 au minimum.

## [2.3.0-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc4) (2022-07-13, pre-release)

### 🐛 Corrections

* Légère refonte ergonomique du formulaire sur le dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Le champ de saisie "Max" suit la valeur du champ de saisie "Min" si ces deux valeurs sont identiques.

### ⚠️ Notes de version

* Code de version : **3061**

## [2.3.0-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc3) (2022-07-04, pre-release)

### 🚀 Nouveautés

* Légère refonte ergonomique du formulaire sur le dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### 🐛 Corrections

* Le champ de saisie "Max" prend la valeur du champ de saisie "Min" si ce dernier est directement
  modifié par l'utilisateur (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### ⚠️ Notes de version

* Code de version : **3049**

## [2.3.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc2) (2022-06-29, pre-release)

### 🚀 Nouveautés

* Refonte ergonomique des champs de saisie "Min" et "Max" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### 🐛 Corrections

* Possibilité de déplacer directement un marqueur existant via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).
* Ajustement d'ordre cosmétique sur le commentaire présenté à l'étape 1 du relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).

### ⚠️ Notes de version

* Code de version : **3041**

## [2.3.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc1) (2022-06-25, pre-release)

### 🚀 Nouveautés

* Le commentaire du relevé est présent également à l'étape 1 du relevé, sous le choix de la date (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).

### 🐛 Corrections

* Possibilité d'ajouter directement un marqueur via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).

### ⚠️ Notes de version

* Code de version : **3037**

## [2.3.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc0) (2022-06-21, pre-release)

### 🚀 Nouveautés

* La rotation de la carte est bloquée par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/139).
  Son paramétrage reste accessible par configuration (cf. [README.md](https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/README.md#parameters-description) du module).
* Possibilité d'ajouter directement un marqueur via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).

### 🐛 Corrections

* Suppression de la double vérification de la validité du cookie de session et du token de session (https://github.com/PnX-SI/gn_mobile_occtax/issues/163).
  L'application reste "connectée" par défaut tant qu'elle ne reçoit pas en retour d'appel d'API une
  erreur 401.
* Libellé du champ date plus clair quand seule la date de début du relevé est configurée (https://github.com/PnX-SI/gn_mobile_occtax/issues/165).

### ⚠️ Notes de version

* Code de version : **3025**
* L'application est officiellement compatible avec les terminaux tournant sur Android 8.0 au minimum.

## [2.2.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0) (2022-05-31, release)

### 🚀 Nouveautés

* Ajout de la possibilité de renseigner la date de fin ainsi que les heures du relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104)
* Ajout des contraintes de validation sur les champs date de début et de fin. La date de fin est
  automatiquement fixée selon la date de début. Possibilité de définir la même date de début et de
  fin (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Configuration des dates et des heures du relevé via le fichier de paramétrage (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
  La description du paramétrage est décrite dans le [README.md](https://github.com/PnX-SI/gn_mobile_occtax/tree/master#input-settings).
* Possibilité de définir plusieurs observateurs par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Légère refonte ergonomique des formulaires d'édition.

### 🐛 Corrections

* Amélioration de la disposition du clavier par rapport aux champs de saisie sur l'écran de login (https://github.com/PnX-SI/gn_mobile_occtax/issues/155).
* Défilement des libellés des jeux de données (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Prise en compte du dénombrement réalisé par taxon dans le récapitulatif.
* Taille des libellés sur les boutons "flottants" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Corrections sur la résolution des fonds de carte embarqués sur le terminal (https://github.com/PnX-SI/gn_mobile_occtax/issues/151), 
  en privilégiant d'abord la carte mémoire externe (si présente) puis l'espace de stockage interne
  (cf. [README.md](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps#base-path) du module)

### ⚠️ Notes de version

* Code de version : **3020**

## [2.2.0-rc5](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc5) (2022-05-26, pre-release)

### 🐛 Corrections

* Ajout des contraintes de validation sur les champs date de début et de fin. La date de fin est
  automatiquement fixée selon la date de début. Possibilité de définir la même date de début et de
  fin (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : **3015**

## [2.2.0-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc4) (2022-05-24, pre-release)

### 🐛 Corrections

* Ajout des contraintes de validation sur les champs date de début et de fin (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : **2985**

## [2.2.0-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc3) (2022-05-24, pre-release)

### 🐛 Corrections

* Rendre "cliquable" l'indicateur de sélection des listes déroulantes (https://github.com/PnX-SI/gn_mobile_occtax/issues/158).
* Prise en compte du fuseau horaire sur les champs date lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Prise en compte du dénombrement réalisé par taxon dans le récapitulatif.

### ⚠️ Notes de version

* Code de version : **2965**

## [2.2.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc2) (2022-05-23, pre-release)

### 🐛 Corrections

* Gestion des attributs `hour_min` et `hour_max` lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Configuration de la date et de l'heure du relevé via le fichier de paramétrage (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Corrections sur la résolution des fonds de carte embarqués sur le terminal, en privilégiant
  d'abord la carte mémoire externe (si présente) puis l'espace de stockage interne
  (cf. [README.md](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps#base-path) du module)

### ⚠️ Notes de version

* Code de version : **2945**

## [2.2.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc1) (2022-05-18, pre-release)

### 🐛 Corrections

* Rendre "cliquable" l'indicateur de sélection des listes déroulantes (https://github.com/PnX-SI/gn_mobile_occtax/issues/158).
* Disposition du clavier par rapport aux champs de saisie sur l'écran de login (https://github.com/PnX-SI/gn_mobile_occtax/issues/155).
* Défilement des libellés des jeux de données (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Taille des libellés sur les boutons "flottants" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Mise à jour de la sélection des observateurs par défaut sur l'écran "Observateurs & Date" (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Gestion des attributs `hour_min` et `hour_max` lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : **2905**

## [2.2.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc0) (2022-05-16, pre-release)

### 🚀 Nouveautés

* Configuration de la date et de l'heure du relevé via le fichier de paramétrage (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
  La description du paramétrage est décrite dans le [README.md](https://github.com/PnX-SI/gn_mobile_occtax/tree/develop#input-settings).
* Possibilité de définir plusieurs observateurs par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Légère refonte ergonomique des formulaires lors de l'édition d'un relevé.

### ⚠️ Notes de version

* Code de version : **2870**

## [2.1.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.1.0) (2022-05-05, release)

### 🚀 Nouveautés

* L'utilisateur ne doit plus renseigner l'URL de TaxHub en plus de celle de GeoNature. Seule celle
  de GeoNature est demandée à l'utilisateur, l'application se chargera de récupérer automatiquement
  les paramètres depuis le serveur GeoNature, notamment l'URL de TaxHub
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/94).
* Clarification des boutons d'ajout d'un dénombrement et d'un taxon
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/114)
* La synchronisation des relevés n'est plus lancée automatiquement au lancement de l'application.
  Elle ne se fait que manuellement par l'utilisateur via le bouton "Synchroniser"
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).

### 🐛 Corrections

* Amélioration et correction du cache et de la synchronisation des paramètres et des données
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/130 ([cf. commentaire](https://github.com/PnX-SI/gn_mobile_occtax/issues/130#issuecomment-1109794834)))
* L'authentification n'est demandée que lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/145).
* Renommage du paramètre `uh_application_id` en `gn_application_id` (https://github.com/PnX-SI/gn_mobile_occtax/issues/116)
* Validation des taxons lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/147)
* Nettoyage des sous-modules et fichiers modifiés
* Complétion des logs

### ⚠️ Notes de version

* Code de version : **2680**
* Le paramètre `uh_application_id` a été renommé `gn_application_id`. L'ancien nom du paramètre
  fonctionne toujours, mais est déprécié. Il est donc conseillé de le renommer dans le fichier de
  paramétrage (`settings.json` côté GeoNature).

## [2.1.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.1.0-rc1) (2022-05-04, pre-release)

### 🐛 Corrections

* Correction sur la mise à jour du fichier de paramétrage lors du lancement de l'application 
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/130)

### ⚠️ Notes de version

* Code de version : **2675**

## [2.1.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.1.0-rc0) (2022-05-03, pre-release)

### 🚀 Nouveautés

* L'utilisateur ne doit plus renseigner l'URL de TaxHub en plus de celle de GeoNature. Seule celle
  de GeoNature est demandée à l'utilisateur, l'application se chargera de récupérer automatiquement
  les paramètres depuis le serveur GeoNature, notamment l'URL de TaxHub
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/94).
* Clarification des boutons d'ajout d'un dénombrement et d'un taxon
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/114)
* La synchronisation des relevés n'est plus lancée automatiquement au lancement de l'application.
  Elle ne se fait que manuellement par l'utilisateur via le bouton "Synchroniser"
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/133).

### 🐛 Corrections

* Amélioration et correction du cache et de la synchronisation des paramètres et des données
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/130 ([cf. commentaire](https://github.com/PnX-SI/gn_mobile_occtax/issues/130#issuecomment-1109794834)))
* L'authentification n'est demandée que lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/145).
* Renommage du paramètre `uh_application_id` en `gn_application_id` (https://github.com/PnX-SI/gn_mobile_occtax/issues/116)
* Validation des taxons lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/147)
* Nettoyage des sous-modules et fichiers modifiés
* Complétion des logs

### ⚠️ Notes de version

* Code de version : **2670**
* Le paramètre `uh_application_id` a été renommé `gn_application_id`. L'ancien nom du paramètre
  fonctionne toujours, mais est déprécié. Il est donc conseillé de le renommer dans le fichier de
  paramétrage (`settings.json` côté GeoNature).

## [2.0.1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.0.1) (2022-03-30, release)

### 🐛 Corrections

* https://github.com/PnX-SI/gn_mobile_occtax/issues/130

### ⚠️ Notes de version

* Code de version : **2580**

## [2.0.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.0.0) (2022-03-22, release)

### 🚀 Nouveautés

* Fusion des applications **Sync-mobile** et **Occtax-mobile** en une seule application, en intégrant le module de synchronisation des données _datasync_ dans **Occtax-mobile** (#94)
* Mise en place d'un système de logs dans l'application pour en faciliter la récupération (#112)
* Suppression des variantes par défaut (#103), seule la version générique par défaut sera proposée
* Révision de la documentation et intégration de diagrammes dans celle-ci
* Dissocier le nom du module _occtax_ de GeoNature du nom de package de l'application (#129)

### ⚠️ Notes de version

* Code de version : **2570**

*Coté serveur pour les administrateurs :*

Les versions 1 et 2 d'Occtax-mobile peuvent cohabiter temporairement, et vous permettre d'avoir une flotte de mobile "mixte" utilisant les deux versions :

* Déposer le fichier APK de la nouvelle version d'Occtax-mobile sur le serveur GeoNature
* Compléter le fichier de configuration d'Occtax-mobile suite à l'intégration du module de synchronisation, en ajoutant la partie `sync` (voir https://github.com/PnX-SI/gn_mobile_occtax/tree/develop#settings). Cette nouvelle partie sera ignorée par les mobiles utilisant encore la version 1 d'Occtax-mobile
* Compléter la table `gn_commons.t_mobile_apps` en ajoutant une nouvelle ligne pour la version 2 d'Occtax-mobile (`fr.geonature.occtax2`). Vous pouvez (temporairement) conserver les lignes liées à la version 1, et conserver des mobiles en version 1 et en version 2 connectées à votre instance GeoNature. 
* Prévoyez de supprimer les lignes liées à Occtax-mobile v1 et Sync-mobile v1 dès que l'ensemble des mobiles connectés à votre GeoNature seront mis à jour avec Occtax-mobile v2

*Coté terminal pour les utilisateurs :* 

À partir de la version 2 d'Occtax-mobile, une seule application est nécessaire (Occtax-mobile v2 intègre les fonctionnalités de l'ancienne application sync). Vous devrez alors désinstaller les applications Occtax-mobile et Sync v1, puis installer Occtax-mobile v2 :

* Terminer et synchroniser les éventuels relevés restant sur le terminal en version 1 des applications
* Désinstaller les versions 1 de Sync-mobile et Occtax-mobile
* Installer la nouvelle version d'Occtax-mobile sur le terminal
* Paramétrer les URL de GeoNature et TaxHub, puis accordez les permissions "Stockage" et "Localisation" à l'application Occtax-mobile v2.

## [1.3.1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/1.3.1) (2022-02-08, pre-release)

### 🐛 Corrections

* Support de Android 11 (https://github.com/PnX-SI/gn_mobile_occtax/issues/88)
* https://github.com/PnX-SI/gn_mobile_maps/issues/7
* https://github.com/PnX-SI/gn_mobile_occtax/issues/109

### ⚠️ Notes de version

* Cette version n'est compatible qu'avec les versions 2.5.x, 2.6.x et 2.7.5 (et plus) de GeoNature et ne fonctionne qu'avec la version [1.3.x](https://github.com/PnX-SI/gn_mobile_core/releases/tag/1.3.0) de "Sync".
* Il est nécessaire de synchroniser tous les relevés en cours sur les terminaux avant d'effectuer la mise à jour. 
* Code de version : **2350**

## [1.3.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/1.3.0) (2021-07-29, release)

### 🐛 Corrections

* Support partiel de Android 11 (https://github.com/PnX-SI/gn_mobile_occtax/issues/88)

### ⚠️ Notes de version

* Cette version n'est compatible qu'avec les versions 2.5.x, 2.6.x et 2.7.5 (et plus) de GeoNature et ne fonctionne qu'avec la version [1.3.x](https://github.com/PnX-SI/gn_mobile_core/releases/tag/1.3.0) de "Sync".
* Il est nécessaire de synchroniser tous les relevés en cours sur les terminaux avant d'effectuer la mise à jour. 
* Code de version : **2290**
