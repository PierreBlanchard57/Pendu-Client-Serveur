package com.blancharddero.pendu;

import java.io.IOException;
import java.util.HashMap;

import com.blancharddero.ClientHandler;

public class MultiplayerRoomState implements GameState {
    private final ClientHandler client;
    private SessionMulti sessionMulti;

    public MultiplayerRoomState(ClientHandler client, SessionMulti sm) {
        this.client = client;
        this.sessionMulti = sm;
    }

    @Override
    public void handleMessage(Object message) throws IOException {
        if(message == null || message instanceof String)return;

        System.out.println("RoomHost: " +sessionMulti.getHost().getUsername()+" Tentatve: "+ client.getUsername());
        // Seul l'host peut envoyer les paramètres d'une Game
        if (sessionMulti.isinGame() || !sessionMulti.getHost().getUsername().equals(client.getUsername())) return;

        @SuppressWarnings("unchecked") // Possible error managed by try-catch
        HashMap<String, Object> gameInfo = (HashMap<String, Object>) message;

        System.out.println(client.getUsername() + ": started a multiplayer game");
        sessionMulti.startGame(new Game(gameInfo));
    }


}
