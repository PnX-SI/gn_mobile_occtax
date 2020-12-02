# Utilisation

Cette documentation présente le fonctionnement de l'application Occtax-mobile.

Pour installer l'application, voir la [documentation dédiée](https://github.com/PnX-SI/gn_mobile_occtax/blob/master/docs/installation-fr.md). 
Elle inclut aussi une section indiquant comment installer une version de démonstration d'Occtax-mobile.

## Sync-mobile

L’application Occtax-mobile fonctionne avec l’application [Sync-mobile](https://github.com/PnX-SI/gn_mobile_core) qui permet de centraliser et de partager la synchronisation des données entre plusieurs applications mobiles. Elle gère la récupération des utilisateurs, des jeux de données, des nomenclatures ainsi que leurs valeurs par défaut et une liste des taxons. 

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-01.jpg"/>

Elle est paramétrée en lien avec une instance de GeoNature, ainsi qu'un ``id_application`` dans UsersHub pour vérifier si l'utilisateur a droit d'accéder à GeoNature.

Une fois authentifié, la synchronisation des données se lance automatiquement.

On peut lancer Occtax-mobile directement depuis l'application de synchronisation ou bien depuis son icône de lancement présente sur le bureau du terminal Android.

## Occtax-mobile

La page d’accueil de l’application Occtax-mobile présente l'état du terminal (dernière synchronisation, relevés à synchroniser et relevés en cours si on 
souhaite les terminer, les modifier ou les supprimer).

Les paramètres de l’application permettent de définir certaines options et valeurs par défaut.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-02.jpg"/>

D'autres paramètres sous forme de fichier de configuration permettent de définir : 
- le chemin de stockage des fonds de carte au format mbtiles, 
- les éventuelles couches vectorielles permettant de définir les couleurs de chaque taxon en fonction de la localisation du relevé, ainsi que leur style d’affichage
- le centrage, la bbox et les niveaux de zoom de la carte
- le niveau de zoom minimal à partir duquel il est possible de localiser le relevé

## Relevé

La première étape consiste à éventuellement modifier les valeurs par défaut d’observateur, jeu de données et date.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-03.jpg"/>

L'étape suivante consiste à localiser le relevé sur la carte sous forme de point. Un zoom minimum de saisie peut être défini en paramètre. 

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-04.jpg"/>

Il est aussi possible de déplacer la localisation du relevé, en cliquant longuement dessus.

## Taxon

L’étape 3 est le choix du taxon observé. 

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-05.jpg"/>

Si une couche vectorielle de zonage a été définie et intégrée dans l'appareil, alors une couleur indique les taxons qui ont été déjà vus dans la zone du relevé (et depuis quand en rouge ou gris), ainsi que le nombre de fois où ils ont été observés dans la zone et la date de dernière observation.

Il est possible de filtrer pour n'afficher que les taxons jamais observés, ou déjà observés.

Il est possible de rechercher dans la liste des taxons (en français ou en latinà, ou de filtrer par rang taxonomique.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-06.jpg"/>

On renseigne ensuite les nomenclatures. Les valeurs par défaut définies dans la base de données sont renseignées par défaut. Chaque nomenclature peut être modifiée.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-07.jpg"/>

Il est aussi possible d’afficher et de modifier les nomenclatures avancées.

## Dénombrement

L'étape suivante concerne le dénombrement. Aucun dénombrement n’est renseigné par défaut.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-08.jpg"/>

Il est possible de renseigner un ou plusieurs dénombrements pour chaque taxon observé.

Chaque dénombrement peut être modifié ou supprimé (en cliquant longuement sur l'un d'eux dans la liste des dénombrements).

## Dénombrement

La dernière étape est un récapitulatif des taxons du relevés, où il est aussi possible de mettre un commentaire général sur le relevé.

<img src="https://geonature.fr/docs/img/occtax-mobile/OM-09.jpg"/>

Il est alors possible d’ajouter un autre taxon au relevé, ou bien de terminer le relevé.

Si l’on termine le relevé, on revient à la page d’accueil de l'application qui liste les relevés en cours et l'état de synchronisation des données.

Elle permet aussi de lancer une synchronisation des données en lançant l'application Sync-mobile.
