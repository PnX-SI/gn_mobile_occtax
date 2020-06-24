# INSTALLATION

## Introduction

Occtax-mobile fonctionne avec Android et interagit avec un serveur GeoNature (2.4.0 minimum) et TaxHub (1.7.0 minimum).

Si vous souhaitez customiser l'application (nom, couleurs, icône), 
vous devez générer l'APK à partir des fichiers source comme détaillé dans la documentation (https://github.com/PnX-SI/gn_mobile_core/tree/master/docs).

Sinon vous pouvez utiliser les APK fournies dans les Assets de chaque release (aux couleurs de certains parcs nationaux ou la version générique verte et rouge).

Pour fonctionner, l'application Occtax-mobile a besoin de l'application Sync-mobile qui centralise la synchronisation des données.
Cela permet d'envisager le développement d'autres applications mobiles connectées à GeoNature en factorisant la synchronisation des données 
dans une seule application dédiée à cela.

Sync-mobile permet de récupérer des données (observateurs, JDD, liste de taxons...) en se interrogeant les routes d'une instance GeoNature après authentification 
de l'utilisateur pour lui appliquer ses droits.
Ces données sont stockées dans une BDD au format SQLite dans le terminal mobile pour disposer des données localement et ainsi pouvoir fonctionner hors-ligne.
Sync-mobile permet aussi de poster les données saisies sur le terminal mobile une fois que celui-ci dispose d'une connexion internet (wifi ou 4G).

Sync-mobile permet aussi de récupérer automatiquement les configurations et les dernières versions des applications mobiles directement sur le serveur GeoNature 
avec lequel elle se synchronise.

## Installer et configurer les applications

Téléchargez les APK dans les fichiers (assets) associés à la version souhaitée.

- Sync-mobile : https://github.com/PnX-SI/gn_mobile_core/releases
- Occtax-mobile : https://github.com/PnX-SI/gn_mobile_occtax/releases

### Sync-mobile

Installer l'application Sync-mobile puis chargez son fichier de configuration ``settings_sync.json`` dans le répertoire ``Android/data/fr.geonature.sync`` 
sur le stockage interne du terminal mobile.

- Détail des paramètres du fichier de configuration : https://github.com/PnX-SI/gn_mobile_core/tree/master/sync
- Exemple de fichier de configuration : https://github.com/PnX-SI/gn_mobile_core/blob/master/sync/src/test/resources/fixtures/settings_sync.json

Les paramètres ``page_size`` et ``page_max_retry`` permettent de paginer les appels aux routes renvoyant de nombreux résultats et ainsi les récupérer par lots 
(voir https://github.com/PnX-SI/gn_mobile_occtax/issues/37). Exemples des routes paginées :

- <URL_TAXHUB>/api/taxref/allnamebylist/100?limit=100&offset=200 (pour renvoyer les noms des 100 taxons à partir du 200ième résultat des taxons de la liste 100)
- <URL_GEONATURE>/api/synthese/color_taxon?code_area_type=M10&limit=10&offset=20 (pour renvoyer 10 résultats à partir du vingtième résultat des couleurs des taxons pour les zonages de type Mailles de 10km)

Ainsi la valeur du paramètre ``page_size`` multipliée par la valeur du paramètre ``page_max_retry`` doit être au moins égale au nombre total de résultats renvoyé par ces routes.

Le comportement de l'application de synchronisation consiste à appeler au maximum n fois (où n = ``page_max_retry``) la route paginée tant que celle-ci retourne 
un tableau de valeurs non vides et que ce tableau à la même taille que ``page_size``. 

Les conditions d'arrêt de l'interrogation de ces routes sont :
- Nombre d'appels = ``page_max_retry``
- Tableau de valeurs vide
- Taille du tableau de valeurs < ``page_size``
- Erreur 404

Le paramètre ``code_area_type`` correspond au type de zonage de votre référentiel géographique de GeoNature 
(champs ``type_code`` de la table ``ref_geo.bib_areas_types``) utilisé pour les unités géographiques.
Voir https://github.com/PnX-SI/gn_mobile_core/issues/15.

Le paramètre ``uh_application_id`` permet de renseigner l'``id_application`` de GeoNature dans sa table ``utilisateurs.t_applications`` 
pour l'authentification des utilisateurs et leurs droits.

Le paramètre ``observers_list_id`` permet de renseigner l``id_liste`` des observateurs d'Occtax dans la table ``utilisateurs.t_listes``.

Le paramètre ``taxa_list_id`` permet de renseigner l'``id_liste`` des taxons saisissables dans Occtax dans la table ``taxonomie.bib_listes``.

### Occtax-mobile

Installer l'application Occtax-mobile puis chargez son fichier de configuration ``settings_occtax.json`` dans le répertoire ``Android/data/fr.geonature.occtax`` 
sur le stockage interne du terminal mobile.

- Détail des paramètres du fichier de configuration : https://github.com/PnX-SI/gn_mobile_occtax#settings
- Exemple de fichier de configuration : https://github.com/PnX-SI/gn_mobile_occtax/blob/master/occtax/src/test/resources/fixtures/settings_occtax.json

Le paramètre ``area_observation_duration`` est lié aux couleurs des taxons dans chaque unités géographiques en fonction de la date de dernière observation 
du taxon dans l'unité géographique. 
Il correspond à la durée en jours définie dans la vue ``gn_synthese.v_color_taxon_area`` permettant d'ajuster à quelle fréquence un taxon change de couleur 
selon sa date de dernière observation dans l'unité géographique (Plus d'un mois, plus d'un an, plus de 5 ans...).

Voir https://github.com/PnX-SI/GeoNature/issues/617 et https://github.com/PnX-SI/gn_mobile_occtax/issues/50 pour plus de détails.

<img src="https://user-images.githubusercontent.com/11782642/75454154-3ac11700-5975-11ea-997c-7c33484e43bb.jpg" width="400"/>

Pour la configuration de la partie cartographique (bloc ``map`` du fichier ``settings_occtax.json``), 
se référer au [README](https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/README.md) du module maps.

Cette partie permet de définir l'affichage des outils cartographiques, le centrage, l'étendue et les niveaux de zoom, 
mais aussi les fonds et couches cartographiques de l'application.

Le module cartographique s'appuie sur la bibliothèque osmdroid (https://github.com/osmdroid/osmdroid) 
et gère notamment les sources locales (https://github.com/osmdroid/osmdroid/wiki/Offline-Map-Tiles) pouvant être généré via l'outil MOBAC ou Maperitive. 
Charger un fond de carte (MBTiles, les autres formats doivent aussi fonctionner) sur le terminal mobile et renseigner son chemin dans le paramètre ``base_path``.

La page "Paramètres" de l'application Sync-mobile indique les chemins absolus de la carte interne et éventuellement de la carte externe SD.

Il n'est cependant pas obligatoire de préciser le chemin pour résoudre le chargement des fonds de carte. 
L'application va privilégier la carte SD externe (si présente) et à défaut la mémoire interne. 
Le paramètre ``base_path`` peut prendre un chemin absolu (pour une résolution rapide), un chemin relatif (selon le point de montage, par exemple "Android/data") 
ou être omis. Dans ce cas, la résolution sera plus lente car elle impliquera un scan complet des stockages du terminal mobile.

Il est possible de charger différents fonds cartographiques (Scan et ortho par exemple) mais aussi d'afficher des couches vectorielles.

On peut ajouter autant de couches vectorielles et pour chacune on peut appliquer des styles différents. 
Vous pouvez vous référer au [README](https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/README.md) du module maps pour le paramétrage.

Pour l'affichage et l'utilisation des unités géographiques permettant d'afficher les taxons de couleur différente selon la date de dernière observation dans l'unité, 
il est nécessaire de charger une couche vectorielle des polygones des unités géographiques en respectant quelques règles.

Par défaut, si aucune couche vectorielle n'est configurée, l'application va simplement charger la base des taxons sans les données additionnelles 
venant des unités géographiques.

- Exemple de fichier WKT : https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/src/test/resources/fixtures/features.wkt
- Exemple de configuration : https://github.com/PnX-SI/gn_mobile_maps/blob/develop/maps/src/test/resources/fixtures/map_settings.json

Il est important que l'ID de chaque zone corresponde à ce que remonte GeoNature pour faire la correspondance.

L'attribut ``area_id`` des données de la route ``/geonature/api/synthese/color_taxon`` correspondent aux identifiants présent dans la couche vectorielle.

Par exemple, si la source vectorielle est du WKT :
```
110,POINT (-1.5487664937973022 47.21628889447996)
108,POINT (-1.5407788753509521 47.241763083159455)
```

Les couches vectorielles peuvent être au format `json`, `geojson` ou `wkt`.

Concernant l'exemple au format WKT, il est au format CSV et le module "maps" doit suivre le schéma suivant :
```
id,wkt
```
par ligne et sans entête.

Par exemple :
```
108,POINT (-1.5407788753509521 47.241763083159455)
```

Concernant le fichier au format WKT, il ne faut pas mettre entre guillemets la géométrie. Exemple :
```
660993,MULTIPOLYGON (((6.73181863107186 45.7539143085928,6.74466771917198 45.7534881584565,6.74405801858532 45.7444934010459,6.73121101630907 45.7449194816323,6.73181863107186 45.7539143085928)))
```

Concernant les fonds cartographiques, il faut donc suivre les règles suivantes :

**wkt**

* fichier texte, où chaque ligne comporte la description d'une géométrie au format WKT
* chaque ligne doit commencer par un identifiant, ce qui donne ceci :
  ```
  <id>,<geometry>
  ...
  ```

Par exemple :
```
110,POINT (-1.5487664937973022 47.21628889447996)
108,POINT (-1.5407788753509521 47.241763083159455)
```

**json, geojson**

* fichier texte au format json contenant un objet de type `FeatureCollection` ou un tableau d'objets de type `Feature`
* chaque objet de type `Feature` doit comporter un identifiant (attribut `id`), en tant qu'attribut de cet objet ou en tant que propriété de cet objet. Par exemple :
  ```json
  {
    "id": 1234,
    "type": "Feature",
    "geometry": {
      ...
    },
    "properties": {
      ...
    }
  }
  ```
  ```json
  {
    "type": "Feature",
    "geometry": {
      ...
    },
    "properties": {
      "id": 1234,
      ...
    }
  }
  ```

### Installation et configuration centralisées

Il est aussi possible de gérer les fichiers de configuration, l'installation et la mise à jour des applications au niveau du serveur GeoNature.
Voir https://github.com/PnX-SI/gn_mobile_core/issues/8.

L'application Sync-mobile se chargera alors de récupérer sur le serveur GeoNature les dernières versions des fichiers de configuration des applications 
ainsi que les dernière APK des applications.

Pour cela, chargez les APK souhaitées des applications Sync-mobile et Occtax-mobile ainsi que leurs fichiers de configuration respectifs nommés ``settings.json``
dans le dossier ``/home/myuser/geonature/backend/static/mobile`` du serveur GeoNature.

Il faut créer un dossier par application, exemple pour Sync-mobile : 

![image](https://user-images.githubusercontent.com/4418840/82023083-fdc01300-968d-11ea-9a74-dfe9e17727b4.png)

Exemple pour Occtax-mobile : 

![image](https://user-images.githubusercontent.com/4418840/82023217-36f88300-968e-11ea-9865-76148108b302.png)

Dans tous les cas, le fichier de configuration sur le serveur doit être nommé ``settings.json``.

Renseigner ensuite la table ``gn_commons.t_mobile_apps``. 
Pour trouver la valeur à renseigner dans le champs ``version_code``, voir les fichiers https://github.com/PnX-SI/gn_mobile_core/blob/master/sync/version.properties 
et https://github.com/PnX-SI/gn_mobile_occtax/blob/master/occtax/version.properties

Exemple de contenu de la table ``gn_commons.t_mobile_apps`` : 
```
1;"OCCTAX";"static/mobile/occtax/occtax-0.3.0-pne-debug.apk";"''";"fr.geonature.occtax";"1660"
2;"SYNC";"static/mobile/sync/sync-0.2.8-pne-debug.apk";"";"fr.geonature.sync";"2280"
```

Le résultat peut être testé en interrogeant la route <URL_GEONATURE>/api/gn_commons/t_mobile_apps qui est celle utilisée par l'application Sync-mobile 
pour mettre à jour les applications et leur configuration.

Installez ensuite uniquement l'application Sync-mobile sur le terminal mobile, lancez-la et déclarez l'URL de GeoNature et de TaxHub dans sa configuration.

L'application de synchronisation se chargera de récupérer le fichier de configuration pour chaque application installée.

Elle proposera aussi d'installer les applications mobiles disponibles et de récupérer leur fichier de configuration.

Si vous faites évoluer la configuration et/ou les versions des applications mobiles sur le serveur GeoNature et dans la table ``gn_commons.t_mobile_apps``, 
alors ils seront mis à jour sur le terminal mobile au prochaine lancement de l'application Sync-mobile.

# Utilisation

Voir https://geonature.fr/documents/0.25-occtax-mobile.pdf (à mettre à jour et migrer en fichier .md à part)
