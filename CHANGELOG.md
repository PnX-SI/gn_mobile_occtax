# Changelog

## 2.0.0 (unreleased)

**🚀 Nouveautés**

* Fusion des applications Sync-mobile et Occtax-mobile en une seule application, en intégrant le module de synchronisation des données dans Occtax-mobile (#94)
* Mise en place d'un système de logs dans l'application pour en faciliter la récupération (#112)
* Révision de la documentation et intégration de diagrammes dans celle-ci

**🐛 Corrections**

* 

**⚠️ Notes de version**

*Coté serveur pour les administrateurs :*

Les versions 1 et 2 d'occtax-mobile peuvent cohabiter temporairement, et vous permettre d'avoir une flotte de mobile "mixte" utilisant les deux versions :

* Déposer le fichier APK de la nouvelle version d'Occtax-mobile sur le serveur GeoNature
* Compléter le fichier de configuration d'Occtax-mobile suite à l'intégration du module de synchronisation, en ajoutant la partie ``sync`` (voir https://github.com/PnX-SI/gn_mobile_occtax/tree/feature/v2#settings). Cette nouvelle partie sera ignorée par les mobiles utilisant encore la version 1 d'Occtax-mobile
* Compléter la table ``gn_commons.t_mobile_apps`` en ajoutant une nouvelle ligne pour la version d'Occtax-mobile. Vous pouvez (temporairement) conserver les lignes liées à la version 1, et conserver des mobiles en version 1 et en version 2 connectées à votre instance GeoNature. 
* Prévoyez de supprimer les lignes liées à Occtax-mobile v1 et Sync v1 dès que l'ensemble des mobiles connectés à votre GeoNature seront mis à jour avec Occtax-mobile v2

*Coté terminal pour les utilisateurs :* 

A partir de la version 2 d'Occtax-mobile, une seule application est nécessaire (Occtax-mobile v2 intègre les fonctionnalités de l'ancienne application sync). Vous devrez alors désinstaller les applications Occtax-mobile et Sync V1, puis installer Occtax-mobile v2 :

* Terminer et synchroniser les éventuels relevés restant sur le terminal en version 1 des applications
* Désinstaller les versions 1 de Sync-mobile et Occtax-mobile
* Installer la nouvelle version d'Occtax-mobile sur le terminal
* Paramétrer les URL de GeoNature et TaxHub, puis accordez les permissions "Stockage" et "Localisation" à l'application Occtax-mobile v2.