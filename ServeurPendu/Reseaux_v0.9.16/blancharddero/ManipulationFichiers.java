package com.blancharddero;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ManipulationFichiers {

    public static boolean connectionJoueur(String joueur){

        HashMap<String, String> listPlayers = new HashMap<>();

        if (joueur.split(":").length != 2) return false;
        String username = joueur.split(":")[0];
        String password = joueur.split(":")[1];

        // Try-catch avec un BufferedReader en ressource
        try (BufferedReader lecteur = new BufferedReader(new FileReader("blancharddero/playersData"))) {

            String ligne;
            // Lecture ligne par ligne du fichier
            while ((ligne = lecteur.readLine()) != null) {

                // Sépare la ligne entre clé et valeur
                String[] parties = ligne.split(":");

                if (parties.length == 2) {
                    // Get the username
                    String clé = parties[0].trim();

                    // Get the password
                    String value = parties[1].trim();

                    // Add to map
                    listPlayers.put(clé, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!listPlayers.keySet().contains(username)) return false;

        return listPlayers.get(username).equals(password);
    }

    public static HashMap<Integer, ArrayList<String>> lireListeMots(String cheminFichier){

        HashMap<Integer, ArrayList<String>> listeMots = new HashMap<>();

        // Try-catch avec un BufferedReader en ressource
        try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(new FileInputStream(("blancharddero/" + cheminFichier)), "UTF-8"))) {

            String ligne;
            // Lecture ligne par ligne du fichier
            while ((ligne = lecteur.readLine()) != null) {

                // Sépare la ligne entre clé et valeur
                String[] parties = ligne.split(":");

                if (parties.length == 2) {
                    // Récupérer et convertir la clé en entier
                    int clé = Integer.parseInt(parties[0].trim());

                    // Récupérer les valeurs en tant qu'ArrayList<String>
                    String[] valeurs = parties[1].trim().split(",");
                    ArrayList<String> listeValeurs = new ArrayList<>();
                    for (String valeur : valeurs) {
                        listeValeurs.add(valeur.trim());
                    }

                    // Ajouter à la map
                    listeMots.put(clé, listeValeurs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listeMots;
    }
}