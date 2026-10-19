package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Connexion.ConnexionUDP;
import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Utils;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JoinRoomController extends Controller {
    @FXML
    ListView<Pane> rooms;
    @FXML
    private void back(ActionEvent e) throws Exception {
        HashMap<String,Object> message=new HashMap<>();
        message.put("mode","multi");
        message.put("action","cancel");
        StringBuilder answer=new StringBuilder();

        Stage stage=Utils.getStage((Node) e.getSource());
        Loader.doLoadingProcess(stage, message, answer, "TCP", new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.changeScene(stage,"multi-view.fxml",new MultiController(username),480,480);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }
    @FXML
    private  void refresh() throws IOException, InterruptedException {
        Stage stage=Utils.getStage(rooms);
        HashMap<String,Object> message=new HashMap<>();
        message.put("mode","multi");
        message.put("action","refresh");
        HashMap<String,Map<String,Object>> answer=new HashMap<>();
        Loader.doLoadingProcess(stage, message, answer, "TCP", new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.changeScene(stage,"joinroom-view.fxml",new JoinRoomController(username,answer),480,480);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    @FXML
    private void join(ActionEvent e) throws SocketException {
        Pane selectedPane=rooms.getSelectionModel().getSelectedItem();
        if(selectedPane!=null){
            Label nameLabel= (Label) selectedPane.lookup("#name");
            Label ownerLabel=(Label) selectedPane.lookup("#owner");
            //mot de pass requis
            if(roomsData.get(nameLabel.getText()).get("password").equals("True")){
                Scene scene=Utils.getScene(rooms);
                Stage stage= (Stage) scene.getWindow();

                Pane pane=new Pane();
                pane.setPrefWidth(480);
                pane.setPrefHeight(480);
                pane.setStyle("-fx-border-color: black");

                Label title=new Label("Mot de passe requis!");
                title.setFont(Font.font("Arial", FontWeight.BOLD,18));
                title.setLayoutX(170);
                title.setLayoutY(50);

                Label text=new Label("Veuillez saisir le mot de passe:");
                text.setFont(Font.font("Arial", FontWeight.NORMAL,12));
                text.setLayoutX(170);
                text.setLayoutY(150);

                TextField textField=new TextField();
                textField.setPrefWidth(200);
                textField.setLayoutX(170);
                textField.setLayoutY(170);

                Button button=new Button("Rejoindre");
                button.setFont(Font.font("Arial",FontWeight.BOLD,20));
                button.setLayoutX(170);
                button.setLayoutY(250);
                button.setOnMouseClicked(mouseEvent -> {
                    HashMap<String,Object> message=new HashMap<>();
                    message.put("mode","multi");
                    message.put("roomName",nameLabel.getText());
                    message.put("roomPass",textField.getText());
                    try {
                        ConnexionUDP.setDatagramSocketChat(new DatagramSocket());
                    } catch (SocketException ex) {
                        throw new RuntimeException(ex);
                    }
                    message.put("port",ConnexionUDP.getDatagramSocketChat().getLocalPort());

                    StringBuilder answer=new StringBuilder();
                    try {
                        Loader.doLoadingProcess(stage, message, answer, "TCP", () -> {
                            Pane root=Utils.getSceneRoot(rooms);
                            root.getChildren().remove(pane);
                            //mot de passe refusé,on reinitialise la scene
                            if(answer.toString().equals("No")){
                                try {
                                    Utils.changeScene(stage,"joinroom-view.fxml",new JoinRoomController(username,roomsData),480,480);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            //mot de passe accepté,on affiche le salon
                            else{
                                HashMap<String,Object> answer1 =new HashMap<>();
                                try {
                                    ConnexionTCP.sendMessage(null);
                                    ConnexionTCP.receiveMessage(answer1);
                                    Utils.changeScene(stage,"room-view.fxml",new RoomController(nameLabel.getText(),
                                            ownerLabel.getText().replace("Propriétaire: ",""),username,
                                            (ArrayList<String>) answer1.get("players")),480,480);
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }

                            }
                        });
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                pane.getChildren().addAll(title,text,textField,button);
                pane.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255,0.85), CornerRadii.EMPTY, Insets.EMPTY)));
                Pane root=Utils.getSceneRoot(rooms);
                root.getChildren().add(pane);
                //animation affichage
                ScaleTransition scaleTransition=new ScaleTransition(Duration.millis(600));
                scaleTransition.setNode(pane);
                scaleTransition.setFromX(0);
                scaleTransition.setFromY(0);
                scaleTransition.setToX(1);
                scaleTransition.setToY(1);
                scaleTransition.play();
            }
            //pas de mot de passe
            else{
                Stage stage= Utils.getStage((Node) e.getSource());
                HashMap<String,Object> message=new HashMap<>();
                message.put("mode","multi");
                message.put("roomName",nameLabel.getText());
                message.put("roomPass","");
                ConnexionUDP.setDatagramSocketChat(new DatagramSocket());
                message.put("port",ConnexionUDP.getDatagramSocketChat().getLocalPort());
                HashMap<String,Object> answer=new HashMap<>();
                try {
                    ConnexionTCP.sendMessage(message);
                    ConnexionTCP.receiveMessage(answer);

                    ConnexionUDP.setServerChatPort((Integer) answer.get("port"));
                    Utils.changeScene(stage,"room-view.fxml",new RoomController(nameLabel.getText(),
                            ownerLabel.getText().replace("Propriétaire: ",""),username,
                            (ArrayList<String>) answer.get("players")),480,480);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    @FXML
    private  void initialize(){
        for(Map.Entry<String,Map<String,Object>> roomData : roomsData.entrySet()){
            String name=roomData.getKey();
            String owner= (String) roomData.getValue().get("host");
            boolean password= !roomData.getValue().get("password").equals("");
            int playerNumber= (int) roomData.getValue().get("nbPlayer");
            int maxPlayer= (int) roomData.getValue().get("maxPlayer");
            rooms.getItems().add(createRoomPane(name,owner,password,playerNumber,maxPlayer));
        }
    }
    String username;
    HashMap<String,Map<String, Object>> roomsData;
    public JoinRoomController(String username, HashMap<String,Map<String, Object>> roomsData){
        this.username=username;
        this.roomsData= roomsData;
    }
    private Pane createRoomPane(String roomName,String roomOwner,boolean isPassword,int playerNumber,int maxPlayer){
        Rectangle icon=new Rectangle(30,30,Color.LIGHTGREEN);
        icon.setLayoutX(10);
        icon.setLayoutY(10);

        Label name=new Label(roomName);
        name.setLayoutX(50);
        name.setLayoutY(15);
        name.setFont(Font.font("Arial", FontWeight.BOLD,18));
        name.setId("name");

        Label owner=new Label("Propriétaire: "+roomOwner);
        owner.setId("owner");
        owner.setLayoutX(180);

        Label password=new Label("Mot de passe: "+(isPassword?"oui":"non"));
        password.setLayoutX(180);
        password.setLayoutY(30);

        Label players=new Label("Joueurs: "+playerNumber+"/"+maxPlayer);
        players.setLayoutX(340);

        Pane pane=new Pane(icon,name,owner,password,players);
        pane.setPrefHeight(50);
        pane.setStyle("-fx-border-color: black");
        return pane;
    }
}
