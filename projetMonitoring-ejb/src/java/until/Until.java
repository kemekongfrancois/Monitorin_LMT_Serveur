/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package until;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author KEF10
 */
public class Until {
    public static String fichieLog = "log.txt";

    /**
     * cette fonction prend en entre un fichier et retourne son contenu dans une liste
     * chaque élèment de la liste es une ligne du fichier
     *
     * @param nomFichier
     * @return "null" si le fichier n'existe pas
     */
    public static List<String> lectureFichier(String nomFichier) {

        File f = new File(nomFichier);
        if (f.exists()) {//on fait le traitement que si le fichier existe
            try {
                System.out.println("----------debut du fichier \" "+ nomFichier +"\" ------------");
                List<String> result = new ArrayList<>();
                try (Scanner scanner = new Scanner(f)) {
                    // On boucle sur chaque champ detecté
                    String ligne;
                    while (scanner.hasNextLine()) {
                        ligne = scanner.nextLine();
                        result.add(ligne);
                        System.out.println("***" + ligne);
                    }
                }
                System.out.println("----------fin du fichier \" "+ nomFichier +"\" ------------");

                return result;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Until.class.getName()).log(Level.SEVERE, null, ex);
                savelog("" + ex, fichieLog);
            }
        }else{
            savelog(nomFichier + ": ce fichier n'existe pas", fichieLog);
        }
        return null;//
    }

    /**
     * cette fonction permet d'écrire le message pris en parametre dans le
     * fichier de log donc le nom est pris en parametre
     *
     * @param msg
     * @param nomfichier
     */
    private static void savelog(String msg, String nomfichier) {
        BufferedWriter bufWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(nomfichier, true);
            bufWriter = new BufferedWriter(fileWriter);

            //construction de la date
            Date dateCourant = new Date();
            Calendar calendHeureCourant = new GregorianCalendar();
            calendHeureCourant.setTime(dateCourant);
            String dateString = calendHeureCourant.get(Calendar.DATE)
                    + "-" + calendHeureCourant.get(Calendar.MONTH)
                    + "-" + calendHeureCourant.get(Calendar.YEAR)
                    + " " + calendHeureCourant.get(Calendar.HOUR_OF_DAY)
                    + ":" + calendHeureCourant.get(Calendar.MINUTE)
                    + ":" + calendHeureCourant.get(Calendar.SECOND);

            //Insérer un saut de ligne
            bufWriter.newLine();
            bufWriter.write("*****************  " + dateString + "  *************************");
            bufWriter.newLine();
            bufWriter.write(msg);
            bufWriter.newLine();
            bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
