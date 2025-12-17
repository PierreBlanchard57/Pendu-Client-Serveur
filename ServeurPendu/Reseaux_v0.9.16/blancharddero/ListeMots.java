package com.blancharddero;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Map;

public class ListeMots {

    // Liste des mots du serveur stockés en fonction de leurs taille (taille -> (mots))
    private Map<Integer, ArrayList<String>> motsMap;
    private static ListeMots instance;

    private ListeMots(){

        // Chemin du fichier contenant les mots
        String cheminFichierListeMots = "listeMots.txt";

        // Appel de la méthode pour lire le fichier contenant les mots
        motsMap = ManipulationFichiers.lireListeMots(cheminFichierListeMots);

    }

    public String choisirMot(int longueurMin, int longueurMax){

        ArrayList<String> motsAdmissibles = new ArrayList<>();

        // Ajoute dans les mots admissibles les listes des mots de longueur correspondant aux paramètres donnés
        for (int i = longueurMin; i <= longueurMax; i++) motsAdmissibles.addAll(motsMap.get(i));

        // Retourne un mot choisit aléatoirement sans accents et en majuscule
        return Normalizer.normalize(motsAdmissibles.get((int)(Math.random()*motsAdmissibles.size())), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
    }

    public static ListeMots getInstance(){
        if(instance==null){
            instance = new ListeMots();
        }
        return instance;
    }
    
}
