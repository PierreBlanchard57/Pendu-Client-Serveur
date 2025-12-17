package com.blancharddero.pendu;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.blancharddero.ClientHandler;

public class SessionSolo extends Session{

    private ScheduledExecutorService scheduler;
    private Game game;

    public SessionSolo(ClientHandler user) {
        super(user);
        scheduler = Executors.newScheduledThreadPool(2);
    }

    @Override
    // Session to play alone with the server
    public void startSession(HashMap<String, Object> parameters) {
        // Start a game
        System.out.println(getHost() + " started a solo game");
        startGame(new Game(parameters));
    }

    /**
     * Return the HashMap filled with the data of the Game required to update all of the clients
     * @param g The game to get the data from
     * @return the HashMap filled with the data of the Game required to update all of the clients
     */
    private HashMap<String, Object> getDataToSend(Game g){

        // Prepare the data to send
        HashMap<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("lives", g.getLives());
        dataToSend.put("word", g.getCurrentWord());
        dataToSend.put("timer", g.getTimer());

        return dataToSend;

    }

    // Send the updated game to the player or if the game has ended send the final state of the game
    public void sendUpdate(){
        if (game.isEnded()) {
            closeAll();

            if (game.getLives() < 0 || (game.getTimer() <= 0 && game.isTimed())) {
                System.out.println(getHost().getUsername() + ": Partie Perdue !");
            } else System.out.println(getHost().getUsername() + "Partie Gagné !");

            // Send the final state of the game with the completed word
            HashMap<String, Object> data = getDataToSend(game);
            data.replace("word", game.getWord());
            getHost().sendMessage(data);
        } else getHost().sendMessage(getDataToSend(game));
    }

    public void receiveLetter(String letter) {
        game.updateUsedLetters(letter);
    }

    private void startGame(Game g){
        game = g;

        // Choose the word from the database
        g.initiateWord();

        //Send back the empty word
        System.out.println("Message choisit : " + g.getWord());
        getHost().sendMessage(getDataToSend(g));

        // Start the timer to decrement time every second
        if (g.isTimed()) {
            scheduler.scheduleAtFixedRate(() -> {
                g.decreaseTimer();
                if (g.getTimer() <= 0 && !scheduler.isShutdown()) {
                    System.out.println(getHost().getUsername() + ": Time's up!");
                    sendUpdate();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        // Start another task to send game state every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            if (!scheduler.isShutdown()) getHost().sendMessage(getDataToSend(g));
        }, 5, 5, TimeUnit.SECONDS);

    }

    public Game getGame(){
        return game;
    }

    // Close all ressources
    public void closeAll(){
        System.out.println(getHost() + ": Closing Session Solo");
        // Shutdown threads used for the game
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
