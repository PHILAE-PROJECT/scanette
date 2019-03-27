/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


/**
 * @author Frederic Dadeau
 */
public interface Caisse {

    /**
     * Admet une connexion de la scanette lorsqu'elle est sollicité alors qu'elle est en attente.
     * @param s la scanette qui se connecte à la caisse.
     * @return  0 si la caisse était en attente et qu'elle ne demande pas de relecture
     *          1 si la caisse demande une relecture
     *         -1 pour tous les autres cas
     */
    public int connexion(Scanette s);
    
    /**
     * Permet de réaliser un paiement sur la caisse. 
     * @param somme la somme qui est payée.
     * @return une valeur >= 0 si le paiement a pu être effectué et qu'il reste un éventuel rendu.
     *         une valeur < 0 pour indiquer une erreur (cf. sujet)
     */
    public double payer(double somme);

    /**
     * Abandonne toute transaction en cours et replace la caisse en attente.
     */
    public void abandon();

    /**
     * Permet à un caissier de s'authentifier pour ouvrir une session.
     * @return 0 si la session a pu s'ouvrir
     *        -1 si l'appel n'a pas été réalisé depuis le(s) bon(s) état(s).
     */
    public int ouvrirSession();

    /**
     * Permet de fermer une session préalablement ouverte. 
     * @return  0 l'appel a réussi et la session est fermée
     *         -1 si l'appel a été effectué alors que le caissier n'était pas authentifié
     */
    public int fermerSession();

    /**
     * Fonction utilitaire, permet de savoir si une relecture est demandée.
     * @return true si une relecture doit être demandée, faux sinon. 
     */
    public boolean demandeRelecture();

    /**
     * Permet à un caissier préalablement authentifié d'ajouter un article
     * aux achats du client.
     * @param ean13 le code de l'article à ajouter
     * @return 0 si l'article a bien été ajouté et si le 
     *        -1 si l'appel a été effectué alors que le caissier n'était pas authentifié
     *        -2 si l'article est inconnu de la caisse
     */
    public int scanner(long ean13);

    /**
     * Permet à un caisser préalablement authentifié de retirer un article de la liste des achats.
     * @param ean13 le code du produit à supprimer
     * @return  0 si le code correspondait à un article existant et que celui-ci a été supprimé
     *         -1 si la méthode a été invoquée depuis le mauvais état
     *         -2 si aucun article ne correspondait au code EAN passé en paramètre
     */
    public int supprimer(long ean13) ;
}

