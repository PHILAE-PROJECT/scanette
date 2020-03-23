Programme pour rejouer les traces d'usage sur la scanette 
=========================================================

Le programme est écrit en Java (nécessite Java 8 ou supérieur pour fonctionner). Il a pour but de rejouer les traces produits par le simulateur et/ou générées par les outils, sur des implantations Java de la scanette. 


# Jouer les tests sur l'implem de référence

La commande Java doit être exécutée dans le répertoire où se situe le répertoire "ressources".

Suivant que vous utilisez le format CSV ou le forma JSON de Agilkia, vous utilisez les lignes de commande :

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette.jar fr.philae.ScanetteTraceExecutor fichierCSV.csv`

ou 

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette.jar fr.philae.ScanetteTraceExecutor ficherJSON.json`

en remplaçant le dernier paramètre sur la ligne. Le programme regarde en fonction de l'extension du fichier pour déterminer quel parseur appeler. 

Attention, sous windows, le séparateur pour le classpath n'est pas : mais ; les lignes de commandes deviennent : 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette.jar fr.philae.ScanetteTraceExecutor fichierCSV.csv`

ou 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette.jar fr.philae.ScanetteTraceExecutor ficherJSON.json`


A noter que le rejeu s'arrête lorsqu'un scénario échoue. Comme dans les traces extraites du simulateur, il n'est pas nécessaire que les appels soient distincts entre les sessions utilisateurs (celles-ci peuvent donc être entremêlées). 

Si vous souhaitez scripter l'exécution et observer les valeurs avec lesquelles le programme termine : 
- 0 terminaison normale (tous les tests sont OK)
- -1 terminaison erreur (pas dûe aux tests : mauvais paramétrage, opération non reconnue, etc.)
- 1 terminaison avec non-conformité détectée par un test. 


# Jouer les tests sur un mutant 

Il y a dans le dossier un certain nombre d'implantations mutantes. Je ne garantit pas qu'elles soient toutes "tuables" par des tests. Chaque mutant est contenu dans un JAR et nommé "scanette-muXYZ.jar".

Pour lancer le rejeu des traces sur un mutant particulier (par exemple scanette-mu42.jar) : 

> `java -cp ScanetteTestReplay.jar:json-simple.jar:junit-4.12.jar:scanette-mu42.jar fr.philae.ScanetteTraceExecutor fichierCSV.csv`

sous Linux/MacOS et 

> `java -cp ScanetteTestReplay.jar;json-simple.jar;junit-4.12.jar;scanette-mu42.jar fr.philae.ScanetteTraceExecutor fichierCSV.csv`

sous Windows. 

On change juste le fichier JAR de la scanette dans le classpath, le reste est inchangé (donc on passe aussi bien du JSON que du CSV si vous avez bien tout suivi).


# Remarques 

Un fichier d'exemple CSV est fourni dans le dossier pour vous permettre de tester. Normalement il passe sans problème.

Pour les JSON d'Agilkia je me suis basé sur l'exemple de format que m'avait envoyé Nicolas. S'il y a des soucis qui ne semblent pas normaux (typiquement en cas d'échec du test une exception Junit est levée), vous pouvez m'envoyer par mail votre exemple et je tâcherai de faire une correction sur le programme -- dans la limite de mon temps disponible (télétravail ne signifiant pas "être H24 sur son ordi prêt à répondre aux sollications" contrairement à ce que certains semblent penser). 


#restezchezvous
