# Utilisation

Cette documentation présente le fonctionnement de l'application Occtax-mobile.
Pour installer l'application, voir la documentation dédiée (https://github.com/PnX-SI/gn_mobile_occtax/blob/master/docs/installation-fr.md). 
Elle inclut aussi une section indiquant comment installer une version de démonstration d'Occtax-mobile.

## Sync-mobile

L’application Occtax-mobile fonctionne avec l’application Sync-mobile (https://github.com/PnX-SI/gn_mobile_core) qui permet de centraliser et de partager 
la synchronisation des données entre plusieurs applications mobiles. Elle gère la récupération des utilisateurs, des nomenclatures ainsi que leurs valeurs 
par défaut et une liste des taxons. 

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-01.jpg"/>

Elle est paramétrée en lien avec une instance de GeoNature, ainsi qu'un ``id_application`` dans UsersHub pour vérifier si l'utilisateur a droit d'accéder 
à GeoNature.

Une fois authentifié, la synchronisation des données se lance automatiquement.

On peut lancer Occtax-mobile directement depuis l'application de synchronisationou bien depuis son icône de lancement présente sur le bureau du terminal Android.

## Occtax-mobile

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-02.jpg"/>

La page d’accueil de l’application Occtax-mobile présente l'état du terminal (dernière synchronisation, relevés à synchroniser et relevés en cours si on 
souhaite les terminer, les modifier ou les supprimer).

Les paramètres de l’application permettent de définir certaines options et valeurs par défaut.

D’autres paramètres sous forme de fichier de configuration permettent de définir : 
- le chemin de stockage des fonds de carte au format mbtiles, 
- les éventuelles couches vectorielles permettant de définir les couleurs de chaque taxon en fonction de la localisation du relevé 
(https://github.com/PnX-SI/gn_mobile_occtax/issues/5), ainsi que leur style d’affichage
- le centrage, la bbox et les niveaux de zoom de la carte
- le niveau de zoom minimal à partir duquel il est possible de localiser le relevé

Voir https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps pour plus de détails.

## Relevé

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-03.jpg"/>

La première étape consiste à éventuellement modifier les valeurs par défaut d’observateur, jeu de données et date.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-04.jpg"/>

L’étape suivante consiste à localiser le relevé sur la carte sous forme de point. Un zoom minimum de saisie peut être défini en paramètre. 

Il est aussi possible de déplacer la localisation du relevé, en cliquant longuement dessus.
