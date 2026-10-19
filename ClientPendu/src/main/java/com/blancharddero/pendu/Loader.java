package com.blancharddero.pendu;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Connexion.ConnexionUDP;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Loader {
    public static void doLoadingProcess(Stage stage, Object message,Object container,String protocol,Runnable afterProcess) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(Menu.class.getResource("load-view.fxml"));
        Scene newScene = new Scene(fxmlLoader.load(), 380, 420);
        stage.setScene(newScene);
        Pane root=(Pane)newScene.getRoot();
        Pane loader=(Pane)root.getChildren().get(1);
        RotateTransition rotateTransition=new RotateTransition();
        rotateTransition.setDuration(Duration.seconds(1));
        rotateTransition.setNode(loader);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.play();
        Thread rotationThread=new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        if(protocol.equals("UDP")){
                            ConnexionUDP.sendMessage((String) message);
                            ConnexionUDP.receiveMessage((StringBuilder) container);
                        }
                        else{
                            if(ConnexionTCP.getSocket()==null)ConnexionTCP.establishConnexion();
                            ConnexionTCP.sendMessage(message);
                            ConnexionTCP.receiveMessage(container);
                        }
                        Platform.runLater(() -> {
                            rotateTransition.stop();
                            afterProcess.run();
                        });
                    } catch (Exception e) {//erreur de connexion avec le serveur,affichage du message d'erreur
                        e.printStackTrace();
                        //destruction des anciennes connexion et reintialisation des connexions
                        try {
                            ConnexionTCP.resetConnexion();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        ConnexionUDP.resetConnexion();
                        Platform.runLater(() -> {
                            Pane pane=new Pane();
                            pane.setPrefWidth(266);
                            pane.setPrefHeight(294);
                            pane.setLayoutX(57);
                            pane.setLayoutY(63);

                            Label title=new Label("Erreur de connexion!");
                            title.setFont(Font.font("Arial", FontWeight.BOLD,18));
                            title.setLayoutX(40);

                            Label text=new Label("Le délai de connexion au serveur est dépassé!");
                            text.setFont(Font.font("Arial", FontWeight.NORMAL,12));
                            text.setLayoutY(100);

                            Button button=new Button("Reconnexion");
                            button.setFont(Font.font("Arial",FontWeight.BOLD,20));
                            button.setLayoutY(200);
                            button.setLayoutX(60);
                            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    FXMLLoader fxmlLoader1 = new FXMLLoader(Menu.class.getResource("login-view.fxml"));
                                    try {
                                        Scene scene = new Scene(fxmlLoader1.load(), 480, 480);
                                        stage.setScene(scene);
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            });
                            pane.getChildren().addAll(title,text,button);
                            pane.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255,0.85), CornerRadii.EMPTY, Insets.EMPTY)));
                            root.getChildren().add(pane);
                            //animation affichage
                            ScaleTransition scaleTransition=new ScaleTransition(Duration.millis(600));
                            scaleTransition.setNode(pane);
                            scaleTransition.setFromX(0);
                            scaleTransition.setFromY(0);
                            scaleTransition.setToX(1);
                            scaleTransition.setToY(1);
                            scaleTransition.play();
                        });

                    }

            }
        });
        rotationThread.setDaemon(true);//arrêt du thread lors de l'arrrêt du thread principal
        rotationThread.start();
    }

}
