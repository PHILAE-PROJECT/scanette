/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


/**
 * @author Frederic Dadeau
 */
public class Article implements Comparable {

    /** Code EAN 13 de l'article */
    long codeEAN13;

    /** Dénomination commerciale de l'article */
    String nom;

    /** Prix unitaire de l'article */
    double prixUnitaire;

    /** Constructeur
     * @param _ean13 le code EAN13 de l'article
     * @param _pu le prix unitaire de l'article
     * @param _nom le nom de l'article
     */
    public Article(long _ean13, double _pu, String _nom) {
        codeEAN13 = _ean13;
        prixUnitaire = _pu;

        nom = (_nom == null) ? "" : _nom;
    }

    /**
     * Accesseur pour le nom de l'article.
     * @return le nom de l'article
     */
    public String getNom() {
        return nom;
    }

    /**
     * Accesseur pour le code barre (EAN13) de l'article.
     * @return le code EAN13 de l'article
     */
    public long getCodeEAN13() {
        return codeEAN13;
    }

    /**
     * Accesseur pour le prix unitaire de l'article
     * @return la valeur du prix unitaire de l'article
     */
    public double getPrixUnitaire() {
        return prixUnitaire;
    }


    public boolean equals(Object a) {
        return a != null && a instanceof Article && ((Article)a).codeEAN13 == this.codeEAN13;
    }

    public int hashCode() {
        return Long.valueOf(codeEAN13).hashCode();
    }

    public boolean isValidEAN13() {
        if (codeEAN13 < 0) {
            return false;
        }
        String tab = Long.toString(codeEAN13);
        if (tab.length() > 13) {
            return false;
        }
        while (tab.length() < 13) {
            tab = "0" + tab;
        }
        int sum = 0;
        for (int i=0; i < 12; i++) {
            int digit = tab.charAt(i) - '0';
            sum += (i % 2 == 1) ? digit * 3 : digit;
        }
        int reste = sum % 10;

        int key = (reste == 0) ? 0 : 10 - reste;

        return key == (tab.charAt(12) - '0');
    }

    public int compareTo(Object o) {
        return this.hashCode() - o.hashCode();
    }
}
