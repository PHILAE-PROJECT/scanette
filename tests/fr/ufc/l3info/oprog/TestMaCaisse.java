/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */
package fr.ufc.l3info.oprog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests pour la Caisse en utilisant un mock pour la classe Scanette.
 *
 * IMPORTANT : utiliser @RunWith(MokitoJUnitRunner.Silent.class) pour
 *  éviter les erreurs liées au "unnecessary" stubbings.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TestMaCaisse {

    private MaCaisse maCaisse;
    private HashSet<Article> panier;
    private HashSet<Long> refsInconnues;
    private Scanette maScanette;
    private double totalPanier;

    @Before
    public void setUp() throws ProductDBFailureException {
        maCaisse = Mockito.spy(new MaCaisse(TestScanette.PATH_TO_CSV + "produitsOK.csv"));

        maScanette = Mockito.mock(Scanette.class);

    }

    private void initPanier() {
        panier = new HashSet<Article>();
        panier.add(new Article(5410188006711L, 2.15, "Tropicana Tonic Breakfast"));
        panier.add(new Article(8715700110622L, 0.96, "Ketchup"));
        panier.add(new Article(45496420598L, 54.99, "Jeu switch Minecraft"));

        when(maScanette.getArticles()).thenReturn(panier);
        when(maScanette.quantite(5410188006711L)).thenReturn(3);
        when(maScanette.quantite(8715700110622L)).thenReturn(2);
        when(maScanette.quantite(45496420598L)).thenReturn(1);

        totalPanier = 3 * 2.15 + 0.96 * 2 + 54.99;
    }

    private void initRefsInconnues() {
        refsInconnues = new HashSet<Long>();
        refsInconnues.add(5410188006712L);
        when(maScanette.getReferencesInconnues()).thenReturn(refsInconnues);
    }


    /**
     * Test de la lecture du fichier
     */

    @Test(expected = ProductDBFailureException.class)
    public void fichierInexistant() throws ProductDBFailureException {
        new MaCaisse(TestScanette.PATH_TO_CSV + "fichierInexistant.csv");
    }

    @Test(expected = ProductDBFailureException.class)
    public void fichierAvecErreur() throws ProductDBFailureException {
        new MaCaisse(TestScanette.PATH_TO_CSV + "produitsKO.csv");
    }


    /**
     * Demande de relecture : fréquence attendue de 10%
     */
    @Test
    public void demandeRelectureFrequence() {
        int nbTrue = 0, nbFalse = 0;
        for (int i = 0; i < 100000; i++) {
            if (maCaisse.demandeRelecture()) {
                nbTrue++;
            } else {
                nbFalse++;
            }
        }
        assertTrue(nbTrue > 9500 && nbTrue < 10500);
    }


    /**
     * Tests de la connexion avec la scanette puis paiement pour vérifier l'état
     */
    
    @Test     // panier vide, pas de refs inconnues, pas de relecture --> 0 attente
    public void connexion0_pasRelu() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    @Test     // panier vide, pas de refs inconnues, pas de relecture --> 0 attente
    public void connexion0_relu() {        // should not happen
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    
    @Test  // panier vide, pas de refs inconnues, relecture --> 0 attente
    public void connexion1_pasRelu() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    @Test    // panier vide, pas de refs inconnues, relecture --> 0 attente
    public void connexion1_relu() {         // should not happen
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    
    @Test // panier vide, refs inconnues, pas de relecture --> 0  attente
    public void connexion2_pasRelu() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    @Test   // panier vide, inconnues, pas de relecture --> 0 attente
    public void connexion2_relu() {       // should not happen
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }

    @Test // panier vide, refs inconnues, relecture --> 0  attente
    public void connexion3_pasRelu() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }
    @Test   // panier vide, refs inconnues, relecture --> 0 attente
    public void connexion3_relu() {     // should not happen
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }

    @Test     // panier non vide, pas de refs inconnues, pas de relecture --> 0 paiement
    public void connexion4_pasRelu() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }
    @Test     // panier non vide, pas de refs inconnues, pas de relecture --> 0 paiement
    public void connexion4_relu() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test  // panier non vide, pas de refs inconnues, relecture --> 1 demande relecture
    public void connexion5_pasRelu() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(1, maCaisse.connexion(maScanette));
        assertEquals(1, maCaisse.connexion(maScanette));
    }
    @Test    // panier non vide, pas de refs inconnues, relecture --> 0 paiement
    public void connexion5_relu() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test // panier non vide, refs inconnues, pas de relecture --> 0  attente
    public void connexion6_pasRelu() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
    }
    @Test   // panier non vide, inconnues, pas de relecture --> 0 attente
    public void connexion6_relu() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
    }

    @Test // panier non vide, refs inconnues, relecture --> 1  relecture
    public void connexion7_pasRelu() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(1, maCaisse.connexion(maScanette));
        assertEquals(1, maCaisse.connexion(maScanette));
    }
    @Test   // panier non vide, refs inconnues, relecture --> 0 attente
    public void connexion7_relu() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
    }


    /**
     * Test du paiement 
     */
    @Test       // mauvais état : attente
    public void paiementDepuisAttente() {
        assertTrue(maCaisse.payer(0) < 0);
    }

    // mauvais état : attente caissier --> cf. connexion7_relu

    @Test       // mauvais état : session ouverte
    public void paiementDepuisSessionOuverte() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(true);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertTrue(maCaisse.payer(0) < 0);
    }

    @Test   // état correct : paiement négatif
    public void paiementNegatif() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(- 42) < 0);
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test   // état correct : paiement insuffisant
    public void paiementInsuffisant() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(true);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(totalPanier - 1) < 0);
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    // etat correct paiement sans rendu --> cf. connexion4_relu

    @Test   // état correct : paiement avec rendu
    public void paiementAvecRendu() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(42, maCaisse.payer(totalPanier)+42, 0.001);
    }


    /**
     *   Test ouverture session 
     */
    @Test   // depuis en attente --> KO
    public void ouvertureSessionDepuisAttente() {
        assertEquals(-1, maCaisse.ouvrirSession());
    }

    @Test   // depuis paiement --> OK puis depuis session ouverte --> KO
    public void ouvertureSessionDepuisPaiement() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-1, maCaisse.ouvrirSession());
    }

    @Test   // depuis attente caissier --> OK puis depuis session ouverte --> KO
    public void ouvertureSessionDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-1, maCaisse.ouvrirSession());
    }


    /**
     *  Test fermeture session
     */
    @Test   // depuis en attente
    public void fermetureSessionDepuisAttente() {
        assertEquals(-1, maCaisse.fermerSession());

    }

    @Test   // depuis paiement --> OK
    public void fermetureSessionDepuisPaiement() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.fermerSession());
    }

    @Test   // depuis session ouverte à partir du paiement (panier > 0)
    public void fermetureSessionDepuisSessionOuverte1() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);

        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test   // depuis session ouverte à partir de l'attente caissier (panier == 0)
    public void fermetureSessionDepuisSessionOuverte2() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.ouvrirSession());
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(0, maCaisse.connexion(maScanette));
    }

    @Test   // depuis session ouverte à partir de l'attente caissier (panier > 0)
    public void fermetureSessionDepuisSessionOuverte3() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }


    /**
     * Scan de produits
     */

    @Test
    public void scanDepuisEnAttente() {
        assertEquals(-1, maCaisse.scanner(5410188006711L));
    }

    @Test
    public void scanDepuisPaiement() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.scanner(5410188006711L));
    }

    @Test
    public void scanDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.scanner(5410188006711L));
    }

    @Test
    public void scanProduitOK() {
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.scanner(5410188006711L));
        assertEquals(0, maCaisse.scanner(5410188006711L));
        assertEquals(0, maCaisse.fermerSession());
        assertTrue(maCaisse.payer(totalPanier) < 0);
        assertEquals(0, maCaisse.payer(totalPanier+2*2.15), 0.001);
    }

    @Test
    public void scanProduitKO() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-2, maCaisse.scanner(5410188006712L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(2.15, maCaisse.payer(totalPanier+2.15), 0.001);
    }


    /*
     * Suppression de produits
     */

    @Test
    public void suppressionDepuisAttente() {
        assertEquals(-1, maCaisse.supprimer(5410188006711L));
    }

    @Test
    public void suppressionDepuisPaiement() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.supprimer(5410188006711L));
    }

    @Test
    public void suppressionDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(-1, maCaisse.supprimer(5410188006711L));    }

    @Test
    public void suppressionProduitUnique() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.supprimer(45496420598L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.payer(totalPanier-54.99), 0.001);
    }

    @Test
    public void suppressionProduitEnPlusieursExemplaires() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.supprimer(5410188006711L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.payer(totalPanier-2.15), 0.001);
    }

    @Test
    public void suppressionProduitInexistant() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-2, maCaisse.scanner(5410188006712L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(2.15, maCaisse.payer(totalPanier+2.15), 0.001);
    }


    /**
     * Abandon
     */
    @Test
    public void abandonDepuisAttente() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        maCaisse.abandon();
        assertEquals(0, maCaisse.connexion(maScanette));
    }

    @Test
    public void abandonDepuisPaiement() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        maCaisse.abandon();
        assertEquals(0, maCaisse.connexion(maScanette));
    }

    @Test
    public void abandonDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        maCaisse.abandon();
        assertEquals(0, maCaisse.connexion(maScanette));
    }
    
    @Test
    public void abandonDepuisCaissierAuthentifie() {
        initPanier();
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        maCaisse.abandon();
        assertEquals(0, maCaisse.connexion(maScanette));
    }


    /**
     * Scenarios d'usage un peu complexes
     */

    @Test
    public void scenarioUnPeuChiade() {
        // panier vide
        when(maScanette.getArticles()).thenReturn(new HashSet<Article>());
        // produits inconnus
        initRefsInconnues();
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(false);
        // ajout 2 produits par le caissier
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.scanner(5410188006711L));
        assertEquals(0, maCaisse.scanner(45496420598L));
        // suppression de 3 produits par le caissier (2 précédents + 1 autre)
        assertEquals(0, maCaisse.supprimer(5410188006711L));
        assertEquals(0, maCaisse.supprimer(45496420598L));
        assertEquals(-2, maCaisse.supprimer(8715700110622L));
        // fermeture session
        assertEquals(0, maCaisse.fermerSession());
        // --> doit être en attente
        assertEquals(0, maCaisse.connexion(maScanette));
        
    }

    @Test
    public void connexionPuisAbandonPuisConnexion() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        maCaisse.abandon();
        panier.clear();
        panier.add(new Article(3017620402678L, 1.86, "Nutella 220g"));
        when(maScanette.quantite(3017620402678L)).thenReturn(1);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(1.86), 0.01);
    }

    @Test
    public void deuxPaiements() {
        initPanier();
        when(maScanette.getReferencesInconnues()).thenReturn(new HashSet<Long>());
        when(maCaisse.demandeRelecture()).thenReturn(false);
        when(maScanette.relectureEffectuee()).thenReturn(true);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.01);
        panier.clear();
        panier.add(new Article(3017620402678L, 1.86, "Nutella 220g"));
        when(maScanette.quantite(3017620402678L)).thenReturn(1);
        assertEquals(0, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(1.86), 0.01);
    }

}

