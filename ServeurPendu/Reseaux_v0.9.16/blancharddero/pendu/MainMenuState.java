package com.blancharddero.pendu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blancharddero.ClientHandler;

public class MainMenuState implements GameState {
    private final ClientHandler client;
    private static HashMap<String, SessionMulti> multiplayer_sessions = new HashMap<String, SessionMulti>();

    public MainMenuState(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handleMessage(Object message) throws IOException {
        if (message == null) return;

        // Delete the Room if the Host is back to the Main Menu
        if (client.getRoomName() != null) removeRoom(client.getRoomName());
        client.setRoomName(null);

        /**
         * Read the Client's choosed game mode
         * The possible arguments of the map are:
         * "mode": To choose between a singleplayer or multiplayer Session
         * "action": To choose between joining or creating a multiplayer Session
         * "roomName": In case of creating a multiplayer Session, the name of the room
         * "roomPassword": In case of creating a multiplayer Session, the password of the room, "" if no password
         * "parameters": In case of a Singleplayer Session, the parameters of a game or room parameter
         * */
        @SuppressWarnings("unchecked")
        HashMap<String, Object> input = (HashMap<String, Object>) message;

        // If the mode is single then start a Solo Game Session
        if (input.get("mode").equals("single")) {
            SoloSessionState sss = new SoloSessionState(client);
            client.setCurrentState(sss);
            sss.startGame(input);

        } else if (input.get("mode").equals("multi")) {
            // Else look if the client wants to join or create a multiplayer Session
            switch (input.get("action").toString()) {
                case "join": // The client wants to join
                    client.sendMessage(getSessions());
                    client.setCurrentState(new RoomChoiceState(client));
                    break;

                case "create": // The client wants to create a new Session
                    // Create a new multiplayer Session with the client as Host and add it to the list of multiplayer sessions
                    String chosenRoomName = (String)input.get("roomName");
                    client.setUDPport((int)input.get("port"));
                    SessionMulti sm = new SessionMulti(client);
                    client.sendMessage("ok");
                    sm.startSession(input);
                    client.setRoomName(chosenRoomName);
                    multiplayer_sessions.put(chosenRoomName, sm);
                    client.setCurrentState(new MultiplayerRoomState(client, sm));
                    break;

                default:
                    break;
            }
        } else {
            System.out.println("Gamemode unknown");
        }
    }

    /**
     * Retourne toute les rooms formattées pour l'envoie au client
     * @return Retourne une HashMap des Rooms multijoueur disponible
     */
    public static HashMap<String, Object> getSessions(){
        HashMap<String, Object> data = new HashMap<>();

        synchronized(multiplayer_sessions){
            // Send the list of available Rooms
            multiplayer_sessions.forEach((key, value) -> {
                data.put(value.getRoomName(), Map.of("password", (value.isPasswordNull()?"":"True"), "maxPlayer", value.getMaxPlayer(), "nbPlayer", value.getNbActivePlayer(), "nbWaiting", value.getNbWaitingPlayer(), "host", value.getHost().getUsername()));
            });
        }

        return data;
    }

    /**
     * Retourne la Room demandée
     * @param roomName Le nom de la Room
     * @return L'Objet représantant la Room
     */
    public static SessionMulti getRoom(String roomName){
        synchronized(multiplayer_sessions){
            return multiplayer_sessions.get(roomName);
        }
    }

    public static void removeRoom(String roomName){
        synchronized(multiplayer_sessions){
            multiplayer_sessions.remove(roomName);
        }
    }

}
