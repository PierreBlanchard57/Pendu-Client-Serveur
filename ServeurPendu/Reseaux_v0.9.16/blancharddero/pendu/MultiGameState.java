package com.blancharddero.pendu;

import java.io.IOException;

import com.blancharddero.ClientHandler;

public class MultiGameState implements GameState {
    private final ClientHandler client;
    private SessionMulti sessionMulti;

    public MultiGameState(ClientHandler client, SessionMulti sm) {
        this.client = client;
        sessionMulti = sm;
    }

    @Override
    public void handleMessage(Object message) throws IOException {
        if (message == null || !(message instanceof String)) return;

        if(sessionMulti.getPlayerToPlay().equals(client.getUsername())){
            String letter = (String) message;
            System.out.println("Receiving letter: "+letter);

            sessionMulti.receiveLetter(letter);
            sessionMulti.nextPlayer();
        }

    }
    
}
