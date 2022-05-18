# Supermarket Scanner Project 

This repository contains the assets that are necessary to the Supermarker Scanner project. This latter relies on an article database, that is queried by two entities in the system: the scanner and the cashier. The scanner makes it possible, for a customer, to add (or suppress) articles to his basket, and to pay his purchase through a special cashier. It is possible that a control occurs to verify the content of the user's basket. 

This project is a Java implementation and a web based simulator, similar to a video game which implements the behavior of different actors that explore the supermarket and make purchases. Thus, usages traces, as mentioned in the Philae Project, may be produced.  

## Repository content 

The "implem" directory contains the Java implementation of the scanner. It presents 4 classes : Article, ArticleDB (article database), Scanette, et Caisse. The API of these classes are described in the specifications (see directory "specs"). This "standalone" application does require any specific library to be executed, and can be compiled with any version of the JDK >= 1.5.

The "tests" directory contains unit and integration JUnit tests that were written, using Mockito (for classes Scanette and Caisse) along with CSV files for the article databases.

Directory "specs" contains the description in French of the specification of each of the classes of the application.


## Simulation and traces

In order to test the usage of the scanner, it is possible to run the simulator in manual mode 
(click on the scanners next to the entry door to display characters). Characters can be moved after being selected. 

The simulator is available at: [https://fdadeau.github.io/scanette/](https://fdadeau.github.io/scanette/)

An automated simulation can be launched using the following URL: [https://fdadeau.github.io/scanette/?simu](https://fdadeau.github.io/scanette/?simu). 
This makes it possible to generate realistic usage traces (limited by the underlying usage model).

The traces are available in the browser's console (visible by opening the dev tools of the browser), by selecting their originating source:  
. ihm_simu.js displays a verbose log describing character's actions,
. scanette.js displays a log of the operation calls to the API of the scanner and the cashier in a parameterized format.

Traces can be displayed/exported into a specific format to facilitate parsing. The existing 
formats are the following (available by completing the above-mentioned by &for=requested_format). The existing formats are the following (replace "requested_format" by the following keywods to obtain the corresponding log) in the console: 
- "fred" : format json `{ "obj": "scan1", "operation": "debloquer", "parametres": [], "timestamp": "134567827654", "result": "0"}`
- "lydie" : format csv `scan1, scanner, [8718309259938], 0, 1553593229207;`
- par défaut : `1553593429994: scan2.debloquer() -> 0`


## Reuse 

The following license applies: CC-NC-ND-4.0
This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
