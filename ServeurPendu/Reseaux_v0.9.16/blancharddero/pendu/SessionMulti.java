package com.blancharddero.pendu;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.blancharddero.ClientHandler;


public class SessionMulti extends Session{

    // Information about the room
    private String roomName;
    private String roomPassword;
    private boolean inGame;
    private int maxPlayer;

    // Lists of the logged players
    private Map<String, ClientHandler> players;
    // Lists of the logged players waiting next round
    private Map<String, ClientHandler> waitingPlayers;

    // Some infos for the game
    private int indexToPlay;
    private String lastLetter = null;
    private Game game;
    private int InitialindividualTimer = 10;
    private int individualTimer;
    private String lastDeco;

    // Thread variables
    private Boolean senderThreadActive;
    private int chatPort;
    DatagramSocket chatSocket;
    private ScheduledExecutorService schedulerRoom;
    private ScheduledExecutorService schedulerGame;
    private BlockingQueue<Object> queue = new LinkedBlockingQueue<>();

    /**
     * Constructor of a multiplayer Session
     * @param user The host of this multiplayer Session
     */
    public SessionMulti(ClientHandler user){
        super(user);
        players = new HashMap<>();
        waitingPlayers = new HashMap<>();
        inGame = false;
        maxPlayer = 4;
        indexToPlay = 0;
    }

    /**
     * If there is not the maximum of player in the room: Add a new player in the room or the waiting list if the room is in game
     * @param player the client to add
     * @return True if added then False
     **/
    public Boolean addPlayer(ClientHandler player){
        // Check if room is full
        if (players.size() + waitingPlayers.size() >= maxPlayer) return false;

        // Add the new player
        (inGame?waitingPlayers:players).put(player.getUsername(), player);

        if(inGame) return true;

        // Send the updated list of players
        HashMap<String, Object> data = new HashMap<>();
        data.put("players", new ArrayList<String>(players.keySet()));
        data.put("update", "players");
        data.put("port", chatPort);
        queue.add(data);

        return true;
    }

    private void checkDisconnected(){
        if (getHost().isDisconnected()) {
            closeAll();
            return;
        }
        players.forEach((key, value) -> {
            if (value.isDisconnected()){
                disconnectPlayer(key);
            }
        });
        if(!inGame){
            // Send the updated list of players
            HashMap<String, Object> data = new HashMap<>();
            data.put("players", new ArrayList<String>(players.keySet()));
            data.put("update", "players");
            queue.add(data);
        }
    }

    /**
     * Add waiting players to the list of active players
     */
    private void activateWaintingPlayers(){
        players.putAll(waitingPlayers);
    }

    /**
     * Remove a player from the room
     * @param p The pseudo of the player to disconnect
     */
    private void disconnectPlayer(String p){
        // Notify the waiting socket of the player and remove them from the room
        ClientHandler s = null;
        if (players.containsKey(p)) s = players.get(p);
        else if (waitingPlayers.containsKey(p)) s = waitingPlayers.get(p);
        synchronized(players){
            if (players.containsKey(p) || waitingPlayers.containsKey(p)) {
                players.remove(p);
                waitingPlayers.remove(p);
                lastDeco = s.getUsername();
                if(s != null) s.setCurrentState(new MainMenuState(s));
            }
            if (inGame && s!=null) nextPlayer();
        }

        // if there are no active players left in the room while a game is taking place, try to activate some waiting player
        //or if there is no waiting players close the session because the game cannot continue.
        if (players.size() == 0 && inGame) {
            if(waitingPlayers.size()==0 && inGame) closeAll();
            else activateWaintingPlayers();
        }
    }

    /**
     * Send an update to all player
     * @param dataToSend The data to be sended
     */
    public void broadcastUpdate(Object dataToSend){
        synchronized(players){
            players.forEach((key, value) -> {
                value.sendMessage(dataToSend);
                if (value.isDisconnected()){
                    System.out.println("Connexion error when sending update to: "+key);
                    disconnectPlayer(key);
                }
            });
        }
        if(getHost().isDisconnected()) return;
        else {
            getHost().sendMessage(dataToSend);
        }
    }

    /**
     * Return the HashMap filled with the data of the Game required to update all of the clients
     * @param g The game to get the data from
     * @return the HashMap filled with the data of the Game required to update all of the clients
     */
    private HashMap<String, Object> getGameDataToSend(Game g){

        // Prepare the data to send
        HashMap<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("lives", g.getLives());
        dataToSend.put("word", g.getCurrentWord());
        dataToSend.put("timer", g.getTimer());
        dataToSend.put("currentPlayer", getPlayerToPlay());
        dataToSend.put("update", "game");
        dataToSend.put("letter", lastLetter);
        if (lastLetter != null) lastLetter = null;
        if (lastDeco != null) {
            dataToSend.put("disconnected", lastDeco);
            lastDeco = null;
        }

        return dataToSend;
    }

    @Override
    public void startSession(HashMap<String, Object> parameters) {

        // Extract the name and password of the room
        roomName = (String) parameters.get("roomName");
        roomPassword = (String) parameters.get("roomPassword");
        if (roomPassword == null) roomPassword = "";

        // Start the writer thread
        // Writer thread that handles all writes
        senderThreadActive = true;
        Thread writer = new Thread( () -> {
            try {
                while (senderThreadActive) {
                    Object obj = queue.take(); // Blocks until there is an object to send
                    broadcastUpdate(obj);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("##ERROR## " + getHost() + ": Error when waiting update from the queue");
                closeAll();
            }
        });
        writer.start();

        // Start the chat server
        startChatServer();

        // Inform host about the chat port
        HashMap<String, Object> chatInfo = new HashMap<>();
        chatInfo.put("port", chatPort);
        queue.add(chatInfo);

        // Define the Scheduled services
        schedulerRoom = Executors.newScheduledThreadPool(1);

        // Check if some players disconnected
        schedulerRoom.scheduleAtFixedRate(() -> {
            checkDisconnected();
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void startGame(Game g){
        inGame = true;
        game = g;

        players.forEach((key, value) -> {
            value.setCurrentState(new MultiGameState(value, this));
        });

        schedulerGame = Executors.newScheduledThreadPool(2);

        // Start the timer to decrement time every second
        if (g.isTimed()) {
            schedulerGame.scheduleAtFixedRate(() -> {
                g.decreaseTimer();
                individualTimer -= 1;
                if (g.getTimer() <= 0) {
                    System.out.println(getHost() + ": Time's up!");
                    inGame = false;
                    queue.add(getGameDataToSend(g));
                }
                if (individualTimer <= 0){
                    System.out.println(getPlayerToPlay() + " took too long, next!");
                    individualTimer = InitialindividualTimer;
                    nextPlayer();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        // Start another task to send game state every 5 seconds
        schedulerGame.scheduleAtFixedRate(() -> {
            if (g.isTimed() && g.getTimer() <=0) return;
            queue.add(getGameDataToSend(g));
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Starts the Room's server
    public void startChatServer() {
        Thread chatThread = new Thread(() -> {
            try {
                chatSocket = new DatagramSocket(0);
                chatPort = chatSocket.getLocalPort(); // Dynamically assign a free port
                System.out.println("Chat server started on UDP port: " + chatPort);

                byte[] buffer = new byte[1024];
                while (!chatSocket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        chatSocket.receive(packet); // Blocking call
                        String message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Received chat message: " + message);

                        // Broadcast to all players
                        synchronized (players) {
                            for (ClientHandler player : players.values()) {
                                InetAddress playerAddress = player.getSocket().getInetAddress();
                                int playerPort = player.getUDPport();
                                DatagramPacket responsePacket = new DatagramPacket(
                                        message.getBytes(),
                                        message.length(),
                                        playerAddress,
                                        playerPort
                                );
                                chatSocket.send(responsePacket);
                            }
                        }
                        // Send to Host too
                        InetAddress playerAddress = getHost().getSocket().getInetAddress();
                        int playerPort = getHost().getUDPport();
                        DatagramPacket responsePacket = new DatagramPacket(
                                message.getBytes(),
                                message.length(),
                                playerAddress,
                                playerPort
                        );
                        chatSocket.send(responsePacket);

                    } catch (SocketException e) {
                        if (chatSocket.isClosed()) {
                            System.out.println("Chat server socket closed. Exiting...");
                            break;
                        }
                        throw e;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in chat server: " + e.getMessage());
                e.printStackTrace();
            }
        });
        chatThread.start();
    }

    /**
     * In case of fatal error or Host's disconnection
     * Close all ressources, disconnect players and inform all players
     */
    public void closeAll(){
        System.out.println(getHost() + ": Closing Multiplayer Session");
        inGame = false;
        chatSocket.close();

        // Inform all players
        activateWaintingPlayers();
        HashMap<String, Object> closingMessage = new HashMap<>();
        closingMessage.put("update", "closing");
        broadcastUpdate(closingMessage);

        // Disconnect them all
        players.forEach((key, value) -> {
            disconnectPlayer(key);
        });

        schedulerRoom.shutdown();
        try {
            schedulerRoom.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error when shutting down the Room's Scheduler");
        }
        senderThreadActive = false;
        getHost().setCurrentState(new MainMenuState(getHost()));
        removeHost();
    }

    public String getRoomName(){
        return roomName;
    }

    public void nextPlayer(){
        indexToPlay++;
        if(indexToPlay >= players.keySet().size())indexToPlay=0;
        queue.add(getGameDataToSend(game));
    }

    public String getPlayerToPlay(){
        return new ArrayList<>(players.keySet()).get(indexToPlay);
    }

    public Boolean isinGame(){
        return inGame;
    }

    public void endGameIfNotInGame(){
        if (game.isEnded() && !inGame) {
            inGame = false;
            schedulerGame.shutdownNow();

            if (game.getLives() < 0 || (game.getTimer() <= 0 && game.isTimed())) {
                System.out.println(getHost() + ": Mot non trouvé !");
            } else System.out.println(getHost() + ": Mot trouvé !");

            // Send the final state of the game with the completed word
            HashMap<String, Object> data = getGameDataToSend(game);
            data.replace("word", game.getWord());
            queue.add(data);
            putAllPlayersInRoom();
        }
    }

    // Change the State of all Players in the game
    public void putAllPlayersInRoom(){
        players.forEach((key, value) -> {
            value.setCurrentState(new MultiplayerRoomState(value, this));
        });
    }

    // Receive a letter
    public void receiveLetter(String letter) {
        lastLetter = letter;
        // Call updateUsedLetters with the received letter
        synchronized(game){
            game.updateUsedLetters(letter);
        }

    }

    public int getMaxPlayer(){
        return maxPlayer;
    }

    public int getNbActivePlayer(){
        return players.size();
    }

    public int getNbWaitingPlayer(){
        return waitingPlayers.size();
    }

    public boolean isPasswordNull(){
        return roomPassword.equals("");
    }

    public Boolean testPassword(String pass){
        return roomPassword.equals(pass);
    }
}