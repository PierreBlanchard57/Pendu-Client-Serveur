package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Controller.Controller;
import com.blancharddero.pendu.Controller.MenuController;
import com.blancharddero.pendu.Controller.OptionController;
import com.blancharddero.pendu.Menu;
import com.blancharddero.pendu.Utils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;

public class GameController extends Controller {
    @FXML
    private Label livesLabel,wordLabel,time;
    @FXML
    private Pane paneTime;
    private int lives,seconds,minLength,maxLength,mode;//mode: 0=solo 1=multi:hote 2=multi:invité
    private String word,username,currentLetter,currentPlayer,roomName;
    private Scene roomScene;
    private ChatController chatController;

    //constructeur solo
    public GameController(String word, int minLength, int maxLength, int lives, int seconds, String username){
        this.lives=lives;
        this.seconds=seconds;
        this.word=word.replaceAll("_","_ ");
        this.minLength=minLength;
        this.maxLength=maxLength;
        this.username=username;
        this.mode=0;
        System.out.println("Game created with parameters: word="+word+" , lives="+lives+" , minutes="+seconds+", username="+username+",mode="+mode);
    }
    //constructeur multi:
    public GameController(String word,int lives,int seconds,boolean host,String username,String currentplayer,String roomName){
        this.word=word.replaceAll("[A-Za-z]","_ ");
        this.seconds=seconds;
        this.lives=lives;
        this.mode=host?1:2;
        this.username=username;
        this.currentPlayer=currentplayer;
        this.roomName=roomName;
    }

    @FXML
    public void initialize(){

        //initialisation des vies et du mot
        livesLabel.setText(String.valueOf(lives));
        word=word.replaceAll("([A-Z])","$1 ");
        wordLabel.setFont(Font.font("Arial", FontWeight.BOLD,18));
        wordLabel.setText(word.replaceAll("([A-Z])","_"));

        //creation du "clavier"+du chat si multi
        Platform.runLater(() -> {
            //activation du timer si necessaire
            if(seconds>0){
                paneTime.setVisible(true);
                time.setText(String.format("%02d",seconds/60)+":"+String.format("%02d",seconds%60));
            }
            wordLabel.setLayoutX((480-wordLabel.getWidth())/2);
            Scene scene= wordLabel.getScene();
            Pane root= (Pane) scene.getRoot();
            Pane letterPane=new Pane();
            letterPane.setId("letterPane");
            root.getChildren().add(letterPane);
            for(int i=0;i<26;i++){
                Button button=new Button();
                //if (!username.equals(currentPlayer) && mode==2)button.setDisable(true);
                button.setText(Character.toString((char)65+i));
                button.setId(button.getText());
                button.setFont(Font.font("Arial",FontWeight.BOLD,18));
                button.setStyle("-fx-background-color: linear-gradient(#00FFFF, #00CED1);-fx-border-color: #000000");
                button.setPrefWidth(46);
                button.setPrefHeight(46);
                button.setLayoutX(10+46*(i%10));
                button.setLayoutY(200+46*(i/10));
                if(mode!=1){
                    button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            Node node = (Node) mouseEvent.getTarget();
                            Stage stage= (Stage) button.getScene().getWindow();
                            String letter= button.getText();
                            try {
                                HashMap<String,Object>answer=new HashMap<>();
                                currentLetter=letter;
                                ConnexionTCP.sendMessage(letter);

                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });
                }
                letterPane.getChildren().add(button);

            }
            if(mode!=0){
                Label turn=new Label("Tour:"+currentPlayer);
                turn.setId("turn");
                root.getChildren().add(turn);
                if(!currentPlayer.equals(username) && mode==2)root.lookup("#letterPane").setDisable(true);
                //ajout du chat
                FXMLLoader fxmlLoader=new FXMLLoader(Menu.class.getResource("chat-view.fxml"));
                chatController=new ChatController(username);
                fxmlLoader.setController(chatController);
                try {
                    Pane chat=fxmlLoader.load();
                    chat.setLayoutX(480);
                    root.getChildren().add(chat);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //lancement du timer si necessaire
        if(seconds>0){
            Timeline timeline=new Timeline(new KeyFrame(Duration.seconds(1), event -> Platform.runLater(() -> {
                String[] timeData=time.getText().split(":");
                int correctedTime=Integer.parseInt(timeData[0])*60+Integer.parseInt(timeData[1])-1;
                time.setText(String.format("%02d",correctedTime/60)+":"+String.format("%02d",correctedTime%60));
            })));
            timeline.setCycleCount(seconds-1);
            timeline.play();
        }
        //lancement du thread de mise à jour automatique
        Thread thread=new Thread(() -> {
            try {
                handleUpdates();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    private void displayEndScreen(Pane root, String title,String word, Paint messageColor,Paint backgroundColor){
        Pane pane=new Pane();
        pane.setPrefWidth(480);
        pane.setPrefHeight(480);
        pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        Label label=new Label(title);
        label.setFont(Font.font("Arial",FontWeight.BOLD,48));
        label.setTextFill(messageColor);
        label.setLayoutX(180);
        label.setLayoutY(50);

        Label text=new Label("Le mot était : "+word);
        text.setFont(Font.font("Arial",FontWeight.BOLD,18));
        text.setTextFill(messageColor);
        text.setLayoutX(180);
        text.setLayoutY(120);

        HashMap<String,Object> message=new HashMap<>();
        message.put("update","cancel");

        Button again=new Button("Rejouer");
        if(mode!=0)again.setText("Quitter");
        again.setLayoutX(140);
        again.setLayoutY(200);
        again.setPrefWidth(200);
        again.setPrefHeight(50);
        again.setFont(Font.font("Arial",FontWeight.BOLD,18));
        again.setOnMouseClicked(mouseEvent -> {
            if (mode == 0) {
                try {
                    ConnexionTCP.sendMessage(message);
                    Utils.changeScene(Utils.getStage(root), "option-view.fxml",
                            new OptionController(username, minLength, maxLength, lives, seconds / 60), 480, 480);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    chatController.setStopListeningThread(true);
                    Utils.forceDisconnexion(Utils.getStage(root));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        Button menu =new Button("Menu");
        if(mode!=0)menu.setText("Quitter le jeu");
        menu.setLayoutX(140);
        menu.setLayoutY(300);
        menu.setPrefWidth(200);
        menu.setPrefHeight(50);
        menu.setFont(Font.font("Arial",FontWeight.BOLD,18));
        menu.setOnMouseClicked(mouseEvent -> {
            if (mode == 0) {
                try {
                    ConnexionTCP.sendMessage(message);
                    Utils.changeScene(Utils.getStage(root), "menu-view.fxml", new MenuController(username), 480, 480);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                chatController.setStopListeningThread(true);
                Utils.getStage(livesLabel).close();
            }
        });

        pane.getChildren().addAll(again,menu,label,text);
        root.getChildren().add(pane);

        FadeTransition fadeTransition=new FadeTransition(Duration.millis(1000));
        fadeTransition.setNode(pane);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
    private void handleUpdates() throws Exception {
        final boolean[] running = {true};
        while (running[0]){
            HashMap<String,Object> map= new HashMap<>();
            ConnexionTCP.receiveMessage(map);
            //cas de deconnexion de l'hote
            if(mode!=0){
                if(map.get("update").equals("closing")){
                    chatController.setStopListeningThread(true);
                    String message="Vous avez été deconnecté de la partie suite à la déconnexion de l'hôte!";
                    Platform.runLater(()-> {
                        try {
                            Utils.changeScene(Utils.getStage(livesLabel),"menu-view.fxml",
                                    new MenuController(username,message),480,480);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    break;
                }
            }
                boolean lost=(int)map.get("lives")<0;
                boolean lostTime=(int)map.get("timer")<=0 && seconds>0;
                boolean win=!((String)map.get("word")).contains("_");
                if(win || lost || lostTime){
                    running[0]=false;
                    if(mode==1){
                        Platform.runLater(() -> {
                            try {
                                Utils.changeScene(Utils.getStage(livesLabel),"room-view.fxml",new RoomController(roomName,username),480,480);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                }
            Pane pane= (Pane) livesLabel.getScene().getRoot();
                if(mode!=0){
                    if(map.get("letter")!=null){

                        if(map.get("letter").equals(currentLetter) && map.containsKey("disconnected")){//cas ou un joueur se deconnecte,la lettre est identique
                            currentLetter=null;
                            if(map.get("currentPlayer").equals(username)){
                                pane.lookup("#letterPane").setDisable(false);
                            }else{
                                pane.lookup("#letterPane").setDisable(true);
                            }
                            Utils.displayInfoMessage(pane,"Le joueur "+map.get("disconnected")+" s'est deconnecté",
                                    20,20,440,440);
                        }
                        else{
                            currentLetter=(String) map.get("letter");
                        }
                    }

                }
                Platform.runLater(() -> {
                    int remainingTime= (int) map.get("timer");
                    time.setText(String.format("%02d",remainingTime/60)+":"+String.format("%02d",remainingTime%60));
                    wordLabel.setText(((String) map.get("word")).replaceAll("([A-Z_])","$1 "));
                    //verification qu'il s'agit d'un retour de lettre
                    if(currentLetter!=null){
                        if(mode!=0){
                            ((Label)pane.lookup("#turn")).setText("Tour:"+map.get("currentPlayer"));
                            if(mode==2){
                                if(map.get("currentPlayer").equals(username)){
                                    pane.lookup("#letterPane").setDisable(false);
                                }else{
                                    pane.lookup("#letterPane").setDisable(true);
                                }
                            }
                        }
                        Button button= (Button) pane.lookup("#"+currentLetter);
                        //lettre incorrecte=perte de vie+bouton rouge
                        if(Integer.parseInt(livesLabel.getText())>(int)map.get("lives")){
                            button.setStyle("-fx-background-color: linear-gradient(#ff0000, #d10000);-fx-border-color: #000000");
                            Timeline timeline=new Timeline(new KeyFrame(Duration.millis(500),
                                    new KeyValue(livesLabel.textFillProperty(),Color.RED)),
                                    new KeyFrame(Duration.millis(1000),new KeyValue(livesLabel.textFillProperty(),Color.BLACK)));
                            timeline.play();
                            livesLabel.setText(String.valueOf((int)map.get("lives")));
                            if(lost){
                                displayEndScreen((Pane) button.getScene().getRoot(),"Perdu!", (String) map.get("word"),
                                        Color.rgb(255,0,0),Color.rgb(64,0,0,0.95));
                            }
                        }
                        //lettre correct,bouton vert
                        else{
                            button.setStyle("-fx-background-color: linear-gradient(#40ff00, #3bd100);-fx-border-color: #000000");
                            if(win){
                                displayEndScreen((Pane) button.getScene().getRoot(),"Gagné!", (String) map.get("word"),
                                        Color.rgb(64,255,0),Color.rgb(16,80,0,0.95));
                            }
                        }

                        button.setDisable(true);
                        currentLetter=null;
                    }
                    if(lostTime){

                        try {
                            ConnexionTCP.sendMessage(null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        displayEndScreen((Pane) livesLabel.getScene().getRoot(),"Perdu!", (String) map.get("word"),
                                Color.rgb(255,0,0),Color.rgb(64,0,0,0.95));
                    }
                });
            }

        }


}
