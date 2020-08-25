package fr.philae;

import fr.ufc.l3info.oprog.*;

import org.junit.Assert;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;


/**
 * Created with IntelliJ IDEA.
 * User: Frederic Dadeau
 * Date: 08/03/2020
 * Time: 09:32
 */
public class ScanetteAdapter {

    // file containing the products
    final String PRODUCTS_SCANETTE = "resources/produitsScanette.csv";
    final String PRODUCTS_CAISSE = "resources/produitsCaisse.csv";

    /** Set of scanettes */
    HashMap<String, Scanette> scanettes;

    /** Set of caisses */
    HashMap<String, Caisse> caisses;

    
    public ScanetteAdapter() {
        // initialize maps
        scanettes = new HashMap<String, Scanette>();
        caisses = new HashMap<String, Caisse>();
    }

    public void process(int line, String obj, String op, String[] params, String res) throws Exception {
        if (op.equals("debloquer")) {
            execDebloquer(obj, res);
            return;
        }
        if (op.equals("scanner")) {
            execScanner(obj, params, res);
            return;
        }
        if (op.equals("transmission")) {
            execTransmission(obj, params, res);
            return;
        }
        if (op.equals("ouvrirSession")) {
            execOuvrirSession(obj, res);
            return;
        }
        if (op.equals("fermerSession")) {
            execFermerSession(obj, res);
            return;
        }
        if (op.equals("ajouter")) {
            execAjouter(obj, params, res);
            return;
        }
        if (op.equals("payer")) {
            execPayer(obj, params, res);
            return;
        }

        // cas particuliers (opérations qui existent dans les deux objets Scanette et Caisse)
        if (op.equals("abandon")) {
            execAbandon(obj);
            return;
        }
        if (op.equals("supprimer")) {
            execSupprimer(obj, params, res);
            return;
        }

        System.err.println("Unknown operation: " + op + " (line " + line + ")");
        System.exit(-1);

    }


    /*
     *  Specifiques à la scanette
     */
    private void execDebloquer(String sc, String res) throws Exception {
        sc = sc.trim();
        if (! scanettes.containsKey(sc)) {
            scanettes.put(sc, new Scanette(PRODUCTS_SCANETTE));
        }
        int r = scanettes.get(sc).debloquer();
        assertEquals((int)Double.parseDouble(res), r);
    }
    private void execScanner(String sc, String[] params, String res) throws Exception {
        sc = sc.trim();
        if (! scanettes.containsKey(sc)) {
            scanettes.put(sc, new Scanette(PRODUCTS_SCANETTE));
        }
        int r = scanettes.get(sc).scanner(Long.valueOf(params[0]));
        assertEquals((int)Double.parseDouble(res), r);
    }

    private void execTransmission(String sc, String[] params, String res) throws Exception {
        sc = sc.trim();
        if (! scanettes.containsKey(sc)) {
            scanettes.put(sc, new Scanette(PRODUCTS_SCANETTE));
        }
        params[0] = params[0].trim();
        if (! caisses.containsKey(params[0])) {
            caisses.put(params[0], new MaCaisse(PRODUCTS_CAISSE));
        }
        int resI = (int)Double.parseDouble(res);
        ((MaCaisse) caisses.get(params[0])).THRESHOLD = (resI == 1) ? 1 : 0;
        int r = scanettes.get(sc).transmission((MaCaisse) caisses.get(params[0]));
        assertEquals(resI, r);
    }

    /*
     *  Specifiques à la caisse
     */
    private void execOuvrirSession(String c, String res) throws Exception {
        c = c.trim();
        if (! caisses.containsKey(c)) {
            caisses.put(c, new MaCaisse(PRODUCTS_CAISSE));
        }
        int r = caisses.get(c).ouvrirSession();
        assertEquals((int)Double.parseDouble(res), r);
    }
    private void execFermerSession(String c, String res) throws Exception {
        c = c.trim();
        if (! caisses.containsKey(c)) {
            caisses.put(c, new MaCaisse(PRODUCTS_CAISSE));
        }
        int r = caisses.get(c).fermerSession();
        assertEquals((int)Double.parseDouble(res), r);
    }
    private void execAjouter(String c, String[] params, String res) throws Exception {
        c = c.trim();
        if (! caisses.containsKey(c)) {
            caisses.put(c, new MaCaisse(PRODUCTS_CAISSE));
        }
        int r = caisses.get(c).scanner(Long.valueOf(params[0]));
        assertEquals((int)Double.parseDouble(res), r);
    }
    private void execPayer(String c, String[] params, String res) throws Exception {
        c = c.trim();
        if (! caisses.containsKey(c)) {
            caisses.put(c, new MaCaisse(PRODUCTS_CAISSE));
        }
        double r = caisses.get(c).payer(Double.valueOf(params[0]));
        assertEquals(Double.parseDouble(res), r, 0.01);
    }

    /*
     *  Communes à la caisse et à la scanette
     */
    private void execAbandon(String c) throws Exception {
        c = c.trim();
        if (! scanettes.containsKey(c) && c.startsWith("s")) {
            scanettes.put(c, new Scanette(PRODUCTS_SCANETTE));
        }
        if (scanettes.containsKey(c)) {
            scanettes.get(c).abandon();
            return;
        }
        if (! caisses.containsKey(c) && c.startsWith("c")) {
            caisses.put(c, new MaCaisse(PRODUCTS_CAISSE));
        }
        if (caisses.containsKey(c)) {
            caisses.get(c).abandon();
            return;
        }
        Assert.fail();
    }
    private void execSupprimer(String c, String[] params, String res) throws Exception {
        c = c.trim();
        if (scanettes.containsKey(c)) {
            int r = scanettes.get(c).supprimer(Long.parseLong(params[0]));
            assertEquals((int)Double.parseDouble(res), r);
            return;
        }
        if (caisses.containsKey(c)) {
            int r = caisses.get(c).supprimer(Long.parseLong(params[0]));
            assertEquals((int)Double.parseDouble(res), r);
            return;
        }
        Assert.fail();
    }
}
