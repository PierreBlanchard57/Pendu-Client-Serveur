package com.blancharddero.pendu;

import java.io.IOException;
import java.util.HashMap;

import com.blancharddero.ClientHandler;

public class RoomChoiceState implements GameState {
    private final ClientHandler client;

    public RoomChoiceState(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handleMessage(Object message) throws IOException {
        /**
         * Receive the choice of the client
         * The possible arguments of the map are:
         * "action": To refresh the selection of Sessions or cancel to go back to main menu
         * "roomName": The name of the room the client desire to connect
         * "roomPass": The Password for that room, "" if none
         */
        @SuppressWarnings("unchecked")
        HashMap<String, Object> choice = (HashMap<String, Object>) message;

        // Check if client's choice is to refresh the selection or to cancel and go back to the main menu
        if(choice.get("action") != null){
            if (choice.get("action").equals("refresh")) {
                client.sendMessage(MainMenuState.getSessions());
                return;
            } else if (choice.get("action").equals("cancel")){
                client.sendMessage("canceling");
                client.setCurrentState(new MainMenuState(client));
                return;
            }
        }

        // Test if the client inputed the right room password
        SessionMulti session = MainMenuState.getRoom((String)choice.get("roomName"));
        client.setUDPport((int)choice.get("port"));
        synchronized(session){
            if(session.testPassword((String)choice.get("roomPass"))){
                if (!choice.get("roomPass").equals("")){
                    client.sendMessage("Yes");
                }

                // Add the client to the Session of another client
                session.addPlayer(client);

                client.setCurrentState(new MultiplayerRoomState(client, session));
            } else client.sendMessage("No");
        }

    }
}