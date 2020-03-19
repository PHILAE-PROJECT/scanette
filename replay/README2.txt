Difficultés rencontrée pour utiliser le replay. (Yves Ledru)

Dans le README.md, la ligne de commande est prévue pour Linux ou Mac. Sur PC, il faut remplacer les ':' par ';' dans le classpath.

Ensuite, il faut ajouter json-simple-1.1.1.jar et junit-4.12.jar dans le path (sinon il ne trouve pas org/json/simple/parser/ParseException)
Je les ai également ajouté au répertoire replay. La ligne de commande est donc:

java -classpath ScanetteTestReplay.jar;scanette.jar;json-simple-1.1.1.jar;junit-4.12.jar fr.philae.ScanetteTraceExecutor 1026-steps.csv

Pour jouer un mutant:
java -classpath ScanetteTestReplay.jar;scanette-mu42.jar;json-simple-1.1.1.jar;junit-4.12.jar fr.philae.ScanetteTraceExecutor 1026-steps.csv
