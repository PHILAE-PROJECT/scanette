/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests d'intégration avec les réductions.
 */
public class TestIntegrationFD {

    private MyCaisse maCaisse;
    private HashSet<Article> panier;
    private HashSet<Long> refsInconnues;
    private Scanette maScanette;
    private double totalPanier;

    @Before
    public void setUp() throws ProductDBFailureException {                
        maCaisse = new MyCaisse("./target/classes/csv/produitsOK.csv");
        maScanette = new Scanette("./target/classes/csv/produitsOK.csv");
    }

    private void initPanier() {
        maScanette.debloquer();
        maScanette.scanner(5410188006711L);
        maScanette.scanner(5410188006711L);
        maScanette.scanner(5410188006711L);
        maScanette.scanner(8715700110622L);
        maScanette.scanner(8715700110622L);
        maScanette.scanner(45496420598L);
        
        totalPanier = 3 * 2.15 + 0.96 * 2 + 52.24;
    }

    private void initPanierVide() {
        maScanette.debloquer();
    }

    private void transfertEtRelecture() {
        int i = maScanette.transmission(maCaisse);
        if (i == 1) {
            // relecture demandée
            assertEquals(0, maScanette.scanner(5410188006711L));
            assertEquals(0, maScanette.scanner(5410188006711L));
            assertEquals(0, maScanette.scanner(5410188006711L));
            assertEquals(0, maScanette.scanner(8715700110622L));
            assertEquals(0, maScanette.scanner(8715700110622L));
            assertEquals(0, maScanette.scanner(45496420598L));
            i = maScanette.transmission(maCaisse);
        }
        assertEquals(0, i);
    }

    private void initRefsInconnues() {
        maScanette.scanner(1L);
    }


    /**
     * Tests de la connexion avec la scanette puis paiement pour vérifier l'état
     */
    
    @Test     // panier vide, pas de refs inconnues --> 0 attente
    public void connexion0_pasRelu() {
        initPanierVide();
        transfertEtRelecture();
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }

    
    @Test // panier vide, refs inconnues --> 0  attente
    public void connexion2_pasRelu() {
        initPanierVide();
        initRefsInconnues();
        transfertEtRelecture();
        assertTrue(maCaisse.payer(0) < 0);
        assertEquals(-1, maCaisse.connexion(maScanette));
    }

    @Test     // panier non vide, pas de refs inconnues, pas de relecture --> 0 paiement
    public void connexion4_pasRelu() {
        initPanier();
        transfertEtRelecture();
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test // panier non vide, refs inconnues, pas de relecture --> 0  attente
    public void connexion6_pasRelu() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();

        assertEquals(-1, maCaisse.connexion(maScanette));
        assertTrue(maCaisse.payer(0) < 0);
    }

    
    
    // mauvais état : attente caissier --> cf. connexion7_relu

    @Test       // mauvais état : session ouverte
    public void paiementDepuisSessionOuverte() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertTrue(maCaisse.payer(0) < 0);
    }

    @Test   // état correct : paiement négatif
    public void paiementNegatif() {
        initPanier();
        transfertEtRelecture();
        assertTrue(maCaisse.payer(- 42) < 0);
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test   // état correct : paiement insuffisant
    public void paiementInsuffisant() {
        initPanier();
        transfertEtRelecture();
        assertTrue(maCaisse.payer(totalPanier - 1) < 0);
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    // etat correct paiement sans rendu --> cf. connexion4_relu

    @Test   // état correct : paiement avec rendu
    public void paiementAvecRendu() {
        initPanier();
        transfertEtRelecture();
        assertEquals(42, maCaisse.payer(totalPanier)+42, 0.001);
    }


    /**
     *   Test ouverture session 
     */
    
    @Test   // depuis paiement --> OK puis depuis session ouverte --> KO
    public void ouvertureSessionDepuisPaiement() {
        initPanier();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-1, maCaisse.ouvrirSession());
    }

    @Test   // depuis attente caissier --> OK puis depuis session ouverte --> KO
    public void ouvertureSessionDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-1, maCaisse.ouvrirSession());
    }
    

    @Test   // depuis paiement --> OK
    public void fermetureSessionDepuisPaiement() {
        initPanier();
        transfertEtRelecture();
        assertEquals(-1, maCaisse.fermerSession());
    }

    @Test   // depuis session ouverte à partir du paiement (panier > 0)
    public void fermetureSessionDepuisSessionOuverte1() {
        initPanier();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test   // depuis session ouverte à partir de l'attente caissier (panier == 0)
    public void fermetureSessionDepuisSessionOuverte2() {
        initPanierVide();
        transfertEtRelecture();
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
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(-1, maCaisse.connexion(maScanette));
        assertEquals(0, maCaisse.payer(totalPanier), 0.001);
    }

    @Test
    public void scanDepuisPaiement() {
        initPanier();
        transfertEtRelecture();
        assertEquals(-1, maCaisse.scanner(5410188006711L));
    }

    @Test
    public void scanDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(-1, maCaisse.scanner(5410188006711L));
    }

    @Test
    public void scanProduitOK() {
        initPanierVide();
        initRefsInconnues();
        transfertEtRelecture();
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
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(-2, maCaisse.scanner(5410188006712L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(2.15, maCaisse.payer(totalPanier+2.15), 0.001);
    }


    /*
     * Suppression de produits
     */

    @Test
    public void suppressionDepuisPaiement() {
        initPanier();
        transfertEtRelecture();
        assertEquals(-1, maCaisse.supprimer(5410188006711L));
    }

    @Test
    public void suppressionDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(-1, maCaisse.supprimer(5410188006711L));    }

    @Test
    public void suppressionProduitUnique() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.supprimer(45496420598L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.payer(totalPanier-52.24), 0.001);
    }

    @Test
    public void suppressionProduitEnPlusieursExemplaires() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        assertEquals(0, maCaisse.supprimer(5410188006711L));
        assertEquals(0, maCaisse.fermerSession());
        assertEquals(0, maCaisse.payer(totalPanier-2.15), 0.001);
    }

    @Test
    public void suppressionProduitInexistant() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
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
        maCaisse.abandon();
        transfertEtRelecture();
    }

    @Test
    public void abandonDepuisPaiement() {
        initPanier();
        transfertEtRelecture();
        maCaisse.abandon();
        initPanier();
        transfertEtRelecture();
    }

    @Test
    public void abandonDepuisAttenteCaissier() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        maCaisse.abandon();
        initPanier();
        transfertEtRelecture();
    }
    
    @Test
    public void abandonDepuisCaissierAuthentifie() {
        initPanier();
        initRefsInconnues();
        transfertEtRelecture();
        assertEquals(0, maCaisse.ouvrirSession());
        maCaisse.abandon();
        initPanier();
        transfertEtRelecture();
    }


    /**
     * Scenarios d'usage un peu complexes
     */

    @Test
    public void scenarioUnPeuChiade() {
        // panier vide
        initPanierVide();
        // produits inconnus
        initRefsInconnues();
        transfertEtRelecture();
        // ajout 2 produits par le caissier
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
        initPanierVide();
        transfertEtRelecture();
    }

    @Test
    public void connexionPuisAbandonPuisConnexion() {
        initPanier();
        transfertEtRelecture();
        maCaisse.abandon();
        maScanette.debloquer();
        maScanette.scanner(3017620402678L);
        int i = maScanette.transmission(maCaisse);
        if (i == 1) {
            maScanette.scanner(3017620402678L);
            i = maScanette.transmission(maCaisse);
        }
        assertEquals(0, i);
        assertEquals(0, maCaisse.payer(1.67), 0.01);
    }

    @Test
    public void deuxPaiements() {
        initPanier();
        transfertEtRelecture();
        assertEquals(0, maCaisse.payer(totalPanier), 0.01);
        maScanette.debloquer();
        maScanette.scanner(3017620402678L);
        int i = maScanette.transmission(maCaisse);
        if (i == 1) {
            maScanette.scanner(3017620402678L);
            i = maScanette.transmission(maCaisse);
        }
        assertEquals(0, i);
        assertEquals(0, maCaisse.payer(1.67), 0.01);
    }

}

