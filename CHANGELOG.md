# Changelog

## 2.0.0 (unreleased)

**🚀 Nouveautés**

* Fusion des applications Sync-mobile et Occtax-mobile en une seule application, en intégrant le module de synchronisation des données dans Occtax-mobile (#94)
* Mise en place d'un système de logs dans l'application pour en faciliter la récupération (#112)
* Révision de la documentation et intégration de diagrammes dans celle-ci

**🐛 Corrections**

* 

**⚠️ Notes de version**

Pour passer de la version 1 à la version 2 d'Occtax-mobile, vous devez désinstaller les versions 1 de Sync-mobile et Occtax-mobile puis installer la version 2 d'Occtax-mobile.
A partir de la version 2, on installe seulement l'application Occtax-mobile, le module de synchronisation étant désormais intégré dans l'application Occtax-mobile.

* Déposer le fichier APK de la nouvelle version d'Occtax-mobile sur le serveur GeoNature
* Compléter le fichier de configuration d'Occtax-mobile suite à l'intégration du module de synchronisation, en ajoutant la partie ``sync`` (voir https://github.com/PnX-SI/gn_mobile_occtax/tree/feature/v2#settings)
* Compléter la table ``gn_commons.t_mobile_apps`` en ajoutant une ligne pour la version d'Occtax-mobile
* Terminer et synchroniser les éventuelles relevés restant sur le terminal en version 1 des applications
* Désinstaller les versions 1 de Sync-mobile et Occtax-mobile
* Installer la nouvelle version d'Occtax-mobile sur le terminal
