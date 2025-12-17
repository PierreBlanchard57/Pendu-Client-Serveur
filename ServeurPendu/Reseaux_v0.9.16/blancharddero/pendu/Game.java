package com.blancharddero.pendu;

import java.io.Serial;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.blancharddero.ListeMots;

public class Game implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;
    private String word;
    private Set<String> usedLetters;
    private int lives;
    private String length;
    private String currentWord;
    private int timer;
    private boolean timed = true;
    private String regex = "[ABCDEFGHIJKLMNOPQRSTUVWXYZ]";
    

    public Game(HashMap<String, Object> map) {
        this.lives = (int)map.get("lives");
        this.length = (String) map.get("length");
        if ((int)map.get("timer") != 0) this.timer = (int)map.get("timer");
        else timed = false;
        if (map.containsKey("word")) {
            this.word = Normalizer.normalize((String)map.get("word"), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
            this.currentWord = word.replaceAll(regex, "_");
        }

    }

    /**
     * Used to see if a game is finished, either by winning or losing
     * @return true if finished or false still in progress
     */
    public Boolean isEnded(){
        return getCurrentWord().equals(getWord()) || getLives() < 0 || (getTimer() <= 0 && isTimed());
    }

    /**
     * Simple getter for the word variable
     * @return The full word of this game
     */
    public String getWord() {
        return word;
    }

    /**
     * Used to check if the game is timed
     * @return true if timed, false if not
     */
    public boolean isTimed(){
        return timed;
    }

    /**
     * Let the Server choose a word from its database to use for this game
     */
    public void initiateWord(){
        word = length.equals("random")? ListeMots.getInstance().choisirMot(3, 25): ListeMots.getInstance().choisirMot(Integer.parseInt(length.split(",")[0]), Integer.parseInt(length.split(",")[1]));
        currentWord = getWord().replaceAll("[A-Z]", "_");
    }

    /**
     * update the set of used letters and decease live if the letter is wrong, then update the current word of the game
     * @param letter The letter submitted by the player
     */
    public void updateUsedLetters(String letter){
        if (usedLetters == null) usedLetters = new HashSet<String>();
        regex = regex.replace(letter, " ");
        usedLetters.add(letter);
        if (!word.contains(letter)) lives--;
        else currentWord = word.replaceAll(regex, "_");
    }

    public String getCurrentWord(){
        return currentWord;
    }

    public int getLives(){
        return lives;
    }

    public int getTimer(){
        return timer;
    }

    public void decreaseTimer(){
        timer --;
    }

    public Set<String> getUsedLetters(){
        return usedLetters;
    }

    @Override
    public String toString() {
        return "Game{" +
                "lives=" + lives +
                ", minutes=" + timer +
                ", word='" + word + '\'' +
                '}';
    }
}
