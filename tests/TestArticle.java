/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


import org.junit.Test;
import org.junit.Before;

import java.util.HashSet;
import java.util.TreeSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for Article.
 */
public class TestArticle {
    

    Article art;

    @Before
    public void setUp() {
        art = new Article(3474377910731l, 1.1, "Marker pour tableau blanc Maxiflo");
    }

    /**
     * Test du bon stockage des informations.
     */
    @Test
    public void testArticle0()
    {
        assertEquals(3474377910731l, art.getCodeEAN13());
        assertTrue(art.getPrixUnitaire() == 1.1);
        assertEquals("Marker pour tableau blanc Maxiflo", art.getNom());
    }

    @Test
    public void testArticle1() {
        assertTrue(art.isValidEAN13());
    }

    @Test
    public void testArticle2() {
        Article a = new Article(123, 0, "Article avec code correct (clé > 0)");
        assertTrue(a.isValidEAN13());
    }

    @Test
    public void testArticle3() {
        Article a = new Article(1232, 0, "Article avec code erroné");
        assertFalse(a.isValidEAN13());
    }

    @Test
    public void testArticle4() {
        Article a = new Article(130, 0, "Article avec code correct (clé = 0)");
        assertTrue(a.isValidEAN13());
    }

    /** Tests de la fonction d'égalité **/

    @Test
    public void testArticle5() {
        Article a = new Article(1232, 0, "Article avec code erroné");
        assertFalse(a.equals(art));
    }

    @Test
    public void testArticle6() {
        Article a = new Article(3474377910731l, 0, "Marker Maxiflo");
        assertTrue(a.equals(art));
    }

    @Test
    public void testArticle7() {
        Article a = new Article(-3474377910731l, 0, "Article avec code erroné");
        assertFalse(a.isValidEAN13());
    }

    @Test
    public void testArticle8() {
        Article a = new Article(45496420598l, 0, "Minecraft Switch");
        assertTrue(a.isValidEAN13());
    }

    @Test
    public void testArticle9() {
        Article a = new Article(45496420598l, 0, null);
        assertTrue(a.getNom().equals(""));
    }

    @Test
    public void testArticle10() {
        assertFalse(art.equals(null));
    }

    @Test
    public void testArticle11() {
        assertTrue(art.equals(art));
    }

    @Test
    public void testArticle12() {
        Article a = new Article(13474377910731l, 1.1, "Marker");
        assertFalse(a.isValidEAN13());
    }

    @Test
    public void testArticle13() {
        Article a = new Article(34743779107310l, 1.1, "Marker");
        assertFalse(a.isValidEAN13());
    }


    @Test
    public void testArticle14() {
        Article a1 = new Article(1, 0, "");
        Article a2 = new Article(1, 0, "");
        Article a3 = new Article(2, 0, "");

        HashSet<Article> hs = new HashSet<Article>();
        assertTrue(hs.add(a1));
        assertFalse(hs.add(a2));
        assertTrue(hs.add(a3));
        assertTrue(hs.size() == 2);
        assertTrue(hs.contains(a1));
        assertTrue(hs.contains(a2)); // considéré comme a1
        assertTrue(hs.contains(a3));
        assertTrue(hs.contains(new Article(2, 1, "toto")));
    }
}