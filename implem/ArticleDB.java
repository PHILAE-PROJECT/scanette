package fr.ufc.l3info.oprog;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Frederic Dadeau
 * Date: 26/08/2018
 * Time: 10:48
 */
public class ArticleDB {

    private LinkedHashMap<Long, Article> DB = new LinkedHashMap<Long, Article>();
    
    /** Initialise la base de données des articles avec un fichier CSV */
    public void init(String initFile) throws IOException, FileFormatException {
        if (initFile == null || !initFile.endsWith(".csv"))
            throw new IOException();

        LinkedHashMap<Long, Article> temp = new LinkedHashMap<Long, Article>();
        FileReader reader = new FileReader(new File(initFile));
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length != 3 && tokens.length != 4)
                throw new FileFormatException(initFile);
            try {
                long ean13 = Long.parseLong(tokens[0]);
                double pu = Double.parseDouble(tokens[1]);
                String nom = tokens[2].trim();
                int reduc = 0;
                if (tokens.length == 4) {
                    tokens[3] = tokens[3].trim();
                    if (tokens[3].charAt(tokens[3].length()-1) != '%') {
                        throw new FileFormatException(initFile);
                    }
                    tokens[3] = tokens[3].substring(0, tokens[3].length()-1);
                    reduc = Integer.parseInt(tokens[3]);
                    pu = Math.floor((pu - (pu * reduc / 100.0)) * 100) / 100;
                }
                if (pu >= 0) {
                //if (false && pu > 0) {
                    Article art = new Article(ean13, pu, nom);
                    if (!art.isValidEAN13()) {
                    //if (false && !art.isValidEAN13()) {
                        throw new FileFormatException(initFile);
                    }
                    temp.put(ean13, art);
                    // DB.put(ean13, art);
                }
                else {
                    throw new FileFormatException(initFile);
                }
            }
            catch (NumberFormatException e) {
                throw new FileFormatException(initFile);
            }
        };
        DB = temp;
    }

    public Article getArticle(long _ean13) throws ArticleNotFoundException {
        if (! DB.containsKey(_ean13)) {
        // if (false && ! DB.containsKey(_ean13)) {
            throw new ArticleNotFoundException(_ean13);
        }
        return DB.get(_ean13);
    }

    public int getTailleDB() {
        return DB.size();
    }
    
}

class ArticleNotFoundException extends Exception {

    public ArticleNotFoundException(long _ean13) {
        super("Article " + _ean13 + " non trouvé dans la base de données");
    }
}

class FileFormatException extends Exception {
    public FileFormatException(String filename) {
        super("Le fichier " + filename + " n'est pas correctement formaté.");
    }
}