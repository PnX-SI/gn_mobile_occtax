# Changelog

## [2.4.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.4.0-rc0) (2022-09-07, pre-release)

### 🚀 Nouveautés

* Amélioration sur la recherche des taxons, notamment sur la distinction des mots (avec ou sans
  majuscules, avec ou sans accents) (https://github.com/PnX-SI/gn_mobile_occtax/issues/91).
* Petites améliorations sur la présentation des jeux de données, aussi bien dans la page de 
  sélection des jeux de données que dans l'affichage du jeu de données sélectionnée dans la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Petites améliorations sur la page de sélection des observateurs et sur la fonction de recherche
  des observateurs (https://github.com/PnX-SI/gn_mobile_occtax/issues/142).
* Petites améliorations sur les messages d'information lors de la synchronisation des données (https://github.com/PnX-SI/gn_mobile_occtax/issues/143).
* Affichage du nom vernaculaire du taxon dans le bilan de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/153).
* Ajout d'une fonction de filtre sur les rangs taxonomique des taxons dans la page du bilan de la
  saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/166).
* Affichage du nombre de taxon en en-tête de page (https://github.com/PnX-SI/gn_mobile_occtax/issues/167).
* Permettre de modifier la date et l'heure de fin des relevés en fin de saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/168).
* Refonte ergonomique sur l'enchaînement des écrans de la saisie. Le bilan de la saisie intervient
  notamment après le pointage sur la carte si le relevé contient au moins un taxon (https://github.com/PnX-SI/gn_mobile_occtax/issues/177).

### 🐛 Corrections

* Correction concernant la mémorisation de la sélection des observateurs lors de la saisie (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).

### ⚠️ Notes de version

* Code de version : 3075

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

* Code de version : 3070
* L'application est officiellement compatible avec les terminaux tournant sur Android 8.0 au minimum.

## [2.3.0-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc4) (2022-07-13, pre-release)

### 🐛 Corrections

* Légère refonte ergonomique du formulaire sur le dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Le champ de saisie "Max" suit la valeur du champ de saisie "Min" si ces deux valeurs sont identiques.

### ⚠️ Notes de version

* Code de version : 3061

## [2.3.0-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc3) (2022-07-04, pre-release)

### 🚀 Nouveautés

* Légère refonte ergonomique du formulaire sur le dénombrement (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### 🐛 Corrections

* Le champ de saisie "Max" prend la valeur du champ de saisie "Min" si ce dernier est directement
  modifié par l'utilisateur (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### ⚠️ Notes de version

* Code de version : 3049

## [2.3.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc2) (2022-06-29, pre-release)

### 🚀 Nouveautés

* Refonte ergonomique des champs de saisie "Min" et "Max" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).

### 🐛 Corrections

* Possibilité de déplacer directement un marqueur existant via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).
* Ajustement d'ordre cosmétique sur le commentaire présenté à l'étape 1 du relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).

### ⚠️ Notes de version

* Code de version : 3041

## [2.3.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.3.0-rc1) (2022-06-25, pre-release)

### 🚀 Nouveautés

* Le commentaire du relevé est présent également à l'étape 1 du relevé, sous le choix de la date (https://github.com/PnX-SI/gn_mobile_occtax/issues/140).

### 🐛 Corrections

* Possibilité d'ajouter directement un marqueur via un "toucher long" sur la carte (https://github.com/PnX-SI/gn_mobile_occtax/issues/14).

### ⚠️ Notes de version

* Code de version : 3037

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

* Code de version : 3025
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

* Code de version : 3020

## [2.2.0-rc5](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc5) (2022-05-26, pre-release)

### 🐛 Corrections

* Ajout des contraintes de validation sur les champs date de début et de fin. La date de fin est
  automatiquement fixée selon la date de début. Possibilité de définir la même date de début et de
  fin (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : 3015

## [2.2.0-rc4](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc4) (2022-05-24, pre-release)

### 🐛 Corrections

* Ajout des contraintes de validation sur les champs date de début et de fin (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : 2985

## [2.2.0-rc3](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc3) (2022-05-24, pre-release)

### 🐛 Corrections

* Rendre "cliquable" l'indicateur de sélection des listes déroulantes (https://github.com/PnX-SI/gn_mobile_occtax/issues/158).
* Prise en compte du fuseau horaire sur les champs date lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Prise en compte du dénombrement réalisé par taxon dans le récapitulatif.

### ⚠️ Notes de version

* Code de version : 2965

## [2.2.0-rc2](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc2) (2022-05-23, pre-release)

### 🐛 Corrections

* Gestion des attributs `hour_min` et `hour_max` lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Configuration de la date et de l'heure du relevé via le fichier de paramétrage (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
* Corrections sur la résolution des fonds de carte embarqués sur le terminal, en privilégiant
  d'abord la carte mémoire externe (si présente) puis l'espace de stockage interne
  (cf. [README.md](https://github.com/PnX-SI/gn_mobile_maps/tree/develop/maps#base-path) du module)

### ⚠️ Notes de version

* Code de version : 2945

## [2.2.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc1) (2022-05-18, pre-release)

### 🐛 Corrections

* Rendre "cliquable" l'indicateur de sélection des listes déroulantes (https://github.com/PnX-SI/gn_mobile_occtax/issues/158).
* Disposition du clavier par rapport aux champs de saisie sur l'écran de login (https://github.com/PnX-SI/gn_mobile_occtax/issues/155).
* Défilement des libellés des jeux de données (https://github.com/PnX-SI/gn_mobile_occtax/issues/120).
* Taille des libellés sur les boutons "flottants" (https://github.com/PnX-SI/gn_mobile_occtax/issues/114).
* Mise à jour de la sélection des observateurs par défaut sur l'écran "Observateurs & Date" (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Gestion des attributs `hour_min` et `hour_max` lors de l'envoi d'un relevé (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).

### ⚠️ Notes de version

* Code de version : 2905

## [2.2.0-rc0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.2.0-rc0) (2022-05-16, pre-release)

### 🚀 Nouveautés

* Configuration de la date et de l'heure du relevé via le fichier de paramétrage (https://github.com/PnX-SI/gn_mobile_occtax/issues/104).
  La description du paramétrage est décrite dans le [README.md](https://github.com/PnX-SI/gn_mobile_occtax/tree/develop#input-settings).
* Possibilité de définir plusieurs observateurs par défaut (https://github.com/PnX-SI/gn_mobile_occtax/issues/110).
* Légère refonte ergonomique des formulaires lors de l'édition d'un relevé.

### ⚠️ Notes de version

* Code de version : 2870

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
* L'authentification n'est demandée que lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/145)
* Renommage du paramètre `uh_application_id` en `gn_application_id` (https://github.com/PnX-SI/gn_mobile_occtax/issues/116)
* Validation des taxons lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/147)
* Nettoyage des sous-modules et fichiers modifiés
* Complétion des logs

### ⚠️ Notes de version

* Code de version : 2680
* Le paramètre `uh_application_id` a été renommé `gn_application_id`. L'ancien nom du paramètre
  fonctionne toujours mais est déprécié. Il est donc conseillé de le renommer dans le fichier de
  paramétrage (`settings.json` côté GeoNature).

## [2.1.0-rc1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.1.0-rc1) (2022-05-04, pre-release)

### 🐛 Corrections

* Correction sur la mise à jour du fichier de paramétrage lors du lancement de l'application 
  (https://github.com/PnX-SI/gn_mobile_occtax/issues/130)

### ⚠️ Notes de version

* Code de version : 2675

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
* L'authentification n'est demandée que lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/145)
* Renommage du paramètre `uh_application_id` en `gn_application_id` (https://github.com/PnX-SI/gn_mobile_occtax/issues/116)
* Validation des taxons lors de la synchronisation (https://github.com/PnX-SI/gn_mobile_occtax/issues/147)
* Nettoyage des sous-modules et fichiers modifiés
* Complétion des logs

### ⚠️ Notes de version

* Code de version : 2670
* Le paramètre `uh_application_id` a été renommé `gn_application_id`. L'ancien nom du paramètre
  fonctionne toujours mais est déprécié. Il est donc conseillé de le renommer dans le fichier de
  paramétrage (`settings.json` côté GeoNature).

## [2.0.1](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.0.1) (2022-03-30, release)

### 🐛 Corrections

* https://github.com/PnX-SI/gn_mobile_occtax/issues/130

### ⚠️ Notes de version

* Code de version : 2580

## [2.0.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/2.0.0) (2022-03-22, release)

### 🚀 Nouveautés

* Fusion des applications **Sync-mobile** et **Occtax-mobile** en une seule application, en intégrant le module de synchronisation des données _datasync_ dans **Occtax-mobile** (#94)
* Mise en place d'un système de logs dans l'application pour en faciliter la récupération (#112)
* Suppression des variantes par défaut (#103), seule la version générique par défaut sera proposée
* Révision de la documentation et intégration de diagrammes dans celle-ci
* Dissocier le nom du module _occtax_ de GeoNature du nom de package de l'application (#129)

### ⚠️ Notes de version

* Code de version : 2570

*Coté serveur pour les administrateurs :*

Les versions 1 et 2 d'Occtax-mobile peuvent cohabiter temporairement, et vous permettre d'avoir une flotte de mobile "mixte" utilisant les deux versions :

* Déposer le fichier APK de la nouvelle version d'Occtax-mobile sur le serveur GeoNature
* Compléter le fichier de configuration d'Occtax-mobile suite à l'intégration du module de synchronisation, en ajoutant la partie `sync` (voir https://github.com/PnX-SI/gn_mobile_occtax/tree/develop#settings). Cette nouvelle partie sera ignorée par les mobiles utilisant encore la version 1 d'Occtax-mobile
* Compléter la table `gn_commons.t_mobile_apps` en ajoutant une nouvelle ligne pour la version 2 d'Occtax-mobile (`fr.geonature.occtax2`). Vous pouvez (temporairement) conserver les lignes liées à la version 1, et conserver des mobiles en version 1 et en version 2 connectées à votre instance GeoNature. 
* Prévoyez de supprimer les lignes liées à Occtax-mobile v1 et Sync-mobile v1 dès que l'ensemble des mobiles connectés à votre GeoNature seront mis à jour avec Occtax-mobile v2

*Coté terminal pour les utilisateurs :* 

A partir de la version 2 d'Occtax-mobile, une seule application est nécessaire (Occtax-mobile v2 intègre les fonctionnalités de l'ancienne application sync). Vous devrez alors désinstaller les applications Occtax-mobile et Sync v1, puis installer Occtax-mobile v2 :

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
* Code de version : 2350

## [1.3.0](https://github.com/PnX-SI/gn_mobile_occtax/releases/tag/1.3.0) (2021-07-29, release)

### 🐛 Corrections

* Support partiel de Android 11 (https://github.com/PnX-SI/gn_mobile_occtax/issues/88)

### ⚠️ Notes de version

* Cette version n'est compatible qu'avec les versions 2.5.x, 2.6.x et 2.7.5 (et plus) de GeoNature et ne fonctionne qu'avec la version [1.3.x](https://github.com/PnX-SI/gn_mobile_core/releases/tag/1.3.0) de "Sync".
* Il est nécessaire de synchroniser tous les relevés en cours sur les terminaux avant d'effectuer la mise à jour. 
* Code de version : 2290
