/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

// Mockito
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.junit.runner.RunWith;

import java.util.Set;

/**
 * Tests pour la scanette en utilisant un mock pour la classe Caisse.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestScanette {

    private Scanette scan;

    @Before
    public void setUp() throws ProductDBFailureException {
        scan = new Scanette("./target/classes/csv/produitsOK.csv");
    }


    /**
     * Test de la lecture du fichier (fichier incorrect)
     */

    @Test(expected=ProductDBFailureException.class)
    public void initialisationKO_fichierInexistant() throws ProductDBFailureException {
        new Scanette("./target/classes/csv");
    }

    @Test(expected=ProductDBFailureException.class)
    public void initialisationKO_fichierKO() throws ProductDBFailureException {
        new Scanette("./target/classes/csv/produitsKO.csv");
    }


    /**
     * Test du déblocage de la scanette
     */

    @Test             // verification achats + refs inconnues vides
    public void deblocageOK() {
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
        assertEquals(-1, scan.debloquer());
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }

    @Test
    public void deblocageApresReblocage() {
        assertEquals(0, scan.debloquer());
        scan.abandon();
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }


    /**
     * Test du blocage après abandon (depuis chaque état)
     */
    
    @Test
    public void deblocageApresAbandonEtPanierNonVide() {
        // déblocage
        assertEquals(0, scan.debloquer());
        // scan d'un article
        scan.scanner(5410188006711L);
        // abandon
        scan.abandon();
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        // déblocage à nouveau
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }

    @Test
    public void abandonDepuisBloque() {
        scan.abandon();
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void abandonDepuisRelecture() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-2, scan.scanner(5410188006712L));
        assertEquals(1, scan.transmission(mockCaisse));
        scan.abandon();
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void abandonDepuisRelectureOK() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(-2, scan.scanner(5410188006712L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(0, scan.scanner(5410188006711L));
        scan.abandon();
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void abandonDepuisRelectureKO() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(-2, scan.scanner(5410188006712L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-3, scan.scanner(8715700110622L));
        scan.abandon();
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }


    /**
     * Test du scan depuis des états incorrects
     */
    
    @Test
    public void scannerDepuisEtatBloque() {
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }
    @Test
    public void scannerDepuisRelectureOK() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(-1, scan.scanner(8715700110622L));
    }
    @Test
    public void scannerDepuisRelectureKO() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-3, scan.scanner(8715700110622L));
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(-1, scan.scanner(8715700110622L));
    }

    /**
     * Test du scan depuis des états EN_COURSES
     */

    @Test
    public void scannerUnSeulProduit() {
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }
    @Test
    public void scannerDeuxProduitsDifferents() {
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(1, scan.quantite(8715700110622L));
        assertEquals(2, scan.getArticles().size());
        assertTrue(scan.getArticles().contains(new Article(5410188006711L, 0, "")));
        assertTrue(scan.getArticles().contains(new Article(8715700110622L, 0, "")));
        assertEquals(0, scan.getReferencesInconnues().size());
    }
    @Test
    public void scannerDeuxProduitsIdentiques() {
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(2, scan.quantite(5410188006711L));
        assertEquals(0, scan.quantite(8715700110622L));
        assertEquals(1, scan.getArticles().size());
        assertTrue(scan.getArticles().contains(new Article(5410188006711L, 0, "")));
        assertEquals(0, scan.getReferencesInconnues().size());
    }
    @Test
    public void scannerUneReferenceInconnue() {
        assertEquals(0, scan.debloquer());
        assertEquals(-2, scan.scanner(5410188006710L));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.quantite(5410188006710L));
        assertEquals(1, scan.getReferencesInconnues().size());
        assertTrue(scan.getReferencesInconnues().contains(5410188006710L));
    }
    @Test
    public void scannerPlusieursReferencesInconnues() {
        assertEquals(0, scan.debloquer());
        assertEquals(-2, scan.scanner(5410188006710L));
        assertEquals(-2, scan.scanner(5410188006710L));
        assertEquals(-2, scan.scanner(5410188006712L));
        assertEquals(0, scan.getArticles().size());
        assertEquals(2, scan.getReferencesInconnues().size());
        assertTrue(scan.getReferencesInconnues().contains(5410188006710L));
        assertTrue(scan.getReferencesInconnues().contains(5410188006712L));
    }
    @Test
    public void scannerPleinDeTrucsAvecRefsInconnues() {
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-2, scan.scanner(5410188006712L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-2, scan.scanner(5410188006710L));
        assertEquals(2, scan.quantite(5410188006711L));
        assertEquals(1, scan.quantite(8715700110622L));
        assertEquals(2, scan.getArticles().size());
        assertTrue(scan.getArticles().contains(new Article(5410188006711L, 0, "")));
        assertTrue(scan.getArticles().contains(new Article(8715700110622L, 0, "")));
        assertEquals(2, scan.getReferencesInconnues().size());
        assertTrue(scan.getReferencesInconnues().contains(5410188006710L));
        assertTrue(scan.getReferencesInconnues().contains(5410188006712L));
    }

    /**
     * Test du scan depuis des états RELECTURE
     */
    
    @Test       // relecture d'un article inconnu + vérification du changement d'état
    public void scannerRelecture1() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110622L);
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-3, scan.scanner(5410188006710L));
        assertEquals(-1, scan.scanner(8715700110622L));
    }
    @Test       // relecture de tous les articles (<12) + vérification du changement d'état
    public void scannerRelecture2() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110622L);
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(-1, scan.scanner(8715700110622L));
        assertEquals(-1, scan.scanner(8715700110621L));
        assertEquals(3, scan.quantite(5410188006711L));
        assertEquals(2, scan.quantite(8715700110622L));
        assertEquals(2, scan.getArticles().size());
        assertTrue(scan.getArticles().contains(new Article(5410188006711L, 0, "")));
        assertTrue(scan.getArticles().contains(new Article(8715700110622L, 0, "")));
    }
    @Test       // relecture de tous 12 articles + vérification du changement d'état
    public void scannerRelecture3() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        for (int i=0; i < 15; i++) {
            scan.scanner(5410188006711L);
        }
        assertEquals(15, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(1, scan.transmission(mockCaisse));
        for (int i=0; i < 12; i++) {
            assertEquals(0, scan.scanner(5410188006711L));
        }
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(15, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }

    @Test       // relecture de 12 articles différents + vérification du changement d'état
    public void scannerRelecture4() throws ProductDBFailureException {
        scan = new Scanette("./target/classes/csv/produits.csv");
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        long[] all17Articles = {5410188006711L, 3560070048786L, 3017800238592L, 3560070976478L,
                3046920010856L, 8715700110622L, 3570590109324L, 3520115810259L, 3270190022534L,
                8718309259938L, 3560071097424L, 3017620402678L, 3245412567216L, 45496420598L,
                7640164630021L, 3560070139675L, 3020120029030L};
        for (long codebarre : all17Articles) {
            assertEquals(0, scan.scanner(codebarre));
        }
        assertEquals(1, scan.transmission(mockCaisse));
        for (int i=0; i < 12; i++) {
            assertEquals(0, scan.scanner(all17Articles[i]));
        }
        assertEquals(-1, scan.scanner(3560070139675L));
    }

    @Test   // relecture d'une seconde occurrence d'un article unique
    public void scannerRelecture5() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.transmission(mockCaisse);
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-3, scan.scanner(5410188006711L));
    }
    @Test   // relecture d'une d'un article inconnu
    public void scannerRelecture6() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        scan.scanner(5410188006710L);
        scan.scanner(8715700110622L);
        scan.transmission(mockCaisse);
        assertEquals(-3, scan.scanner(5410188006710L));
        assertEquals(-1, scan.scanner(8715700110622L));
    }


    /**
     * Test des suppressions depuis les différents états
     */

    @Test   
    public void supprimerDepuisBloque() {
        assertEquals(-1, scan.supprimer(8715700110622L));
    }
    @Test
    public void supprimerDepuisRelecture() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-1, scan.supprimer(5410188006711L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
    }
    @Test
    public void supprimerDepuisRelectureOK() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(-1, scan.supprimer(5410188006711L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
    }
    @Test
    public void supprimerDepuisRelectureKO() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-3, scan.scanner(5410188006712L));
        assertEquals(-1, scan.scanner(5410188006711L));
        assertEquals(-1, scan.scanner(8715700110622L));
        assertEquals(-1, scan.supprimer(5410188006711L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(1, scan.getArticles().size());
    }
    @Test   // suppression d'un article existant
    public void supprimerArticleExistant() {
        scan.debloquer();
        scan.scanner(5410188006711L);
        assertEquals(0, scan.supprimer(5410188006711L));
        assertEquals(0, scan.getArticles().size());
        assertEquals(-2, scan.supprimer(5410188006711L));
    }
    @Test   // suppression d'un article existant
    public void supprimerArticleExistantEnPlusieursExemplaires() {
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        assertEquals(1, scan.getArticles().size());
        assertEquals(2, scan.quantite(5410188006711L));
        assertEquals(0, scan.supprimer(5410188006711L));
        assertEquals(1, scan.getArticles().size());
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(0, scan.supprimer(5410188006711L));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.quantite(5410188006711L));
        assertEquals(-2, scan.supprimer(5410188006711L));
    }
    @Test   // suppression d'un article qui n'est pas dans le achats
    public void supprimerArticleInexistantDansPanier() {
        scan.debloquer();
        assertEquals(-2, scan.supprimer(5410188006711L));
        scan.scanner(5410188006711L);
        assertEquals(-2, scan.supprimer(5410188006710L));
    }
    @Test   // suppression d'un article detecté comme inconnu
    public void supprimerArticleInconnu() {
        scan.debloquer();
        assertEquals(-2, scan.scanner(5410188006710L));
        assertEquals(1, scan.getReferencesInconnues().size());
        assertEquals(-2, scan.supprimer(5410188006710L));
        assertEquals(1, scan.getReferencesInconnues().size());
    }
    

    @Test
    public void quantiteUnArticle() throws ProductDBFailureException
    {
        scan.debloquer();
        assertEquals(0, scan.quantite(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.quantite(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(2, scan.quantite(5410188006711L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(2, scan.quantite(5410188006711L));
        assertEquals(1, scan.quantite(8715700110622L));
    }


    @Test
    public void transmissionBloque() {
        assertEquals(-1, scan.transmission(null));
    }
    @Test
    public void transmissionDebloqueNull() {
        scan.debloquer();
        scan.scanner(5410188006711L);
        assertEquals(-1, scan.transmission(null));
    }
    @Test
    public void transmissionRelecture() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-1, scan.transmission(mockCaisse));
    }
    @Test
    public void transmissionRelectureKO() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        assertEquals(0, scan.debloquer());
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(1, scan.transmission(mockCaisse));
        assertEquals(-3, scan.scanner(5410188006710L));
        assertEquals(-1, scan.transmission(mockCaisse));
    }
    @Test
    public void transmissionSansRelecture()
    {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(0);

        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        assertEquals(0, scan.transmission(mockCaisse));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void transmissionAvecPanierVideSansRelecture()
    {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(0);
        scan.debloquer();
        assertEquals(0, scan.transmission(mockCaisse));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
        assertEquals(-1, scan.debloquer());
    }
    @Test
    public void transmissionAvecPanierVideAvecRelecture()
    {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);
        scan.debloquer();
        assertEquals(1, scan.transmission(mockCaisse));
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(0);
        assertEquals(0, scan.transmission(mockCaisse));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void transmissionAvecPanierPleinSansRelecture() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(0);
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110622L);
        assertEquals(0, scan.transmission(mockCaisse));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        assertEquals(0, scan.debloquer());
    }
    @Test
    public void transmissionAvecPanierPleinMauvaisEtatCaisse() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(-1);
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110621L);
        assertEquals(-1, scan.transmission(mockCaisse));
        assertEquals(2, scan.getArticles().size());
        assertEquals(1, scan.getReferencesInconnues().size());
    }
    @Test
    public void transmissionSuiteRelectureOK() {
        Caisse mockCaisse = Mockito.mock(Caisse.class);
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(1);

        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110621L);
        assertEquals(1, scan.transmission(mockCaisse));
        Mockito.when(mockCaisse.connexion(scan)).thenReturn(0);
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(5410188006711L));
        assertEquals(0, scan.scanner(8715700110622L));
        assertEquals(0, scan.transmission(mockCaisse));
        assertEquals(0, scan.getArticles().size());
        assertEquals(0, scan.getReferencesInconnues().size());
    }

    /**
     * Enchaînement de scénarios
     */

    @Test
    public void scenarioSansRelecturePuisRelecture() {
        transmissionAvecPanierPleinSansRelecture();
        transmissionSuiteRelectureOK();
    }
    @Test
    public void scenarioRelectureOKx2() {
        transmissionSuiteRelectureOK();
        transmissionSuiteRelectureOK();
    }
    @Test
    public void scenarioRelecturePuisSansRelecture() {
        transmissionSuiteRelectureOK();
        transmissionAvecPanierPleinSansRelecture();
    }


    /**
     * Intégrité des données
     */
    @Test
    public void integritePanier() {
        scan.debloquer();
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(5410188006711L);
        scan.scanner(8715700110622L);
        scan.scanner(8715700110622L);
        assertEquals(2, scan.getArticles().size());
        Set<Article> s = scan.getArticles();
        s.clear();
        assertEquals(2, scan.getArticles().size());
    }

    @Test
    public void integriteReferencesInconnues() {
        scan.debloquer();
        scan.scanner(5410188006712L);
        scan.scanner(5410188006712L);
        scan.scanner(5410188006712L);
        scan.scanner(8715700110623L);
        scan.scanner(8715700110623L);
        assertEquals(2, scan.getReferencesInconnues().size());
        Set<Long> s = scan.getReferencesInconnues();
        s.clear();
        assertEquals(2, scan.getReferencesInconnues().size());
    }


}
