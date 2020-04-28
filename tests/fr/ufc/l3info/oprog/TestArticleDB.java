/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */
package fr.ufc.l3info.oprog;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit test for Article.
 */
public class TestArticleDB {

    ArticleDB db;

    @Before
    public void setUp() {
        db = new ArticleDB();
    }

    /**
     * Test de la lecture du fichier (fichier incorrect)
     */
    @Test(expected=IOException.class)
    public void testArticleDB0() throws IOException, FileFormatException
    {
        db.init("/Users/fred/dev/work/Scanette/src/test/testfiles");
    }
    /**
     * Test de la lecture du fichier (extension incorrecte)
     */
    @Test(expected=IOException.class)
    public void testArticleDB0bis() throws IOException, FileFormatException
    {
        db.init("/Users/fred/dev/work/Scanette/src/test/produits.txt");
    }
    /**
     * Test de la lecture du fichier (fichier vide)
     */
    @Test
    public void testArticleDB1() throws IOException, FileFormatException
    {
        db.init(TestScanette.PATH_TO_CSV + "emptyFile.csv");
        assertEquals(0, db.getTailleDB());
    }
    /**
     * Test de la lecture du fichier (fichier vide)
     */
    @Test(expected = IOException.class)
    public void testArticleDB1bis() throws IOException, FileFormatException
    {
        db.init(null);
    }
    /**
     * Test de la lecture du fichier (fichier correct mais avec des doublons)
     */
    @Test
    public void testArticleDB2() throws IOException, FileFormatException, ArticleNotFoundException
    {
        db.init(TestScanette.PATH_TO_CSV + "validFileWithDuplicates.csv");
        assertEquals(1, db.getTailleDB());
        Article a = db.getArticle(3474377910731L);
        assertTrue(a.isValidEAN13());
        assertTrue(a.getPrixUnitaire() > 0);
        assertNotEquals(null, a.getNom());
        assertNotEquals(0, a.getNom().length());
    }

    /**
     * Test de la lecture du fichier (fichier correct)
     */
    @Test
    public void testArticleDB3() throws IOException, FileFormatException, ArticleNotFoundException
    {
        db.init(TestScanette.PATH_TO_CSV + "validFile.csv");
        assertEquals(2, db.getTailleDB());
        Article a = db.getArticle(3474377910731L);
        assertTrue(a.isValidEAN13());
        assertTrue(a.getPrixUnitaire() > 0);
        assertNotEquals(null, a.getNom());
        assertNotEquals(0, a.getNom().length());
        a = db.getArticle(3760244111005L);
        assertTrue(a.isValidEAN13());
        assertTrue(a.getPrixUnitaire() > 0);
        assertNotEquals(null, a.getNom());
        assertNotEquals(0, a.getNom().length());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : 4e champ dans une ligne)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB4() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile0.csv");
    }
   @Test
    public void testArticleDB4bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile0.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : clé incorrecte)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB5() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile1.csv");
    }
    @Test
    public void testArticleDB5bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile1.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : prix négatif)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB6() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile2.csv");
    }
    @Test
    public void testArticleDB6bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile2.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : 2 champs seulement)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB7() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile3.csv");
    }
    @Test
    public void testArticleDB7bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile3.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : nom vide)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB8() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile4.csv");
        assertEquals(0, db.getTailleDB());
    }
    @Test
    public void testArticleDB8bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile4.csv");
        }
        catch (FileFormatException f) { }
        assertEquals(0, db.getTailleDB());
    }


    /**
     * Test de la lecture du fichier (fichier incorrect : 2e ligne avec valeurs invalides)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB9() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile5.csv");
    }
    @Test
    public void testArticleDB9bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile5.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : clé non numérique)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB10() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile6.csv");
        assertEquals(0, db.getTailleDB());
    }
    @Test
    public void testArticleDB10bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile6.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : prix non numérique)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB11() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile7.csv");
        assertEquals(0, db.getTailleDB());
    }
    @Test
    public void testArticleDB11bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile7.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : nombre négatif && nom vide)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB12() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile8.csv");
        assertEquals(0, db.getTailleDB());
    }
    @Test
    public void testArticleDB12bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile8.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }

    /**
     * Test de la lecture du fichier (fichier correct) et récupération d'article inexistant
     */
    @Test(expected=ArticleNotFoundException.class)
    public void testArticleDB13() throws IOException, FileFormatException, ArticleNotFoundException
    {
        db.init(TestScanette.PATH_TO_CSV + "validFile.csv");
        assertEquals(2, db.getTailleDB());
        db.getArticle(3474377910730L);
    }

    /**
     * Test de la lecture du fichier (fichier correct)
     */
    @Test
    public void testArticleDB14() throws IOException, FileFormatException, ArticleNotFoundException
    {
        db.init(TestScanette.PATH_TO_CSV + "produits.csv");
        assertEquals(17, db.getTailleDB());
        long[] all17Articles = { 5410188006711L, 3560070048786L, 3017800238592L, 3560070976478L,
                3046920010856L, 8715700110622L, 3570590109324L, 3520115810259L, 3270190022534L,
                8718309259938L, 3560071097424L, 3017620402678L, 3245412567216L, 45496420598L,
                7640164630021L, 3560070139675L, 3020120029030L };
        for (long codebarre : all17Articles) {
            Article a = db.getArticle(codebarre);
            assertTrue(a.isValidEAN13());
            assertTrue(a.getPrixUnitaire() > 0);
            assertNotEquals(null, a.getNom());
            assertNotEquals(0, a.getNom().length());
        }
    }

    /**
     * Test de la lecture du fichier (fichier incorrect : 4e paramtre mal formatté)
     */
    @Test(expected=FileFormatException.class)
    public void testArticleDB15() throws IOException, FileFormatException {
        db.init(TestScanette.PATH_TO_CSV + "invalidFile10.csv");
        assertEquals(0, db.getTailleDB());
    }
    @Test
    public void testArticleDB15bis() throws IOException, FileFormatException {
        try {
            db.init(TestScanette.PATH_TO_CSV + "invalidFile10.csv");
        }
        catch (FileFormatException e) { }
        assertEquals(0, db.getTailleDB());
    }


    /**
     * Test de la lecture du fichier (fichier incorrect : nombre négatif && nom vide)
     */
    @Test
    public void testArticleDB16() throws IOException, FileFormatException, ArticleNotFoundException {
        db.init(TestScanette.PATH_TO_CSV + "produitsOK.csv");
        assertEquals(8.49, db.getArticle(3520115810259l).getPrixUnitaire(), 0.01);
        assertEquals(1.67, db.getArticle(3017620402678l).getPrixUnitaire(), 0.01);
        assertEquals(52.24, db.getArticle(45496420598l).getPrixUnitaire(), 0.01);
    }
    
}

