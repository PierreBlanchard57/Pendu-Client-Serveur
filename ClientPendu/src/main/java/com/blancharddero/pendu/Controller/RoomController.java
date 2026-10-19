package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Connexion.ConnexionUDP;
import com.blancharddero.pendu.Menu;
import com.blancharddero.pendu.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RoomController extends Controller {

    @FXML
    private Label roomName,roomOwner,playerNumber;
    @FXML
    private ListView<String> players;
    @FXML
    public void initialize() {
        Thread thread=new Thread(() -> {
            try {
                handleUpdates();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();


        roomName.setText(roomNameText);
        roomOwner.setText("Salon de "+ hostUsername);
        playerNumber.setText("Nombre de joueurs : "+nbPlayers);
        Platform.runLater(() -> {
            roomName.setLayoutX((480-roomName.getWidth())/2);
            roomOwner.setLayoutX((480-roomOwner.getWidth())/2);

            if(guestUsername==null){//configuration hôte
                FXMLLoader fxmlLoader=new FXMLLoader(Menu.class.getResource("roominterface-owner.fxml"));
                fxmlLoader.setController(new OwnerRoomController(players));
                try {
                    Pane pane=fxmlLoader.load();
                    pane.setLayoutX(10);
                    pane.setLayoutY(210);
                    Utils.getSceneRoot(roomName).getChildren().add(pane);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            //configuration invité
            else{
                players.getItems().addAll(playersNames);
            }
        });



    }
    String roomNameText, hostUsername,guestUsername;//host=propriétaire     guest=personne actuelle qui se connecte au salon
    int nbPlayers=0;
    ArrayList<String> playersNames;
    //constructeur hôte,il n'y a que l'hote dans le salon.
    public RoomController(String roomName,String hostUsername){
        roomNameText=roomName;
        this.hostUsername =hostUsername;
        guestUsername=null;
    }
    //constructeur invité
    public RoomController(String roomName, String hostUsername, String guestUsername, ArrayList<String> players){
        roomNameText=roomName;
        this.hostUsername =hostUsername;
        this.guestUsername=guestUsername;
        this.playersNames=players;
        this.nbPlayers=players.size();//le nombre de joueurs présents dans le salon+la personne qui arrive
    }
    private void handleUpdates() {
        while (true){
            //mise à jour de la liste des joueurs
            HashMap<String,Object> message=new HashMap<>();
            try {
                ConnexionTCP.receiveMessage(message,200);
                if(message.get("update").equals("players")){
                    ConnexionUDP.setServerChatPort((Integer) message.get("port"));
                    Platform.runLater(() -> {
                        players.getItems().clear();
                        players.getItems().addAll((ArrayList<String>) message.get("players"));
                        playerNumber.setText("Nombre de joueurs : "+players.getItems().size());

                    });
                }//mise à jour des infos du jeux et accessoirement demarrage du jeu
                else if (message.get("update").equals("game")) {
                    if(guestUsername==null){
                        Platform.runLater(() -> {
                            try {
                                Utils.changeScene(Utils.getStage(players),"game-view.fxml",new GameController(
                                        (String) message.get("word"),
                                        (int) message.get("lives"),(int)message.get("timer"),
                                        true,guestUsername==null?hostUsername:guestUsername,
                                        (String) message.get("currentPlayer"),roomNameText
                                ),780,480);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    else{
                        Platform.runLater(() -> {
                            try {
                                Utils.changeScene(Utils.getStage(players),"game-view.fxml",new GameController(
                                        (String) message.get("word"),
                                        (int) message.get("lives"),(int)message.get("timer"),
                                        false,guestUsername==null?hostUsername:guestUsername,
                                        (String) message.get("currentPlayer"),roomNameText
                                ),780,480);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    break;
                }
            }
            catch (Exception e){
            }

        }
    }
}
