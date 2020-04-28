package fr.ufc.l3info.oprog;

//
// Source code recreated from a ScanetteAdapter.class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.HashMap;
import org.junit.Assert;

public class ScanetteAdapter {
    final String PRODUCTS_SCANETTE = "resources/produitsScanette.csv";
    final String PRODUCTS_CAISSE = "resources/produitsCaisse.csv";
    HashMap<String, Scanette> scanettes = new HashMap();
    HashMap<String, Caisse> caisses = new HashMap();

    public ScanetteAdapter() {
    }

    public void process(int line, String obj, String op, String[] params, String res) throws Exception {
        if (op.equals("debloquer")) {
            this.execDebloquer(obj, res);
        } else if (op.equals("scanner")) {
            this.execScanner(obj, params, res);
        } else if (op.equals("transmission")) {
            this.execTransmission(obj, params, res);
        } else if (op.equals("ouvrirSession")) {
            this.execOuvrirSession(obj, res);
        } else if (op.equals("fermerSession")) {
            this.execFermerSession(obj, res);
        } else if (op.equals("ajouter")) {
            this.execAjouter(obj, params, res);
        } else if (op.equals("payer")) {
            this.execPayer(obj, params, res);
        } else if (op.equals("abandon")) {
            this.execAbandon(obj);
        } else if (op.equals("supprimer")) {
            this.execSupprimer(obj, params, res);
        } else {
            System.err.println("Unknown operation: " + op + " (line " + line + ")");
            System.exit(-1);
        }
    }

    private void execDebloquer(String sc, String res) throws Exception {
        sc = sc.trim();
        if (!this.scanettes.containsKey(sc)) {
            this.scanettes.put(sc, new Scanette("resources/produitsScanette.csv"));
        }

        int r = ((Scanette)this.scanettes.get(sc)).debloquer();
        Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
    }

    private void execScanner(String sc, String[] params, String res) throws Exception {
        sc = sc.trim();
        if (!this.scanettes.containsKey(sc)) {
            this.scanettes.put(sc, new Scanette("resources/produitsScanette.csv"));
        }

        int r = ((Scanette)this.scanettes.get(sc)).scanner(Long.valueOf(params[0]));
        Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
    }

    private void execTransmission(String sc, String[] params, String res) throws Exception {
        sc = sc.trim();
        if (!this.scanettes.containsKey(sc)) {
            this.scanettes.put(sc, new Scanette("resources/produitsScanette.csv"));
        }

        params[0] = params[0].trim();
        if (!this.caisses.containsKey(params[0])) {
            this.caisses.put(params[0], new MaCaisse("resources/produitsCaisse.csv"));
        }

        int resI = (int)Double.parseDouble(res);
        ((MaCaisse)this.caisses.get(params[0])).THRESHOLD = resI == 1 ? 1.0D : 0.0D;
        int r = ((Scanette)this.scanettes.get(sc)).transmission((MaCaisse)this.caisses.get(params[0]));
        Assert.assertEquals((long)resI, (long)r);
    }

    private void execOuvrirSession(String c, String res) throws Exception {
        c = c.trim();
        if (!this.caisses.containsKey(c)) {
            this.caisses.put(c, new MaCaisse("resources/produitsCaisse.csv"));
        }

        int r = ((Caisse)this.caisses.get(c)).ouvrirSession();
        Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
    }

    private void execFermerSession(String c, String res) throws Exception {
        c = c.trim();
        if (!this.caisses.containsKey(c)) {
            this.caisses.put(c, new MaCaisse("resources/produitsCaisse.csv"));
        }

        int r = ((Caisse)this.caisses.get(c)).fermerSession();
        Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
    }

    private void execAjouter(String c, String[] params, String res) throws Exception {
        c = c.trim();
        if (!this.caisses.containsKey(c)) {
            this.caisses.put(c, new MaCaisse("resources/produitsCaisse.csv"));
        }

        int r = ((Caisse)this.caisses.get(c)).scanner(Long.valueOf(params[0]));
        Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
    }

    private void execPayer(String c, String[] params, String res) throws Exception {
        c = c.trim();
        if (!this.caisses.containsKey(c)) {
            this.caisses.put(c, new MaCaisse("resources/produitsCaisse.csv"));
        }

        double r = ((Caisse)this.caisses.get(c)).payer(Double.valueOf(params[0]));
        Assert.assertEquals(Double.parseDouble(res), r, 0.01D);
    }

    private void execAbandon(String c) throws Exception {
        c = c.trim();
        if (!this.scanettes.containsKey(c) && c.startsWith("s")) {
            this.scanettes.put(c, new Scanette("resources/produitsScanette.csv"));
        }

        if (this.scanettes.containsKey(c)) {
            ((Scanette)this.scanettes.get(c)).abandon();
        } else {
            if (!this.caisses.containsKey(c) && c.startsWith("c")) {
                this.caisses.put(c, new MaCaisse("resources/produitsCaisse.csv"));
            }

            if (this.caisses.containsKey(c)) {
                ((Caisse)this.caisses.get(c)).abandon();
            } else {
                Assert.fail();
            }
        }
    }

    private void execSupprimer(String c, String[] params, String res) throws Exception {
        c = c.trim();
        int r;
        if (this.scanettes.containsKey(c)) {
            r = ((Scanette)this.scanettes.get(c)).supprimer(Long.parseLong(params[0]));
            Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
        } else if (this.caisses.containsKey(c)) {
            r = ((Caisse)this.caisses.get(c)).supprimer(Long.parseLong(params[0]));
            Assert.assertEquals((long)((int)Double.parseDouble(res)), (long)r);
        } else {
            Assert.fail();
        }
    }
}

