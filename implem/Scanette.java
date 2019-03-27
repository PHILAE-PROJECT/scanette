/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Frederic Dadeau
 */
public class Scanette {

    /** Etats possibles de la scanette */
    enum ETAT { BLOQUEE, EN_COURSES, RELECTURE, RELECTURE_OK, RELECTURE_KO };

    /** Etat courant de la scanette */
    private ETAT etat;

    /** Base de données des produits */
    private ArticleDB produits;


    /** Données utiles pendant les courses */

    /** Panier de l'utilisateur */
    private HashMap<Long, Integer> panier;
    /** Articles non trouvés */
    private ArrayList<Long> nonReconnus;


    /** Données utiles pour la relecture */

    /** Nombre maximum de produits à rescanner */
    final private int A_RESCANNER = 12; 
    /** Nombre actuel de produits à rescanner */
    private int aRescanner = 0;
    /** Verification en cours (sorte de second achats) */
    private HashMap<Long, Integer> verif;
    

    /**
     * Créé une scanette en l'initialisant avec la base de données d'articles dont
     * le chemin est donné en paramètre.
     * @param pathToProductFile chemin vers le fichier de la base de données d'articles
     * @throws ProductDBFailureException lorsque la base de données n'a pas pu s'initialiser.
     */
    public Scanette(String pathToProductFile) throws ProductDBFailureException {
        try {
            produits = new ArticleDB();
            produits.init(pathToProductFile);
            etat = ETAT.BLOQUEE;
            panier = new HashMap<Long, Integer>();
            nonReconnus = new ArrayList<Long>();
            verif = new HashMap<Long, Integer>();
        }
        catch (FileFormatException e) {
            throw new ProductDBFailureException();
        } catch (IOException e) {
            throw new ProductDBFailureException();
        }
    }

    /**
     * Permet de débloquer la scanette pour un client donné. On supposera que
     * l'on ne gère pas les identifiants des clients.
     * @return  0 si la scanette a bien été débloquée,
     *          -1 si celle-ci n'était pas bloquée.
     */
    public int debloquer() {
        if (etat == ETAT.BLOQUEE) {
            etat = ETAT.EN_COURSES;
            return 0;
        }
        return -1;  
    }

    /**
     * Scanne un produit par l'intermédiaire de son code EAN13.
     * Cette méthode sert à la fois pour ajouter un produit au
     * achats du client et pour effectuer une relecture.
     * @param ean13 le code EAN13 du produit scanné
     * @return  0 si le scan du produit s'est correctement déroulé.
     *          -1 si la scanette n'était pas dans le bon état
     *          -2 si le produit scanné n'était pas reconnu (en courses)
     *          -3 si le produit n'était pas dans le achats (en relecture)
     */
    public int scanner(long ean13) {
        if (etat == ETAT.EN_COURSES) {
            try {
                Article a = produits.getArticle(ean13);
                int qu = quantite(ean13);
                qu++;
                panier.put(ean13, qu);
                return 0;
            } catch (ArticleNotFoundException e) {
                nonReconnus.add(ean13);
                return -2;
            }
        }
        if (etat == ETAT.RELECTURE) {
            // ajout aux articles relus
            int qt = (verif.containsKey(ean13)) ? verif.get(ean13) : 0;
            verif.put(ean13, qt + 1);
            if (!panier.containsKey(ean13) || verif.get(ean13) > panier.get(ean13)) {
                etat = ETAT.RELECTURE_KO;
                return -3; 
            } else {
                aRescanner--;
                if (aRescanner == 0) {
                    etat = ETAT.RELECTURE_OK;
                }
                return 0;
            }
        }
        return -1;
    }

    /**
     * Supprime du achats le produit dont le code EAN13 est donné en paramètre.
     * @param ean13 le code EAN du produit à supprimer.
     * @return  0 si l'occurrence du produit a bien été supprimée,
     *          -1 si la scanette n'était pas dans le bon état,
     *          -2 si le produit n'existait pas dans le achats
     */
    public int supprimer(long ean13) {
        if (etat != ETAT.EN_COURSES) {
            return -1;
        }
        int qu = quantite(ean13);
        if (qu < 1) {
            return -2;  
        }
        if (qu == 1) {
            panier.remove(ean13);
            return 0;
        }
        panier.put(ean13, qu - 1);
        return 0;
    }

    /**
     * Permet de connaître la quantité d'un produit dans le achats.
     * @param ean13 le code EAN13 du produit dont on souhaite connaître la quantité.
     * @return le nombre d'occurrences du produit dans le achats.
     */
    public int quantite(long ean13) {
        return panier.containsKey(ean13) ? panier.get(ean13) : 0;
    }


    /**
     * Permet d'abandonner toute transaction en cours et de re-bloquer la scanette.
     */
    public void abandon() {
        etat = ETAT.BLOQUEE;
        panier.clear();
        nonReconnus.clear();
    }


    /**
     * Permet de consulter les codes EAN qui n'ont pas été reconnus lors des courses.
     * @return Un ensemble de codes EAN13 non reconnus.
     */
    public Set<Long> getReferencesInconnues() {
        return new HashSet<Long>(nonReconnus); 
    }

    
    /**
     * Permet d'extraire les articles composant le achats du client.
     * @return Un ensemble d'Article reconnus par la scanette.
     */
    public Set<Article> getArticles() {
        HashSet<Article> ret = new HashSet<Article>();
        for (long l : panier.keySet()) {
            try {
                ret.add(produits.getArticle(l));
            }
            catch (ArticleNotFoundException e) { /* should not happen */ }
        }
        return ret;
    }


    private int getNbArticles() {
        int nb = 0;
        for (long l : panier.keySet()) {
            nb += panier.get(l);
        }
        return nb;
    }

    /**
     * Permet de transmettre les informations de la scanette à la caisse en se connectant
     *  à celle-ci. En fonction de la réponse de la caisse, la scanette changera d'état.
     * @param c La caisse avec laquelle la scanette interagit.
     * @return  0 si la scanette a terminé son travail (pas de relecture)
     *          1 si une relecture est demandée par la caisse
     *          -1 en cas d'erreur
     */
    public int transmission(Caisse c) {

        if (c == null) {
            return -1;
        }

        if (etat != ETAT.RELECTURE_OK && etat != ETAT.EN_COURSES) {
            return -1;
        }

        int codeRetourCaisse = c.connexion(this);

        if (codeRetourCaisse == 0) {
            etat = ETAT.BLOQUEE;
            panier.clear();
            nonReconnus.clear();
            return 0;
        }
        else if (etat == ETAT.EN_COURSES && codeRetourCaisse == 1) {
            etat = ETAT.RELECTURE;
            verif.clear();
            int nb = getNbArticles();
            aRescanner = (nb > A_RESCANNER) ? A_RESCANNER : nb;
            if (aRescanner == 0) {
                etat = ETAT.RELECTURE_OK;
            }
            return 1;
        }

        return -1;
    }

    /**
     * Indique si la scanette vient de finir une relecture avec succès. 
     * @return true si la scanette est dans l'état RELECTURE_OK, false sinon.
     */
    public boolean relectureEffectuee() {
        return etat == ETAT.RELECTURE_OK;
    }
}

class ProductDBFailureException extends Exception {

}

