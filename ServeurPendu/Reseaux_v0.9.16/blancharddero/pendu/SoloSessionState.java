package com.blancharddero.pendu;

import java.io.IOException;
import java.util.HashMap;

import com.blancharddero.ClientHandler;

public class SoloSessionState implements GameState {
    private final ClientHandler client;
    private SessionSolo ss;
    private Boolean inGame;

    public SoloSessionState(ClientHandler client) {
        this.client = client;
        ss = new SessionSolo(client);
        inGame = false;
    }

    public void startGame(HashMap<String, Object> params){
        inGame = true;
        ss.startSession(params);
    }

    @Override
    public void handleMessage(Object message) throws IOException {
        if (message == null) return;

        if(inGame && !ss.getGame().isEnded()){
            // Receive letter
            System.out.println(client.getUsername() + ": Received letter...");
            String letter = (String) message;

            // Call updateUsedLetters with the received letter
            if(letter != null) ss.receiveLetter(letter);
            System.out.println(client.getUsername() + ": " + ss.getGame().getCurrentWord() + " lives: " + ss.getGame().getLives());

            // Exit the game loop if the word is found or lives/timer are depleted
            if (ss.getGame().isEnded()) inGame = false;

            // Send the updated game
            ss.sendUpdate();
        } else {
            if(message instanceof String) return;
            @SuppressWarnings("unchecked")
            HashMap<String, Object> input = (HashMap<String, Object>) message;

            if("cancel".equals(input.get("update"))) {
                client.setCurrentState(new MainMenuState(client));
                return;
            }

            // Start a game
            inGame = true;
            ss.startSession(input);
        }

    }
}
