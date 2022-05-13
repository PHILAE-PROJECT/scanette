# Projet scanette 

Ce répertoire contient les données nécessaires au projet Scanette, qui représente une implémentation d'un système de scanette de supermarché. Celle-ci s'appuie sur une base de données d'articles, consultée par les deux entités présentes dans le système : la scanette et la caisse. La scanette permet à un client d'ajouter des articles dans son panier virtuel (et d'en supprimer), et de se présenter à une caisse spéciale qui lui permettra de régler ses achats. Il est possible qu'une relecture soit déclenchée pour contrôler le contenu du panier de l'utilisateur. 

Ce projet se présente sous la forme d'une implémentation Java, et d'un simulateur écrit en technos web (HTML, CSS, JS) sous la forme d'un mini-jeu vidéo qui implémente le comportement de divers acteurs qui parcourent le magasin pour réaliser des achats. Cette seconde partie permet de produire les traces d'utilisation mentionnées dans le projet Philae. 

## Contenu du répertoire 

Le répertoire "implem" contient l'implémentation de la scanette en Java. Il contient 4 classes : Article, ArticleDB (base de données d'articles), Scanette, et Caisse. Les API de ces classes sont décrites dans les spécifications (voir répertoire "specs"). Cette application "standalone" ne nécessite pas de bibliothèque spéciale pour fonctionner et peut se compiler avec n'importe quelle version du JDK >= 1.5.

Le répertoire "tests" contient les tests unitaires et d'intégration développés, format JUnit avec parfois Mockito (pour les classes Scanette et Caisse), ainsi que les fichiers de données de tests (base de données d'articles au format CSV). 

Le répertoire "specs" contient la description en français des spécifications de chacune des 4 classes composant l'application.


## Simulation et traces

Pour tester l'utilisation de la scanette, vous pouvez lancer le simulateur en mode manuel (cliquez sur les scanettes en bas à côté de la porte d'entrée pour faire apparaître les personnages). On déplace ensuite les personnages en les sélectionnant (le personnage sélectionné à un petit point blanc au dessus de la tête), et en les amenant dans les rayons. 

Le simulateur est accessible via l'adresse : [https://fdadeau.github.io/scanette/](https://fdadeau.github.io/scanette/)

Une simulation automatique dans laquelle l'apparition et le contrôle des personnages se fait automatiquement est disponible à l'adresse [https://fdadeau.github.io/scanette/?simu](https://fdadeau.github.io/scanette/?simu) et permet de générer des traces d'usages réalistes (dans la limite du modèle d'usage qu'elle implémente). En substance : le personnage arrive avec une liste de courses, il parcourt les rayons pour ajouter les articles à son panier puis se présente en caisse pour faire son paiement, après éventuelle relecture. Certains comportements aléatoires ont été implémentés comme la possibilité d'oublier de scanner un produit, la possibilité de reposer un produit qui ne lui plait plus (potentiellement en oubliant de le supprimer de la scanette), refaire un tour dans le magasin si aucune caisse n'est libre, abandonner ses achats en plein milieu, etc. 

Les traces sont disponibles en ouvrant la console (outils développeurs du navigateur), puis en sélectionnant l'origine des messages :
. ihm_simu.js affiche un log verbeux décrivant en français les actions des personnages
. scanette.js affiche un log des appels aux opérations de l'API de la scanette et de la caisse, dans un format paramétrable

Les traces des API peuvent être affichées/exportées dans un format spécifique pour faciliter le parsing. Me demander.  
Formats existants (accessibles en complétant l'url précédente par &for=format_demandé). Les formats existants sont les suivants (remplacez "format_demandé" par les mots-clés ci-dessous pour avoir le format de log correspondant) dans la console :
- "fred" : format json `{ "obj": "scan1", "operation": "debloquer", "parametres": [], "timestamp": "134567827654", "result": "0"}`
- "lydie" : format csv `scan1, scanner, [8718309259938], 0, 1553593229207;`
- par défaut : `1553593429994: scan2.debloquer() -> 0`


## Réutilisation 

La licence suivante s'applique : CC-NC-ND-4.0
This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.